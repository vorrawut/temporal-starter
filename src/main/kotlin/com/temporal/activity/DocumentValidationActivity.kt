package com.temporal.activity

import com.temporal.model.DocumentType
import com.temporal.model.LoanApplication
import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface DocumentValidationActivity {
    
    @ActivityMethod
    fun validateDocuments(application: LoanApplication): DocumentValidationResult
}

data class DocumentValidationResult(
    val isValid: Boolean,
    val validDocuments: List<DocumentType>,
    val missingDocuments: List<DocumentType>,
    val issues: List<String>
) 