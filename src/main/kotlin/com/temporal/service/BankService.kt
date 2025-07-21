package com.temporal.service

import com.temporal.model.BankTransferRequest
import com.temporal.model.BankTransferResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import kotlin.random.Random

@Service
class BankService {
    
    private val logger = LoggerFactory.getLogger(BankService::class.java)
    private val pendingTransfers = mutableMapOf<String, BankTransferResponse>()
    
    fun transferFunds(request: BankTransferRequest): BankTransferResponse {
        logger.info("Processing bank transfer: ${request.amount} from ${request.fromAccount} to ${request.toAccount}")
        
        // Simulate processing time
        Thread.sleep(Random.nextLong(2000, 5000))
        
        val transactionId = "BNK-${UUID.randomUUID()}"
        
        // Simulate different outcomes based on amount
        val response = when {
            request.amount.toDouble() > 100000 -> {
                // Large amounts may require additional processing
                logger.info("Large transfer amount, marking as pending")
                BankTransferResponse(
                    transactionId = transactionId,
                    status = "PENDING",
                    message = "Transfer pending approval for large amount"
                )
            }
            shouldSimulateFailure() -> {
                // Simulate occasional failures
                logger.warn("Simulating bank transfer failure")
                BankTransferResponse(
                    transactionId = transactionId,
                    status = "FAILED",
                    message = "Insufficient funds or account restrictions"
                )
            }
            else -> {
                // Successful transfer
                logger.info("Bank transfer successful: $transactionId")
                BankTransferResponse(
                    transactionId = transactionId,
                    status = "SUCCESS",
                    message = "Transfer completed successfully"
                )
            }
        }
        
        // Store pending transfers for status checking
        if (response.status == "PENDING") {
            pendingTransfers[transactionId] = response
        }
        
        return response
    }
    
    fun checkTransferStatus(transactionId: String): BankTransferResponse {
        logger.info("Checking transfer status for: $transactionId")
        
        // Check if it's a pending transfer
        val pendingTransfer = pendingTransfers[transactionId]
        if (pendingTransfer != null) {
            // Simulate approval process
            val approved = Random.nextBoolean() // 50% chance of approval
            
            val finalResponse = if (approved) {
                BankTransferResponse(
                    transactionId = transactionId,
                    status = "COMPLETED",
                    message = "Transfer approved and completed"
                )
            } else {
                BankTransferResponse(
                    transactionId = transactionId,
                    status = "FAILED",
                    message = "Transfer rejected by compliance review"
                )
            }
            
            // Remove from pending
            pendingTransfers.remove(transactionId)
            
            logger.info("Pending transfer resolved: ${finalResponse.status}")
            return finalResponse
        }
        
        // For non-pending transfers, return success (assuming they completed)
        return BankTransferResponse(
            transactionId = transactionId,
            status = "COMPLETED",
            message = "Transfer completed"
        )
    }
    
    fun cancelTransfer(transactionId: String): Boolean {
        logger.info("Attempting to cancel transfer: $transactionId")
        
        // Check if it's still pending
        val pendingTransfer = pendingTransfers[transactionId]
        if (pendingTransfer != null) {
            pendingTransfers.remove(transactionId)
            logger.info("Transfer cancelled successfully: $transactionId")
            return true
        }
        
        // Can't cancel completed or non-existent transfers
        logger.warn("Cannot cancel transfer $transactionId - not in pending state")
        return false
    }
    
    private fun shouldSimulateFailure(): Boolean {
        // Simulate failures 15% of the time
        return Random.nextFloat() < 0.15f
    }
} 