# Concept 9: Error Handling in Workflows

## Objective

Master comprehensive error handling strategies in Temporal workflows, including custom exception design, compensation patterns (saga), circuit breakers, and graceful degradation techniques for building resilient distributed systems.

## Key Concepts

### 1. **Error Handling Strategy Hierarchy**

#### **Error Classification Framework**
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
    
    // External service errors - retry with circuit breaker
    class ExternalServiceError(service: String, reason: String) :
        BusinessError("External service '$service' error: $reason", "EXTERNAL_SERVICE_ERROR")
    
    // Infrastructure errors - retry aggressively
    class InfrastructureError(component: String, reason: String) :
        BusinessError("Infrastructure error in $component: $reason", "INFRASTRUCTURE_ERROR")
}
```

#### **Error Handling Decision Matrix**
| Error Type | Retry Strategy | Compensation | User Impact | Example |
|------------|---------------|--------------|-------------|---------|
| Validation | No retry | None | Immediate failure | Invalid email format |
| Business Rule | No retry | None | Immediate failure | Insufficient balance |
| Resource Conflict | Exponential backoff | Possible | Delayed processing | Database lock |
| External Service | Circuit breaker | Full compensation | Graceful degradation | Payment gateway down |
| Infrastructure | Aggressive retry | None | Transparent | Network timeout |

### 2. **Compensation Patterns (Saga)**

#### **Forward Recovery vs Backward Recovery**
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

#### **Idempotent Compensation Activities**
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

### 3. **Circuit Breaker Implementation**

#### **Workflow-Level Circuit Breaker**
```kotlin
class ResilientOrderWorkflowImpl : ResilientOrderWorkflow {
    
    private val circuitBreakerState = mutableMapOf<String, CircuitBreakerInfo>()
    
    override fun processOrderWithCircuitBreaker(order: OrderRequest): OrderResult {
        val logger = Workflow.getLogger(this::class.java)
        
        // Check payment service circuit breaker
        if (isCircuitOpen("payment-service")) {
            logger.warn("Payment service circuit breaker is OPEN, using fallback")
            return processOrderWithFallbackPayment(order)
        }
        
        return try {
            val paymentResult = paymentActivity.processPayment(order.paymentInfo)
            
            // Record success
            recordCircuitBreakerSuccess("payment-service")
            
            processOrderAfterPayment(order, paymentResult)
            
        } catch (e: ExternalServiceError) {
            // Record failure
            recordCircuitBreakerFailure("payment-service")
            
            // Check if we should open the circuit
            if (shouldOpenCircuit("payment-service")) {
                openCircuit("payment-service")
                logger.warn("Opening circuit breaker for payment-service")
            }
            
            // Try fallback
            return processOrderWithFallbackPayment(order)
        }
    }
    
    private fun isCircuitOpen(serviceName: String): Boolean {
        val info = circuitBreakerState[serviceName] ?: return false
        
        if (info.state != CircuitState.OPEN) return false
        
        // Check if circuit should transition to half-open
        val now = Workflow.currentTimeMillis()
        if (now - info.lastFailureTime > CIRCUIT_BREAKER_TIMEOUT_MS) {
            circuitBreakerState[serviceName] = info.copy(state = CircuitState.HALF_OPEN)
            return false
        }
        
        return true
    }
    
    private fun processOrderWithFallbackPayment(order: OrderRequest): OrderResult {
        // Implement alternative payment processing
        // Could be: different payment provider, store credit, manual processing, etc.
        val fallbackResult = fallbackPaymentActivity.processPayment(order.paymentInfo)
        return processOrderAfterPayment(order, fallbackResult)
    }
}
```

### 4. **Graceful Degradation Patterns**

#### **Feature Toggle Integration**
```kotlin
class FeatureAwareWorkflowImpl : FeatureAwareWorkflow {
    
    override fun processOrder(order: OrderRequest): OrderResult {
        val steps = mutableListOf<ProcessingStep>()
        
        // Core functionality - always execute
        val coreResult = coreOrderActivity.processCore(order)
        steps.add(ProcessingStep.core(coreResult))
        
        // Optional features - degrade gracefully if failing
        try {
            if (isFeatureEnabled("recommendation-engine")) {
                val recommendations = recommendationActivity.generateRecommendations(order)
                steps.add(ProcessingStep.recommendations(recommendations))
            }
        } catch (e: Exception) {
            logger.warn("Recommendation service failed, continuing without recommendations: ${e.message}")
            steps.add(ProcessingStep.skipped("recommendations", e.message))
        }
        
        try {
            if (isFeatureEnabled("loyalty-points")) {
                val loyaltyPoints = loyaltyActivity.calculatePoints(order)
                steps.add(ProcessingStep.loyalty(loyaltyPoints))
            }
        } catch (e: Exception) {
            logger.warn("Loyalty service failed, continuing without points: ${e.message}")
            steps.add(ProcessingStep.skipped("loyalty", e.message))
        }
        
        // Essential notifications - retry more aggressively
        val notificationResult = try {
            notificationActivity.sendOrderConfirmation(order)
        } catch (e: Exception) {
            // Try fallback notification method
            fallbackNotificationActivity.sendBasicConfirmation(order)
        }
        steps.add(ProcessingStep.notification(notificationResult))
        
        return OrderResult(
            orderId = order.orderId,
            coreResult = coreResult,
            processingSteps = steps,
            degradedFeatures = steps.filter { it.isSkipped() }.map { it.featureName }
        )
    }
}
```

### 5. **Error Context and Debugging**

#### **Rich Error Context**
```kotlin
data class WorkflowError(
    val workflowId: String,
    val runId: String,
    val errorType: ErrorType,
    val errorMessage: String,
    val failedActivity: String?,
    val executionContext: Map<String, Any>,
    val stackTrace: String,
    val retryAttempt: Int,
    val timestamp: Instant
) {
    companion object {
        fun fromException(
            e: Exception,
            workflowInfo: WorkflowInfo,
            context: Map<String, Any>
        ): WorkflowError {
            return WorkflowError(
                workflowId = workflowInfo.workflowId,
                runId = workflowInfo.runId,
                errorType = classifyError(e),
                errorMessage = e.message ?: "Unknown error",
                failedActivity = extractActivityName(e),
                executionContext = context,
                stackTrace = e.stackTraceToString(),
                retryAttempt = extractRetryAttempt(e),
                timestamp = Instant.now()
            )
        }
    }
}
```

#### **Structured Logging for Debugging**
```kotlin
class DebuggableWorkflowImpl : DebuggableWorkflow {
    
