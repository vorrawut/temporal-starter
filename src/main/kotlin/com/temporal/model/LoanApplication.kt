package com.temporal.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime

data class LoanApplication @JsonCreator constructor(
    @JsonProperty("workflowId") val workflowId: String,
    @JsonProperty("userId") val userId: String,
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("phone") val phone: String,
    @JsonProperty("loanAmount") val loanAmount: BigDecimal,
    @JsonProperty("purpose") val purpose: LoanPurpose,
    @JsonProperty("annualIncome") val annualIncome: BigDecimal,
    @JsonProperty("documents") val documents: List<DocumentType>,
    @JsonProperty("status") var status: ApplicationStatus = ApplicationStatus.SUBMITTED,
    @JsonProperty("createdAt") val createdAt: LocalDateTime = LocalDateTime.now(),
    @JsonProperty("updatedAt") var updatedAt: LocalDateTime = LocalDateTime.now()
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