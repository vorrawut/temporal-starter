package com.temporal.model

import java.time.LocalDateTime

data class ApprovalDecision(
    val applicationId: String,
    val approvedBy: String,
    val status: ApprovalStatus,
    val notes: String,
    val decidedAt: LocalDateTime = LocalDateTime.now()
)

enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    REQUIRES_MORE_INFO
}

data class RejectionDecision(
    val applicationId: String,
    val rejectedBy: String,
    val reason: String,
    val notes: String,
    val decidedAt: LocalDateTime = LocalDateTime.now()
) 