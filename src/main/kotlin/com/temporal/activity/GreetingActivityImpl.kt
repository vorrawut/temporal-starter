package com.temporal.activity

import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class GreetingActivityImpl : GreetingActivity {

    private val logger = KotlinLogging.logger {}

    override fun generateGreeting(name: String): String {
        logger.info { "Generating greeting for: $name" }

        // Simulate some processing
        Thread.sleep(100)

        val greeting = "Hello, $name! Welcome to Temporal workflows!"

        logger.info { "Generated greeting: $greeting" }
        return greeting
    }
}