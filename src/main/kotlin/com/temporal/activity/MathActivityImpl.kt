package com.temporal.activity

import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class MathActivityImpl : MathActivity {

    private val logger = KotlinLogging.logger {}

    override fun performAddition(a: Int, b: Int): Int {
        logger.info { "MathActivity: Adding $a + $b" }

        // Simulate some work
        Thread.sleep(100)

        val result = a + b

        logger.info { "MathActivity: Result = $result" }

        return result
    }
}