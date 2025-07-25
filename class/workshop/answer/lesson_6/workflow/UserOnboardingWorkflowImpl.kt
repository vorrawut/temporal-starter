package com.temporal.bootcamp.lesson6.workflow

import com.temporal.bootcamp.lesson6.activity.AccountCreationActivity
import com.temporal.bootcamp.lesson6.activity.NotificationActivity
import com.temporal.bootcamp.lesson6.activity.UserValidationActivity
import io.temporal.activity.ActivityOptions
import io.temporal.workflow.Workflow
import java.time.Duration

/**
 * Implementation of UserOnboardingWorkflow demonstrating clean activity separation.
 * Each activity has a single responsibility and appropriate timeout configuration.
 */
class UserOnboardingWorkflowImpl : UserOnboardingWorkflow {
    
    /**
     * Validation activity - quick operation, short timeout
     */
    private val validationActivity = Workflow.newActivityStub(
        UserValidationActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .build()
    )
    
    /**
     * Account creation activity - database operation, medium timeout
     */
    private val accountCreationActivity = Workflow.newActivityStub(
        AccountCreationActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .build()
    )
    
    /**
     * Notification activity - external service, longer timeout
     */
    private val notificationActivity = Workflow.newActivityStub(
        NotificationActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(1))
            .build()
    )
    
    override fun onboardUser(email: String): OnboardingResult {
        val logger = Workflow.getLogger(this::class.java)
        val steps = mutableListOf<String>()
        
        logger.info("Starting user onboarding for: $email")
        
        try {
            // Step 1: Validate user data
            logger.info("Step 1: Validating user data")
            val validationResult = validationActivity.validateUser(email)
            steps.add("Validation: ${if (validationResult.isValid) "Passed" else "Failed"}")
            
            if (!validationResult.isValid) {
                logger.warn("User validation failed: ${validationResult.errorMessage}")
                return OnboardingResult(
                    success = false,
                    userId = null,
                    message = "Validation failed: ${validationResult.errorMessage}",
                    steps = steps
                )
            }
            
            // Step 2: Create user account
            logger.info("Step 2: Creating user account")
            val creationResult = accountCreationActivity.createAccount(email)
            steps.add("Account Creation: ${if (creationResult.success) "Success" else "Failed"}")
            
            if (!creationResult.success) {
                logger.warn("Account creation failed: ${creationResult.errorMessage}")
                return OnboardingResult(
                    success = false,
                    userId = null,
                    message = "Account creation failed: ${creationResult.errorMessage}",
                    steps = steps
                )
            }
            
            val userId = creationResult.userId!!
            
            // Step 3: Send welcome notification (best effort)
            logger.info("Step 3: Sending welcome notification")
            try {
                val notificationResult = notificationActivity.sendWelcomeEmail(email, userId)
                steps.add("Notification: ${if (notificationResult.sent) "Sent" else "Failed"}")
                
                if (!notificationResult.sent) {
                    logger.warn("Welcome email failed but continuing: ${notificationResult.errorMessage}")
                }
            } catch (e: Exception) {
                logger.warn("Notification failed but user onboarding continues: ${e.message}")
                steps.add("Notification: Failed (non-critical)")
            }
            
            logger.info("User onboarding completed successfully for: $email")
            
            return OnboardingResult(
                success = true,
                userId = userId,
                message = "User onboarded successfully",
                steps = steps
            )
            
        } catch (e: Exception) {
            logger.error("Unexpected error during onboarding: ${e.message}")
            steps.add("Error: ${e.message}")
            
            return OnboardingResult(
                success = false,
                userId = null,
                message = "Onboarding failed: ${e.message}",
                steps = steps
            )
        }
    }
} 