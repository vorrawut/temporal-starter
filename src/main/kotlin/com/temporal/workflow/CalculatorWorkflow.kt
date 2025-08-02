package com.temporal.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface CalculatorWorkflow {

    @WorkflowMethod
    fun add(a: Int, b: Int): Int
}