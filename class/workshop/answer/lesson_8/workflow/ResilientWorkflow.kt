package com.temporal.bootcamp.lesson8.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import io.temporal.workflow.Workflow
import io.temporal.activity.ActivityOptions
import io.temporal.common.RetryOptions
import java.time.Duration

/**
 * Resilient workflow demonstrating advanced retry and timeout patterns.
 */
@WorkflowInterface
interface ResilientWorkflow {
    
    /**
     * Processes requests with sophisticated retry and timeout strategies.
     * 
     * @param request The processing request
     * @return ProcessingResult with retry information
     */
    @WorkflowMethod
    fun processWithRetries(request: ProcessingRequest): ProcessingResult
}

/**
 * Implementation demonstrating different retry strategies for different operation types.
 */
class ResilientWorkflowImpl : ResilientWorkflow {
    
    // Quick validation - aggressive retries
    private val validationActivity = Workflow.newActivityStub(
        ValidationActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofMillis(500))
                    .setMaximumInterval(Duration.ofSeconds(10))
                    .setBackoffCoefficient(1.5)
                    .setMaximumAttempts(10)
                    .build()
            )
            .build()
    )
    
    // External API calls - conservative retries
    private val externalApiActivity = Workflow.newActivityStub(
        ExternalApiActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(2))
            .setScheduleToCloseTimeout(Duration.ofMinutes(10))
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
    
    // Database operations - moderate retries
    private val databaseActivity = Workflow.newActivityStub(
        DatabaseActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .setScheduleToCloseTimeout(Duration.ofMinutes(5))
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(1))
                    .setMaximumInterval(Duration.ofSeconds(30))
                    .setBackoffCoefficient(2.0)
                    .setMaximumAttempts(5)
                    .build()
            )
            .build()
    )
    
    override fun processWithRetries(request: ProcessingRequest): ProcessingResult {
        val logger = Workflow.getLogger(this::class.java)
        val errors = mutableListOf<String>()
        var totalRetries = 0
        
        logger.info("Processing request: ${request.requestId} with priority: ${request.priority}")
        
        try {
            // Step 1: Validation (quick, many retries)
            val validationResult = validationActivity.validateRequest(request)
            logger.info("Validation completed: ${validationResult.isValid}")
            
            if (!validationResult.isValid) {
                return ProcessingResult(
                    success = false,
                    result = null,
                    errors = listOf("Validation failed: ${validationResult.error}"),
                    retryCount = 0
                )
            }
            
            // Step 2: External API call (conservative retries)
            val apiResult = externalApiActivity.callExternalService(request.data)
            totalRetries += apiResult.retryCount
            logger.info("External API call completed, retries: ${apiResult.retryCount}")
            
            // Step 3: Database operation (moderate retries)
            val dbResult = databaseActivity.saveResult(request.requestId, apiResult.data)
            totalRetries += dbResult.retryCount
            logger.info("Database operation completed, retries: ${dbResult.retryCount}")
            
            return ProcessingResult(
                success = true,
                result = "Processing completed successfully",
                errors = errors,
                retryCount = totalRetries
            )
            
        } catch (e: Exception) {
            logger.error("Processing failed: ${e.message}")
            errors.add("Processing failed: ${e.message}")
            
            return ProcessingResult(
                success = false,
                result = null,
                errors = errors,
                retryCount = totalRetries
            )
        }
    }
}

data class ProcessingRequest(
    val requestId: String,
    val data: Map<String, Any>,
    val priority: RequestPriority = RequestPriority.NORMAL
)

data class ProcessingResult(
    val success: Boolean,
    val result: String?,
    val errors: List<String> = emptyList(),
    val retryCount: Int = 0
)

enum class RequestPriority {
    LOW, NORMAL, HIGH, CRITICAL
} 