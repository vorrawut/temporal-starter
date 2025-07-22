package com.temporal.workflow

import com.temporal.activity.*
import com.temporal.model.*
import io.temporal.testing.TestWorkflowRule
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import org.junit.Assert.*
import io.mockk.*
import java.math.BigDecimal

class LoanApplicationWorkflowTest {
    
    @get:Rule
    val testWorkflowRule = TestWorkflowRule.newBuilder()
        .setWorkflowTypes(LoanApplicationWorkflowImpl::class.java)
        .build()
    
    private lateinit var workflow: LoanApplicationWorkflow
    private lateinit var documentActivity: DocumentValidationActivity
    private lateinit var riskScoringActivity: RiskScoringActivity
    private lateinit var disbursementActivity: LoanDisbursementActivity
    private lateinit var notificationActivity: NotificationActivity
    
    @Before
    fun setUp() {
        // Create mock activities
        documentActivity = mockk<DocumentValidationActivity>()
        riskScoringActivity = mockk<RiskScoringActivity>()
        disbursementActivity = mockk<LoanDisbursementActivity>()
        notificationActivity = mockk<NotificationActivity>()
        
        // Register activities with test rule
        testWorkflowRule.worker.registerActivitiesImplementations(
            documentActivity,
            riskScoringActivity,
            disbursementActivity,
            notificationActivity
        )
        
        // Create workflow stub
        workflow = testWorkflowRule.workflowClient.newWorkflowStub(
            LoanApplicationWorkflow::class.java,
            testWorkflowRule.taskQueue
        )
    }
    
    @Test
    fun `should reject application with invalid documents`() {
        // Given: Application with invalid documents
        val application = createValidLoanApplication()
        
        every { documentActivity.validateDocuments(application) } returns DocumentValidationResult(
            isValid = false,
            validDocuments = emptyList(),
            missingDocuments = listOf(DocumentType.INCOME_STATEMENT),
            issues = listOf("Missing income statement")
        )
        
        every { notificationActivity.sendRejectionNotification(any(), any()) } returns true
        
        // When: Process application directly
        val result = workflow.processLoanApplication(application)
        
        // Then: Should be rejected
        assertEquals("REJECTED - Invalid documents", result)
        
        // Verify rejection notification sent
        verify { notificationActivity.sendRejectionNotification(any(), any()) }
        
        // Verify risk scoring was not called
        verify(exactly = 0) { riskScoringActivity.calculateRiskScore(any()) }
    }
    
    @Test
    fun `should calculate risk score for valid documents`() {
        // Given: Valid loan application
        val application = createValidLoanApplication()
        
        every { documentActivity.validateDocuments(application) } returns DocumentValidationResult(
            isValid = true,
            validDocuments = application.documents,
            missingDocuments = emptyList(),
            issues = emptyList()
        )
        
        every { riskScoringActivity.calculateRiskScore(application) } returns RiskAssessment(
            applicationId = application.workflowId,
            creditScore = 750,
            riskLevel = RiskLevel.LOW,
            debtToIncomeRatio = BigDecimal("0.25"),
            riskFactors = emptyList()
        )
        
        every { notificationActivity.sendApplicationConfirmation(application) } returns true
        
        // When: Query risk assessment
        val riskAssessment = workflow.getRiskAssessment()
        
        // Then: Should have risk assessment (this will trigger the workflow to run internally)
        assertNotNull(riskAssessment)
        
        // Verify activities were called
        verify { documentActivity.validateDocuments(application) }
        verify { riskScoringActivity.calculateRiskScore(application) }
    }
    
    private fun createValidLoanApplication(): LoanApplication {
        return LoanApplication(
            workflowId = "test-loan-${System.currentTimeMillis()}",
            userId = "test-user-123",
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@test.com",
            phone = "+1-555-0123",
            loanAmount = BigDecimal("25000"),
            purpose = LoanPurpose.HOME_IMPROVEMENT,
            annualIncome = BigDecimal("75000"),
            documents = listOf(
                DocumentType.ID_CARD,
                DocumentType.INCOME_STATEMENT,
                DocumentType.BANK_STATEMENT
            )
        )
    }
} 