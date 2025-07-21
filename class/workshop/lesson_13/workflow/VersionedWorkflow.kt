package com.temporal.bootcamp.lesson13.workflow

// TODO: Add imports for WorkflowInterface, WorkflowMethod, and Workflow

/**
 * TODO: Create a workflow interface for VersionedWorkflow
 * 
 * This workflow demonstrates safe versioning patterns:
 * 
 * Requirements:
 * 1. Annotate with @WorkflowInterface
 * 2. Create a @WorkflowMethod called processWithVersioning that:
 *    - Takes a ProcessingRequest parameter
 *    - Returns a ProcessingResult
 * 3. This workflow will evolve through multiple versions safely
 */

// TODO: Define your VersionedWorkflow interface here

/**
 * TODO: Create the VersionedWorkflow implementation
 * 
 * Requirements:
 * 1. Implement the VersionedWorkflow interface
 * 2. Use Workflow.getVersion() to handle multiple versions
 * 3. Demonstrate safe migration from v1 to v2 to v3
 * 4. Handle backward compatibility for running workflows
 * 5. Show different versioning strategies (additive, breaking changes)
 */

// TODO: Implement VersionedWorkflowImpl class here

/**
 * Example versioning scenarios to implement:
 * 
 * Version 1: Basic processing
 * Version 2: Add new validation step (additive change)
 * Version 3: Change processing order (breaking change)
 */

data class ProcessingRequest(
    val requestId: String,
    val data: Map<String, Any>,
    val priority: ProcessingPriority = ProcessingPriority.NORMAL
)

data class ProcessingResult(
    val requestId: String,
    val success: Boolean,
    val result: String?,
    val version: Int,
    val processingSteps: List<String>
)

enum class ProcessingPriority {
    LOW, NORMAL, HIGH, URGENT
} 