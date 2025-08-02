package com.temporal.activity

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod
import io.temporal.workflow.SignalMethod

@ActivityInterface
interface NotificationActivity {

    @ActivityMethod
    fun sendWelcomeEmail(email: String, userId: String): NotificationResult

    @SignalMethod
    fun sendSignal()
}

data class NotificationResult(
    val sent: Boolean,
    val errorMessage: String?
)