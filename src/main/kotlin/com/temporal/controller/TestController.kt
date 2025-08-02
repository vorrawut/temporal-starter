package com.temporal.controller

import com.temporal.config.TemporalConfig
import com.temporal.workflow.CalculatorWorkflow
import com.temporal.workflow.UserOnboardingWorkflow
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test")
class TestController(
    private val workflowClient: WorkflowClient
) {
    @GetMapping("/api")
    @ResponseStatus(value = HttpStatus.OK)
    fun testApi(): Unit = testLocalApi()

    fun testLocalApi() {

        val workflow = workflowClient.newWorkflowStub(
            CalculatorWorkflow::class.java,
            WorkflowOptions.newBuilder()
                .setTaskQueue(TemporalConfig.TASK_CALCULATOR)
                .setWorkflowId("calc-${System.currentTimeMillis()}")
                .build()
        )

        WorkflowClient.start {
            val result = workflow.add(5, 3)
            println("Result: $result")
        }
    }

    @GetMapping("/api2")
    @ResponseStatus(value = HttpStatus.OK)
    fun testApi2(): Unit = testLocalApi2()

    fun testLocalApi2() {

        val workflow = workflowClient.newWorkflowStub(
            UserOnboardingWorkflow::class.java,
            WorkflowOptions.newBuilder()
                .setTaskQueue(TemporalConfig.TASK_ONBOARDING)
                .setWorkflowId("onboard-${System.currentTimeMillis()}")
                .build()
        )

        WorkflowClient.start {
            val result = workflow.onboardUser("test222@example.com")
            println("Result: $result")
        }
    }
}