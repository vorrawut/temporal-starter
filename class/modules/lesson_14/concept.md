---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Timers and Cron Workflows

## Lesson 14: Time-Based Workflow Patterns

Master time-based workflow patterns including delays, timeouts, conditional waiting, and recurring scheduled tasks. Learn how to build reliable timer workflows that handle scheduling, timezone considerations, and long-running recurring processes.

---

# Objective

By the end of this lesson, you will understand:

- ✅ **Timer fundamentals** in Temporal with `Workflow.sleep()`
- ✅ **Timer patterns** - delay, timeout, conditional waiting
- ✅ **Cron workflow patterns** for recurring scheduled tasks
- ✅ **Time-based scheduling** with timezone handling
- ✅ **`continueAsNew`** for long-running recurring processes
- ✅ **Production considerations** for timer workflows

---

# 1. **Timer Fundamentals in Temporal**

## **Workflow.sleep() - The Foundation**

```kotlin
// Basic sleep - duration-based delay
Workflow.sleep(Duration.ofSeconds(30))
Workflow.sleep(Duration.ofMinutes(5))
Workflow.sleep(Duration.ofHours(1))

// Sleep until specific time
val targetTime = Instant.parse("2024-12-25T00:00:00Z")
val now = Workflow.currentTimeMillis()
val delay = Duration.ofMillis(targetTime.toEpochMilli() - now)
if (delay.isPositive) {
    Workflow.sleep(delay)
}
```

**Temporal timers are durable, accurate, efficient, and scalable**

---

# Key Properties of Temporal Timers

## **Timer Characteristics:**

- ✅ **Durable**: Timers survive worker crashes and restarts
- ✅ **Accurate**: Precisely scheduled, not affected by clock drift
- ✅ **Efficient**: No polling - event-driven execution
- ✅ **Scalable**: Millions of timers can be scheduled simultaneously

## **Production Benefits:**
- **No resource consumption** while waiting
- **Automatic recovery** after infrastructure failures
- **Precise scheduling** regardless of system load

---

# 2. **Timer Patterns**

## **Simple Delay Pattern**

```kotlin
class DelayWorkflowImpl : DelayWorkflow {
    
    override fun processWithDelay(delayMinutes: Int): String {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Starting process, will delay for $delayMinutes minutes")
        
        // Perform initial processing
        val initialResult = performInitialWork()
        
        // Wait for specified duration
        Workflow.sleep(Duration.ofMinutes(delayMinutes.toLong()))
        
        // Continue with delayed processing
        val finalResult = performDelayedWork(initialResult)
        
        return finalResult
    }
}
```

---

# Timeout Pattern

```kotlin
class TimeoutWorkflowImpl : TimeoutWorkflow {
    
    override fun processWithTimeout(timeoutSeconds: Long): ProcessResult {
        val operation = Async.procedure {
            // Long-running operation
            performLongOperation()
        }
        
        return try {
            // Wait for operation with timeout
            operation.get(Duration.ofSeconds(timeoutSeconds))
            ProcessResult.success("Operation completed within timeout")
            
        } catch (e: TimeoutException) {
            // Handle timeout
            ProcessResult.timeout("Operation timed out after ${timeoutSeconds}s")
            
        } catch (e: Exception) {
            // Handle other failures
            ProcessResult.error("Operation failed: ${e.message}")
        }
    }
}
```

---

# Conditional Waiting Pattern

```kotlin
class ConditionalWaitWorkflowImpl : ConditionalWaitWorkflow {
    
    override fun waitForCondition(maxWaitMinutes: Int): ConditionResult {
        val logger = Workflow.getLogger(this::class.java)
        var checkCount = 0
        
        val conditionMet = Workflow.await(Duration.ofMinutes(maxWaitMinutes.toLong())) {
            checkCount++
            logger.info("Checking condition, attempt: $checkCount")
            
            val conditionStatus = checkConditionActivity.checkCondition()
            
            if (conditionStatus.isMet) {
                logger.info("Condition met after $checkCount checks")
                return@await true
            }
            
            // Wait between checks
            Workflow.sleep(Duration.ofSeconds(30))
            false
        }
        
        return ConditionResult(
            conditionMet = conditionMet,
            checkCount = checkCount,
            timeoutReached = !conditionMet
        )
    }
}
```

---

# Periodic Processing Pattern

```kotlin
class PeriodicWorkflowImpl : PeriodicWorkflow {
    
    override fun runPeriodicProcess(intervalMinutes: Int, maxIterations: Int): PeriodicResult {
        val results = mutableListOf<String>()
        
        repeat(maxIterations) { iteration ->
            val logger = Workflow.getLogger(this::class.java)
            logger.info("Starting iteration ${iteration + 1} of $maxIterations")
            
            // Perform periodic work
            val result = performPeriodicWork(iteration)
            results.add(result)
            
            // Wait before next iteration (except last)
            if (iteration < maxIterations - 1) {
                Workflow.sleep(Duration.ofMinutes(intervalMinutes.toLong()))
            }
        }
        
        return PeriodicResult(
            completedIterations = maxIterations,
            results = results
        )
    }
}
```

---

# 3. **Cron Workflow Patterns**

## **Basic Cron Implementation**

```kotlin
class CronWorkflowImpl : CronWorkflow {
    
    override fun runCronJob(config: CronConfig): CronResult {
        val executionTime = Instant.now()
        val logger = Workflow.getLogger(this::class.java)
        
        try {
            // Execute the job
            val jobResult = executeScheduledJob(config)
            
            // Calculate next execution time
            val nextRun = calculateNextRunTime(config.cronExpression, executionTime, config.timezone)
            
            if (nextRun != null) {
                // Sleep until next execution
                val sleepDuration = Duration.between(executionTime, nextRun)
                if (sleepDuration.isPositive) {
                    Workflow.sleep(sleepDuration)
                }
                
                // Continue with next execution
                Workflow.continueAsNew(config)
            }
            
            return CronResult.success(config.jobId, executionTime, jobResult, nextRun)
            // Continued on next slide...
```

---

# Cron Error Handling

```kotlin
        } catch (e: Exception) {
            logger.error("Cron job failed", e)
            
            // Schedule next run even on failure
            val nextRun = calculateNextRunTime(config.cronExpression, executionTime, config.timezone)
            if (nextRun != null) {
                val sleepDuration = Duration.between(executionTime, nextRun)
                if (sleepDuration.isPositive) {
                    Workflow.sleep(sleepDuration)
                }
                Workflow.continueAsNew(config)
            }
            
            return CronResult.failure(config.jobId, executionTime, e.message, nextRun)
        }
    }
}
```

**Cron workflows use `continueAsNew` to prevent history growth in long-running schedules**

---

# Timer Pattern Use Cases

## **When to Use Each Pattern:**

| Pattern | Use Case | Example |
|---------|----------|---------|
| **Simple Delay** | Fixed waiting period | Wait 30 minutes before retry |
| **Timeout** | Maximum operation time | API calls with 2-minute limit |
| **Conditional Wait** | Event-based waiting | Wait for file to appear |
| **Periodic** | Regular intervals | Health checks every 5 minutes |
| **Cron** | Complex scheduling | Daily reports at 9 AM |

---

# 💡 Key Takeaways

## **What You've Learned:**

- ✅ **Temporal timers** are durable and survive infrastructure failures
- ✅ **Timer patterns** handle different time-based scenarios
- ✅ **Cron workflows** enable complex recurring schedules
- ✅ **`continueAsNew`** prevents history growth in long-running processes
- ✅ **Production-ready** timer implementations

---