package com.temporal.activity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface AccountCreationActivity {

    @ActivityMethod
    fun createAccount(email: String): CreationResult
}

//data class CreationResult(
//    val success: Boolean,
//    val userId: String?,
//    val errorMessage: String?
//)

data class CreationResult @JsonCreator constructor(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("userId") val userId: String?,
    @JsonProperty("errorMessage") val errorMessage: String?
)