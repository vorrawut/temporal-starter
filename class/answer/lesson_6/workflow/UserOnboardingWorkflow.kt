package com.temporal.bootcamp.lesson6.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

/**
 * User onboarding workflow that demonstrates clean separation of concerns.
 * Orchestrates multiple activities for complete user registration process.
 */
@WorkflowInterface
interface UserOnboardingWorkflow {
    
    /**
     * Onboards a new user through a multi-step process.
     * 
     * @param email The user's email address
     * @return OnboardingResult with success status and details
     */
    @WorkflowMethod
    fun onboardUser(email: String): OnboardingResult
}

/**
 * Result of the user onboarding process.
 */
data class OnboardingResult(
    val success: Boolean,
    val userId: String?,
    val message: String,
    val steps: List<String>
) 