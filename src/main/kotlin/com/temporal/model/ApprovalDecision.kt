package com.temporal.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ApprovalDecision @JsonCreator constructor(
    @JsonProperty("applicationId") val applicationId: String,
    @JsonProperty("approvedBy") val approvedBy: String,
    @JsonProperty("status") val status: ApprovalStatus,
    @JsonProperty("notes") val notes: String,
    @JsonProperty("decidedAt") val decidedAt: LocalDateTime = LocalDateTime.now()
)

enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    REQUIRES_MORE_INFO
}

data class RejectionDecision @JsonCreator constructor(
    @JsonProperty("applicationId") val applicationId: String,
    @JsonProperty("rejectedBy") val rejectedBy: String,
    @JsonProperty("reason") val reason: String,
    @JsonProperty("notes") val notes: String,
    @JsonProperty("decidedAt") val decidedAt: LocalDateTime = LocalDateTime.now()
) 