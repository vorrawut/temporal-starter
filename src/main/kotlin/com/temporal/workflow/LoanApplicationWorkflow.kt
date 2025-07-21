package com.temporal.workflow

import com.temporal.model.*
import io.temporal.workflow.QueryMethod
import io.temporal.workflow.SignalMethod
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface LoanApplicationWorkflow {
    
    @WorkflowMethod
    fun processLoanApplication(application: LoanApplication): String
    
    @SignalMethod
    fun approveApplication(decision: ApprovalDecision)
    
    @SignalMethod
    fun rejectApplication(decision: RejectionDecision)
    
    @SignalMethod
    fun requestMoreInformation(message: String)
    
    @QueryMethod
    fun getCurrentState(): ApplicationStatus
    
    @QueryMethod
    fun getApplicationDetails(): LoanApplication
    
    @QueryMethod
    fun getRiskAssessment(): RiskAssessment?
    
    @QueryMethod
    fun getProcessingHistory(): List<String>
} 