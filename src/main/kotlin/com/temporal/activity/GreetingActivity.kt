package com.temporal.activity

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface GreetingActivity {

    @ActivityMethod
    fun generateGreeting(name: String): String
}