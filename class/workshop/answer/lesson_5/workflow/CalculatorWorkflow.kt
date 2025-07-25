package com.temporal.bootcamp.lesson5.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

/**
 * Simple calculator workflow for Lesson 5.
 * Demonstrates basic workflow-activity interaction.
 */
@WorkflowInterface
interface CalculatorWorkflow {
    
    /**
     * Adds two numbers using an activity.
     * 
     * @param a First number
     * @param b Second number
     * @return Sum of a and b
     */
    @WorkflowMethod
    fun add(a: Int, b: Int): Int
} 