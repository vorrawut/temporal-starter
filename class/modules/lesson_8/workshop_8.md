---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Workshop 8: Activity Retry + Timeout

## Building Robust Fault-Tolerant Workflows

*Implement robust activity retry and timeout configurations to handle failures gracefully*

---

# What we want to build

Implement **robust activity retry and timeout configurations** to handle failures gracefully. 

Learn **different retry strategies** for different types of operations.

---

# Expecting Result

## By the end of this workshop, you'll have:

- ✅ **Activities with custom retry policies**
- ✅ **Different timeout strategies** for different operation types
- ✅ **Proper failure handling** and exponential backoff
- ✅ **Circuit breaker patterns** for external services

---

# Code Steps

## Step 1: Configure Retry Policies

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
    // Continued on next slide...
```

---

# External API Configuration

```kotlin
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

**Notice different strategies: aggressive for internal, conservative for external**

---

# Step 2: Handle Different Failure Types

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
                // Continued on next slide...
```

---

# Error Classification Continued

```kotlin
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

## **Error Classification Strategy:**
- ✅ **4xx errors** (400, 401, 403, 404) → **Don't retry**
- ✅ **5xx errors** (500, 502, 503) → **Retry with backoff**
- ✅ **Network timeouts** → **Retry aggressively**

---

# Step 3: Activity Heartbeats for Long Operations

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

---

# Heartbeat Pattern Benefits

## **Why Use Heartbeats:**

- ✅ **Progress tracking** - Monitor long-running operations
- ✅ **Cancellation detection** - Respond to workflow cancellation
- ✅ **Timeout prevention** - Keep activity alive during processing
- ✅ **Failure detection** - Detect worker crashes quickly
- ✅ **Resource optimization** - Clean up abandoned work

**Use heartbeats for any activity taking more than 30 seconds**

---

# How to Run

## Configure heartbeat timeout:

```kotlin
private val longRunningActivity = Workflow.newActivityStub(
    LongRunningActivity::class.java,
    ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofMinutes(10))
        .setHeartbeatTimeout(Duration.ofSeconds(30))
        .build()
)
```

**Heartbeat timeout should be less than start-to-close timeout**

---

# Retry Strategy Examples

## **Operation Type → Retry Strategy:**

| Operation | Initial Interval | Max Interval | Backoff | Max Attempts |
|-----------|------------------|--------------|---------|--------------|
| **Validation** | 1s | 10s | 2.0 | 5 |
| **Database** | 500ms | 30s | 1.5 | 15 |
| **External API** | 5s | 5m | 3.0 | 3 |
| **File I/O** | 2s | 1m | 2.0 | 10 |

**Match retry strategy to operation characteristics and failure patterns**

---

# 💡 Key Patterns

## **Exponential Backoff:**
- **Start small** (1-5 seconds) and **grow exponentially**
- **Cap maximum** wait time to prevent infinite delays
- **Use jitter** to prevent thundering herd

## **Circuit Breaker:**
- **Fail fast** when external service is down
- **Allow recovery** through half-open state
- **Protect resources** from cascading failures

---

# 🚀 Production Tips

## **Monitoring and Alerting:**
- ✅ **Track retry counts** by activity type
- ✅ **Alert on high failure rates**
- ✅ **Monitor timeout patterns**
- ✅ **Dashboard heartbeat status**

## **Testing:**
- ✅ **Test timeout scenarios**
- ✅ **Simulate network failures**
- ✅ **Verify compensation logic**
- ✅ **Load test retry behavior**

**Building bulletproof distributed systems! 🎉** 