package com.temporal.activity

import mu.KotlinLogging
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * Implementation of NotificationActivity.
 * Handles email sending with simulated external service calls.
 */
@Component
class NotificationActivityImpl : NotificationActivity {
    
    private val logger = KotlinLogging.logger {}
    
    override fun sendWelcomeEmail(email: String, userId: String): NotificationResult {
        logger.info { "Sending welcome email to: $email (User ID: $userId)" }
        
        // Simulate email service processing time
        Thread.sleep(300 + Random.nextLong(700))
        
        try {
            // Simulate occasional email service failures (10% chance)
            if (Random.nextDouble() < 0.10) {
                throw RuntimeException("Email service temporarily unavailable")
            }
            
            // Simulate email creation and sending process
            logger.info { "📧 Composing welcome email..." }
            logger.info { "📧 Personalizing content for user: $userId" }
            logger.info { "📧 Connecting to email service..." }
            logger.info { "📧 Sending email to: $email" }
            
            // Simulate successful delivery
            logger.info { "✅ Welcome email sent successfully to: $email" }
            
            return NotificationResult(
                sent = true,
                errorMessage = null
            )
            
        } catch (e: Exception) {
            logger.warn { "⚠️ Failed to send welcome email to: $email - ${e.message}" }
            
            return NotificationResult(
                sent = false,
                errorMessage = e.message
            )
        }
    }
} 