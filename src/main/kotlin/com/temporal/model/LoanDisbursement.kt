package com.temporal.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime

data class LoanDisbursement @JsonCreator constructor(
    @JsonProperty("applicationId") val applicationId: String,
    @JsonProperty("transactionId") val transactionId: String,
    @JsonProperty("amount") val amount: BigDecimal,
    @JsonProperty("bankAccount") val bankAccount: String,
    @JsonProperty("routingNumber") val routingNumber: String,
    @JsonProperty("status") val status: DisbursementStatus,
    @JsonProperty("disbursedAt") val disbursedAt: LocalDateTime = LocalDateTime.now(),
    @JsonProperty("failureReason") val failureReason: String? = null
)

enum class DisbursementStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUNDED
}

data class BankTransferRequest(
    val amount: BigDecimal,
    val fromAccount: String,
    val toAccount: String,
    val routingNumber: String,
    val purpose: String,
    val reference: String
)

data class BankTransferResponse(
    val transactionId: String,
    val status: String,
    val message: String,
    val processedAt: LocalDateTime = LocalDateTime.now()
) 