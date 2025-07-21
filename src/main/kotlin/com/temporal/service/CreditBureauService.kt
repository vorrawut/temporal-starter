package com.temporal.service

import com.temporal.model.CreditBureauResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import kotlin.random.Random

@Service
class CreditBureauService {
    
    private val logger = LoggerFactory.getLogger(CreditBureauService::class.java)
    private var callCount = 0
    
    fun getCreditScore(fullName: String, email: String): CreditBureauResponse {
        callCount++
        logger.info("Credit bureau API call #$callCount for: $fullName")
        
        // Simulate network delays
        Thread.sleep(Random.nextLong(1000, 3000))
        
        // Simulate failures for demonstration of retry logic
        when {
            callCount == 1 && shouldSimulateFailure() -> {
                logger.warn("Simulating timeout on first call")
                throw RuntimeException("Service timeout - please retry")
            }
            callCount == 2 && shouldSimulateFailure() -> {
                logger.warn("Simulating rate limiting on second call")
                throw RuntimeException("Rate limited - too many requests")
            }
        }
        
        // Generate realistic credit data based on email (for consistent demo results)
        val baseScore = when {
            email.contains("high") -> 780
            email.contains("low") -> 520
            email.contains("medium") -> 650
            else -> 650 + (email.hashCode() % 150) // 650-800 range
        }
        
        val creditScore = baseScore.coerceIn(300, 850)
        
        val response = CreditBureauResponse(
            creditScore = creditScore,
            creditHistory = generateCreditHistory(creditScore),
            outstandingDebts = generateOutstandingDebts(creditScore),
            bankruptcyHistory = creditScore < 500,
            latePayments = generateLatePayments(creditScore),
            creditUtilization = generateCreditUtilization(creditScore)
        )
        
        logger.info("Credit bureau response: score=$creditScore")
        return response
    }
    
    private fun shouldSimulateFailure(): Boolean {
        // Simulate failures 30% of the time to demonstrate retry logic
        return Random.nextFloat() < 0.3f
    }
    
    private fun generateCreditHistory(creditScore: Int): String {
        return when {
            creditScore >= 750 -> "Excellent credit history with consistent payments"
            creditScore >= 700 -> "Good credit history with minor issues"
            creditScore >= 650 -> "Fair credit history with some late payments"
            creditScore >= 600 -> "Below average credit with multiple late payments"
            else -> "Poor credit history with defaults and collections"
        }
    }
    
    private fun generateOutstandingDebts(creditScore: Int): BigDecimal {
        val baseDebt = when {
            creditScore >= 750 -> Random.nextDouble(5000.0, 15000.0)
            creditScore >= 700 -> Random.nextDouble(10000.0, 25000.0)
            creditScore >= 650 -> Random.nextDouble(15000.0, 35000.0)
            creditScore >= 600 -> Random.nextDouble(20000.0, 45000.0)
            else -> Random.nextDouble(25000.0, 60000.0)
        }
        return BigDecimal.valueOf(baseDebt).setScale(2, java.math.RoundingMode.HALF_UP)
    }
    
    private fun generateLatePayments(creditScore: Int): Int {
        return when {
            creditScore >= 750 -> Random.nextInt(0, 2)
            creditScore >= 700 -> Random.nextInt(1, 4)
            creditScore >= 650 -> Random.nextInt(2, 6)
            creditScore >= 600 -> Random.nextInt(4, 8)
            else -> Random.nextInt(6, 15)
        }
    }
    
    private fun generateCreditUtilization(creditScore: Int): BigDecimal {
        val utilization = when {
            creditScore >= 750 -> Random.nextDouble(0.1, 0.3)
            creditScore >= 700 -> Random.nextDouble(0.2, 0.4)
            creditScore >= 650 -> Random.nextDouble(0.3, 0.6)
            creditScore >= 600 -> Random.nextDouble(0.5, 0.8)
            else -> Random.nextDouble(0.7, 0.95)
        }
        return BigDecimal.valueOf(utilization).setScale(2, java.math.RoundingMode.HALF_UP)
    }
} 