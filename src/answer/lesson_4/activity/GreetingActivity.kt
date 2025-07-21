package com.temporal.bootcamp.lesson4.activity

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

/**
 * GreetingActivity interface - our first Temporal activity!
 * 
 * Activities contain the business logic that can:
 * - Make external API calls
 * - Access databases
 * - Perform I/O operations
 * - Fail and be retried
 */
@ActivityInterface
interface GreetingActivity {
    
    /**
     * Generates a personalized greeting message.
     * 
     * This activity can be retried if it fails, so it should be idempotent.
     * 
     * @param name The name to include in the greeting
     * @return A formatted greeting message
     */
    @ActivityMethod
    fun generateGreeting(name: String): String
} 