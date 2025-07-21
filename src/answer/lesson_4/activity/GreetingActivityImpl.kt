package com.temporal.bootcamp.lesson4.activity

import mu.KotlinLogging
import org.springframework.stereotype.Component

/**
 * GreetingActivity implementation - the actual business logic.
 * 
 * This is where the real work happens:
 * - Can access external systems
 * - Can be retried if it fails
 * - Should be idempotent (safe to run multiple times)
 * - Managed by Spring for dependency injection
 */
@Component
class GreetingActivityImpl : GreetingActivity {
    
    private val logger = KotlinLogging.logger {}
    
    override fun generateGreeting(name: String): String {
        logger.info { "Generating greeting for: $name" }
        
        // Simulate some work (in real apps, this might be:
        // - Database lookup
        // - API call to user service
        // - Template rendering
        // - etc.)
        Thread.sleep(100) // Simulate brief processing time
        
        val greeting = "Hello, $name! Welcome to Temporal workflows!"
        
        logger.info { "Generated greeting: $greeting" }
        
        return greeting
    }
} 