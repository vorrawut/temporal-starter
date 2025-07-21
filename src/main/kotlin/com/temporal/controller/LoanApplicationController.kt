package com.temporal.controller

import com.temporal.model.*
import com.temporal.workflow.LoanApplicationWorkflow
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.util.*

@RestController
@RequestMapping("/api/loan")
@CrossOrigin(origins = ["*"])
class LoanApplicationController(
    private val workflowClient: WorkflowClient
) {
    
    private val logger = LoggerFactory.getLogger(LoanApplicationController::class.java)
    
    @PostMapping("/apply")
    fun applyForLoan(@RequestBody request: LoanApplicationRequest): ResponseEntity<LoanApplicationResponse> {
        logger.info("Received loan application for user: ${request.userId}")
        
        try {
            val workflowId = "loan-app-${request.userId}-${System.currentTimeMillis()}"
            
            val application = LoanApplication(
                workflowId = workflowId,
                userId = request.userId,
                firstName = request.firstName,
                lastName = request.lastName,
                email = request.email,
                phone = request.phone,
                loanAmount = request.loanAmount,
                purpose = request.purpose,
                annualIncome = request.annualIncome,
                documents = request.documents
            )
            
            val workflowOptions = WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId)
                .setTaskQueue("loan-processing-queue")
                .setWorkflowExecutionTimeout(Duration.ofDays(30))
                .setWorkflowRunTimeout(Duration.ofDays(30))
                .build()
            
            val workflow = workflowClient.newWorkflowStub(
                LoanApplicationWorkflow::class.java,
                workflowOptions
            )
            
            // Start workflow asynchronously
            WorkflowClient.start { workflow.processLoanApplication(application) }
            
            logger.info("Loan application workflow started: $workflowId")
            
            val response = LoanApplicationResponse(
                workflowId = workflowId,
                status = "SUBMITTED",
                message = "Your loan application has been submitted and is being processed"
            )
            
            return ResponseEntity.ok(response)
            
        } catch (e: Exception) {
            logger.error("Failed to submit loan application", e)
            
            val errorResponse = LoanApplicationResponse(
                workflowId = "",
                status = "ERROR",
                message = "Failed to submit application: ${e.message}"
            )
            
            return ResponseEntity.badRequest().body(errorResponse)
        }
    }
    
    @PostMapping("/approve/{userId}")
    fun approveLoan(
        @PathVariable userId: String,
        @RequestBody decision: ApprovalRequest
    ): ResponseEntity<Map<String, String>> {
        logger.info("Approving loan for user: $userId")
        
        return try {
            val workflowId = findWorkflowIdByUserId(userId)
            
            if (workflowId == null) {
                return ResponseEntity.badRequest().body(
                    mapOf("error" to "No active loan application found for user: $userId")
                )
            }
            
            val workflow = workflowClient.newWorkflowStub(
                LoanApplicationWorkflow::class.java,
                workflowId
            )
            
            val approvalDecision = ApprovalDecision(
                applicationId = workflowId,
                approvedBy = decision.approvedBy,
                status = ApprovalStatus.APPROVED,
                notes = decision.notes ?: "Application approved"
            )
            
            workflow.approveApplication(approvalDecision)
            
            logger.info("Approval signal sent for workflow: $workflowId")
            
            ResponseEntity.ok(
                mapOf(
                    "message" to "Loan approved successfully",
                    "workflowId" to workflowId
                )
            )
            
        } catch (e: Exception) {
            logger.error("Failed to approve loan for user: $userId", e)
            
            ResponseEntity.badRequest().body(
                mapOf("error" to "Failed to approve loan: ${e.message}")
            )
        }
    }
    
    @PostMapping("/reject/{userId}")
    fun rejectLoan(
        @PathVariable userId: String,
        @RequestBody decision: RejectionRequest
    ): ResponseEntity<Map<String, String>> {
        logger.info("Rejecting loan for user: $userId")
        
        return try {
            val workflowId = findWorkflowIdByUserId(userId)
            
            if (workflowId == null) {
                return ResponseEntity.badRequest().body(
                    mapOf("error" to "No active loan application found for user: $userId")
                )
            }
            
            val workflow = workflowClient.newWorkflowStub(
                LoanApplicationWorkflow::class.java,
                workflowId
            )
            
            val rejectionDecision = RejectionDecision(
                applicationId = workflowId,
                rejectedBy = decision.rejectedBy,
                reason = decision.reason,
                notes = decision.notes ?: "Application rejected"
            )
            
            workflow.rejectApplication(rejectionDecision)
            
            logger.info("Rejection signal sent for workflow: $workflowId")
            
            ResponseEntity.ok(
                mapOf(
                    "message" to "Loan rejected",
                    "workflowId" to workflowId
                )
            )
            
        } catch (e: Exception) {
            logger.error("Failed to reject loan for user: $userId", e)
            
            ResponseEntity.badRequest().body(
                mapOf("error" to "Failed to reject loan: ${e.message}")
            )
        }
    }
    
    @GetMapping("/status/{userId}")
    fun getLoanStatus(@PathVariable userId: String): ResponseEntity<LoanStatusResponse> {
        logger.info("Getting loan status for user: $userId")
        
        return try {
            val workflowId = findWorkflowIdByUserId(userId)
            
            if (workflowId == null) {
                return ResponseEntity.badRequest().body(
                    LoanStatusResponse(
                        workflowId = "",
                        status = ApplicationStatus.SUBMITTED,
                        message = "No loan application found for user: $userId"
                    )
                )
            }
            
            val workflow = workflowClient.newWorkflowStub(
                LoanApplicationWorkflow::class.java,
                workflowId
            )
            
            val currentState = workflow.getCurrentState()
            val application = workflow.getApplicationDetails()
            val riskAssessment = workflow.getRiskAssessment()
            val history = workflow.getProcessingHistory()
            
            val response = LoanStatusResponse(
                workflowId = workflowId,
                status = currentState,
                application = application,
                riskAssessment = riskAssessment,
                processingHistory = history,
                message = "Status retrieved successfully"
            )
            
            ResponseEntity.ok(response)
            
        } catch (e: Exception) {
            logger.error("Failed to get loan status for user: $userId", e)
            
            val errorResponse = LoanStatusResponse(
                workflowId = "",
                status = ApplicationStatus.SUBMITTED,
                message = "Failed to get status: ${e.message}"
            )
            
            ResponseEntity.badRequest().body(errorResponse)
        }
    }
    
    @GetMapping("/query/{userId}/state")
    fun queryWorkflowState(@PathVariable userId: String): ResponseEntity<Map<String, Any?>> {
        return try {
            val workflowId = findWorkflowIdByUserId(userId)
            
            if (workflowId == null) {
                return ResponseEntity.badRequest().body(
                    mapOf("error" to "No active workflow found for user: $userId")
                )
            }
            
            val workflow = workflowClient.newWorkflowStub(
                LoanApplicationWorkflow::class.java,
                workflowId
            )
            
            ResponseEntity.ok(
                mapOf(
                    "workflowId" to workflowId,
                    "currentState" to workflow.getCurrentState(),
                    "applicationDetails" to workflow.getApplicationDetails(),
                    "riskAssessment" to workflow.getRiskAssessment(),
                    "processingHistory" to workflow.getProcessingHistory()
                )
            )
            
        } catch (e: Exception) {
            logger.error("Failed to query workflow state", e)
            ResponseEntity.badRequest().body(
                mapOf("error" to "Failed to query state: ${e.message}")
            )
        }
    }
    
    private fun findWorkflowIdByUserId(userId: String): String? {
        // In a real application, you'd store this mapping in a database
        // For demo purposes, we'll use a simple pattern matching
        // This is a simplified approach - in production you'd use proper workflow search
        
        return try {
            // For demo: assume workflow ID follows pattern "loan-app-{userId}-{timestamp}"
            // In real implementation, you'd query your database or use Temporal's search API
            "loan-app-$userId-latest" // Simplified for demo
        } catch (e: Exception) {
            null
        }
    }
}

// Request/Response DTOs
data class LoanApplicationRequest(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val loanAmount: java.math.BigDecimal,
    val purpose: LoanPurpose,
    val annualIncome: java.math.BigDecimal,
    val documents: List<DocumentType>
)

data class LoanApplicationResponse(
    val workflowId: String,
    val status: String,
    val message: String
)

data class ApprovalRequest(
    val approvedBy: String,
    val notes: String?
)

data class RejectionRequest(
    val rejectedBy: String,
    val reason: String,
    val notes: String?
)

data class LoanStatusResponse(
    val workflowId: String,
    val status: ApplicationStatus,
    val application: LoanApplication? = null,
    val riskAssessment: RiskAssessment? = null,
    val processingHistory: List<String>? = null,
    val message: String
) 