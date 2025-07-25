package com.temporal.activity

import mu.KotlinLogging
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * Implementation of UserValidationActivity.
 * Handles email format validation and availability checking.
 */
@Component
class UserValidationActivityImpl : UserValidationActivity {
    
    private val logger = KotlinLogging.logger {}
    
    // Simulated existing users for demo purposes
    private val existingUsers = setOf(
        "admin@example.com",
        "test@example.com", 
        "demo@example.com"
    )
    
    override fun validateUser(email: String): ValidationResult {
        logger.info { "Validating user: $email" }
        
        // Simulate validation processing time
        Thread.sleep(200 + Random.nextLong(300))
        
        // Step 1: Basic email format validation
        if (!isValidEmailFormat(email)) {
            logger.warn { "Invalid email format: $email" }
            return ValidationResult(
                isValid = false,
                errorMessage = "Invalid email format"
            )
        }
        
        // Step 2: Check if email already exists
        if (existingUsers.contains(email.lowercase())) {
            logger.warn { "Email already exists: $email" }
            return ValidationResult(
                isValid = false,
                errorMessage = "Email already registered"
            )
        }
        
        // Step 3: Business rule validation (example: no disposable emails)
        if (isDisposableEmail(email)) {
            logger.warn { "Disposable email not allowed: $email" }
            return ValidationResult(
                isValid = false,
                errorMessage = "Disposable email addresses not allowed"
            )
        }
        
        logger.info { "✅ User validation passed for: $email" }
        
        return ValidationResult(
            isValid = true,
            errorMessage = null
        )
    }
    
    /**
     * Basic email format validation
     */
    private fun isValidEmailFormat(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }
    
    /**
     * Check for disposable email domains (simplified example)
     */
    private fun isDisposableEmail(email: String): Boolean {
        val disposableDomains = setOf("tempmail.com", "10minutemail.com", "guerrillamail.com")
        val domain = email.substringAfter("@").lowercase()
        return disposableDomains.contains(domain)
    }
} 