package com.temporal.bootcamp.lesson4.workflow

import com.temporal.bootcamp.lesson4.activity.GreetingActivity
import io.temporal.activity.ActivityOptions
import io.temporal.workflow.Workflow
import java.time.Duration

/**
 * HelloWorkflow implementation - the orchestration logic.
 * 
 * This class contains the workflow logic that coordinates activities.
 * Key points:
 * - Must be deterministic (no random numbers, current time, etc.)
 * - Activities are called via stubs with timeouts
 * - Workflow can be replayed safely
 */
class HelloWorkflowImpl : HelloWorkflow {
    
    /**
     * Create an activity stub with timeout configuration.
     * The stub acts as a proxy to the actual activity implementation.
     */
    private val greetingActivity = Workflow.newActivityStub(
        GreetingActivity::class.java,
        ActivityOptions.newBuilder()
            .setScheduleToCloseTimeout(Duration.ofMinutes(1))
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .build()
    )
    
    override fun sayHello(name: String): String {
        // Get the workflow logger for debugging
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("HelloWorkflow started for: $name")
        
        // Call the activity to generate the greeting
        // This is where the actual work happens
        val greeting = greetingActivity.generateGreeting(name)
        
        logger.info("HelloWorkflow completed for: $name")
        
        return greeting
    }
} 