package com.temporal.bootcamp.lesson5.activity

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

/**
 * Math operations activity for Lesson 5.
 * Contains the actual calculation logic.
 */
@ActivityInterface
interface MathActivity {
    
    /**
     * Performs addition of two numbers.
     * 
     * @param a First number
     * @param b Second number
     * @return Sum of a and b
     */
    @ActivityMethod
    fun performAddition(a: Int, b: Int): Int
} 