package com.temporal.activity

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

/**
 * Activity responsible for sending notifications.
 * Single responsibility: handle all notification types and delivery.
 */
@ActivityInterface
interface NotificationActivity {
    
    /**
     * Sends a welcome email to a newly registered user.
     * 
     * @param email The user's email address
     * @param userId The newly created user ID
     * @return NotificationResult with delivery status
     */
    @ActivityMethod
    fun sendWelcomeEmail(email: String, userId: String): NotificationResult
}

/**
 * Result of notification sending process.
 */
data class NotificationResult(
    val sent: Boolean,
    val errorMessage: String?
) 