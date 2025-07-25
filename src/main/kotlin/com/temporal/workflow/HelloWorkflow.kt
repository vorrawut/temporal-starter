package com.temporal.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

/**
 * HelloWorkflow interface - our first Temporal workflow!
 * 
 * This workflow demonstrates the basic structure:
 * - @WorkflowInterface marks this as a workflow contract
 * - @WorkflowMethod marks the main workflow entry point
 */
@WorkflowInterface
interface HelloWorkflow {
    
    /**
     * The main workflow method that orchestrates the greeting process.
     * 
     * @param name The name of the person to greet
     * @return A personalized greeting message
     */
    @WorkflowMethod
    fun sayHello(name: String): String
} 