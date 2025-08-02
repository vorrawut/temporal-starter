package com.temporal.activity

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface MathActivity {

    @ActivityMethod
    fun performAddition(a: Int, b: Int): Int
}