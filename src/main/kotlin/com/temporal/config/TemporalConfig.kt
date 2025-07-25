package com.temporal.config

import io.micrometer.observation.annotation.Observed
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.worker.WorkerFactory
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

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
     * Event listener that starts the Temporal worker factory when the application is ready.
     * This ensures that the worker factory is properly initialized after all Spring beans are created.
     *
     * @param event ApplicationReadyEvent triggered when the application is fully started
     */
    @Observed
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady(event: ApplicationReadyEvent) {
        try {
            logger.info { "Starting Temporal worker..." }

            workerFactory.newWorker("lesson2-test-queue")

            workerFactory.start()

            logger.info { "✅ Temporal worker started successfully!" }
        } catch (ex: Exception) {
            logger.error(ex) { "❌ Failed to start Temporal worker!" }
            throw ex // Optional: rethrow to still fail fast
        }
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