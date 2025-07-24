package com.temporal.workflow

import com.temporal.activity.*
import com.temporal.model.*
import io.temporal.activity.ActivityOptions
import io.temporal.common.RetryOptions
import io.temporal.failure.ApplicationFailure
import io.temporal.workflow.ChildWorkflowOptions
import io.temporal.workflow.Workflow
import java.time.Duration
import java.time.LocalDateTime

class LoanApplicationWorkflowImpl : LoanApplicationWorkflow {
    
    // Activity stubs with different configurations
    private val documentActivity = Workflow.newActivityStub(
        DocumentValidationActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(5))
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(1))
                    .setMaximumInterval(Duration.ofSeconds(10))
                    .setBackoffCoefficient(2.0)
                    .setMaximumAttempts(3)
                    .build()
            )
            .build()
    )
    
    private val riskScoringActivity = Workflow.newActivityStub(
        RiskScoringActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(10))
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(2))
                    .setMaximumInterval(Duration.ofSeconds(30))
                    .setBackoffCoefficient(2.0)
                    .setMaximumAttempts(5)
                    .build()
            )
            .build()
    )
    
    private val disbursementActivity = Workflow.newActivityStub(
        LoanDisbursementActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(15))
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(5))
                    .setMaximumInterval(Duration.ofMinutes(1))
                    .setBackoffCoefficient(2.0)
                    .setMaximumAttempts(3)
                    .build()
            )
            .build()
    )
    
    private val notificationActivity = Workflow.newActivityStub(
        NotificationActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(5))
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(1))
                    .setMaximumInterval(Duration.ofSeconds(10))
                    .setBackoffCoefficient(1.5)
                    .setMaximumAttempts(10)
                    .build()
            )
            .build()
    )
    
    private val followUpWorkflow = Workflow.newChildWorkflowStub(
        FollowUpWorkflow::class.java,
        ChildWorkflowOptions.newBuilder()
            .setTaskQueue("follow-up-queue")
            .build()
    )
    
    // Workflow state
    private lateinit var currentApplication: LoanApplication
    private var currentState: ApplicationStatus = ApplicationStatus.SUBMITTED
    private var riskAssessment: RiskAssessment? = null
    private var approvalReceived = false
    private var rejectionReceived = false
    private var moreInfoRequested = false
    private val processingHistory = mutableListOf<String>()
    private var disbursement: LoanDisbursement? = null
    
    override fun processLoanApplication(application: LoanApplication): String {
        currentApplication = application
        addToHistory("Application received for user ${application.userId}")
        
        try {
            // Step 1: Document Validation
            currentState = ApplicationStatus.DOCUMENT_VALIDATION
            addToHistory("Starting document validation")
            
            val documentResult = documentActivity.validateDocuments(application)
            if (!documentResult.isValid) {
                currentState = ApplicationStatus.REJECTED
                addToHistory("Application rejected due to invalid documents: ${documentResult.issues}")
                
                // Send rejection notification
                notificationActivity.sendRejectionNotification(
                    application, 
                    "Invalid documents: ${documentResult.issues.joinToString(", ")}"
                )
                
                return "REJECTED - Invalid documents"
            }
            addToHistory("Document validation completed successfully")
            
            // Step 2: Risk Scoring
            currentState = ApplicationStatus.RISK_SCORING
            addToHistory("Starting risk assessment")
            
            try {
                riskAssessment = riskScoringActivity.calculateRiskScore(application)
                addToHistory("Risk assessment completed: ${riskAssessment!!.riskLevel}")
                
                // Auto-reject high-risk applications
                if (riskAssessment!!.riskLevel == RiskLevel.VERY_HIGH) {
                    currentState = ApplicationStatus.REJECTED
                    addToHistory("Application auto-rejected due to very high risk")
                    
                    notificationActivity.sendRejectionNotification(
                        application, 
                        "Application rejected due to high risk factors"
                    )
                    
                    return "REJECTED - High risk"
                }
                
            } catch (e: Exception) {
                addToHistory("Risk scoring failed: ${e.message}")
                throw ApplicationFailure.newFailure("Risk scoring failed", "RISK_SCORING_ERROR")
            }
            
            // Step 3: Wait for Manual Approval
            currentState = ApplicationStatus.AWAITING_APPROVAL
            addToHistory("Awaiting manual approval")
            
            // Send notification to application received
            notificationActivity.sendApplicationConfirmation(application)
            
            // Wait for approval/rejection signal with timeout
            val approvalTimeout = Duration.ofDays(7)
            val approved = Workflow.await(approvalTimeout) { 
                approvalReceived || rejectionReceived || moreInfoRequested 
            }
            
            if (!approved) {
                // Timeout - auto-reject
                currentState = ApplicationStatus.EXPIRED
                addToHistory("Application expired due to no response within 7 days")
                
                notificationActivity.sendRejectionNotification(
                    application, 
                    "Application expired - no response within 7 days"
                )
                
                return "EXPIRED"
            }
            
            if (rejectionReceived) {
                currentState = ApplicationStatus.REJECTED
                addToHistory("Application manually rejected")
                return "REJECTED - Manual rejection"
            }
            
            if (moreInfoRequested) {
                addToHistory("More information requested - workflow paused")
                // In a real scenario, we might wait for additional documents
                // For demo purposes, we'll continue with approval
            }
            
            // Step 4: Loan Disbursement
            currentState = ApplicationStatus.APPROVED
            addToHistory("Application approved, starting disbursement")
            
            notificationActivity.sendApprovalNotification(application)
            
            currentState = ApplicationStatus.DISBURSING
            addToHistory("Starting loan disbursement")
            
            try {
                disbursement = disbursementActivity.disburseLoan(application)
                
                if (disbursement!!.status == DisbursementStatus.COMPLETED) {
                    currentState = ApplicationStatus.DISBURSED
                    addToHistory("Loan successfully disbursed: ${disbursement!!.transactionId}")
                    
                    // Send final confirmation
                    notificationActivity.sendDisbursementConfirmation(application, disbursement!!)
                    
                    // Step 5: Start Follow-up Workflow
                    addToHistory("Starting follow-up workflow")
                    followUpWorkflow.startFollowUp(application.workflowId, application.userId)
                    
                    return "DISBURSED - ${disbursement!!.transactionId}"
                    
                } else {
                    currentState = ApplicationStatus.FAILED
                    addToHistory("Disbursement failed: ${disbursement!!.failureReason}")
                    
                    // Compensation: Reverse any partial operations
                    compensateFailedDisbursement()
                    
                    return "FAILED - Disbursement failed"
                }
                
            } catch (e: Exception) {
                currentState = ApplicationStatus.FAILED
                addToHistory("Disbursement error: ${e.message}")
                
                // Compensation logic
                compensateFailedDisbursement()
                
                throw ApplicationFailure.newFailure("Disbursement failed", "DISBURSEMENT_ERROR")
            }
            
        } catch (e: ApplicationFailure) {
            addToHistory("Workflow failed: ${e.message}")
            currentState = ApplicationStatus.FAILED
            
            // Send failure notification
            try {
                notificationActivity.sendRejectionNotification(
                    currentApplication, 
                    "Application processing failed: ${e.message}"
                )
            } catch (notificationError: Exception) {
                addToHistory("Failed to send failure notification: ${notificationError.message}")
            }
            
            throw e
        }
    }
    
    private fun compensateFailedDisbursement() {
        addToHistory("Starting compensation for failed disbursement")
        
        try {
            // Reverse any partial transactions
            disbursementActivity.compensateFailedDisbursement(currentApplication.workflowId)
            addToHistory("Compensation completed successfully")
            
            // Notify about the failure and compensation
            notificationActivity.sendRejectionNotification(
                currentApplication, 
                "Loan disbursement failed but all changes have been reversed"
            )
            
        } catch (e: Exception) {
            addToHistory("Compensation failed: ${e.message}")
            // Log the compensation failure but don't throw - this is a critical error
            // In production, this would trigger alerts for manual intervention
        }
    }
    
    override fun approveApplication(decision: ApprovalDecision) {
        addToHistory("Approval received from ${decision.approvedBy}: ${decision.notes}")
        approvalReceived = true
    }
    
    override fun rejectApplication(decision: RejectionDecision) {
        addToHistory("Rejection received from ${decision.rejectedBy}: ${decision.reason}")
        rejectionReceived = true
    }
    
    override fun requestMoreInformation(message: String) {
        addToHistory("More information requested: $message")
        moreInfoRequested = true
    }
    
    override fun getCurrentState(): ApplicationStatus = currentState
    
    override fun getApplicationDetails(): LoanApplication = currentApplication
    
    override fun getRiskAssessment(): RiskAssessment? = riskAssessment
    
    override fun getProcessingHistory(): List<String> = processingHistory.toList()
    
    private fun addToHistory(message: String) {
        processingHistory.add("${java.time.LocalDateTime.now()}: $message")
    }
} 