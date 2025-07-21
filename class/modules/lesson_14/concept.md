# Concept 14: Timers and Cron Workflows

## Objective

Master time-based workflow patterns including delays, timeouts, conditional waiting, and recurring scheduled tasks. Learn how to build reliable timer workflows that handle scheduling, timezone considerations, and long-running recurring processes.

## Key Concepts

### 1. **Timer Fundamentals in Temporal**

#### **Workflow.sleep() - The Foundation**
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

#### **Key Properties of Temporal Timers**
- **Durable**: Timers survive worker crashes and restarts
- **Accurate**: Precisely scheduled, not affected by clock drift
- **Efficient**: No polling - event-driven execution
- **Scalable**: Millions of timers can be scheduled simultaneously

### 2. **Timer Patterns**

#### **Simple Delay Pattern**
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

#### **Timeout Pattern**
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

#### **Conditional Waiting Pattern**
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

#### **Periodic Processing Pattern**
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

### 3. **Cron Workflow Patterns**

#### **Basic Cron Implementation**
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

#### **Advanced Cron with Configuration Updates**
```kotlin
class ConfigurableCronWorkflowImpl : ConfigurableCronWorkflow {
    
    @SignalMethod
    override fun updateConfiguration(newConfig: CronConfig) {
        this.currentConfig = newConfig
    }
    
    @QueryMethod
    override fun getCurrentConfig(): CronConfig = currentConfig
    
    @QueryMethod
    override fun getExecutionHistory(): List<ExecutionRecord> = executionHistory.toList()
    
    override fun runConfigurableCronJob(initialConfig: CronConfig): CronResult {
        var currentConfig = initialConfig
        val executionHistory = mutableListOf<ExecutionRecord>()
        
        while (currentConfig.enabled) {
            val executionTime = Instant.now()
            
            try {
                // Execute job with current configuration
                val result = executeJob(currentConfig)
                executionHistory.add(ExecutionRecord.success(executionTime, result))
                
                // Wait for next execution or configuration change
                val nextRun = calculateNextRunTime(currentConfig.cronExpression, executionTime, currentConfig.timezone)
                
                if (nextRun != null) {
                    val sleepDuration = Duration.between(executionTime, nextRun)
                    
                    // Use await to allow configuration updates during sleep
                    val configChanged = Workflow.await(sleepDuration) {
                        // Check if configuration was updated via signal
                        currentConfig != this.currentConfig
                    }
                    
                    if (configChanged) {
                        currentConfig = this.currentConfig
                        // Recalculate next run with new config
                        continue
                    }
                }
                
            } catch (e: Exception) {
                executionHistory.add(ExecutionRecord.failure(executionTime, e.message))
                
                // Continue even on failure
                val nextRun = calculateNextRunTime(currentConfig.cronExpression, executionTime, currentConfig.timezone)
                if (nextRun != null) {
                    val sleepDuration = Duration.between(executionTime, nextRun)
                    Workflow.sleep(sleepDuration)
                }
            }
        }
        
        return CronResult.disabled(currentConfig.jobId, Instant.now())
    }
}
```

### 4. **Timezone and Scheduling Considerations**

