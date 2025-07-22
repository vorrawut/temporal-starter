package com.temporal.bootcamp.lesson6.activity

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

/**
 * Activity responsible for user data validation.
 * Single responsibility: validate user input and business rules.
 */
@ActivityInterface
interface UserValidationActivity {
    
    /**
     * Validates user data for onboarding.
     * 
     * @param email The user's email address to validate
     * @return ValidationResult with validation status and details
     */
    @ActivityMethod
    fun validateUser(email: String): ValidationResult
}

/**
 * Result of user validation process.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String?
) 