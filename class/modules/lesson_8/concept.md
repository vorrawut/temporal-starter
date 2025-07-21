# Concept 8: Activity Retry + Timeout

## Objective

Master the art of building resilient Temporal workflows through sophisticated retry policies, timeout strategies, and failure handling patterns. Learn how to make your distributed systems fault-tolerant and self-healing.

## Key Concepts

### 1. **Retry Policy Deep Dive**

#### **Exponential Backoff Strategy**
```kotlin
RetryOptions.newBuilder()
    .setInitialInterval(Duration.ofSeconds(1))      // Start with 1 second
    .setMaximumInterval(Duration.ofMinutes(5))      // Cap at 5 minutes
    .setBackoffCoefficient(2.0)                     // Double each time
    .setMaximumAttempts(10)                         // Try up to 10 times
    .build()

// Retry intervals: 1s, 2s, 4s, 8s, 16s, 32s, 64s, 128s, 256s, 300s (capped)
```

#### **Operation-Specific Retry Strategies**
```kotlin
class ResilientWorkflowImpl {
    
    // Database queries: Quick retries, many attempts
    private val databaseActivity = Workflow.newActivityStub(
        DatabaseActivity::class.java,
        ActivityOptions.newBuilder()
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofMillis(500))
                    .setMaximumInterval(Duration.ofSeconds(30))
                    .setBackoffCoefficient(1.5)
                    .setMaximumAttempts(15)
                    .build()
            )
            .build()
    )
    
    // External API calls: Conservative retries
    private val apiActivity = Workflow.newActivityStub(
        ExternalApiActivity::class.java,
        ActivityOptions.newBuilder()
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(5))
                    .setMaximumInterval(Duration.ofMinutes(10))
                    .setBackoffCoefficient(3.0)
                    .setMaximumAttempts(5)
                    .build()
            )
            .build()
    )
    
    // Payment processing: Careful retries
    private val paymentActivity = Workflow.newActivityStub(
        PaymentActivity::class.java,
        ActivityOptions.newBuilder()
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(2))
                    .setMaximumInterval(Duration.ofMinutes(2))
                    .setBackoffCoefficient(2.0)
                    .setMaximumAttempts(3)
                    .build()
            )
            .build()
    )
}
```

### 2. **Timeout Configurations**

#### **Timeout Types and Usage**
```kotlin
ActivityOptions.newBuilder()
    // Total time allowed (including all retries)
    .setScheduleToCloseTimeout(Duration.ofMinutes(30))
    
    // Time for single execution attempt
    .setStartToCloseTimeout(Duration.ofMinutes(5))
    
    // Maximum time waiting in queue
    .setScheduleToStartTimeout(Duration.ofMinutes(2))
    
    // Heartbeat frequency (for long-running activities)
    .setHeartbeatTimeout(Duration.ofSeconds(30))
    .build()
```

#### **Timeout Strategy Matrix**
| Operation Type | Start-to-Close | Schedule-to-Close | Heartbeat | Rationale |
|---------------|----------------|-------------------|-----------|-----------|
| Quick validation | 10s | 30s | N/A | Fast operations |
| Database query | 30s | 5m | N/A | Network + DB time |
| File processing | 10m | 1h | 30s | Long operations need heartbeat |
| External API | 2m | 15m | N/A | Network dependencies |
| Email sending | 1m | 10m | N/A | External service |

### 3. **Failure Classification**

#### **Retriable vs Non-Retriable Failures**
```kotlin
@Component
class PaymentActivityImpl : PaymentActivity {
    
    override fun processPayment(paymentInfo: PaymentInfo): PaymentResult {
        try {
            return paymentGateway.charge(paymentInfo)
        } catch (e: Exception) {
            when (e) {
                // Retriable: Temporary issues
                is ConnectTimeoutException,
                is SocketTimeoutException,
                is ServiceUnavailableException -> {
                    throw ApplicationFailure.newFailure(
                        "Temporary payment service issue: ${e.message}",
                        "PAYMENT_SERVICE_TEMPORARY_ERROR"
                    )
                }
                
                // Retriable: Rate limiting
                is RateLimitException -> {
                    // Longer delay for rate limiting
                    throw ApplicationFailure.newFailure(
                        "Rate limited by payment service",
                        "PAYMENT_RATE_LIMITED"
                    )
                }
                
                // Non-retriable: Client errors
                is InvalidCardException,
                is InsufficientFundsException,
                is ExpiredCardException -> {
                    throw ApplicationFailure.newNonRetryableFailure(
                        "Payment failed: ${e.message}",
                        "PAYMENT_DECLINED"
                    )
                }
                
                // Non-retriable: Configuration errors
                is InvalidMerchantException,
                is InvalidApiKeyException -> {
                    throw ApplicationFailure.newNonRetryableFailure(
                        "Payment configuration error: ${e.message}",
                        "PAYMENT_CONFIG_ERROR"
                    )
                }
                
                else -> throw e // Let Temporal decide
            }
        }
    }
}
```

