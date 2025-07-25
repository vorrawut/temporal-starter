package com.temporal.bootcamp.lesson10.workflow

import io.temporal.workflow.*
import java.time.Duration
import java.time.Instant

/**
 * Approval workflow demonstrating signals and queries.
 */
@WorkflowInterface
interface ApprovalWorkflow {
    
    /**
     * Main workflow method that processes approval requests.
     */
    @WorkflowMethod
    fun processApprovalRequest(request: ApprovalRequest): ApprovalResult
    
    /**
     * Signal to approve the request.
     */
    @SignalMethod
    fun approve(approverEmail: String, comment: String)
    
    /**
     * Signal to reject the request.
     */
    @SignalMethod
    fun reject(approverEmail: String, reason: String)
    
    /**
     * Query to get current approval status.
     */
    @QueryMethod
    fun getStatus(): ApprovalStatus
    
    /**
     * Query to get list of pending approvers.
     */
    @QueryMethod
    fun getPendingApprovers(): List<String>
}

/**
 * Implementation of approval workflow with signal and query handling.
 */
class ApprovalWorkflowImpl : ApprovalWorkflow {
    
    private var status = ApprovalStatus.PENDING
    private var approvals = mutableListOf<Approval>()
    private var rejections = mutableListOf<Rejection>()
    private val pendingApprovers = mutableSetOf<String>()
    
    override fun processApprovalRequest(request: ApprovalRequest): ApprovalResult {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Starting approval process for: ${request.id}")
        
        // Initialize pending approvers
        pendingApprovers.addAll(request.requiredApprovers)
        
        // Wait for approval, rejection, or timeout (7 days)
        val approved = Workflow.await(Duration.ofDays(7)) {
            status != ApprovalStatus.PENDING
        }
        
        return when (status) {
            ApprovalStatus.APPROVED -> {
                logger.info("Request approved: ${request.id}")
                ApprovalResult.Approved(
                    requestId = request.id,
                    approvals = approvals.toList(),
                    executionResult = "Request executed successfully"
                )
            }
            ApprovalStatus.REJECTED -> {
                logger.info("Request rejected: ${request.id}")
                ApprovalResult.Rejected(
                    requestId = request.id,
                    rejections = rejections.toList()
                )
            }
            ApprovalStatus.PENDING -> {
                logger.warn("Request timed out: ${request.id}")
                ApprovalResult.TimedOut(
                    requestId = request.id,
                    timeoutDays = 7
                )
            }
        }
    }
    
    override fun approve(approverEmail: String, comment: String) {
        if (status != ApprovalStatus.PENDING) return
        
        val approval = Approval(
            approverEmail = approverEmail,
            comment = comment,
            timestamp = Instant.now()
        )
        
        approvals.add(approval)
        pendingApprovers.remove(approverEmail)
        
        // If all required approvers have approved, mark as approved
        if (pendingApprovers.isEmpty()) {
            status = ApprovalStatus.APPROVED
        }
    }
    
    override fun reject(approverEmail: String, reason: String) {
        if (status != ApprovalStatus.PENDING) return
        
        val rejection = Rejection(
            approverEmail = approverEmail,
            reason = reason,
            timestamp = Instant.now()
        )
        
        rejections.add(rejection)
        status = ApprovalStatus.REJECTED
    }
    
    override fun getStatus(): ApprovalStatus = status
    
    override fun getPendingApprovers(): List<String> = pendingApprovers.toList()
}

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
    val timestamp: Instant
)

data class Rejection(
    val approverEmail: String,
    val reason: String,
    val timestamp: Instant
)

enum class ApprovalStatus {
    PENDING, APPROVED, REJECTED
} 