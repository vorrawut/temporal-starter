# Workshop 14: Timers and Cron Workflows

## What we want to build

Create timer-based workflows and recurring scheduled tasks using Temporal's timer capabilities. This lesson covers `Workflow.sleep()`, timeout patterns, conditional waiting, and implementing cron-like recurring workflows with `continueAsNew`.

## Expecting Result

By the end of this lesson, you'll have:

- Workflows that use timers for delays and scheduling
- Understanding of different timer patterns (simple delays, timeouts, conditional waits)
- Implementation of cron-like recurring workflows
- Knowledge of timezone handling and scheduling best practices

## Code Steps

### Step 1: Create Basic Timer Workflow Interface

Open `class/workshop/lesson_14/workflow/TimerWorkflow.kt` and create the interfaces:

```kotlin
package com.temporal.bootcamp.lesson14.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface TimerWorkflow {
    @WorkflowMethod
    fun processWithTimer(request: TimerRequest): TimerResult
}

@WorkflowInterface
interface CronWorkflow {
    @WorkflowMethod
    fun runCronJob(config: CronConfig): CronResult
}
```

### Step 2: Implement Simple Timer Patterns

Create the timer workflow implementation:

```kotlin
class TimerWorkflowImpl : TimerWorkflow {
    
    override fun processWithTimer(request: TimerRequest): TimerResult {
        val logger = Workflow.getLogger(this::class.java)
        val startTime = Instant.now()
        
        when (request.operation) {
            "simple_delay" -> return processWithSimpleDelay(request, startTime)
            "timeout_pattern" -> return processWithTimeout(request, startTime)
            "conditional_wait" -> return processWithConditionalWait(request, startTime)
            // ... other patterns
        }
        
        throw IllegalArgumentException("Unknown operation: ${request.operation}")
    }
    
    private fun processWithSimpleDelay(request: TimerRequest, startTime: Instant): TimerResult {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Sleeping for ${request.delaySeconds} seconds")
        
        // Simple delay using Workflow.sleep
        Workflow.sleep(Duration.ofSeconds(request.delaySeconds))
        
        logger.info("Timer completed, performing operation")
        
        val actualDelay = Duration.between(startTime, Instant.now())
        
        return TimerResult(
            requestId = request.requestId,
            success = true,
            result = "Simple delay completed after ${request.delaySeconds}s",
            actualDelay = actualDelay
        )
    }
}
```

**Key points:**
- `Workflow.sleep()` is the primary timer mechanism
- Timers are durable and survive worker restarts
- Always measure actual delay for monitoring

### Step 3: Implement Timeout Patterns

Add timeout handling with concurrent operations:

```kotlin
private fun processWithTimeout(request: TimerRequest, startTime: Instant): TimerResult {
    val logger = Workflow.getLogger(this::class.java)
    val timeoutSeconds = request.timeoutSeconds ?: 30
    
    // Simulate long-running operation
    val operationFuture = Async.procedure {
        logger.info("Starting long operation")
        Workflow.sleep(Duration.ofSeconds(request.delaySeconds))
        logger.info("Long operation completed")
    }
    
    // Wait for operation or timeout
    val timedOut = try {
        operationFuture.get(Duration.ofSeconds(timeoutSeconds))
        false
    } catch (e: Exception) {
        logger.warn("Operation timed out after ${timeoutSeconds}s")
        true
    }
    
    val actualDelay = Duration.between(startTime, Instant.now())
    
    return TimerResult(
        requestId = request.requestId,
        success = !timedOut,
        result = if (timedOut) "Operation timed out" else "Operation completed within timeout",
        actualDelay = actualDelay,
        timedOut = timedOut
    )
}
```

**Key points:**
- Use `Async.procedure` for concurrent operations
- `get(timeout)` provides timeout functionality
- Handle timeout exceptions gracefully

### Step 4: Implement Conditional Waiting

Add conditional waiting with periodic checks:

```kotlin
private fun processWithConditionalWait(request: TimerRequest, startTime: Instant): TimerResult {
    val logger = Workflow.getLogger(this::class.java)
    
    var conditionMet = false
    var iterations = 0
    
    // Wait for condition with periodic checks
    val success = Workflow.await(Duration.ofSeconds(request.delaySeconds)) {
        iterations++
        
        // Simulate checking some condition
        if (iterations >= 5) {
            conditionMet = true
            logger.info("Condition met after $iterations iterations")
            return@await true
        }
        
        // Sleep between checks
        Workflow.sleep(Duration.ofSeconds(2))
        false
    }
    
    val actualDelay = Duration.between(startTime, Instant.now())
    
    return TimerResult(
        requestId = request.requestId,
        success = success && conditionMet,
        result = if (conditionMet) "Condition met after $iterations checks" else "Condition not met, timed out",
        actualDelay = actualDelay,
        timedOut = !success
    )
}
```

**Key points:**
- `Workflow.await()` provides conditional waiting with timeout
- Combine with periodic checks for polling patterns
- Return `true` from the condition to exit early

### Step 5: Implement Cron Workflow

Create a recurring workflow using `continueAsNew`:

