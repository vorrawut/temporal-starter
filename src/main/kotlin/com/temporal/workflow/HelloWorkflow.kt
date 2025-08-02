package com.temporal.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface HelloWorkflow {

    @WorkflowMethod
    fun sayHello(name: String): String
}