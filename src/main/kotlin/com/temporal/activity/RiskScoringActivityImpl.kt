package com.temporal.activity

import com.temporal.model.*
import com.temporal.service.CreditBureauService
import io.temporal.failure.ApplicationFailure
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

@Component
class RiskScoringActivityImpl(
    private val creditBureauService: CreditBureauService
) : RiskScoringActivity {
    
    private val logger = LoggerFactory.getLogger(RiskScoringActivityImpl::class.java)
    
    override fun calculateRiskScore(application: LoanApplication): RiskAssessment {
        logger.info("Starting risk assessment for application ${application.workflowId}")
        
        try {
            // Step 1: Get credit score from external bureau (with retry logic)
            val creditResponse = creditBureauService.getCreditScore(
                application.firstName + " " + application.lastName,
                application.email
            )
            
            logger.info("Credit score retrieved: ${creditResponse.creditScore}")
            
            // Step 2: Calculate debt-to-income ratio
            val debtToIncomeRatio = calculateDebtToIncomeRatio(
                creditResponse.outstandingDebts,
                application.annualIncome
            )
            
            // Step 3: Determine risk level
            val riskLevel = determineRiskLevel(
                creditResponse.creditScore,
                debtToIncomeRatio,
                application.loanAmount,
                application.annualIncome
            )
            
            // Step 4: Identify risk factors
            val riskFactors = identifyRiskFactors(creditResponse, application, debtToIncomeRatio)
            
            val assessment = RiskAssessment(
                applicationId = application.workflowId,
                creditScore = creditResponse.creditScore,
                riskLevel = riskLevel,
                debtToIncomeRatio = debtToIncomeRatio,
                riskFactors = riskFactors
            )
            
            logger.info("Risk assessment completed for ${application.workflowId}: $riskLevel")
            return assessment
            
        } catch (e: Exception) {
            logger.error("Risk scoring failed for ${application.workflowId}", e)
            throw ApplicationFailure.newFailure(
                "Risk scoring failed: ${e.message}",
                "RISK_SCORING_ERROR"
            )
        }
    }
    
    private fun calculateDebtToIncomeRatio(outstandingDebts: BigDecimal, annualIncome: BigDecimal): BigDecimal {
        val monthlyIncome = annualIncome.divide(BigDecimal("12"), 2, RoundingMode.HALF_UP)
        val monthlyDebtPayments = outstandingDebts.multiply(BigDecimal("0.03")) // Assume 3% monthly payment
        
        return if (monthlyIncome > BigDecimal.ZERO) {
            monthlyDebtPayments.divide(monthlyIncome, 4, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ONE // 100% if no income
        }
    }
    
    private fun determineRiskLevel(
        creditScore: Int,
        debtToIncomeRatio: BigDecimal,
        loanAmount: BigDecimal,
        annualIncome: BigDecimal
    ): RiskLevel {
        return when {
            creditScore < 500 -> RiskLevel.VERY_HIGH
            creditScore < 600 -> RiskLevel.HIGH
            creditScore < 700 -> {
                // Additional factors for medium risk
                when {
                    debtToIncomeRatio > BigDecimal("0.5") -> RiskLevel.HIGH
                    loanAmount > annualIncome.multiply(BigDecimal("0.5")) -> RiskLevel.HIGH
                    else -> RiskLevel.MEDIUM
                }
            }
            else -> {
                // High credit score, but check other factors
                when {
                    debtToIncomeRatio > BigDecimal("0.4") -> RiskLevel.MEDIUM
                    loanAmount > annualIncome -> RiskLevel.MEDIUM
                    else -> RiskLevel.LOW
                }
            }
        }
    }
    
    private fun identifyRiskFactors(
        creditResponse: CreditBureauResponse,
        application: LoanApplication,
        debtToIncomeRatio: BigDecimal
    ): List<String> {
        val riskFactors = mutableListOf<String>()
        
        if (creditResponse.creditScore < 600) {
            riskFactors.add("Low credit score (${creditResponse.creditScore})")
        }
        
        if (creditResponse.bankruptcyHistory) {
            riskFactors.add("Bankruptcy history")
        }
        
        if (creditResponse.latePayments > 3) {
            riskFactors.add("Multiple late payments (${creditResponse.latePayments})")
        }
        
        if (creditResponse.creditUtilization > BigDecimal("0.8")) {
            riskFactors.add("High credit utilization (${creditResponse.creditUtilization.multiply(BigDecimal("100"))}%)")
        }
        
        if (debtToIncomeRatio > BigDecimal("0.4")) {
            riskFactors.add("High debt-to-income ratio (${debtToIncomeRatio.multiply(BigDecimal("100"))}%)")
        }
        
        if (application.loanAmount > application.annualIncome) {
            riskFactors.add("Loan amount exceeds annual income")
        }
        
        // Check loan-to-income ratio
        val loanToIncomeRatio = application.loanAmount.divide(application.annualIncome, 4, RoundingMode.HALF_UP)
        if (loanToIncomeRatio > BigDecimal("0.5")) {
            riskFactors.add("High loan-to-income ratio (${loanToIncomeRatio.multiply(BigDecimal("100"))}%)")
        }
        
        return riskFactors
    }
} 