package com.temporal.activity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface UserValidationActivity {

    @ActivityMethod
    fun validateUser(email: String): ValidationResult
}

//data class ValidationResult(
//    val isValid: Boolean,
//    val errorMessage: String?
//)

data class ValidationResult @JsonCreator constructor(
    @JsonProperty("valid") val isValid: Boolean,
    @JsonProperty("errorMessage") val errorMessage: String?
)