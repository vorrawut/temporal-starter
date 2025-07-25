package com.temporal.bootcamp.lesson5.workflow

import com.temporal.bootcamp.lesson5.activity.MathActivity
import io.temporal.activity.ActivityOptions
import io.temporal.workflow.Workflow
import java.time.Duration

/**
 * Implementation of CalculatorWorkflow.
 * Shows how to call an activity from a workflow.
 */
class CalculatorWorkflowImpl : CalculatorWorkflow {
    
    /**
     * Create activity stub with timeout configuration.
     */
    private val mathActivity = Workflow.newActivityStub(
        MathActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .build()
    )
    
    override fun add(a: Int, b: Int): Int {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Calculator workflow started: $a + $b")
        
        // Delegate the actual calculation to the activity
        val result = mathActivity.performAddition(a, b)
        
        logger.info("Calculator workflow completed: $a + $b = $result")
        
        return result
    }
} 