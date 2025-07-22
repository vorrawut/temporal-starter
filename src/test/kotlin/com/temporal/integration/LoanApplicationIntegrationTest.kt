package com.temporal.integration

import com.temporal.activity.*
import com.temporal.model.*
import com.temporal.workflow.LoanApplicationWorkflow
import com.temporal.workflow.LoanApplicationWorkflowImpl
import io.temporal.testing.TestWorkflowRule
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import org.junit.Assert.*
import java.math.BigDecimal

class LoanApplicationIntegrationTest {
    
    @get:Rule
    val testWorkflowRule = TestWorkflowRule.newBuilder()
        .setWorkflowTypes(LoanApplicationWorkflowImpl::class.java)
        .build()
    
    private lateinit var workflow: LoanApplicationWorkflow
    
    @Before
    fun setUp() {
        // Create real activity implementations for integration testing
        val documentActivity = TestDocumentValidationActivity()
        val riskScoringActivity = TestRiskScoringActivity()
        val disbursementActivity = TestLoanDisbursementActivity()
        val notificationActivity = TestNotificationActivity()
        
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
    fun `should reject application with missing documents`() {
        // Given: Application with missing required documents
        val application = LoanApplication(
            workflowId = "integration-test-${System.currentTimeMillis()}",
            userId = "integration-user-123",
            firstName = "Bob",
            lastName = "Smith",
            email = "bob.smith@test.com",
            phone = "+1-555-0789",
            loanAmount = BigDecimal("30000"),
            purpose = LoanPurpose.HOME_IMPROVEMENT,
            annualIncome = BigDecimal("80000"),
            documents = listOf(DocumentType.ID_CARD) // Missing income statement
        )
        
        // When: Process application
        val result = workflow.processLoanApplication(application)
        
        // Then: Should be rejected due to missing documents
        assertEquals("REJECTED - Invalid documents", result)
    }
    
    @Test
    fun `should process application with all valid documents`() {
        // Given: Valid loan application with all required documents
        val application = LoanApplication(
            workflowId = "integration-test-valid-${System.currentTimeMillis()}",
            userId = "integration-user-456",
            firstName = "Alice",
            lastName = "Johnson",
            email = "alice.johnson@test.com",
            phone = "+1-555-0789",
            loanAmount = BigDecimal("30000"),
            purpose = LoanPurpose.HOME_IMPROVEMENT,
            annualIncome = BigDecimal("80000"),
            documents = listOf(
                DocumentType.ID_CARD,
                DocumentType.INCOME_STATEMENT,
                DocumentType.BANK_STATEMENT
            )
        )
        
        // When: Process application
        val result = workflow.processLoanApplication(application)
        
        // Then: Should reach awaiting approval stage (since we're not sending approval signal)
        assertEquals("AWAITING_APPROVAL - Risk score: 750 (LOW)", result)
    }
    
    // Test activity implementations for integration testing
    private class TestDocumentValidationActivity : DocumentValidationActivity {
        override fun validateDocuments(application: LoanApplication): DocumentValidationResult {
            val requiredDocs = listOf(DocumentType.ID_CARD, DocumentType.INCOME_STATEMENT)
            val missing = requiredDocs.filter { it !in application.documents }
            
            return DocumentValidationResult(
                isValid = missing.isEmpty(),
                validDocuments = application.documents,
                missingDocuments = missing,
                issues = if (missing.isNotEmpty()) listOf("Missing required documents") else emptyList()
            )
        }
    }
    
    private class TestRiskScoringActivity : RiskScoringActivity {
        override fun calculateRiskScore(application: LoanApplication): RiskAssessment {
            // Simulate credit scoring logic
            val creditScore = when {
                application.email.contains("low") -> 450
                application.annualIncome < BigDecimal("40000") -> 550
                application.loanAmount > application.annualIncome -> 600
                else -> 750
            }
            
            val riskLevel = when {
                creditScore < 500 -> RiskLevel.VERY_HIGH
                creditScore < 600 -> RiskLevel.HIGH
                creditScore < 700 -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            }
            
            return RiskAssessment(
                applicationId = application.workflowId,
                creditScore = creditScore,
                riskLevel = riskLevel,
                debtToIncomeRatio = BigDecimal("0.25"),
                riskFactors = if (riskLevel != RiskLevel.LOW) listOf("Test risk factor") else emptyList()
            )
        }
    }
    
    private class TestLoanDisbursementActivity : LoanDisbursementActivity {
        override fun disburseLoan(application: LoanApplication): LoanDisbursement {
            return LoanDisbursement(
                applicationId = application.workflowId,
                transactionId = "TEST-TXN-${System.currentTimeMillis()}",
                amount = application.loanAmount,
                bankAccount = "TEST-ACC-123",
                routingNumber = "123456789",
                status = DisbursementStatus.COMPLETED
            )
        }
        
        override fun compensateFailedDisbursement(workflowId: String): Boolean {
            return true // Always succeed in tests
        }
    }
    
    private class TestNotificationActivity : NotificationActivity {
        override fun sendApplicationConfirmation(application: LoanApplication) = true
        override fun sendApprovalNotification(application: LoanApplication) = true
        override fun sendRejectionNotification(application: LoanApplication, reason: String) = true
        override fun sendDisbursementConfirmation(application: LoanApplication, disbursement: LoanDisbursement) = true
        override fun sendFollowUpReminder(applicationId: String, message: String) = true
    }
} 