```kotlin
class CronWorkflowImpl : CronWorkflow {
    
    override fun runCronJob(config: CronConfig): CronResult {
        val logger = Workflow.getLogger(this::class.java)
        val executionTime = Instant.now()
        
        logger.info("Executing cron job: ${config.jobId} at $executionTime")
        
        if (!config.enabled) {
            // Skip execution but continue scheduling
            val nextRun = calculateNextRunTime(config, executionTime)
            
            if (nextRun != null) {
                val sleepDuration = Duration.between(executionTime, nextRun)
                if (sleepDuration.isPositive) {
                    Workflow.sleep(sleepDuration)
                }
                Workflow.continueAsNew(config)
            }
            
            return CronResult(
                jobId = config.jobId,
                executionTime = executionTime,
                success = true,
                nextRun = nextRun,
                result = "Job skipped (disabled)"
            )
        }
        
        try {
            // Execute the actual job
            val jobResult = executeJob(config)
            
            // Calculate and schedule next run
            val nextRun = calculateNextRunTime(config, executionTime)
            
            if (nextRun != null) {
                val sleepDuration = Duration.between(executionTime, nextRun)
                if (sleepDuration.isPositive) {
                    Workflow.sleep(sleepDuration)
                }
                
                // Use continueAsNew to prevent history bloat
                Workflow.continueAsNew(config)
            }
            
            return CronResult(
                jobId = config.jobId,
                executionTime = executionTime,
                success = true,
                nextRun = nextRun,
                result = jobResult
            )
            
        } catch (e: Exception) {
            logger.error("Job failed: ${e.message}")
            
            // Schedule next run even on failure
            val nextRun = calculateNextRunTime(config, executionTime)
            if (nextRun != null) {
                val sleepDuration = Duration.between(executionTime, nextRun)
                if (sleepDuration.isPositive) {
                    Workflow.sleep(sleepDuration)
                }
                Workflow.continueAsNew(config)
            }
            
            return CronResult(
                jobId = config.jobId,
                executionTime = executionTime,
                success = false,
                nextRun = nextRun,
                result = "Job failed: ${e.message}"
            )
        }
    }
}
```

### Step 6: Implement Cron Time Calculation

Add cron expression parsing and next run calculation:

```kotlin
private fun calculateNextRunTime(config: CronConfig, currentTime: Instant): Instant? {
    val zoneId = ZoneId.of(config.timezone)
    val currentDateTime = currentTime.atZone(zoneId)
    
    return when (config.cronExpression) {
        "0 0 * * *" -> {
            // Daily at midnight
            currentDateTime.plusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .toInstant()
        }
        
        "0 */5 * * *" -> {
            // Every 5 minutes
            currentDateTime.plusMinutes(5)
                .withSecond(0)
                .withNano(0)
                .toInstant()
        }
        
        "0 0 */6 * *" -> {
            // Every 6 hours
            currentDateTime.plusHours(6)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .toInstant()
        }
        
        else -> {
            // Default: run every hour
            currentDateTime.plusHours(1)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .toInstant()
        }
    }
}

private fun executeJob(config: CronConfig): String {
    return when (config.jobId) {
        "daily-report" -> {
            Workflow.sleep(Duration.ofSeconds(5))
            "Daily report generated successfully"
        }
        
        "data-cleanup" -> {
            Workflow.sleep(Duration.ofSeconds(10))
            "Data cleanup completed, 1000 records processed"
        }
        
        else -> {
            Workflow.sleep(Duration.ofSeconds(3))
            "Generic job completed"
    }
}
```

## How to Run

### 1. Start Timer Workflow
```kotlin
val timerWorkflow = workflowClient.newWorkflowStub(
    TimerWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("timer-queue")
        .setWorkflowId("timer-${System.currentTimeMillis()}")
        .build()
)

// Simple delay
val request = TimerRequest(
    requestId = "REQ-001",
    delaySeconds = 10,
    operation = "simple_delay"
)

val result = timerWorkflow.processWithTimer(request)
// Output: Simple delay completed after 10s

// Timeout pattern
val timeoutRequest = TimerRequest(
    requestId = "REQ-002",
    delaySeconds = 30,
    operation = "timeout_pattern",
    timeoutSeconds = 15
)

val timeoutResult = timerWorkflow.processWithTimer(timeoutRequest)
// Output: Operation timed out (because 30s > 15s timeout)
```

### 2. Start Cron Workflow
```kotlin
val cronWorkflow = workflowClient.newWorkflowStub(
    CronWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("cron-queue")
        .setWorkflowId("daily-report-cron")
        .build()
)

val cronConfig = CronConfig(
    jobId = "daily-report",
    cronExpression = "0 0 * * *", // Daily at midnight
    timezone = "America/New_York",
    enabled = true
)

// This starts an infinite recurring workflow
cronWorkflow.runCronJob(cronConfig)
```

### 3. Schedule Future Task
```kotlin
val scheduledWorkflow = workflowClient.newWorkflowStub(
    ScheduledTaskWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("scheduled-queue")
        .setWorkflowId("scheduled-${System.currentTimeMillis()}")
        .build()
)

val task = ScheduledTask(
    taskId = "reminder-001",
    taskType = TaskType.EMAIL_REMINDER,
    scheduledTime = Instant.now().plus(Duration.ofHours(24)), // 24 hours from now
    parameters = mapOf("recipient" to "user@example.com")
)

val taskResult = scheduledWorkflow.runScheduledTask(task)
```

### 4. Expected Output
```
Timer workflow: Sleeping for 10 seconds
Timer workflow: Timer completed, performing operation
Result: Simple delay completed after 10s

Cron workflow: Executing cron job: daily-report at 2024-01-15T00:00:00Z
Cron workflow: Daily report generated successfully
Cron workflow: Next run scheduled for: 2024-01-16T00:00:00Z
```

## What You've Learned

- ✅ How to use `Workflow.sleep()` for durable delays
- ✅ Implementing timeout patterns with `Async` and timeouts
- ✅ Conditional waiting with `Workflow.await()`
- ✅ Building recurring workflows with `continueAsNew`
- ✅ Handling cron expressions and timezone calculations
- ✅ Creating scheduled task workflows
- ✅ Best practices for timer-based workflows

Timer workflows enable powerful scheduling and time-based automation patterns! 