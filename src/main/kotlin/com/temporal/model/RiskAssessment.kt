package com.temporal.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime

data class RiskAssessment @JsonCreator constructor(
    @JsonProperty("applicationId") val applicationId: String,
    @JsonProperty("creditScore") val creditScore: Int,
    @JsonProperty("riskLevel") val riskLevel: RiskLevel,
    @JsonProperty("debtToIncomeRatio") val debtToIncomeRatio: BigDecimal,
    @JsonProperty("riskFactors") val riskFactors: List<String>,
    @JsonProperty("assessedAt") val assessedAt: LocalDateTime = LocalDateTime.now()
)

enum class RiskLevel {
    LOW,     // Credit score 700+
    MEDIUM,  // Credit score 600-699
    HIGH,    // Credit score 500-599
    VERY_HIGH // Credit score below 500
}

data class CreditBureauResponse @JsonCreator constructor(
    @JsonProperty("creditScore") val creditScore: Int,
    @JsonProperty("creditHistory") val creditHistory: String,
    @JsonProperty("outstandingDebts") val outstandingDebts: BigDecimal,
    @JsonProperty("bankruptcyHistory") val bankruptcyHistory: Boolean,
    @JsonProperty("latePayments") val latePayments: Int,
    @JsonProperty("creditUtilization") val creditUtilization: BigDecimal
) 