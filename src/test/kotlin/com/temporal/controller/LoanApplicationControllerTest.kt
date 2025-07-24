package com.temporal.controller

import com.temporal.model.*
import com.temporal.workflow.LoanApplicationWorkflow
import com.fasterxml.jackson.databind.ObjectMapper
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.client.WorkflowStub
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import io.mockk.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import org.hamcrest.Matchers.containsString

@WebMvcTest(LoanApplicationController::class)
class LoanApplicationControllerTest {
    
    @TestConfiguration
    class TestConfig {
        @Bean
        fun workflowClient() = mockk<WorkflowClient>(relaxed = true)
        
        @Bean
        fun mockWorkflow() = mockk<LoanApplicationWorkflow>(relaxed = true)
    }
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @Autowired
    private lateinit var workflowClient: WorkflowClient
    
    @Autowired
    private lateinit var mockWorkflow: LoanApplicationWorkflow
    
    @BeforeEach
    fun setUp() {
        clearAllMocks()
        
        // Setup mock workflow client to return our mock workflow for both method signatures
        every { 
            workflowClient.newWorkflowStub(
                LoanApplicationWorkflow::class.java,
                any<String>()
            )
        } returns mockWorkflow
        
        every { 
            workflowClient.newWorkflowStub(
                LoanApplicationWorkflow::class.java,
                any<WorkflowOptions>()
            )
        } returns mockWorkflow
        
        // Set up default workflow responses
        every { mockWorkflow.getCurrentState() } returns ApplicationStatus.SUBMITTED
        every { mockWorkflow.getApplicationDetails() } returns mockk(relaxed = true)
        every { mockWorkflow.getRiskAssessment() } returns null
        every { mockWorkflow.getProcessingHistory() } returns emptyList()
        every { mockWorkflow.approveApplication(any()) } just Runs
        every { mockWorkflow.rejectApplication(any()) } just Runs
    }
    
    @Test
    fun `should submit loan application successfully`() {
        // Given: Valid loan application request
        val request = LoanApplicationRequest(
            userId = "test-user-123",
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@test.com",
            phone = "+1-555-0123",
            loanAmount = BigDecimal("25000"),
            purpose = LoanPurpose.HOME_IMPROVEMENT,
            annualIncome = BigDecimal("75000"),
            documents = listOf(DocumentType.ID_CARD, DocumentType.INCOME_STATEMENT)
        )
        
        // When & Then: Submit application
        mockMvc.perform(post("/api/loan/apply")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("SUBMITTED"))
            .andExpect(jsonPath("$.workflowId").exists())
            .andExpect(jsonPath("$.message").value("Your loan application has been submitted and is being processed"))
    }
    
