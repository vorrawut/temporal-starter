package com.temporal.model

data class OnboardingResult(
    val success: Boolean,
    val userId: String?,
    val message: String,
    val steps: List<String>
)