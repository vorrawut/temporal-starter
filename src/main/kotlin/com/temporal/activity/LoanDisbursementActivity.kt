package com.temporal.activity

import com.temporal.model.LoanApplication
import com.temporal.model.LoanDisbursement
import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface LoanDisbursementActivity {
    
    @ActivityMethod
    fun disburseLoan(application: LoanApplication): LoanDisbursement
    
    @ActivityMethod
    fun compensateFailedDisbursement(workflowId: String): Boolean
} 