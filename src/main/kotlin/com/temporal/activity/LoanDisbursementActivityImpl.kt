package com.temporal.activity

import com.temporal.model.*
import com.temporal.service.BankService
import io.temporal.failure.ApplicationFailure
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*

@Component
class LoanDisbursementActivityImpl(
    private val bankService: BankService
) : LoanDisbursementActivity {
    
    private val logger = LoggerFactory.getLogger(LoanDisbursementActivityImpl::class.java)
    
    // In-memory storage for demo purposes (in production, use a database)
    private val disbursementRecords = mutableMapOf<String, LoanDisbursement>()
    
    override fun disburseLoan(application: LoanApplication): LoanDisbursement {
        logger.info("Starting loan disbursement for application ${application.workflowId}")
        
        try {
            // Step 1: Prepare disbursement
            val transactionId = "TXN-${UUID.randomUUID()}"
            val bankAccount = generateBankAccount(application.userId)
            
            val disbursement = LoanDisbursement(
                applicationId = application.workflowId,
                transactionId = transactionId,
                amount = application.loanAmount,
                bankAccount = bankAccount,
                routingNumber = "123456789", // Mock routing number
                status = DisbursementStatus.PENDING
            )
            
            // Store the record
            disbursementRecords[application.workflowId] = disbursement
            
            logger.info("Disbursement prepared: $transactionId")
            
            // Step 2: Execute bank transfer
            val transferRequest = BankTransferRequest(
                amount = application.loanAmount,
                fromAccount = "LOAN-FUND-ACCOUNT", // Company loan fund account
                toAccount = bankAccount,
                routingNumber = disbursement.routingNumber,
                purpose = "Loan disbursement for ${application.purpose}",
                reference = application.workflowId
            )
            
            val transferResponse = bankService.transferFunds(transferRequest)
            
            // Step 3: Update disbursement status based on bank response
            val updatedDisbursement = when (transferResponse.status) {
                "SUCCESS" -> {
                    logger.info("Bank transfer successful: ${transferResponse.transactionId}")
                    disbursement.copy(
                        status = DisbursementStatus.COMPLETED,
                        transactionId = transferResponse.transactionId
                    )
                }
                "FAILED" -> {
                    logger.error("Bank transfer failed: ${transferResponse.message}")
                    disbursement.copy(
                        status = DisbursementStatus.FAILED,
                        failureReason = transferResponse.message
                    )
                }
                "PENDING" -> {
                    logger.info("Bank transfer pending: ${transferResponse.transactionId}")
                    disbursement.copy(
                        status = DisbursementStatus.PROCESSING,
                        transactionId = transferResponse.transactionId
                    )
                }
                else -> {
                    logger.error("Unknown bank transfer status: ${transferResponse.status}")
                    disbursement.copy(
                        status = DisbursementStatus.FAILED,
                        failureReason = "Unknown status: ${transferResponse.status}"
                    )
                }
            }
            
            // Update stored record
            disbursementRecords[application.workflowId] = updatedDisbursement
            
            // Step 4: Handle processing status (simulate async processing)
            if (updatedDisbursement.status == DisbursementStatus.PROCESSING) {
                // Simulate processing time
                Thread.sleep(5000)
                
                // Check final status (simulate checking with bank)
                val finalStatus = bankService.checkTransferStatus(updatedDisbursement.transactionId)
                
                val finalDisbursement = if (finalStatus.status == "COMPLETED") {
                    updatedDisbursement.copy(status = DisbursementStatus.COMPLETED)
                } else {
                    updatedDisbursement.copy(
                        status = DisbursementStatus.FAILED,
                        failureReason = finalStatus.message
                    )
                }
                
                disbursementRecords[application.workflowId] = finalDisbursement
                return finalDisbursement
            }
            
            return updatedDisbursement
            
        } catch (e: Exception) {
            logger.error("Disbursement failed for ${application.workflowId}", e)
            
            val failedDisbursement = LoanDisbursement(
                applicationId = application.workflowId,
                transactionId = "FAILED-${UUID.randomUUID()}",
                amount = application.loanAmount,
                bankAccount = "",
                routingNumber = "",
                status = DisbursementStatus.FAILED,
                failureReason = e.message
            )
            
            disbursementRecords[application.workflowId] = failedDisbursement
            
            throw ApplicationFailure.newFailure(
                "Disbursement failed: ${e.message}",
                "DISBURSEMENT_ERROR"
            )
        }
    }
    
    override fun compensateFailedDisbursement(workflowId: String): Boolean {
        logger.info("Starting compensation for failed disbursement: $workflowId")
        
        try {
            val disbursement = disbursementRecords[workflowId]
            
            if (disbursement == null) {
                logger.warn("No disbursement record found for $workflowId")
                return true // Nothing to compensate
            }
            
            when (disbursement.status) {
                DisbursementStatus.COMPLETED -> {
                    // Need to reverse the transaction
                    logger.info("Reversing completed disbursement: ${disbursement.transactionId}")
                    
                    val reverseRequest = BankTransferRequest(
                        amount = disbursement.amount,
                        fromAccount = disbursement.bankAccount,
                        toAccount = "LOAN-FUND-ACCOUNT", // Return to company account
                        routingNumber = disbursement.routingNumber,
                        purpose = "Reversal of failed loan disbursement",
                        reference = "REVERSE-$workflowId"
                    )
                    
                    val reverseResponse = bankService.transferFunds(reverseRequest)
                    
                    if (reverseResponse.status == "SUCCESS") {
                        val compensatedDisbursement = disbursement.copy(
                            status = DisbursementStatus.REFUNDED
                        )
                        disbursementRecords[workflowId] = compensatedDisbursement
                        
                        logger.info("Disbursement successfully reversed: ${reverseResponse.transactionId}")
                        return true
                    } else {
                        logger.error("Failed to reverse disbursement: ${reverseResponse.message}")
                        return false
                    }
                }
                
                DisbursementStatus.PROCESSING -> {
                    // Try to cancel the pending transaction
                    logger.info("Cancelling pending disbursement: ${disbursement.transactionId}")
                    
                    val cancelled = bankService.cancelTransfer(disbursement.transactionId)
                    
                    if (cancelled) {
                        val cancelledDisbursement = disbursement.copy(
                            status = DisbursementStatus.CANCELLED
                        )
                        disbursementRecords[workflowId] = cancelledDisbursement
                        
                        logger.info("Disbursement successfully cancelled")
                        return true
                    } else {
                        logger.error("Failed to cancel disbursement")
                        return false
                    }
                }
                
                DisbursementStatus.FAILED,
                DisbursementStatus.CANCELLED,
                DisbursementStatus.REFUNDED -> {
                    // Already in a safe state
                    logger.info("Disbursement already in safe state: ${disbursement.status}")
                    return true
                }
                
                else -> {
                    logger.warn("Unknown disbursement status for compensation: ${disbursement.status}")
                    return false
                }
            }
            
        } catch (e: Exception) {
            logger.error("Compensation failed for $workflowId", e)
            return false
        }
    }
    
    private fun generateBankAccount(userId: String): String {
        // Generate a mock bank account number
        val hash = userId.hashCode().toString().takeLast(8)
        return "ACC-$hash"
    }
} 