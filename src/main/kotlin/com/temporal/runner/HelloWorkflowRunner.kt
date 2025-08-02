package com.temporal.runner

import com.temporal.config.TemporalConfig
import com.temporal.workflow.HelloWorkflow
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import mu.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class HelloWorkflowRunner(
    private val workflowClient: WorkflowClient
) : CommandLineRunner {

    private val logger = KotlinLogging.logger {}

    override fun run(vararg args: String?) {
        logger.info { "🚀 Running HelloWorkflow..." }

        val workflow = workflowClient.newWorkflowStub(
            HelloWorkflow::class.java,
            WorkflowOptions.newBuilder()
                .setTaskQueue(TemporalConfig.TASK_QUEUE)
                .setWorkflowId("hello-workflow-${System.currentTimeMillis()}")
                .build()
        )

        WorkflowClient.start {
            val result = workflow.sayHello("Temporal Learner")
            

            // Print the result
            logger.info { "✅ Workflow completed!" }
            logger.info { "   Result: $result" }

            // Give some guidance to the user
            logger.info { "" }
            logger.info { "🌐 Check the Temporal Web UI at http://localhost:8233" }
            logger.info { "   You should see your workflow execution in the 'Workflows' tab!" }
        }
    }
}