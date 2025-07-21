package com.temporal.config

import com.temporal.activity.*
import com.temporal.workflow.FollowUpWorkflowImpl
import com.temporal.workflow.LoanApplicationWorkflowImpl
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.serviceclient.WorkflowServiceStubsOptions
import io.temporal.worker.Worker
import io.temporal.worker.WorkerFactory
import io.temporal.worker.WorkerOptions
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

@Configuration
@ConditionalOnProperty(name = ["temporal.enabled"], matchIfMissing = true)
class TemporalConfig(
    private val documentValidationActivity: DocumentValidationActivityImpl,
    private val riskScoringActivity: RiskScoringActivityImpl,
    private val loanDisbursementActivity: LoanDisbursementActivityImpl,
    private val notificationActivity: NotificationActivityImpl
) {
    
    private val logger = LoggerFactory.getLogger(TemporalConfig::class.java)
    
    @Value("\${temporal.server.host:localhost}")
    private lateinit var temporalHost: String
    
    @Value("\${temporal.server.port:7233}")
    private var temporalPort: Int = 7233
    
    @Value("\${temporal.namespace:default}")
    private lateinit var namespace: String
    
    private var temporalWorkerFactory: WorkerFactory? = null
    
    @Bean
    fun workflowServiceStubs(): WorkflowServiceStubs {
        val options = WorkflowServiceStubsOptions.newBuilder()
            .setTarget("$temporalHost:$temporalPort")
            .build()
        
        return WorkflowServiceStubs.newServiceStubs(options)
    }
    
    @Bean
    fun workflowClient(workflowServiceStubs: WorkflowServiceStubs): WorkflowClient {
        val options = WorkflowClientOptions.newBuilder()
            .setNamespace(namespace)
            .build()
        
        return WorkflowClient.newInstance(workflowServiceStubs, options)
    }
    
    @Bean
    fun workerFactory(workflowClient: WorkflowClient): WorkerFactory {
        temporalWorkerFactory = WorkerFactory.newInstance(workflowClient)
        return temporalWorkerFactory!!
    }
    
    @PostConstruct
    fun startWorkers() {
        logger.info("Starting Temporal workers...")
        
        try {
            val factory = temporalWorkerFactory
            if (factory == null) {
                logger.warn("WorkerFactory not initialized, skipping worker startup")
                return
            }
            
            // Create main loan processing worker
            val loanWorker = factory.newWorker("loan-processing-queue")
            
            // Register workflow implementations
            loanWorker.registerWorkflowImplementationTypes(
                LoanApplicationWorkflowImpl::class.java
            )
            
            // Register activity implementations
            loanWorker.registerActivitiesImplementations(
                documentValidationActivity,
                riskScoringActivity,
                loanDisbursementActivity,
                notificationActivity
            )
            
            // Create follow-up worker for child workflows
            val followUpWorker = factory.newWorker("follow-up-queue")
            
            // Register follow-up workflow
            followUpWorker.registerWorkflowImplementationTypes(
                FollowUpWorkflowImpl::class.java
            )
            
            // Register notification activity for follow-up workflow
            followUpWorker.registerActivitiesImplementations(
                notificationActivity
            )
            
            // Start all workers
            factory.start()
            
            logger.info("Temporal workers started successfully")
            logger.info("- Loan processing queue: loan-processing-queue")
            logger.info("- Follow-up queue: follow-up-queue")
            logger.info("- Namespace: $namespace")
            logger.info("- Temporal server: $temporalHost:$temporalPort")
            
        } catch (e: Exception) {
            logger.error("Failed to start Temporal workers", e)
            // Don't throw exception in test environments
        }
    }
    
    @PreDestroy
    fun stopWorkers() {
        logger.info("Stopping Temporal workers...")
        
        try {
            temporalWorkerFactory?.let { factory ->
                factory.shutdownNow()
                factory.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)
                logger.info("Temporal workers stopped successfully")
            }
        } catch (e: Exception) {
            logger.error("Error stopping Temporal workers", e)
        }
    }
} 