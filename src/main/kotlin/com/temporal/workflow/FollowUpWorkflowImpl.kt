package com.temporal.workflow

import com.temporal.activity.NotificationActivity
import io.temporal.activity.ActivityOptions
import io.temporal.common.RetryOptions
import io.temporal.workflow.Workflow
import org.slf4j.LoggerFactory
import java.time.Duration

class FollowUpWorkflowImpl : FollowUpWorkflow {
    
    private val logger = LoggerFactory.getLogger(FollowUpWorkflowImpl::class.java)
    
    private val notificationActivity = Workflow.newActivityStub(
        NotificationActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(5))
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(1))
                    .setMaximumInterval(Duration.ofSeconds(10))
                    .setBackoffCoefficient(1.5)
                    .setMaximumAttempts(5)
                    .build()
            )
            .build()
    )
    
    override fun startFollowUp(applicationId: String, userId: String): String {
        logger.info("Starting follow-up workflow for application: $applicationId")
        
        // Schedule multiple follow-ups over time
        scheduleFollowUps(applicationId, userId)
        
        return "Follow-up workflow completed for $applicationId"
    }
    
    private fun scheduleFollowUps(applicationId: String, userId: String) {
        try {
            // Follow-up #1: 30 days after disbursement - Welcome check-in
            Workflow.sleep(Duration.ofDays(30))
            logger.info("Sending 30-day follow-up for $applicationId")
            
            notificationActivity.sendFollowUpReminder(
                applicationId,
                "Welcome follow-up: How are you enjoying your loan? We're here to help with any questions."
            )
            
            // Follow-up #2: 90 days after disbursement - Payment reminder
            Workflow.sleep(Duration.ofDays(60)) // Additional 60 days (90 total)
            logger.info("Sending 90-day follow-up for $applicationId")
            
            notificationActivity.sendFollowUpReminder(
                applicationId,
                "Payment reminder: Your next payment is due soon. Set up automatic payments to never miss a due date."
            )
            
            // Follow-up #3: 180 days after disbursement - Account review
            Workflow.sleep(Duration.ofDays(90)) // Additional 90 days (180 total)
            logger.info("Sending 180-day follow-up for $applicationId")
            
            notificationActivity.sendFollowUpReminder(
                applicationId,
                "Account review: You're halfway through your loan term! Check if you qualify for better rates or additional services."
            )
            
            // Follow-up #4: 270 days after disbursement - Satisfaction survey
            Workflow.sleep(Duration.ofDays(90)) // Additional 90 days (270 total)
            logger.info("Sending 270-day follow-up for $applicationId")
            
            notificationActivity.sendFollowUpReminder(
                applicationId,
                "Satisfaction survey: Help us improve our services by sharing your experience with our loan process."
            )
            
            // Follow-up #5: 365 days after disbursement - Annual review
            Workflow.sleep(Duration.ofDays(95)) // Additional 95 days (365 total)
            logger.info("Sending annual follow-up for $applicationId")
            
            notificationActivity.sendFollowUpReminder(
                applicationId,
                "Annual review: It's been a year since your loan. You may qualify for new products or refinancing options."
            )
            
            // Continue with annual check-ins using continueAsNew pattern
            continueAsNewAnnualFollowUp(applicationId, userId)
            
        } catch (e: Exception) {
            logger.error("Follow-up workflow failed for $applicationId", e)
            // In production, you might want to implement error handling and compensation
        }
    }
    
    private fun continueAsNewAnnualFollowUp(applicationId: String, userId: String) {
        logger.info("Starting annual follow-up cycle for $applicationId")
        
        // Use continueAsNew to reset workflow history and prevent it from growing too large
        // This is a best practice for long-running workflows
        val continuedWorkflow = Workflow.newContinueAsNewStub(
            FollowUpWorkflow::class.java
        )
        
        // Schedule the next annual follow-up
        continuedWorkflow.startFollowUp(applicationId, userId)
    }
} 