### 4. **Activity Heartbeats**

#### **Long-Running Activity Pattern**
```kotlin
@Component
class FileProcessingActivityImpl : FileProcessingActivity {
    
    override fun processLargeFile(filePath: String): ProcessingResult {
        val file = File(filePath)
        val totalSize = file.length()
        var processedBytes = 0L
        
        val activityContext = Activity.getExecutionContext()
        
        file.bufferedReader().use { reader ->
            reader.lineSequence().forEachIndexed { lineNumber, line ->
                
                // Process the line
                processLine(line)
                processedBytes += line.length + 1 // +1 for newline
                
                // Send heartbeat every 100 lines
                if (lineNumber % 100 == 0) {
                    val progress = ProcessingProgress(
                        linesProcessed = lineNumber + 1,
                        bytesProcessed = processedBytes,
                        percentComplete = (processedBytes * 100 / totalSize).toInt()
                    )
                    
                    activityContext.heartbeat(progress)
                    
                    // Check for cancellation
                    if (activityContext.isCancelRequested) {
                        logger.info("Processing cancelled at line $lineNumber")
                        throw CancellationException("File processing cancelled")
                    }
                }
                
                // Simulate processing time
                Thread.sleep(10)
            }
        }
        
        return ProcessingResult(
            linesProcessed = processedBytes.toInt(),
            bytesProcessed = processedBytes,
            success = true
        )
    }
}
```

#### **Heartbeat Configuration**
```kotlin
private val fileProcessingActivity = Workflow.newActivityStub(
    FileProcessingActivity::class.java,
    ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofHours(2))        // Max processing time
        .setHeartbeatTimeout(Duration.ofMinutes(1))         // Heartbeat every minute
        .setScheduleToCloseTimeout(Duration.ofHours(3))     // Total time including retries
        .setRetryOptions(
            RetryOptions.newBuilder()
                .setMaximumAttempts(3)
                .build()
        )
        .build()
)
```

### 5. **Advanced Retry Patterns**

#### **Custom Retry Logic**
```kotlin
class SmartRetryWorkflowImpl : SmartRetryWorkflow {
    
    override fun processWithSmartRetry(request: ProcessingRequest): ProcessingResult {
        var attempt = 0
        var lastError: Exception? = null
        
        while (attempt < MAX_ATTEMPTS) {
            try {
                return when (attempt) {
                    0 -> primaryActivity.process(request)           // Try primary service
                    1 -> secondaryActivity.process(request)         // Fallback to secondary
                    else -> fallbackActivity.process(request)       // Last resort
                }
            } catch (e: Exception) {
                lastError = e
                attempt++
                
                if (attempt < MAX_ATTEMPTS) {
                    // Dynamic delay based on error type
                    val delay = calculateRetryDelay(e, attempt)
                    Workflow.sleep(delay)
                }
            }
        }
        
        throw lastError ?: RuntimeException("All retry attempts exhausted")
    }
    
    private fun calculateRetryDelay(error: Exception, attempt: Int): Duration {
        return when (error) {
            is RateLimitException -> Duration.ofMinutes(5)     // Wait longer for rate limits
            is ServiceUnavailableException -> Duration.ofSeconds(30)
            else -> Duration.ofSeconds(attempt.toLong() * 2)    // Exponential backoff
        }
    }
}
```