    @Test
    fun `should reject invalid loan application`() {
        // Given: Invalid request (missing required fields)
        val invalidRequest = """
            {
                "userId": "",
                "firstName": "",
                "loanAmount": -1000
            }
        """.trimIndent()
        
        // When & Then: Submit invalid application
        mockMvc.perform(post("/api/loan/apply")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequest))
            .andExpect(status().isBadRequest)
    }
    
    @Test
    fun `should approve loan successfully`() {
        // Given: Approval request
        val approvalRequest = ApprovalRequest(
            genId = "test-gen-id-123",
            approvedBy = "manager-123",
            notes = "Application meets all criteria"
        )
        
        // When & Then: Approve loan
        mockMvc.perform(post("/api/loan/approve/test-user-123")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(approvalRequest)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Loan approved successfully"))
            .andExpect(jsonPath("$.workflowId").exists())
        
        // Verify workflow method was called
        verify { mockWorkflow.approveApplication(any()) }
    }
    
    @Test
    fun `should reject loan successfully`() {
        // Given: Rejection request
        val rejectionRequest = RejectionRequest(
            genId = "test-gen-id-456",
            rejectedBy = "manager-123",
            reason = "Insufficient income documentation",
            notes = "Need more documents"
        )
        
        // When & Then: Reject loan
        mockMvc.perform(post("/api/loan/reject/test-user-123")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(rejectionRequest)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Loan rejected"))
            .andExpect(jsonPath("$.workflowId").exists())
        
        // Verify workflow method was called
        verify { mockWorkflow.rejectApplication(any()) }
    }
    
    @Test
    fun `should get loan status successfully`() {
        // Given: Mock workflow state
        val application = LoanApplication(
            workflowId = "test-workflow-123",
            userId = "test-user-123",
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@test.com",
            phone = "+1-555-0123",
            loanAmount = BigDecimal("25000"),
            purpose = LoanPurpose.HOME_IMPROVEMENT,
            annualIncome = BigDecimal("75000"),
            documents = listOf(DocumentType.ID_CARD)
        )
        
        val riskAssessment = RiskAssessment(
            applicationId = "test-workflow-123",
            creditScore = 750,
            riskLevel = RiskLevel.LOW,
            debtToIncomeRatio = BigDecimal("0.25"),
            riskFactors = emptyList()
        )
        
        val history = listOf(
            "Application received",
            "Document validation completed", 
            "Risk assessment completed: LOW"
        )
        
        every { mockWorkflow.getCurrentState() } returns ApplicationStatus.AWAITING_APPROVAL
        every { mockWorkflow.getApplicationDetails() } returns application
        every { mockWorkflow.getRiskAssessment() } returns riskAssessment
        every { mockWorkflow.getProcessingHistory() } returns history
        
        // When & Then: Get status
        mockMvc.perform(get("/api/loan/status/test-user-123"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("AWAITING_APPROVAL"))
            .andExpect(jsonPath("$.workflowId").exists())
            .andExpect(jsonPath("$.application.userId").value("test-user-123"))
            .andExpect(jsonPath("$.riskAssessment.creditScore").value(750))
            .andExpect(jsonPath("$.riskAssessment.riskLevel").value("LOW"))
            .andExpect(jsonPath("$.processingHistory").isArray)
            .andExpect(jsonPath("$.processingHistory[0]").value("Application received"))
    }
    
    @Test
    fun `should handle user not found for status check`() {
        // Given: Workflow that throws exception when queried (simulating non-existent workflow)
        every { mockWorkflow.getCurrentState() } throws RuntimeException("Workflow not found")
        
        // When & Then: Get status for non-existent user
        mockMvc.perform(get("/api/loan/status/non-existent-user"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message", containsString("Failed to get status")))
    }
    
    @Test
    fun `should query workflow state successfully`() {
        // Given: Mock workflow state for query
        val application = LoanApplication(
            workflowId = "test-workflow-123",
            userId = "test-user-123",
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@test.com",
            phone = "+1-555-0123",
            loanAmount = BigDecimal("25000"),
            purpose = LoanPurpose.HOME_IMPROVEMENT,
            annualIncome = BigDecimal("75000"),
            documents = listOf(DocumentType.ID_CARD)
        )
        
        every { mockWorkflow.getCurrentState() } returns ApplicationStatus.RISK_SCORING
        every { mockWorkflow.getApplicationDetails() } returns application
        every { mockWorkflow.getRiskAssessment() } returns null // Not yet assessed
        every { mockWorkflow.getProcessingHistory() } returns listOf("Application received")
        
        // When & Then: Query workflow state
        mockMvc.perform(get("/api/loan/query/test-user-123/state"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.workflowId").exists())
            .andExpect(jsonPath("$.currentState").value("RISK_SCORING"))
            .andExpect(jsonPath("$.applicationDetails.userId").value("test-user-123"))
            .andExpect(jsonPath("$.riskAssessment").isEmpty)
            .andExpect(jsonPath("$.processingHistory").isArray)
    }
    
    @Test
    fun `should handle workflow client errors gracefully`() {
        // Given: Workflow client throws exception
        every { 
            workflowClient.newWorkflowStub(
                LoanApplicationWorkflow::class.java,
                any<String>()
            )
        } throws RuntimeException("Temporal service unavailable")
        
        // When & Then: Try to get status
        mockMvc.perform(get("/api/loan/status/test-user-123"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message", containsString("Failed to get status")))
    }
    
    @Test
    fun `should validate loan application request data`() {
        // Given: Request with invalid data types and missing fields
        val invalidRequest = """
            {
                "userId": "test-user",
                "firstName": "John",
                "lastName": "Doe", 
                "email": "invalid-email",
                "phone": "",
                "loanAmount": "not-a-number",
                "annualIncome": -50000,
                "documents": []
            }
        """.trimIndent()
        
        // When & Then: Submit invalid application
        mockMvc.perform(post("/api/loan/apply")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequest))
            .andExpect(status().isBadRequest)
    }
    
    @Test
    fun `should handle large loan amounts`() {
        // Given: Large loan application
        val largeRequest = LoanApplicationRequest(
            userId = "high-value-user",
            firstName = "Jane",
            lastName = "Smith",
            email = "jane.smith@test.com",
            phone = "+1-555-0456",
            loanAmount = BigDecimal("500000"), // Large amount
            purpose = LoanPurpose.BUSINESS,
            annualIncome = BigDecimal("200000"),
            documents = listOf(
                DocumentType.ID_CARD,
                DocumentType.INCOME_STATEMENT,
                DocumentType.BANK_STATEMENT,
                DocumentType.TAX_RETURNS,
                DocumentType.BUSINESS_LICENSE
            )
        )
        
        // When & Then: Submit large application
        mockMvc.perform(post("/api/loan/apply")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(largeRequest)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("SUBMITTED"))
            .andExpect(jsonPath("$.workflowId").exists())
    }
} 