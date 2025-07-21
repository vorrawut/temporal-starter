package com.temporal.bootcamp.lesson14.workflow

// TODO: Add imports for WorkflowInterface, WorkflowMethod, and timer-related classes

/**
 * TODO: Create a workflow interface for TimerWorkflow
 * 
 * This workflow demonstrates timer and scheduling patterns:
 * 
 * Requirements:
 * 1. Annotate with @WorkflowInterface
 * 2. Create a @WorkflowMethod called processWithTimer that:
 *    - Takes a TimerRequest parameter
 *    - Returns a TimerResult
 * 3. This workflow will use various timer patterns
 */

// TODO: Define your TimerWorkflow interface here

/**
 * TODO: Create a workflow interface for CronWorkflow
 * 
 * This demonstrates recurring workflows:
 * 
 * Requirements:
 * 1. Annotate with @WorkflowInterface
 * 2. Create a @WorkflowMethod called runCronJob that:
 *    - Takes a CronConfig parameter
 *    - Returns a CronResult
 * 3. This workflow will run on a schedule
 */

// TODO: Define your CronWorkflow interface here

/**
 * TODO: Create the TimerWorkflow implementation
 * 
 * Requirements:
 * 1. Implement the TimerWorkflow interface
 * 2. Use Workflow.sleep() for delays
 * 3. Demonstrate timeout patterns
 * 4. Show conditional waiting with timers
 * 5. Handle timer cancellation
 */

// TODO: Implement TimerWorkflowImpl class here

/**
 * TODO: Create the CronWorkflow implementation
 * 
 * Requirements:
 * 1. Implement the CronWorkflow interface
 * 2. Use continueAsNew for recurring execution
 * 3. Calculate next run time based on cron expression
 * 4. Handle timezone considerations
 * 5. Support job configuration changes
 */

// TODO: Implement CronWorkflowImpl class here

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
    val actualDelay: java.time.Duration,
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
    val executionTime: java.time.Instant,
    val success: Boolean,
    val nextRun: java.time.Instant?,
    val result: String?
) 