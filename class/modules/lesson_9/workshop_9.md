# Workshop 9: Error Handling in Workflows

## What we want to build

Implement comprehensive error handling strategies in workflows including try-catch blocks, custom exceptions, compensation logic, and graceful degradation patterns.

## Expecting Result

- Workflows that handle errors gracefully without failing
- Custom business exceptions with proper context
- Compensation logic for partial failures
- Circuit breaker patterns to prevent cascading failures

## Code Steps

### Step 1: Custom Business Exceptions
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

### Step 2: Error Handling Patterns in Workflows
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

### Step 3: Circuit Breaker Pattern
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

## How to Run

Test error scenarios:
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
``` 