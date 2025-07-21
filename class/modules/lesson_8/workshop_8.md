# Workshop 8: Activity Retry + Timeout

## What we want to build

Implement robust activity retry and timeout configurations to handle failures gracefully. Learn different retry strategies for different types of operations.

## Expecting Result

- Activities with custom retry policies
- Different timeout strategies for different operation types
- Proper failure handling and exponential backoff
- Circuit breaker patterns for external services

## Code Steps

### Step 1: Configure Retry Policies
```kotlin
class ResilientWorkflowImpl : ResilientWorkflow {
    
    // Quick operations - aggressive retries
    private val validationActivity = Workflow.newActivityStub(
        ValidationActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(1))
                    .setMaximumInterval(Duration.ofSeconds(10))
                    .setBackoffCoefficient(2.0)
                    .setMaximumAttempts(5)
                    .build()
            )
            .build()
    )
    
    // External API calls - conservative retries
    private val externalApiActivity = Workflow.newActivityStub(
        ExternalApiActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(2))
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(5))
                    .setMaximumInterval(Duration.ofMinutes(5))
                    .setBackoffCoefficient(3.0)
                    .setMaximumAttempts(3)
                    .build()
            )
            .build()
    )
}
```

### Step 2: Handle Different Failure Types
```kotlin
@Component
class ExternalApiActivityImpl : ExternalApiActivity {
    
    override fun callExternalService(request: ApiRequest): ApiResponse {
        try {
            return httpClient.post(request)
        } catch (e: ConnectTimeoutException) {
            // Retriable - network issue
            throw ApplicationFailure.newFailure("Network timeout", "NETWORK_ERROR")
        } catch (e: HttpClientErrorException) {
            when (e.statusCode.value()) {
                400, 401, 403, 404 -> {
                    // Non-retriable - client error
                    throw ApplicationFailure.newNonRetryableFailure(
                        "Client error: ${e.statusText}", 
                        "CLIENT_ERROR"
                    )
                }
                429, 500, 502, 503 -> {
                    // Retriable - server issue
                    throw ApplicationFailure.newFailure(
                        "Server error: ${e.statusText}", 
                        "SERVER_ERROR"
                    )
                }
                else -> throw e
            }
        }
    }
}
```

### Step 3: Activity Heartbeats for Long Operations
```kotlin
@Component
class LongRunningActivityImpl : LongRunningActivity {
    
    override fun processLargeFile(filePath: String): ProcessingResult {
        val totalSteps = 100
        
        for (step in 1..totalSteps) {
            // Report progress via heartbeat
            Activity.getExecutionContext().heartbeat(step)
            
            // Do actual work
            processFileChunk(filePath, step)
            
            // Check for cancellation
            if (Activity.getExecutionContext().isCancelRequested) {
                logger.info("Activity cancelled at step $step")
                throw CancellationException("Processing cancelled")
            }
            
            Thread.sleep(1000) // Simulate work
        }
        
        return ProcessingResult("File processed successfully")
    }
}
```

## How to Run

Configure heartbeat timeout:
```kotlin
private val longRunningActivity = Workflow.newActivityStub(
    LongRunningActivity::class.java,
    ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofMinutes(10))
        .setHeartbeatTimeout(Duration.ofSeconds(30))
        .build()
)
``` 