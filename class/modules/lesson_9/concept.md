---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Error Handling in Workflows

## Lesson 9: Building Resilient Distributed Systems

Master comprehensive error handling strategies in Temporal workflows, including custom exception design, compensation patterns (saga), circuit breakers, and graceful degradation.

---

# Objective

By the end of this lesson, you will understand:

- ✅ **Error handling strategy hierarchy** with custom exceptions
- ✅ **Compensation patterns (Saga)** for distributed transactions
- ✅ **Circuit breaker implementation** for service protection
- ✅ **Graceful degradation patterns** for partial failures
- ✅ **Error context and debugging** for production systems
- ✅ **Best practices** for resilient error handling

---

# 1. **Error Handling Strategy Hierarchy**

## **Error Classification Framework**

```kotlin
sealed class BusinessError(message: String, val errorCode: String) : Exception(message) {
    
    // Validation errors - fail fast, don't retry
    class ValidationError(field: String, reason: String) : 
        BusinessError("Validation failed for $field: $reason", "VALIDATION_FAILED")
    
    // Business rule violations - fail fast, don't retry  
    class BusinessRuleError(rule: String, context: String) :
        BusinessError("Business rule '$rule' violated: $context", "BUSINESS_RULE_VIOLATED")
    
    // Resource conflicts - may retry with backoff
    class ResourceConflictError(resource: String, conflictReason: String) :
        BusinessError("Resource conflict on $resource: $conflictReason", "RESOURCE_CONFLICT")
    // Continued on next slide...
```

---

# More Error Types

```kotlin
    // External service errors - retry with circuit breaker
    class ExternalServiceError(service: String, reason: String) :
        BusinessError("External service '$service' error: $reason", "EXTERNAL_SERVICE_ERROR")
    
    // Infrastructure errors - retry aggressively
    class InfrastructureError(component: String, reason: String) :
        BusinessError("Infrastructure error in $component: $reason", "INFRASTRUCTURE_ERROR")
}
```

**Structured error hierarchy enables appropriate handling strategies**

---

# Error Handling Decision Matrix

| Error Type | Retry Strategy | Compensation | User Impact | Example |
|------------|---------------|--------------|-------------|---------|
| **Validation** | No retry | None | Immediate failure | Invalid email format |
| **Business Rule** | No retry | None | Immediate failure | Insufficient balance |
| **Resource Conflict** | Exponential backoff | Possible | Delayed processing | Database lock |
| **External Service** | Circuit breaker | Full compensation | Graceful degradation | Payment gateway down |
| **Infrastructure** | Aggressive retry | None | Transparent | Network timeout |

---

# 2. **Compensation Patterns (Saga)**

## **Forward Recovery vs Backward Recovery**

```kotlin
class OrderProcessingWorkflowImpl : OrderProcessingWorkflow {
    
    override fun processOrder(order: OrderRequest): OrderResult {
        val compensationActions = mutableListOf<CompensationAction>()
        
        try {
            // Step 1: Reserve inventory
            val inventoryReservation = inventoryActivity.reserveItems(order.items)
            compensationActions.add(
                CompensationAction.releaseInventory(inventoryReservation.reservationId)
            )
            
            // Step 2: Process payment
            val paymentResult = paymentActivity.processPayment(order.paymentInfo)
            compensationActions.add(
                CompensationAction.refundPayment(paymentResult.transactionId)
            )
            // Continued on next slide...
```

---

# Compensation Implementation

```kotlin
            // Step 3: Create shipment
            val shipmentResult = shippingActivity.createShipment(order.shippingInfo)
            compensationActions.add(
                CompensationAction.cancelShipment(shipmentResult.shipmentId)
            )
            
            return OrderResult.success(order.orderId, paymentResult, shipmentResult)
            
        } catch (e: Exception) {
            // Execute compensation in reverse order
            executeCompensation(compensationActions.reversed(), order.orderId)
            throw e
        }
    }
    
    private fun executeCompensation(
        actions: List<CompensationAction>, 
        orderId: String
    ) {
        val logger = Workflow.getLogger(this::class.java)
        logger.warn("Executing compensation for order: $orderId")
        
        actions.forEach { action ->
            try {
                compensationActivity.execute(action)
                logger.info("Compensation action executed: ${action.type}")
            } catch (e: Exception) {
                logger.error("Compensation failed for action: ${action.type} - ${e.message}")
                // Log but continue with other compensations
            }
        }
    }
}
```

---

# Idempotent Compensation Activities

```kotlin
@Component
class CompensationActivityImpl : CompensationActivity {
    
    override fun execute(action: CompensationAction): CompensationResult {
        return when (action) {
            is CompensationAction.ReleaseInventory -> releaseInventory(action.reservationId)
            is CompensationAction.RefundPayment -> refundPayment(action.transactionId)
            is CompensationAction.CancelShipment -> cancelShipment(action.shipmentId)
        }
    }
    
    private fun refundPayment(transactionId: String): CompensationResult {
        try {
            // Check if already refunded (idempotency)
            val existingRefund = paymentService.findRefund(transactionId)
            if (existingRefund != null) {
                logger.info("Payment already refunded: $transactionId")
                return CompensationResult.alreadyCompleted(transactionId)
            }
            
            // Execute refund
            val refundResult = paymentService.refund(transactionId)
            logger.info("Payment refunded: $transactionId")
            
            return CompensationResult.success(refundResult.refundId)
            
        } catch (e: PaymentNotFoundException) {
            // Payment doesn't exist - compensation not needed
            logger.warn("Payment not found for refund: $transactionId")
            return CompensationResult.notRequired("Payment not found")
            
        } catch (e: Exception) {
            logger.error("Refund failed: $transactionId - ${e.message}")
            return CompensationResult.failed(e.message ?: "Unknown error")
        }
    }
}
```

**Idempotent compensations are safe to retry multiple times**

---

# 💡 Key Takeaways

## **What You've Learned:**

- ✅ **Structured error hierarchy** enables appropriate handling strategies
- ✅ **Compensation patterns** provide distributed transaction safety
- ✅ **Circuit breakers** protect against cascading failures
- ✅ **Idempotent operations** are safe to retry
- ✅ **Error classification** determines retry vs fail-fast decisions

---