package com.temporal.answer.lesson_2.config

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.worker.WorkerFactory
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Complete Temporal configuration for Lesson 2.
 * Sets up connection to local Temporal server and creates a basic worker.
 */
@Configuration
class TemporalConfig {
    
    private val logger = KotlinLogging.logger {}
    private lateinit var workerFactory: WorkerFactory
    
    /**
     * Creates connection stubs to Temporal server.
     * By default, connects to localhost:7233
     */
    @Bean
    fun workflowServiceStubs(): WorkflowServiceStubs {
        logger.info { "Creating Temporal service stubs for local server" }
        return WorkflowServiceStubs.newLocalServiceStubs()
    }
    
    /**
     * Creates the Temporal workflow client.
     * This is used to start workflows and interact with Temporal.
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
     * Workers execute workflows and activities.
     */
    @Bean
    fun workerFactory(workflowClient: WorkflowClient): WorkerFactory {
        logger.info { "Creating Temporal worker factory" }
        workerFactory = WorkerFactory.newInstance(workflowClient)
        return workerFactory
    }
    
    /**
     * Starts the worker after Spring context is initialized.
     * For now, we just create an empty worker to test the connection.
     */
    @PostConstruct
    fun startWorker() {
        logger.info { "Starting Temporal worker..." }
        
        // Create a worker for a test task queue
        // We'll add workflows and activities in later lessons
        workerFactory.newWorker("lesson2-test-queue")
        
        // Start the worker factory
        workerFactory.start()
        
        logger.info { "✅ Temporal worker started successfully! Connected to local Temporal server." }
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