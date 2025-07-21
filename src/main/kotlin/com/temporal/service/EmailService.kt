package com.temporal.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class EmailService {
    
    private val logger = LoggerFactory.getLogger(EmailService::class.java)
    
    fun sendEmail(to: String, subject: String, body: String): Boolean {
        logger.info("Sending email to: $to, subject: $subject")
        
        return try {
            // Simulate email processing time
            Thread.sleep(Random.nextLong(500, 2000))
            
            // Simulate occasional failures (5% failure rate)
            if (Random.nextFloat() < 0.05f) {
                logger.warn("Simulating email service failure")
                throw RuntimeException("SMTP server temporarily unavailable")
            }
            
            // Log email content (in production, you'd use actual email service)
            logger.info("EMAIL SENT:")
            logger.info("To: $to")
            logger.info("Subject: $subject")
            logger.info("Body: ${body.take(100)}...")
            
            true
            
        } catch (e: Exception) {
            logger.error("Failed to send email to $to: ${e.message}")
            false
        }
    }
} 