    override fun processWithLogging(request: ProcessingRequest): ProcessingResult {
        val logger = Workflow.getLogger(this::class.java)
        val workflowInfo = Workflow.getInfo()
        
        // Create execution context for debugging
        val executionContext = mapOf(
            "workflowId" to workflowInfo.workflowId,
            "runId" to workflowInfo.runId,
            "workflowType" to workflowInfo.workflowType,
            "requestId" to request.id,
            "userId" to request.userId,
            "timestamp" to Workflow.currentTimeMillis()
        )
        
        logger.info("Workflow started", executionContext)
        
        try {
            val step1Result = step1Activity.execute(request.step1Data)
            logger.info("Step 1 completed", executionContext + ("step1Result" to step1Result))
            
            val step2Result = step2Activity.execute(request.step2Data, step1Result)
            logger.info("Step 2 completed", executionContext + ("step2Result" to step2Result))
            
            val finalResult = finalActivity.combine(step1Result, step2Result)
            logger.info("Workflow completed successfully", executionContext + ("finalResult" to finalResult))
            
            return ProcessingResult.success(finalResult)
            
        } catch (e: Exception) {
            val errorContext = executionContext + mapOf(
                "errorType" to e::class.simpleName,
                "errorMessage" to e.message,
                "stackTrace" to e.stackTraceToString()
            )
            
            logger.error("Workflow failed", errorContext)
            
            // Create structured error for external systems
            val workflowError = WorkflowError.fromException(e, workflowInfo, executionContext)
            errorReportingActivity.reportError(workflowError)
            
            throw e
        }
    }
}
```

## Best Practices

### ✅ Error Design

1. **Create Specific Exception Types**
   ```kotlin
   // Good: Specific, actionable exceptions
   class InsufficientInventoryException(
       val productId: String,
       val requested: Int,
       val available: Int
   ) : BusinessException("Insufficient inventory for $productId")
   
   class PaymentDeclinedException(
       val reason: PaymentDeclineReason,
       val merchantMessage: String
   ) : BusinessException("Payment declined: $merchantMessage")
   ```

2. **Include Recovery Information**
   ```kotlin
   data class RecoverableError(
       val errorCode: String,
       val message: String,
       val retryable: Boolean,
       val retryAfter: Duration?,
       val alternativeActions: List<AlternativeAction>
   )
   ```

3. **Use ApplicationFailure for Temporal**
   ```kotlin
   // Mark non-retriable errors clearly
   throw ApplicationFailure.newNonRetryableFailure(
       "Invalid credit card number",
       "INVALID_PAYMENT_METHOD"
   )
   
   // Provide retry guidance
   throw ApplicationFailure.newFailure(
       "Payment service temporarily unavailable",
       "PAYMENT_SERVICE_DOWN"
   )
   ```

### ✅ Compensation Design

1. **Make Compensations Idempotent**
   ```kotlin
   override fun compensatePayment(transactionId: String) {
       if (paymentService.isAlreadyRefunded(transactionId)) {
           return // Safe to call multiple times
       }
       paymentService.refund(transactionId)
   }
   ```

2. **Log Compensation Actions**
   ```kotlin
   override fun executeCompensation(action: CompensationAction) {
       logger.info("Executing compensation: ${action.type} for ${action.resourceId}")
       try {
           performCompensation(action)
           logger.info("Compensation successful: ${action.type}")
       } catch (e: Exception) {
           logger.error("Compensation failed: ${action.type} - ${e.message}")
           throw e
       }
   }
   ```

### ❌ Common Anti-Patterns

1. **Swallowing Exceptions**
   ```kotlin
   // Bad: Hiding errors
   try {
       criticalOperation()
   } catch (e: Exception) {
       logger.error("Something went wrong") // Lost context!
       return defaultValue
   }
   
   // Good: Proper error handling
   try {
       criticalOperation()
   } catch (e: Exception) {
       logger.error("Critical operation failed: ${e.message}", e)
       throw BusinessException("Operation failed", e)
   }
   ```

2. **Generic Error Handling**
   ```kotlin
   // Bad: One size fits all
   catch (e: Exception) {
       return ErrorResult("Something went wrong")
   }
   
   // Good: Specific handling
   catch (e: ValidationException) {
       return ErrorResult.validation(e.field, e.reason)
   } catch (e: ServiceUnavailableException) {
       return ErrorResult.serviceUnavailable(e.serviceName, e.retryAfter)
   }
   ```

---

**Next**: Lesson 10 will explore signals and queries for building interactive, long-running workflows! 