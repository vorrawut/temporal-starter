package com.temporal.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class LoanApplication(
    val workflowId: String,
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val loanAmount: BigDecimal,
    val purpose: LoanPurpose,
    val annualIncome: BigDecimal,
    val documents: List<DocumentType>,
    var status: ApplicationStatus = ApplicationStatus.SUBMITTED,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class LoanPurpose {
    HOME_IMPROVEMENT,
    DEBT_CONSOLIDATION,
    BUSINESS,
    PERSONAL,
    EDUCATION,
    MEDICAL,
    VACATION,
    OTHER
}

enum class DocumentType {
    ID_CARD,
    PASSPORT,
    DRIVER_LICENSE,
    INCOME_STATEMENT,
    TAX_RETURNS,
    BANK_STATEMENT,
    EMPLOYMENT_VERIFICATION,
    BUSINESS_LICENSE,
    FINANCIAL_STATEMENT
}

enum class ApplicationStatus {
    SUBMITTED,
    DOCUMENT_VALIDATION,
    RISK_SCORING,
    AWAITING_APPROVAL,
    APPROVED,
    REJECTED,
    DISBURSING,
    DISBURSED,
    FAILED,
    EXPIRED
} 