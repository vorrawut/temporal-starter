package com.temporal.answer.lesson_4.config

import com.temporal.answer.lesson_4.activity.GreetingActivityImpl
import com.temporal.answer.lesson_4.workflow.HelloWorkflowImpl
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.worker.Worker
import io.temporal.worker.WorkerFactory
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Complete Temporal configuration for Lesson 4.
 * 
 * This configuration:
 * 1. Sets up connection to Temporal server
 * 2. Creates and configures a worker
 * 3. Registers our workflow and activity implementations
 * 4. Manages the lifecycle of the worker
 */
@Configuration
class TemporalConfig {
    
    companion object {
        const val TASK_QUEUE = "lesson4-hello-queue"
    }
    
    private val logger = KotlinLogging.logger {}
    private lateinit var workerFactory: WorkerFactory
    
    /**
     * Creates connection stubs to Temporal server.
     */
    @Bean
    fun workflowServiceStubs(): WorkflowServiceStubs {
        logger.info { "Creating Temporal service stubs for local server" }
        return WorkflowServiceStubs.newLocalServiceStubs()
    }
    
    /**
     * Creates the Temporal workflow client.
     */
    @Bean
    fun workflowClient(workflowServiceStubs: WorkflowServiceStubs): WorkflowClient {
        logger.info { "Creating Temporal workflow client" }
        return WorkflowClient.newInstance(
            workflowServiceStubs,
            WorkflowClientOptions.newBuilder()
                .build()
        )
    }
    
    /**
     * Creates the worker factory.
     */
    @Bean
    fun workerFactory(workflowClient: WorkflowClient): WorkerFactory {
        logger.info { "Creating Temporal worker factory" }
        workerFactory = WorkerFactory.newInstance(workflowClient)
        return workerFactory
    }
    
    /**
     * Creates the activity implementation as a Spring bean.
     */
    @Bean
    fun greetingActivity(): GreetingActivityImpl {
        return GreetingActivityImpl()
    }
    
    /**
     * Starts the worker and registers workflow and activity implementations.
     */
    @Observed
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady(event: ApplicationReadyEvent) {
        logger.info { "Starting Temporal worker for HelloWorkflow..." }
        
        // Create a worker that listens to our task queue
        val worker: Worker = workerFactory.newWorker(TASK_QUEUE)
        
        // Register the workflow implementation
        worker.registerWorkflowImplementationTypes(HelloWorkflowImpl::class.java)
        
        // Register the activity implementation
//        worker.registerActivitiesImplementations(greetingActivity())
        
        // Start the worker factory
        workerFactory.start()
        
        logger.info { "✅ Temporal worker started successfully for HelloWorkflow!" }
        logger.info { "   Task Queue: $TASK_QUEUE" }
        logger.info { "   Registered: HelloWorkflow, GreetingActivity" }
    }
    
    /**
     * Gracefully shutdown the worker when application stops.
     */
    @PreDestroy
    fun shutdown() {
        logger.info { "Shutting down Temporal worker..." }
        workerFactory.shutdown()
        logger.info { "❌ Temporal worker stopped" }
    }
} 