#### **Timezone-Aware Scheduling**
```kotlin
class TimezoneAwareCronWorkflowImpl : TimezoneAwareCronWorkflow {
    
    private fun calculateNextRunTime(
        cronExpression: String,
        currentTime: Instant,
        timezone: String
    ): Instant? {
        val zoneId = ZoneId.of(timezone)
        val currentDateTime = currentTime.atZone(zoneId)
        
        // Handle daylight saving time transitions
        val nextRun = when (cronExpression) {
            "0 0 2 * *" -> {
                // 2 AM daily - handle DST carefully
                var nextExecution = currentDateTime.plusDays(1).withHour(2).withMinute(0).withSecond(0)
                
                // Check for DST transition
                if (!nextExecution.zone.rules.isValidOffset(nextExecution.toLocalDateTime(), nextExecution.offset)) {
                    // DST transition occurred, adjust time
                    nextExecution = nextExecution.withHour(3) // Spring forward
                }
                
                nextExecution.toInstant()
            }
            
            else -> calculateStandardNextRun(cronExpression, currentDateTime)
        }
        
        return nextRun
    }
    
    private fun handleDaylightSavingTransition(scheduledTime: ZonedDateTime): ZonedDateTime {
        val rules = scheduledTime.zone.rules
        val localDateTime = scheduledTime.toLocalDateTime()
        
        return if (!rules.isValidOffset(localDateTime, scheduledTime.offset)) {
            // DST transition - find valid time
            val validOffsets = rules.getValidOffsets(localDateTime)
            if (validOffsets.isNotEmpty()) {
                scheduledTime.withZoneSameLocal(scheduledTime.zone)
            } else {
                // Time doesn't exist (spring forward) - move to next valid time
                scheduledTime.plusHours(1)
            }
        } else {
            scheduledTime
        }
    }
}
```

#### **Distributed Cron Coordination**
```kotlin
class DistributedCronWorkflowImpl : DistributedCronWorkflow {
    
    override fun runDistributedCronJob(config: DistributedCronConfig): CronResult {
        val instanceId = Workflow.getInfo().workflowId
        val executionTime = Instant.now()
        
        // Ensure only one instance executes at a time
        val lockAcquired = try {
            distributedLockActivity.acquireLock(config.jobId, instanceId, Duration.ofMinutes(5))
        } catch (e: LockAcquisitionException) {
            false
        }
        
        if (!lockAcquired) {
            // Another instance is already running this job
            val nextRun = calculateNextRunTime(config.cronExpression, executionTime, config.timezone)
            if (nextRun != null) {
                Workflow.sleep(Duration.between(executionTime, nextRun))
                Workflow.continueAsNew(config)
            }
            
            return CronResult.skipped(config.jobId, executionTime, "Another instance already running")
        }
        
        try {
            // Execute the job while holding the lock
            val result = executeJob(config)
            
            return CronResult.success(config.jobId, executionTime, result, null)
            
        } finally {
            // Always release the lock
            try {
                distributedLockActivity.releaseLock(config.jobId, instanceId)
            } catch (e: Exception) {
                // Log but don't fail the workflow
                Workflow.getLogger(this::class.java).warn("Failed to release lock", e)
            }
            
            // Schedule next execution
            val nextRun = calculateNextRunTime(config.cronExpression, executionTime, config.timezone)
            if (nextRun != null) {
                Workflow.sleep(Duration.between(executionTime, nextRun))
                Workflow.continueAsNew(config)
            }
        }
    }
}
```

### 5. **Advanced Timer Patterns**

#### **Jittered Scheduling**
```kotlin
class JitteredSchedulingWorkflowImpl : JitteredSchedulingWorkflow {
    
    override fun runWithJitter(baseInterval: Duration, jitterPercent: Int): String {
        val random = Workflow.newRandom()
        
        repeat(10) { iteration ->
            // Add jitter to prevent thundering herd
            val jitterMs = (baseInterval.toMillis() * jitterPercent / 100.0 * random.nextDouble()).toLong()
            val actualInterval = baseInterval.plusMillis(jitterMs)
            
            Workflow.getLogger(this::class.java).info("Iteration $iteration, sleeping for ${actualInterval.seconds}s")
            
            Workflow.sleep(actualInterval)
            
            // Perform work
            performWork(iteration)
        }
        
        return "Completed 10 iterations with jitter"
    }
}
```

