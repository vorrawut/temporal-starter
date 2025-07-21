# Lesson 15 Concepts: External Service Integration

## Objective

Learn how to safely and efficiently integrate Temporal workflows with external services like APIs, databases, payment gateways, and notification systems. Master the patterns for handling external dependencies with proper error handling, timeouts, and retry strategies.

## Key Concepts

### 1. **Service Encapsulation Pattern**
- **Activity Boundary**: All external service calls must happen within activities, never directly in workflows
- **Interface Segregation**: Create separate activity interfaces for each external service type
- **Configuration Isolation**: Use different activity options for different service types (external APIs vs internal services)

### 2. **Timeout and Retry Strategy by Service Type**
- **External APIs**: Longer timeouts (5+ minutes), more retry attempts (5+), exponential backoff
- **Internal Services**: Shorter timeouts (2 minutes), fewer retries (3), faster backoff
- **Database Operations**: Medium timeouts, immediate retries for connection issues
- **Payment Gateways**: Aggressive timeouts, limited retries to avoid duplicate charges

### 3. **Error Handling Patterns**
- **Graceful Degradation**: Continue workflow execution even if non-critical services fail
- **Error Aggregation**: Collect all errors rather than failing fast
- **Partial Success**: Return detailed results showing what succeeded and what failed
- **Compensation Logic**: Implement rollback mechanisms for critical failures

### 4. **Data Modeling for Integration**
- **Request/Response Pairs**: Model clear input and output data structures
- **Rich Metadata**: Include correlation IDs, timestamps, and context information
- **Serialization Safety**: Use simple types that serialize well across service boundaries
- **Version Compatibility**: Design data structures that can evolve over time

### 5. **Activity Configuration Patterns**
- **Service-Specific Options**: Different configurations for different service types
- **Circuit Breaker**: Fail fast when services are known to be down
- **Heartbeat**: For long-running external operations to prevent timeouts
- **Idempotency**: Ensure activities can be safely retried without side effects

## Best Practices

### 1. **Service Organization**
```kotlin
// ✅ Good: Separate interfaces by service domain
@ActivityInterface
interface PaymentService {
    fun processPayment(request: PaymentRequest): PaymentResult
    fun refundPayment(transactionId: String, amount: Double): PaymentResult
}

@ActivityInterface
interface NotificationService {
    fun sendEmail(request: EmailRequest): EmailResult
    fun sendSMS(request: SMSRequest): SMSResult
}

// ❌ Bad: Mixing different service types in one interface
@ActivityInterface
interface ExternalService {
    fun processPayment(request: PaymentRequest): PaymentResult
    fun sendEmail(request: EmailRequest): EmailResult
    fun saveToDatabase(data: Map<String, Any>): Boolean
}
```

### 2. **Configuration Strategy**
```kotlin
// ✅ Good: Service-specific configurations
private val paymentServiceOptions = ActivityOptions.newBuilder()
    .setStartToCloseTimeout(Duration.ofMinutes(10))  // Longer for critical operations
    .setRetryOptions(
        RetryOptions.newBuilder()
            .setMaximumAttempts(2)  // Limited retries to avoid duplicate charges
            .build()
    )
    .build()

private val notificationServiceOptions = ActivityOptions.newBuilder()
    .setStartToCloseTimeout(Duration.ofMinutes(2))   // Shorter for non-critical
    .setRetryOptions(
        RetryOptions.newBuilder()
            .setMaximumAttempts(5)  // More retries for notifications
            .build()
    )
    .build()
```

### 3. **Error Handling Strategy**
```kotlin
// ✅ Good: Collect errors, continue processing
override fun processUserRegistration(email: String, userData: Map<String, Any>): IntegrationResult {
    val errors = mutableListOf<String>()
    var profileCreated = false
    var paymentProcessed = false
    
    // Critical operation
    try {
        userProfileService.createUserProfile(userProfile)
        profileCreated = true
    } catch (e: Exception) {
        errors.add("Profile creation failed: ${e.message}")
        // Don't return early - continue with other operations
    }
    
    // Optional operation
    if (userData.containsKey("initialPayment")) {
        try {
            paymentService.processPayment(paymentRequest)
            paymentProcessed = true
        } catch (e: Exception) {
            errors.add("Payment failed: ${e.message}")
            // Continue - payment is optional
        }
    }
    
    return IntegrationResult(profileCreated, paymentProcessed, errors)
}
```

### 4. **Idempotency Considerations**
- **Unique Operation IDs**: Include correlation IDs in all external service calls
- **Check Before Action**: Query state before performing operations
- **Safe Retries**: Design activities to be safely retryable
- **External Service Support**: Leverage external service idempotency keys when available

### 5. **Monitoring and Observability**
```kotlin
// ✅ Good: Rich logging and metrics
try {
    val startTime = Workflow.currentTimeMillis()
    val result = externalService.callAPI(request)
    val duration = Workflow.currentTimeMillis() - startTime
    
    // Log success with metrics
    logger.info("API call succeeded", mapOf(
        "service" to "external-api",
        "duration" to duration,
        "requestId" to request.id
    ))
    
    return result
} catch (e: Exception) {
    // Log failure with context
    logger.error("API call failed", mapOf(
        "service" to "external-api",
        "error" to e.message,
        "requestId" to request.id
    ))
    throw e
}
```

### 6. **Production Considerations**

**Security:**
- Store credentials securely (not in workflow code)
- Use service accounts and proper authentication
- Implement rate limiting and throttling
- Validate all external service responses

**Performance:**
- Pool connections for database activities
- Cache frequently accessed data
- Use async patterns where possible
- Monitor external service SLAs

**Reliability:**
- Implement circuit breakers for failing services
- Use multiple service endpoints for redundancy
- Plan for external service downtime
- Design workflows to be resumable

**Cost Optimization:**
- Batch operations when possible
- Use appropriate timeout values to avoid resource waste
- Monitor external service costs and usage
- Implement proper cleanup for failed operations

### 7. **Common Anti-Patterns to Avoid**

❌ **Direct Service Calls in Workflows**: Never call external services directly from workflow code
❌ **Shared Activity Options**: Don't use the same timeout/retry configuration for all services
❌ **Silent Failures**: Always log and handle external service failures appropriately
❌ **Blocking Operations**: Avoid long-running synchronous calls without proper timeouts
❌ **Credentials in Code**: Never hardcode API keys or credentials in workflow/activity code 