package com.temporal.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface FollowUpWorkflow {
    
    @WorkflowMethod
    fun startFollowUp(applicationId: String, userId: String): String
} 