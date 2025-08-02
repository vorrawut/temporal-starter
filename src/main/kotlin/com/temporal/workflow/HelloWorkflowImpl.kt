package com.temporal.workflow

import com.temporal.activity.GreetingActivity
import io.temporal.activity.ActivityOptions
import io.temporal.workflow.Workflow
import java.time.Duration

class HelloWorkflowImpl : HelloWorkflow {

    private val greetingActivity = Workflow.newActivityStub(
        GreetingActivity::class.java,
        ActivityOptions.newBuilder()
            .setScheduleToCloseTimeout(Duration.ofMinutes(1))
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .build()
    )

    override fun sayHello(name: String): String {
        val logger = Workflow.getLogger(this::class.java)

        logger.info("HelloWorkflow started for: $name")

        // Call the activity
        val greeting = greetingActivity.generateGreeting(name)

        logger.info("HelloWorkflow completed for: $name")

        return greeting
    }
}
