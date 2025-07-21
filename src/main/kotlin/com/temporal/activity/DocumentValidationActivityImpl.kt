package com.temporal.activity

import com.temporal.model.DocumentType
import com.temporal.model.LoanApplication
import com.temporal.model.LoanPurpose
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DocumentValidationActivityImpl : DocumentValidationActivity {
    
    private val logger = LoggerFactory.getLogger(DocumentValidationActivityImpl::class.java)
    
    override fun validateDocuments(application: LoanApplication): DocumentValidationResult {
        logger.info("Starting document validation for application ${application.workflowId}")
        
        // Simulate processing time
        Thread.sleep(2000)
        
        val requiredDocuments = getRequiredDocuments(application.purpose, application.loanAmount)
        val providedDocuments = application.documents
        
        val missingDocuments = requiredDocuments.filter { it !in providedDocuments }
        val issues = mutableListOf<String>()
        
        // Basic validation rules
        if (!providedDocuments.contains(DocumentType.ID_CARD) && 
            !providedDocuments.contains(DocumentType.PASSPORT) &&
            !providedDocuments.contains(DocumentType.DRIVER_LICENSE)) {
            issues.add("Valid government-issued ID required")
        }
        
        if (!providedDocuments.contains(DocumentType.INCOME_STATEMENT) &&
            !providedDocuments.contains(DocumentType.TAX_RETURNS)) {
            issues.add("Income verification required")
        }
        
        // Loan amount specific validation
        if (application.loanAmount > BigDecimal("50000")) {
            if (!providedDocuments.contains(DocumentType.TAX_RETURNS)) {
                issues.add("Tax returns required for loans over $50,000")
            }
            if (!providedDocuments.contains(DocumentType.BANK_STATEMENT)) {
                issues.add("Bank statements required for loans over $50,000")
            }
        }
        
        // Purpose-specific validation
        when (application.purpose) {
            LoanPurpose.BUSINESS -> {
                if (!providedDocuments.contains(DocumentType.BUSINESS_LICENSE)) {
                    issues.add("Business license required for business loans")
                }
                if (!providedDocuments.contains(DocumentType.FINANCIAL_STATEMENT)) {
                    issues.add("Financial statements required for business loans")
                }
            }
            LoanPurpose.EDUCATION -> {
                // In a real system, we might require enrollment verification
            }
            else -> {
                // Standard validation already covered above
            }
        }
        
        val isValid = missingDocuments.isEmpty() && issues.isEmpty()
        
        val result = DocumentValidationResult(
            isValid = isValid,
            validDocuments = providedDocuments,
            missingDocuments = missingDocuments,
            issues = issues
        )
        
        logger.info("Document validation completed for ${application.workflowId}: valid=$isValid")
        if (!isValid) {
            logger.warn("Validation issues: ${issues.joinToString(", ")}")
            logger.warn("Missing documents: ${missingDocuments.joinToString(", ")}")
        }
        
        return result
    }
    
    private fun getRequiredDocuments(purpose: LoanPurpose, amount: BigDecimal): List<DocumentType> {
        val baseDocuments = mutableListOf(
            DocumentType.ID_CARD, // or PASSPORT or DRIVER_LICENSE
            DocumentType.INCOME_STATEMENT, // or TAX_RETURNS
            DocumentType.BANK_STATEMENT
        )
        
        // Add purpose-specific requirements
        when (purpose) {
            LoanPurpose.BUSINESS -> {
                baseDocuments.addAll(listOf(
                    DocumentType.BUSINESS_LICENSE,
                    DocumentType.FINANCIAL_STATEMENT
                ))
            }
            LoanPurpose.HOME_IMPROVEMENT -> {
                if (amount > BigDecimal("25000")) {
                    baseDocuments.add(DocumentType.EMPLOYMENT_VERIFICATION)
                }
            }
            else -> {
                // Standard requirements
            }
        }
        
        // Add amount-specific requirements
        if (amount > BigDecimal("50000")) {
            baseDocuments.addAll(listOf(
                DocumentType.TAX_RETURNS,
                DocumentType.EMPLOYMENT_VERIFICATION
            ))
        }
        
        return baseDocuments.distinct()
    }
} 