package com.temporal.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class LoanDisbursement(
    val applicationId: String,
    val transactionId: String,
    val amount: BigDecimal,
    val bankAccount: String,
    val routingNumber: String,
    val status: DisbursementStatus,
    val disbursedAt: LocalDateTime = LocalDateTime.now(),
    val failureReason: String? = null
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