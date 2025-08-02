package com.temporal.workflow

import com.temporal.activity.MathActivity
import io.temporal.activity.ActivityOptions
import io.temporal.workflow.Workflow
import java.time.Duration

class CalculatorWorkflowImpl : CalculatorWorkflow {

    private val mathActivity = Workflow.newActivityStub(
        MathActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .build()
    )

    override fun add(a: Int, b: Int): Int {
        val logger = Workflow.getLogger(this::class.java)

        logger.info("CalculatorWorkflow: Starting addition of $a + $b")

        val result = mathActivity.performAddition(a, b)

        logger.info("CalculatorWorkflow: Final result = $result")

        return result
    }
}