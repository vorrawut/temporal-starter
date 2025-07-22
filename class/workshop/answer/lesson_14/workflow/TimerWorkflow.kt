package com.temporal.bootcamp.lesson14.workflow

import io.temporal.workflow.*
import java.time.*
import java.time.format.DateTimeFormatter

/**
 * Timer workflow demonstrating various timing patterns.
 */
@WorkflowInterface
interface TimerWorkflow {
    
    @WorkflowMethod
    fun processWithTimer(request: TimerRequest): TimerResult
}

/**
 * Cron workflow for recurring scheduled tasks.
 */
@WorkflowInterface
interface CronWorkflow {
    
    @WorkflowMethod
    fun runCronJob(config: CronConfig): CronResult
}

/**
 * Timer workflow implementation showing different timer patterns.
 */
class TimerWorkflowImpl : TimerWorkflow {
    
    override fun processWithTimer(request: TimerRequest): TimerResult {
        val logger = Workflow.getLogger(this::class.java)
        val startTime = Instant.now()
        
        logger.info("Starting timer workflow for: ${request.requestId}")
        
        try {
            when (request.operation) {
                "simple_delay" -> {
                    return processWithSimpleDelay(request, startTime)
                }
                "timeout_pattern" -> {
                    return processWithTimeout(request, startTime)
                }
                "conditional_wait" -> {
                    return processWithConditionalWait(request, startTime)
                }
                "multiple_timers" -> {
                    return processWithMultipleTimers(request, startTime)
                }
                "cancellable_timer" -> {
                    return processWithCancellableTimer(request, startTime)
                }
                else -> {
                    throw IllegalArgumentException("Unknown operation: ${request.operation}")
                }
            }
            
        } catch (e: Exception) {
            logger.error("Timer workflow failed: ${e.message}")
            
            val actualDelay = Duration.between(startTime, Instant.now())
            return TimerResult(
                requestId = request.requestId,
                success = false,
                result = null,
                actualDelay = actualDelay,
                timedOut = false
            )
        }
    }
    
    private fun processWithSimpleDelay(request: TimerRequest, startTime: Instant): TimerResult {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Sleeping for ${request.delaySeconds} seconds")
        
        // Simple delay using Workflow.sleep
        Workflow.sleep(Duration.ofSeconds(request.delaySeconds))
        
        logger.info("Timer completed, performing operation")
        
        // Simulate some work after the delay
        Workflow.sleep(Duration.ofMillis(500))
        
        val actualDelay = Duration.between(startTime, Instant.now())
        
        return TimerResult(
            requestId = request.requestId,
            success = true,
            result = "Simple delay completed after ${request.delaySeconds}s",
            actualDelay = actualDelay
        )
    }
    
