package com.temporal.activity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.temporal.model.DocumentType
import com.temporal.model.LoanApplication
import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface DocumentValidationActivity {
    
    @ActivityMethod
    fun validateDocuments(application: LoanApplication): DocumentValidationResult
}

data class DocumentValidationResult @JsonCreator constructor(
    @JsonProperty("valid") val isValid: Boolean,
    @JsonProperty("validDocuments") val validDocuments: List<DocumentType>,
    @JsonProperty("missingDocuments") val missingDocuments: List<DocumentType>,
    @JsonProperty("issues") val issues: List<String>
) 