#### **Adaptive Scheduling**
```kotlin
class AdaptiveSchedulingWorkflowImpl : AdaptiveSchedulingWorkflow {
    
    override fun runAdaptiveScheduling(initialInterval: Duration): String {
        var currentInterval = initialInterval
        var consecutiveFailures = 0
        
        repeat(20) { iteration ->
            try {
                performWork(iteration)
                
                // Success - reset failure count and possibly reduce interval
                consecutiveFailures = 0
                if (currentInterval > Duration.ofSeconds(30)) {
                    currentInterval = currentInterval.multipliedBy(95).dividedBy(100) // Reduce by 5%
                }
                
            } catch (e: Exception) {
                consecutiveFailures++
                
                // Increase interval on failures (exponential backoff)
                currentInterval = currentInterval.multipliedBy(2)
                
                // Cap maximum interval
                if (currentInterval > Duration.ofMinutes(10)) {
                    currentInterval = Duration.ofMinutes(10)
                }
                
                Workflow.getLogger(this::class.java).warn("Work failed, increasing interval to ${currentInterval.seconds}s")
            }
            
            // Sleep with adaptive interval
            Workflow.sleep(currentInterval)
        }
        
        return "Completed adaptive scheduling"
    }
}
```

## Best Practices

### ✅ Timer Design

1. **Use Appropriate Timer Granularity**
   ```kotlin
   // Good: Reasonable granularity
   Workflow.sleep(Duration.ofMinutes(5))    // 5 minutes
   Workflow.sleep(Duration.ofHours(1))      // 1 hour
   
   // Avoid: Too fine granularity
   Workflow.sleep(Duration.ofMillis(100))   // 100ms - too frequent
   ```

2. **Handle Timezone Changes**
   ```kotlin
   // Good: Timezone-aware scheduling
   val zoneId = ZoneId.of("America/New_York")
   val scheduledTime = currentTime.atZone(zoneId)
       .withHour(9)  // 9 AM in local time
       .toInstant()
   
   // Bad: Ignoring timezone
   val badTime = Instant.now().plus(Duration.ofHours(24)) // Not timezone-aware
   ```

3. **Use continueAsNew for Long-Running Crons**
   ```kotlin
   // Good: Reset workflow history
   override fun runCronJob(config: CronConfig): CronResult {
       executeJob(config)
       val nextRun = calculateNextRunTime(config)
       Workflow.sleep(Duration.between(Instant.now(), nextRun))
       Workflow.continueAsNew(config) // Prevent history bloat
   }
   ```

### ✅ Error Handling

1. **Graceful Timeout Handling**
   ```kotlin
   val result = try {
       operation.get(Duration.ofMinutes(5))
   } catch (e: TimeoutException) {
       // Clean up and return appropriate result
       cleanup()
       ProcessResult.timeout("Operation timed out")
   }
   ```

2. **Retry Failed Cron Jobs**
   ```kotlin
   try {
       executeJob(config)
   } catch (e: Exception) {
       if (isRetriableError(e) && retryCount < maxRetries) {
           // Retry with backoff
           Workflow.sleep(Duration.ofMinutes(retryCount * 2))
           return runCronJob(config.withRetryCount(retryCount + 1))
       } else {
           // Continue to next scheduled execution
           scheduleNextRun(config)
       }
   }
   ```

### ❌ Common Mistakes

1. **Busy Waiting**
   ```kotlin
   // Bad: Busy waiting
   while (!isConditionMet()) {
       checkCondition()
       Workflow.sleep(Duration.ofSeconds(1)) // Too frequent polling
   }
   
   // Good: Appropriate polling interval
   Workflow.await(Duration.ofMinutes(10)) {
       val condition = checkCondition()
       if (!condition.isMet) {
           Workflow.sleep(Duration.ofMinutes(1)) // Reasonable interval
       }
       condition.isMet
   }
   ```

2. **Not Using continueAsNew for Recurring Workflows**
   ```kotlin
   // Bad: Infinite loop without continueAsNew
   override fun runForever(): String {
       while (true) {
           doWork()
           Workflow.sleep(Duration.ofHours(1))
           // History grows forever!
       }
   }
   
   // Good: Use continueAsNew
   override fun runRecurring(iteration: Int): String {
       doWork()
       Workflow.sleep(Duration.ofHours(1))
       Workflow.continueAsNew(iteration + 1) // Reset history
   }
   ```

---

**Next**: Lesson 15 will explore external service integration patterns and best practices! 