    private fun processWithTimeout(request: TimerRequest, startTime: Instant): TimerResult {
        val logger = Workflow.getLogger(this::class.java)
        val timeoutSeconds = request.timeoutSeconds ?: 30
        
        logger.info("Processing with ${timeoutSeconds}s timeout")
        
        // Simulate long-running operation with timeout
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
    
    private fun processWithConditionalWait(request: TimerRequest, startTime: Instant): TimerResult {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Waiting for condition or timeout")
        
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
    
    private fun processWithMultipleTimers(request: TimerRequest, startTime: Instant): TimerResult {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Starting multiple concurrent timers")
        
        // Start multiple timers concurrently
        val timer1 = Async.procedure {
            Workflow.sleep(Duration.ofSeconds(request.delaySeconds / 3))
            logger.info("Timer 1 completed")
        }
        
        val timer2 = Async.procedure {
            Workflow.sleep(Duration.ofSeconds(request.delaySeconds / 2))
            logger.info("Timer 2 completed")
        }
        
        val timer3 = Async.procedure {
            Workflow.sleep(Duration.ofSeconds(request.delaySeconds))
            logger.info("Timer 3 completed")
        }
        
        // Wait for all timers to complete
        timer1.get()
        timer2.get()
        timer3.get()
        
        logger.info("All timers completed")
        
        val actualDelay = Duration.between(startTime, Instant.now())
        
        return TimerResult(
            requestId = request.requestId,
            success = true,
            result = "All three timers completed successfully",
            actualDelay = actualDelay
        )
    }
    
    private fun processWithCancellableTimer(request: TimerRequest, startTime: Instant): TimerResult {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Starting cancellable timer")
        
        val timerFuture = Async.procedure {
            Workflow.sleep(Duration.ofSeconds(request.delaySeconds))
            logger.info("Timer completed without cancellation")
        }
        
        // Simulate cancellation condition
        val cancellationCheckFuture = Async.procedure {
            Workflow.sleep(Duration.ofSeconds(request.delaySeconds / 2))
            logger.info("Cancellation condition triggered")
        }
        
        // Wait for either timer completion or cancellation
        val cancelled = try {
            cancellationCheckFuture.get()
            timerFuture.cancel()
            true
        } catch (e: Exception) {
            false
        }
        
        val actualDelay = Duration.between(startTime, Instant.now())
        
        return TimerResult(
            requestId = request.requestId,
            success = !cancelled,
            result = if (cancelled) "Timer was cancelled" else "Timer completed normally",
            actualDelay = actualDelay
        )
    }
}

/**
 * Cron workflow implementation for scheduled recurring tasks.
 */
class CronWorkflowImpl : CronWorkflow {
    
    override fun runCronJob(config: CronConfig): CronResult {
        val logger = Workflow.getLogger(this::class.java)
        val executionTime = Instant.now()
        
        logger.info("Executing cron job: ${config.jobId} at $executionTime")
        
        if (!config.enabled) {
            logger.info("Job ${config.jobId} is disabled, skipping execution")
            
            // Calculate next run time even if disabled
            val nextRun = calculateNextRunTime(config, executionTime)
            
            // Wait until next run time
            if (nextRun != null) {
                val sleepDuration = Duration.between(executionTime, nextRun)
                if (sleepDuration.isPositive) {
                    Workflow.sleep(sleepDuration)
                }
                
                // Continue with next execution
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
            
            logger.info("Job ${config.jobId} completed successfully")
            
            // Calculate next run time
            val nextRun = calculateNextRunTime(config, executionTime)
            
            if (nextRun != null) {
                logger.info("Next run scheduled for: $nextRun")
                
                // Sleep until next execution
                val sleepDuration = Duration.between(executionTime, nextRun)
                if (sleepDuration.isPositive) {
                    Workflow.sleep(sleepDuration)
                }
                
                // Continue with next execution using continueAsNew
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
            logger.error("Job ${config.jobId} failed: ${e.message}")
            
            // Even on failure, schedule next run
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
    
    private fun executeJob(config: CronConfig): String {
        val logger = Workflow.getLogger(this::class.java)
        
        // Simulate job execution
        when (config.jobId) {
            "daily-report" -> {
                logger.info("Generating daily report")
                Workflow.sleep(Duration.ofSeconds(5))
                return "Daily report generated successfully"
            }
            
            "data-cleanup" -> {
                logger.info("Performing data cleanup")
                Workflow.sleep(Duration.ofSeconds(10))
                return "Data cleanup completed, 1000 records processed"
            }
            
            "health-check" -> {
                logger.info("Running health check")
                Workflow.sleep(Duration.ofSeconds(2))
                return "All systems healthy"
            }
            
            "backup" -> {
                logger.info("Creating backup")
                Workflow.sleep(Duration.ofSeconds(15))
                return "Backup created successfully"
            }
            
            else -> {
                logger.info("Executing generic job: ${config.jobId}")
                Workflow.sleep(Duration.ofSeconds(3))
                return "Generic job completed"
        }
    }
    
    private fun calculateNextRunTime(config: CronConfig, currentTime: Instant): Instant? {
        // Simplified cron calculation - in practice, use a proper cron library
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
            
            "0 0 0 * * MON" -> {
                // Weekly on Monday at midnight
                var nextRun = currentDateTime.plusDays(1)
                while (nextRun.dayOfWeek != DayOfWeek.MONDAY) {
                    nextRun = nextRun.plusDays(1)
                }
                nextRun.withHour(0)
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
}

/**
 * Advanced timer workflow with scheduling capabilities.
 */
@WorkflowInterface
interface ScheduledTaskWorkflow {
    
    @WorkflowMethod
    fun runScheduledTask(task: ScheduledTask): TaskResult
}

class ScheduledTaskWorkflowImpl : ScheduledTaskWorkflow {
    
    override fun runScheduledTask(task: ScheduledTask): TaskResult {
        val logger = Workflow.getLogger(this::class.java)
        val now = Instant.now()
        
        // Wait until scheduled time if in the future
        if (task.scheduledTime.isAfter(now)) {
            val delay = Duration.between(now, task.scheduledTime)
            logger.info("Waiting ${delay.seconds} seconds until scheduled time: ${task.scheduledTime}")
            Workflow.sleep(delay)
        }
        
        logger.info("Executing scheduled task: ${task.taskId}")
        
        try {
            // Execute the task
            val result = when (task.taskType) {
                TaskType.EMAIL_REMINDER -> executeEmailReminder(task)
                TaskType.DATA_PROCESSING -> executeDataProcessing(task)
                TaskType.SYSTEM_MAINTENANCE -> executeSystemMaintenance(task)
                TaskType.REPORT_GENERATION -> executeReportGeneration(task)
            }
            
            return TaskResult(
                taskId = task.taskId,
                success = true,
                executedAt = Instant.now(),
                result = result
            )
            
        } catch (e: Exception) {
            logger.error("Task execution failed: ${e.message}")
            
            return TaskResult(
                taskId = task.taskId,
                success = false,
                executedAt = Instant.now(),
                result = "Task failed: ${e.message}"
            )
        }
    }
    
    private fun executeEmailReminder(task: ScheduledTask): String {
        Workflow.sleep(Duration.ofSeconds(2))
        return "Email reminder sent to ${task.parameters["recipient"]}"
    }
    
    private fun executeDataProcessing(task: ScheduledTask): String {
        Workflow.sleep(Duration.ofSeconds(10))
        return "Processed ${task.parameters["recordCount"]} records"
    }
    
    private fun executeSystemMaintenance(task: ScheduledTask): String {
        Workflow.sleep(Duration.ofSeconds(30))
        return "System maintenance completed"
    }
    
    private fun executeReportGeneration(task: ScheduledTask): String {
        Workflow.sleep(Duration.ofSeconds(15))
        return "Report generated: ${task.parameters["reportType"]}"
    }
}

// Data classes
data class TimerRequest(
    val requestId: String,
    val delaySeconds: Long,
    val operation: String,
    val timeoutSeconds: Long? = null
)

data class TimerResult(
    val requestId: String,
    val success: Boolean,
    val result: String?,
    val actualDelay: Duration,
    val timedOut: Boolean = false
)

data class CronConfig(
    val jobId: String,
    val cronExpression: String,
    val timezone: String = "UTC",
    val enabled: Boolean = true
)

data class CronResult(
    val jobId: String,
    val executionTime: Instant,
    val success: Boolean,
    val nextRun: Instant?,
    val result: String?
)

data class ScheduledTask(
    val taskId: String,
    val taskType: TaskType,
    val scheduledTime: Instant,
    val parameters: Map<String, String>
)

data class TaskResult(
    val taskId: String,
    val success: Boolean,
    val executedAt: Instant,
    val result: String
)

enum class TaskType {
    EMAIL_REMINDER, DATA_PROCESSING, SYSTEM_MAINTENANCE, REPORT_GENERATION
} 