#### **Circuit Breaker Integration**
```kotlin
class CircuitBreakerActivityImpl : CircuitBreakerActivity {
    
    private val circuitBreaker = CircuitBreaker.ofDefaults("payment-service")
    
    override fun callExternalService(request: ServiceRequest): ServiceResponse {
        return circuitBreaker.executeSupplier {
            try {
                externalServiceClient.call(request)
            } catch (e: Exception) {
                // Transform exceptions for circuit breaker
                when (e) {
                    is ConnectTimeoutException,
                    is ServiceUnavailableException -> {
                        // These count as failures for circuit breaker
                        throw RuntimeException("Service failure", e)
                    }
                    else -> throw e
                }
            }
        }
    }
}
```

### 6. **Monitoring and Observability**

#### **Retry Metrics Collection**
```kotlin
@Component
class MetricsCollectingActivity : BaseActivity {
    
    private val retryCounter = Counter.build()
        .name("activity_retries_total")
        .help("Total activity retries")
        .labelNames("activity_name", "error_type")
        .register()
    
    override fun executeWithMetrics(operation: () -> Any): Any {
        val activityName = this::class.simpleName
        
        return try {
            operation()
        } catch (e: Exception) {
            // Record retry metrics
            val errorType = e::class.simpleName ?: "Unknown"
            retryCounter.labels(activityName, errorType).inc()
            
            // Re-throw for normal retry handling
            throw e
        }
    }
}
```

## Best Practices

### ✅ Retry Configuration

1. **Match Retry Strategy to Operation Type**
   ```kotlin
   // Quick operations: Aggressive retries
   val quickRetry = RetryOptions.newBuilder()
       .setInitialInterval(Duration.ofMillis(100))
       .setMaximumAttempts(10)
       .build()
   
   // Expensive operations: Conservative retries
   val expensiveRetry = RetryOptions.newBuilder()
       .setInitialInterval(Duration.ofSeconds(5))
       .setMaximumAttempts(3)
       .build()
   ```

2. **Set Reasonable Maximum Intervals**
   ```kotlin
   RetryOptions.newBuilder()
       .setMaximumInterval(Duration.ofMinutes(5))  // Prevent overly long waits
       .build()
   ```

3. **Consider Total Execution Time**
   ```kotlin
   // Ensure schedule-to-close allows for all retries
   val retryTime = calculateMaxRetryTime(retryOptions)
   val scheduleToClose = singleExecutionTime.plus(retryTime)
   ```

### ✅ Timeout Management

1. **Layer Timeouts Appropriately**
   ```kotlin
   ActivityOptions.newBuilder()
       .setStartToCloseTimeout(Duration.ofMinutes(5))      // Single attempt
       .setScheduleToCloseTimeout(Duration.ofMinutes(20))  // All attempts
       .setHeartbeatTimeout(Duration.ofSeconds(30))        // If needed
       .build()
   ```

2. **Use Heartbeats for Long Operations**
   ```kotlin
   // Any activity taking > 1 minute should heartbeat
   if (expectedDuration > Duration.ofMinutes(1)) {
       activityOptions = activityOptions.toBuilder()
           .setHeartbeatTimeout(Duration.ofSeconds(30))
           .build()
   }
   ```

### ❌ Common Mistakes

1. **Retrying Non-Retriable Errors**
   ```kotlin
   // Bad: Retrying validation errors
   catch (e: ValidationException) {
       throw e  // This will retry forever!
   }
   
   // Good: Mark as non-retriable
   catch (e: ValidationException) {
       throw ApplicationFailure.newNonRetryableFailure(
           e.message,
           "VALIDATION_ERROR"
       )
   }
   ```

2. **Insufficient Timeout Buffers**
   ```kotlin
   // Bad: No buffer for retries
   .setStartToCloseTimeout(Duration.ofMinutes(5))
   .setScheduleToCloseTimeout(Duration.ofMinutes(5))  // Same!
   
   // Good: Buffer for retries
   .setStartToCloseTimeout(Duration.ofMinutes(5))
   .setScheduleToCloseTimeout(Duration.ofMinutes(20)) // 4x buffer
   ```

3. **Missing Heartbeats**
   ```kotlin
   // Bad: Long operation without heartbeats
   fun processLargeFile() {
       for (i in 1..1000000) {
           processItem(i)  // No heartbeat!
       }
   }
   
   // Good: Regular heartbeats
   fun processLargeFile() {
       for (i in 1..1000000) {
           processItem(i)
           if (i % 1000 == 0) {
               Activity.getExecutionContext().heartbeat(i)
           }
       }
   }
   ```

---

**Next**: Lesson 9 will explore comprehensive error handling strategies and compensation patterns! 