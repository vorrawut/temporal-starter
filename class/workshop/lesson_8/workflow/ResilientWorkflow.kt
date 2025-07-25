package com.temporal.bootcamp.lesson8.workflow

// TODO: Add imports for WorkflowInterface, WorkflowMethod, and Temporal classes

/**
 * TODO: Create a workflow interface for ResilientWorkflow
 * 
 * This workflow demonstrates advanced retry and timeout patterns:
 * 
 * Requirements:
 * 1. Annotate with @WorkflowInterface  
 * 2. Create a method called processWithRetries that:
 *    - Takes a ProcessingRequest parameter
 *    - Returns a ProcessingResult
 *    - Is annotated with @WorkflowMethod
 */

// TODO: Define your ResilientWorkflow interface here

// TODO: Create data classes:
// - ProcessingRequest (requestId, data, priority)
// - ProcessingResult (success, result, errors, retryCount)

/**
 * TODO: Create the ResilientWorkflow implementation
 * 
 * Requirements:
 * 1. Implement the ResilientWorkflow interface
 * 2. Create activity stubs with different retry strategies:
 *    - Quick validation activity (aggressive retries)
 *    - External API activity (conservative retries)  
 *    - Database activity (moderate retries)
 * 3. Configure different timeouts for each activity type
 * 4. Handle different failure types appropriately
 */

// TODO: Implement ResilientWorkflowImpl class here

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