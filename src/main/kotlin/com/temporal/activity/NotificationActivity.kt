package com.temporal.activity

import com.temporal.model.LoanApplication
import com.temporal.model.LoanDisbursement
import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface NotificationActivity {
    
    @ActivityMethod
    fun sendApplicationConfirmation(application: LoanApplication): Boolean
    
    @ActivityMethod
    fun sendApprovalNotification(application: LoanApplication): Boolean
    
    @ActivityMethod
    fun sendRejectionNotification(application: LoanApplication, reason: String): Boolean
    
    @ActivityMethod
    fun sendDisbursementConfirmation(application: LoanApplication, disbursement: LoanDisbursement): Boolean
    
    @ActivityMethod
    fun sendFollowUpReminder(applicationId: String, message: String): Boolean
} 