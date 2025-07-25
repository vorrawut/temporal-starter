package com.temporal.runner

import com.temporal.config.TemporalConfig
import com.temporal.workflow.HelloWorkflow
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import mu.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

/**
 * HelloWorkflowRunner - executes our workflow when the app starts.
 * 
 * This demonstrates how to:
 * 1. Create a workflow stub
 * 2. Configure workflow options
 * 3. Execute a workflow
 * 4. Handle the result
 */
@Component
class HelloWorkflowRunner(
    private val workflowClient: WorkflowClient
) : CommandLineRunner {
    
    private val logger = KotlinLogging.logger {}
    
    override fun run(vararg args: String?) {
        logger.info { "🚀 Running HelloWorkflow..." }
        
        try {
            // Create a workflow stub - this is how we interact with workflows
            val workflow = workflowClient.newWorkflowStub(
                HelloWorkflow::class.java,
                WorkflowOptions.newBuilder()
                    .setTaskQueue(TemporalConfig.TASK_QUEUE)
                    .setWorkflowId("hello-workflow-${System.currentTimeMillis()}")
                    .build()
            )

            // Execute the workflow
            WorkflowClient.start { val result = workflow.sayHello("Temporal Learner")

                // Print the result
                logger.info { "✅ Workflow completed!" }
                logger.info { "   Result: $result" }

                // Give some guidance to the user
                logger.info { "" }
                logger.info { "🌐 Check the Temporal Web UI at http://localhost:8233" }
                logger.info { "   You should see your workflow execution in the 'Workflows' tab!" }
            }
            
        } catch (e: Exception) {
            logger.error(e) { "❌ Error running HelloWorkflow" }
            logger.error { "   Make sure Temporal server is running: temporal server start-dev" }
        }
    }
} 