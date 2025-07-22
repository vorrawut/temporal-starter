package com.temporal.bootcamp.lesson6.activity

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

/**
 * Activity responsible for user account creation.
 * Single responsibility: create user accounts and generate IDs.
 */
@ActivityInterface
interface AccountCreationActivity {
    
    /**
     * Creates a new user account.
     * 
     * @param email The user's email address
     * @return CreationResult with account creation status and user ID
     */
    @ActivityMethod
    fun createAccount(email: String): CreationResult
}

/**
 * Result of account creation process.
 */
data class CreationResult(
    val success: Boolean,
    val userId: String?,
    val errorMessage: String?
) 