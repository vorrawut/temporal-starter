package com.temporal.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class RiskAssessment(
    val applicationId: String,
    val creditScore: Int,
    val riskLevel: RiskLevel,
    val debtToIncomeRatio: BigDecimal,
    val riskFactors: List<String>,
    val assessedAt: LocalDateTime = LocalDateTime.now()
)

enum class RiskLevel {
    LOW,     // Credit score 700+
    MEDIUM,  // Credit score 600-699
    HIGH,    // Credit score 500-599
    VERY_HIGH // Credit score below 500
}

data class CreditBureauResponse(
    val creditScore: Int,
    val creditHistory: String,
    val outstandingDebts: BigDecimal,
    val bankruptcyHistory: Boolean,
    val latePayments: Int,
    val creditUtilization: BigDecimal
) 