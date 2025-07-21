package com.temporal.activity

import com.temporal.model.LoanApplication
import com.temporal.model.LoanDisbursement
import com.temporal.service.EmailService
import com.temporal.service.SmsService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class NotificationActivityImpl(
    private val emailService: EmailService,
    private val smsService: SmsService
) : NotificationActivity {
    
    private val logger = LoggerFactory.getLogger(NotificationActivityImpl::class.java)
    
    override fun sendApplicationConfirmation(application: LoanApplication): Boolean {
        logger.info("Sending application confirmation for ${application.workflowId}")
        
        return try {
            val emailSubject = "Loan Application Received - ${application.workflowId}"
            val emailBody = buildApplicationConfirmationEmail(application)
            
            val emailSent = emailService.sendEmail(
                to = application.email,
                subject = emailSubject,
                body = emailBody
            )
            
            val smsMessage = "Your loan application ${application.workflowId} has been received and is being processed. You'll receive updates via email."
            val smsSent = smsService.sendSms(
                to = application.phone,
                message = smsMessage
            )
            
            val success = emailSent && smsSent
            logger.info("Application confirmation sent: email=$emailSent, sms=$smsSent")
            
            success
            
        } catch (e: Exception) {
            logger.error("Failed to send application confirmation for ${application.workflowId}", e)
            false
        }
    }
    
    override fun sendApprovalNotification(application: LoanApplication): Boolean {
        logger.info("Sending approval notification for ${application.workflowId}")
        
        return try {
            val emailSubject = "Loan Application Approved! - ${application.workflowId}"
            val emailBody = buildApprovalEmail(application)
            
            val emailSent = emailService.sendEmail(
                to = application.email,
                subject = emailSubject,
                body = emailBody
            )
            
            val smsMessage = "Great news! Your loan application ${application.workflowId} has been approved. Funds will be disbursed shortly."
            val smsSent = smsService.sendSms(
                to = application.phone,
                message = smsMessage
            )
            
            val success = emailSent && smsSent
            logger.info("Approval notification sent: email=$emailSent, sms=$smsSent")
            
            success
            
        } catch (e: Exception) {
            logger.error("Failed to send approval notification for ${application.workflowId}", e)
            false
        }
    }
    
    override fun sendRejectionNotification(application: LoanApplication, reason: String): Boolean {
        logger.info("Sending rejection notification for ${application.workflowId}")
        
        return try {
            val emailSubject = "Loan Application Update - ${application.workflowId}"
            val emailBody = buildRejectionEmail(application, reason)
            
            val emailSent = emailService.sendEmail(
                to = application.email,
                subject = emailSubject,
                body = emailBody
            )
            
            val smsMessage = "Your loan application ${application.workflowId} requires attention. Please check your email for details."
            val smsSent = smsService.sendSms(
                to = application.phone,
                message = smsMessage
            )
            
            val success = emailSent && smsSent
            logger.info("Rejection notification sent: email=$emailSent, sms=$smsSent")
            
            success
            
        } catch (e: Exception) {
            logger.error("Failed to send rejection notification for ${application.workflowId}", e)
            false
        }
    }
    
    override fun sendDisbursementConfirmation(application: LoanApplication, disbursement: LoanDisbursement): Boolean {
        logger.info("Sending disbursement confirmation for ${application.workflowId}")
        
        return try {
            val emailSubject = "Loan Disbursed Successfully! - ${application.workflowId}"
            val emailBody = buildDisbursementEmail(application, disbursement)
            
            val emailSent = emailService.sendEmail(
                to = application.email,
                subject = emailSubject,
                body = emailBody
            )
            
            val smsMessage = "Your loan has been disbursed! Check your account for $${disbursement.amount}. Transaction ID: ${disbursement.transactionId}"
            val smsSent = smsService.sendSms(
                to = application.phone,
                message = smsMessage
            )
            
            val success = emailSent && smsSent
            logger.info("Disbursement confirmation sent: email=$emailSent, sms=$smsSent")
            
            success
            
        } catch (e: Exception) {
            logger.error("Failed to send disbursement confirmation for ${application.workflowId}", e)
            false
        }
    }
    
    override fun sendFollowUpReminder(applicationId: String, message: String): Boolean {
        logger.info("Sending follow-up reminder for $applicationId")
        
        return try {
            // In a real system, we'd look up the application details
            // For demo purposes, we'll just log the reminder
            logger.info("Follow-up reminder: $message")
            
            // Simulate sending reminder (could be email, SMS, or in-app notification)
            Thread.sleep(1000)
            
            true
            
        } catch (e: Exception) {
            logger.error("Failed to send follow-up reminder for $applicationId", e)
            false
        }
    }
    
    private fun buildApplicationConfirmationEmail(application: LoanApplication): String {
        return """
            Dear ${application.firstName} ${application.lastName},
            
            Thank you for your loan application. We have received your request for $${application.loanAmount} 
            for ${application.purpose.name.lowercase().replace("_", " ")}.
            
            Application Details:
            - Application ID: ${application.workflowId}
            - Loan Amount: $${application.loanAmount}
            - Purpose: ${application.purpose.name.lowercase().replace("_", " ")}
            - Submitted: ${application.createdAt}
            
            Your application is currently being reviewed. We will process your application and 
            notify you of any updates within 2-3 business days.
            
            If you have any questions, please contact our customer service team.
            
            Thank you for choosing our services.
            
            Best regards,
            Loan Processing Team
        """.trimIndent()
    }
    
    private fun buildApprovalEmail(application: LoanApplication): String {
        return """
            Dear ${application.firstName} ${application.lastName},
            
            Congratulations! Your loan application has been approved.
            
            Approved Loan Details:
            - Application ID: ${application.workflowId}
            - Approved Amount: $${application.loanAmount}
            - Purpose: ${application.purpose.name.lowercase().replace("_", " ")}
            - Approval Date: ${java.time.LocalDateTime.now()}
            
            Your funds will be disbursed to your account within 1-2 business days. 
            You will receive a confirmation once the transfer is complete.
            
            Please review the terms and conditions attached to this email.
            
            Thank you for choosing our services.
            
            Best regards,
            Loan Processing Team
        """.trimIndent()
    }
    
    private fun buildRejectionEmail(application: LoanApplication, reason: String): String {
        return """
            Dear ${application.firstName} ${application.lastName},
            
            Thank you for your interest in our loan services. After careful review of your application,
            we regret to inform you that we are unable to approve your loan request at this time.
            
            Application Details:
            - Application ID: ${application.workflowId}
            - Requested Amount: $${application.loanAmount}
            - Purpose: ${application.purpose.name.lowercase().replace("_", " ")}
            
            Reason: $reason
            
            This decision is based on our current lending criteria. You may reapply in the future
            or contact our customer service team to discuss alternative options.
            
            Thank you for considering our services.
            
            Best regards,
            Loan Processing Team
        """.trimIndent()
    }
    
    private fun buildDisbursementEmail(application: LoanApplication, disbursement: LoanDisbursement): String {
        return """
            Dear ${application.firstName} ${application.lastName},
            
            Great news! Your loan has been successfully disbursed.
            
            Disbursement Details:
            - Application ID: ${application.workflowId}
            - Amount Disbursed: $${disbursement.amount}
            - Transaction ID: ${disbursement.transactionId}
            - Account: ${disbursement.bankAccount}
            - Disbursement Date: ${disbursement.disbursedAt}
            
            The funds should appear in your account within 1-2 business days depending on your bank.
            
            Please keep this email for your records. If you don't see the funds in your account
            after 2 business days, please contact our customer service team.
            
            Thank you for choosing our services.
            
            Best regards,
            Loan Processing Team
        """.trimIndent()
    }
} 