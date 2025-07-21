package com.temporal.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class SmsService {
    
    private val logger = LoggerFactory.getLogger(SmsService::class.java)
    
    fun sendSms(to: String, message: String): Boolean {
        logger.info("Sending SMS to: $to")
        
        return try {
            // Simulate SMS processing time
            Thread.sleep(Random.nextLong(300, 1500))
            
            // Simulate occasional failures (10% failure rate - SMS tends to be less reliable than email)
            if (Random.nextFloat() < 0.1f) {
                logger.warn("Simulating SMS service failure")
                throw RuntimeException("SMS gateway temporarily unavailable")
            }
            
            // Log SMS content (in production, you'd use actual SMS service like Twilio)
            logger.info("SMS SENT:")
            logger.info("To: $to")
            logger.info("Message: $message")
            
            true
            
        } catch (e: Exception) {
            logger.error("Failed to send SMS to $to: ${e.message}")
            false
        }
    }
} 