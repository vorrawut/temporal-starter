package com.temporal.activity

import com.temporal.model.LoanApplication
import com.temporal.model.RiskAssessment
import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface RiskScoringActivity {
    
    @ActivityMethod
    fun calculateRiskScore(application: LoanApplication): RiskAssessment
} 