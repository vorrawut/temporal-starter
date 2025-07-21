package com.temporal.bootcamp.lesson6.workflow

// TODO: Add imports for WorkflowInterface and WorkflowMethod

/**
 * TODO: Create a workflow interface for UserOnboardingWorkflow
 * 
 * This workflow will demonstrate clean separation by orchestrating
 * multiple activities for user onboarding:
 * 
 * Requirements:
 * 1. Annotate with @WorkflowInterface
 * 2. Create a method called onboardUser that:
 *    - Takes a String parameter (email)
 *    - Returns an OnboardingResult (you'll create this data class)
 *    - Is annotated with @WorkflowMethod
 */

// TODO: Define your UserOnboardingWorkflow interface here

// TODO: Create a data class for OnboardingResult with:
// - success: Boolean
// - userId: String?
// - message: String
// - steps: List<String> 