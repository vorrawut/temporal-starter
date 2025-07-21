package com.temporal.bootcamp.lesson10.workflow

// TODO: Add imports for WorkflowInterface, WorkflowMethod, SignalMethod, QueryMethod

/**
 * TODO: Create a workflow interface for ApprovalWorkflow
 * 
 * This workflow demonstrates signals and queries:
 * 
 * Requirements:
 * 1. Annotate with @WorkflowInterface
 * 2. Create a @WorkflowMethod called processApprovalRequest
 * 3. Create @SignalMethod methods for approve() and reject()
 * 4. Create @QueryMethod methods for getStatus() and getPendingApprovers()
 */

// TODO: Define your ApprovalWorkflow interface here

/**
 * TODO: Create the ApprovalWorkflow implementation
 * 
 * Requirements:
 * 1. Implement the ApprovalWorkflow interface
 * 2. Maintain workflow state (status, approvals, rejections)
 * 3. Use Workflow.await() to wait for signals or timeout
 * 4. Handle signal methods to update state
 * 5. Implement query methods to return current state
 * 6. Include timeout handling (e.g., 7 days)
 */

// TODO: Implement ApprovalWorkflowImpl class here

data class ApprovalRequest(
    val id: String,
    val description: String,
    val requiredApprovers: List<String>,
    val requestedBy: String
)

sealed class ApprovalResult {
    data class Approved(
        val requestId: String,
        val approvals: List<Approval>,
        val executionResult: String
    ) : ApprovalResult()
    
    data class Rejected(
        val requestId: String,
        val rejections: List<Rejection>
    ) : ApprovalResult()
    
    data class TimedOut(
        val requestId: String,
        val timeoutDays: Int
    ) : ApprovalResult()
}

data class Approval(
    val approverEmail: String,
    val comment: String,
    val timestamp: java.time.Instant
)

data class Rejection(
    val approverEmail: String,
    val reason: String,
    val timestamp: java.time.Instant
)

enum class ApprovalStatus {
    PENDING, APPROVED, REJECTED
} 