package com.temporal.activity

import mu.KotlinLogging
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class NotificationActivityImpl : NotificationActivity {

    private val logger = KotlinLogging.logger {}

    override fun sendWelcomeEmail(email: String, userId: String): NotificationResult {
        logger.info { "📧 Sending welcome email to: $email" }

        // Simulate email service (with occasional failures)
        Thread.sleep(300)

        if (Random.nextDouble() < 0.1) {
            return NotificationResult(false, "Email service unavailable")
        }

        logger.info { "✅ Welcome email sent successfully" }
        return NotificationResult(true, null)
    }

    override fun sendSignal() {

    }
}