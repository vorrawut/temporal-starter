package com.temporal.activity

import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*
import kotlin.random.Random

/**
 * Implementation of AccountCreationActivity.
 * Handles user account creation with simulated database operations.
 */
@Component
class AccountCreationActivityImpl : AccountCreationActivity {
    
    private val logger = KotlinLogging.logger {}
    
    override fun createAccount(email: String): CreationResult {
        logger.info { "Creating account for: $email" }
        
        // Simulate database operation time
        Thread.sleep(500 + Random.nextLong(1000))
        
        try {
            // Simulate occasional database failures (5% chance)
            if (Random.nextDouble() < 0.05) {
                throw RuntimeException("Database connection timeout")
            }
            
            // Generate unique user ID
            val userId = "user_${UUID.randomUUID().toString().take(8)}"
            
            // Simulate account creation process
            logger.info { "Generating user ID: $userId" }
            logger.info { "Saving user profile to database..." }
            logger.info { "Setting up default user preferences..." }
            logger.info { "Initializing user permissions..." }
            
            // Final validation
            if (email.isBlank()) {
                throw IllegalArgumentException("Email cannot be blank")
            }
            
            logger.info { "✅ Account created successfully for: $email with ID: $userId" }
            
            return CreationResult(
                success = true,
                userId = userId,
                errorMessage = null
            )
            
        } catch (e: Exception) {
            logger.error { "❌ Account creation failed for: $email - ${e.message}" }
            
            return CreationResult(
                success = false,
                userId = null,
                errorMessage = e.message
            )
        }
    }
} 