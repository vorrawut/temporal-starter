---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Workshop 9: Error Handling in Workflows

## Building Resilient Distributed Systems

*Implement comprehensive error handling strategies in workflows including try-catch blocks, custom exceptions, compensation logic, and graceful degradation patterns*

---

# What we want to build

Implement **comprehensive error handling strategies** in workflows including:

- **Try-catch blocks** and custom exceptions
- **Compensation logic** for partial failures  
- **Circuit breaker patterns** to prevent cascading failures
- **Graceful degradation** for non-critical services

---

# Expecting Result

## By the end of this workshop, you'll have:

- ✅ **Workflows that handle errors gracefully** without failing
- ✅ **Custom business exceptions** with proper context
- ✅ **Compensation logic** for partial failures
- ✅ **Circuit breaker patterns** to prevent cascading failures

---

# Code Steps

## Step 1: Custom Business Exceptions

```kotlin
// Define custom exception types
class InsufficientInventoryException(
    val productId: String,
    val requested: Int,
    val available: Int
) : Exception("Insufficient inventory for product $productId: requested $requested, available $available")

class PaymentDeclinedException(
    val reason: String,
    val errorCode: String
) : Exception("Payment declined: $reason")

class ShippingUnavailableException(
    val address: Address,
    val reason: String
) : Exception("Shipping unavailable to ${address.zipCode}: $reason")
```

**Specific, contextual exceptions enable better error handling**

---

# Step 2: Error Handling Patterns in Workflows

```kotlin
class OrderWorkflowImpl : OrderWorkflow {
    
    override fun processOrder(order: OrderRequest): OrderResult {
        val logger = Workflow.getLogger(this::class.java)
        
        try {
            // Step 1: Inventory check
            val inventoryResult = inventoryActivity.checkAndReserve(order.items)
            
            // Step 2: Payment processing
            val paymentResult = try {
                paymentActivity.processPayment(order.paymentInfo)
            } catch (e: PaymentDeclinedException) {
                // Compensate: release inventory
                inventoryActivity.releaseReservation(inventoryResult.reservationId)
                
                return OrderResult.failed(
                    reason = "Payment declined: ${e.reason}",
                    compensationPerformed = true
                )
            }
            // Continued on next slide...
```

---

# Error Handling Continued

```kotlin
            // Step 3: Shipping arrangement
            val shippingResult = try {
                shippingActivity.arrangeShipping(order.shippingAddress)
            } catch (e: ShippingUnavailableException) {
                // Payment succeeded but shipping failed
                // Option 1: Refund and release inventory
                paymentActivity.refund(paymentResult.transactionId)
                inventoryActivity.releaseReservation(inventoryResult.reservationId)
                
                return OrderResult.failed(
                    reason = "Shipping unavailable: ${e.reason}",
                    compensationPerformed = true
                )
            }
            
            return OrderResult.success(
                orderId = generateOrderId(),
                paymentId = paymentResult.transactionId,
                shippingId = shippingResult.trackingId
            )
            // Continued on next slide...
```

---

# Global Error Handling

```kotlin
        } catch (e: InsufficientInventoryException) {
            // Fail fast - no compensation needed
            logger.warn("Order failed due to insufficient inventory: ${e.message}")
            
            return OrderResult.failed(
                reason = "Product unavailable: ${e.productId}",
                compensationPerformed = false
            )
        } catch (e: Exception) {
            // Unexpected error - perform full compensation
            logger.error("Unexpected error during order processing: ${e.message}")
            
            // Best effort compensation
            try {
                compensateOrder(order)
            } catch (compensationError: Exception) {
                logger.error("Compensation failed: ${compensationError.message}")
            }
            
            return OrderResult.failed(
                reason = "System error occurred",
                compensationPerformed = true
            )
        }
    }
    
    private fun compensateOrder(order: OrderRequest) {
        // Implement saga pattern compensation
        // Release any resources that were allocated
    }
}
```

---

# Step 3: Circuit Breaker Pattern

```kotlin
class CircuitBreakerWorkflowImpl : CircuitBreakerWorkflow {
    
    override fun processWithCircuitBreaker(request: ProcessingRequest): ProcessingResult {
        val circuitBreakerState = getCircuitBreakerState("external-service")
        
        if (circuitBreakerState.isOpen()) {
            // Circuit is open, fail fast
            return ProcessingResult.failed("External service unavailable (circuit open)")
        }
        
        return try {
            val result = externalServiceActivity.processRequest(request)
            
            // Success - record for circuit breaker
            recordSuccess("external-service")
            
            ProcessingResult.success(result)
            // Continued on next slide...
```

---

# Circuit Breaker Continued

```kotlin
        } catch (e: Exception) {
            // Failure - record for circuit breaker
            recordFailure("external-service", e)
            
            // Check if we should open the circuit
            if (shouldOpenCircuit("external-service")) {
                openCircuit("external-service")
            }
            
            ProcessingResult.failed("External service error: ${e.message}")
        }
    }
}
```

## **Circuit Breaker Benefits:**
- ✅ **Prevents cascading failures** when services are down
- ✅ **Fast failure** instead of waiting for timeouts
- ✅ **Automatic recovery** when service becomes available

---

# How to Run

## Test error scenarios:

```kotlin
// Test insufficient inventory
val orderWithTooManyItems = OrderRequest(
    customerId = "customer123",
    items = listOf(OrderItem("rare-product", 1000, BigDecimal("1.00")))
)

// Test payment failure  
val orderWithBadPayment = OrderRequest(
    customerId = "customer123",
    paymentInfo = PaymentInfo(cardNumber = "invalid")
)

val workflow = workflowClient.newWorkflowStub(OrderWorkflow::class.java)

val result1 = workflow.processOrder(orderWithTooManyItems)
val result2 = workflow.processOrder(orderWithBadPayment)
```

---

# Error Handling Patterns

## **Error Classification:**

| Error Type | Strategy | Compensation | Example |
|------------|----------|--------------|---------|
| **Validation** | Fail fast | None | Invalid email |
| **Business Rule** | Fail fast | None | Insufficient funds |
| **Resource** | Retry + Compensate | Release resources | Database lock |
| **External Service** | Circuit breaker | Full rollback | Payment gateway |

---

# 💡 Key Takeaways

## **What You've Learned:**

- ✅ **Custom exceptions** provide meaningful error context
- ✅ **Compensation patterns** ensure data consistency
- ✅ **Circuit breakers** prevent cascading failures
- ✅ **Graceful degradation** maintains system stability
- ✅ **Error classification** determines appropriate handling strategy

---

# 🚀 Next Steps

**You now understand building error-resilient workflows!**

## **Lesson 10 will cover:**
- Interactive workflow patterns using Signals
- Real-time workflow state queries
- Event-driven workflow behavior
- Long-running approval workflows

**Ready to build interactive workflows? Let's continue! 🎉** 