package com.temporal.workflow

import com.temporal.model.OnboardingResult
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface UserOnboardingWorkflow {

    @WorkflowMethod
    fun onboardUser(email: String): OnboardingResult
}