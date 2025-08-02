package com.temporal.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.temporal.activity.*
import com.temporal.workflow.CalculatorWorkflowImpl
import com.temporal.workflow.HelloWorkflowImpl
import com.temporal.workflow.UserOnboardingWorkflowImpl
import io.micrometer.observation.annotation.Observed
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.common.converter.*
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.serviceclient.WorkflowServiceStubsOptions
import io.temporal.worker.WorkerFactory
import jakarta.annotation.PreDestroy
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

@Configuration
class TemporalConfig {

    companion object {
        const val TASK_QUEUE = "lesson4-hello-queue"
        const val TASK_CALCULATOR = "calculator-queue"
        const val TASK_ONBOARDING = "onboarding-queue"
    }

    private val logger = KotlinLogging.logger {}
    private lateinit var workerFactory: WorkerFactory

    @Bean
    fun dataConverter(objectMapper: ObjectMapper): DataConverter {
        return DefaultDataConverter(
            NullPayloadConverter(),
            ByteArrayPayloadConverter(),
            ProtobufJsonPayloadConverter(),
            JacksonJsonPayloadConverter(objectMapper),
        )
    }

    @Bean
    fun workflowServiceStubs(): WorkflowServiceStubs {
        logger.info { "Creating Temporal service stubs for local server" }
        return WorkflowServiceStubs.newServiceStubs(
            WorkflowServiceStubsOptions.newBuilder()
                .setTarget("localhost:7233")
                .build()
        )
    }

    @Bean
    fun workflowClient(workflowServiceStubs: WorkflowServiceStubs): WorkflowClient {
        logger.info { "Creating Temporal workflow client" }
        return WorkflowClient.newInstance(
            workflowServiceStubs,
            WorkflowClientOptions.newBuilder()
                .build()
        )
    }

    @Bean
    fun workerFactory(workflowClient: WorkflowClient): WorkerFactory {
        logger.info { "Creating Temporal worker factory" }
        workerFactory = WorkerFactory.newInstance(workflowClient)
        return workerFactory
    }

    @Bean
    fun greetingActivity(): GreetingActivityImpl {
        return GreetingActivityImpl()
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

//            workerFactory.newWorker("lesson2-test-queue")

           val worker =  workerFactory.newWorker(TASK_QUEUE)

            // Register workflow and activity
            worker.registerWorkflowImplementationTypes(HelloWorkflowImpl::class.java)
            worker.registerActivitiesImplementations(greetingActivity())


            val worker2 =  workerFactory.newWorker(TASK_CALCULATOR)
            worker2.registerWorkflowImplementationTypes(CalculatorWorkflowImpl::class.java)
            worker2.registerActivitiesImplementations(MathActivityImpl())


            val worker3 =  workerFactory.newWorker(TASK_ONBOARDING)
            worker3.registerWorkflowImplementationTypes(UserOnboardingWorkflowImpl::class.java)
            worker3.registerActivitiesImplementations(
                UserValidationActivityImpl(),
                AccountCreationActivityImpl(),
                NotificationActivityImpl()
            )

            workerFactory.start()

            logger.info { "✅ Temporal worker started successfully!" }
        } catch (ex: Exception) {
            logger.error(ex) { "❌ Failed to start Temporal worker!" }
            throw ex // Optional: rethrow to still fail fast
        }
    }

    @PreDestroy
    fun shutdown() {
        logger.info { "Shutting down Temporal worker..." }
        workerFactory.shutdown()
        logger.info { "❌ Temporal worker stopped" }
    }
}