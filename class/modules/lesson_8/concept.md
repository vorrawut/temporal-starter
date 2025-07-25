---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Activity Retry + Timeout

## Lesson 8: Building Resilient Fault-Tolerant Systems

Master the art of building resilient Temporal workflows through sophisticated retry policies, timeout strategies, and failure handling patterns.

---

# Objective

By the end of this lesson, you will understand:

- ✅ **Retry policy deep dive** with exponential backoff strategies
- ✅ **Timeout configurations** for different operation types
- ✅ **Failure classification** - retriable vs non-retriable errors
- ✅ **Activity heartbeats** for long-running operations
- ✅ **Advanced retry patterns** and circuit breakers
- ✅ **Monitoring and observability** for resilient systems

---

# 1. **Retry Policy Deep Dive**

## **Exponential Backoff Strategy**

```kotlin
RetryOptions.newBuilder()
    .setInitialInterval(Duration.ofSeconds(1))      // Start with 1 second
    .setMaximumInterval(Duration.ofMinutes(5))      // Cap at 5 minutes
    .setBackoffCoefficient(2.0)                     // Double each time
    .setMaximumAttempts(10)                         // Try up to 10 times
    .build()

// Retry intervals: 1s, 2s, 4s, 8s, 16s, 32s, 64s, 128s, 256s, 300s (capped)
```

**Exponential backoff prevents overwhelming failed services while allowing recovery**

---

# Operation-Specific Retry Strategies

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
    // Continued on next slide...
```

---

# More Retry Strategies

```kotlin
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

---

# 2. **Timeout Configurations**

## **Timeout Types and Usage**

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

**Different timeout types serve different purposes in the activity lifecycle**

---

# Timeout Strategy Matrix

| Operation Type | Start-to-Close | Schedule-to-Close | Heartbeat | Rationale |
|---------------|----------------|-------------------|-----------|-----------|
| **Quick validation** | 10s | 30s | N/A | Fast operations |
| **Database query** | 30s | 5m | N/A | Network + DB time |
| **File processing** | 10m | 1h | 30s | Long operations need heartbeat |
| **External API** | 2m | 15m | N/A | Network dependencies |
| **Email sending** | 1m | 10m | N/A | External service |

**Match timeouts to actual operation characteristics and business requirements**

---

# 3. **Failure Classification**

## **Retriable vs Non-Retriable Failures**

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
                // Continued on next slide...
```

---

# More Failure Classification

```kotlin
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
            }
        }
    }
}
```

---

# Classification Strategy

## **Error Types and Retry Decisions:**

- ✅ **Temporary network issues** → **Retry aggressively**
- ✅ **Rate limiting** → **Retry with longer delays**
- ✅ **Service unavailable** → **Retry with backoff**
- ❌ **Invalid input** → **Don't retry**
- ❌ **Authentication errors** → **Don't retry**
- ❌ **Business rule violations** → **Don't retry**

**Smart error classification prevents wasted resources and faster failure detection**

---

# 💡 Key Takeaways

## **What You've Learned:**

- ✅ **Exponential backoff** prevents service overload during failures
- ✅ **Operation-specific strategies** match retry patterns to operation types
- ✅ **Timeout configurations** control activity lifecycle timing
- ✅ **Failure classification** determines retry vs immediate failure
- ✅ **Smart retry policies** balance resilience with resource efficiency

---

# 🚀 Next Steps

**You now understand building fault-tolerant distributed systems!**

## **Lesson 9 will cover:**
- Comprehensive error handling strategies
- Compensation patterns (Saga)
- Circuit breakers and graceful degradation
- Advanced debugging and monitoring

**Ready to build bulletproof workflows? Let's continue! 🎉** 