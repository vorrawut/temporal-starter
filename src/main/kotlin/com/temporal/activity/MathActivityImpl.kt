package com.temporal.activity

import mu.KotlinLogging
import org.springframework.stereotype.Component

/**
 * Implementation of MathActivity.
 * Performs actual mathematical operations.
 */
@Component
class MathActivityImpl : MathActivity {
    
    private val logger = KotlinLogging.logger {}
    
    override fun performAddition(a: Int, b: Int): Int {
        logger.info { "MathActivity: Adding $a + $b" }
        
        // Simulate some work (in real scenarios, this might involve
        // database lookups, external API calls, complex calculations, etc.)
        Thread.sleep(100)
        
        val result = a + b
        
        logger.info { "MathActivity: Result = $result" }
        
        return result
    }
} 