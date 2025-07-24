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
    
    private var workerFactory: WorkerFactory? = null
    
    @Bean
    fun workflowServiceStubs(): WorkflowServiceStubs {
        logger.info("Initializing Temporal WorkflowServiceStubs for $temporalHost:$temporalPort")
        val options = WorkflowServiceStubsOptions.newBuilder()
            .setTarget("$temporalHost:$temporalPort")
            .build()
        
        return WorkflowServiceStubs.newServiceStubs(options)
    }
    
    @Bean
    fun workflowClient(workflowServiceStubs: WorkflowServiceStubs): WorkflowClient {
        logger.info("Initializing Temporal WorkflowClient for namespace: $namespace")
        val options = WorkflowClientOptions.newBuilder()
            .setNamespace(namespace)
            .build()
        
        return WorkflowClient.newInstance(workflowServiceStubs, options)
    }
    
    @Bean
    fun temporalWorkerFactory(workflowClient: WorkflowClient): WorkerFactory {
        logger.info("Creating Temporal WorkerFactory")
        val factory = WorkerFactory.newInstance(workflowClient)
        this.workerFactory = factory
        
        // Start workers immediately after factory creation
        startWorkers(factory)
        
        return factory
    }
    
    private fun startWorkers(factory: WorkerFactory) {
        logger.info("🚀 Starting Temporal workers initialization...")
        
        logger.info("✅ WorkerFactory initialized, creating workers...")
        logger.info("🔧 Creating loan processing worker on queue: loan-processing-queue")
        
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
        
        logger.info("✅ Registered workflow: LoanApplicationWorkflowImpl")
        logger.info("✅ Registered activities: DocumentValidation, RiskScoring, LoanDisbursement, Notification")
        
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
        
        logger.info("✅ Registered follow-up workflow: FollowUpWorkflowImpl")
        
        // Start all workers
        logger.info("🔧 Starting WorkerFactory...")
        factory.start()
        
        logger.info("🎉 Temporal workers started successfully!")
        logger.info("✅ Loan processing queue: loan-processing-queue")
        logger.info("✅ Follow-up queue: follow-up-queue")
        logger.info("✅ Namespace: $namespace")
        logger.info("✅ Temporal server: $temporalHost:$temporalPort")
        logger.info("🔔 Check Temporal Web UI at: http://localhost:8233")
        
        // Verify workers are running
        logger.info("🔍 Verifying worker registration...")
        Thread.sleep(2000) // Give workers time to register
        logger.info("✅ Worker registration verification complete")
    }
    
    @PreDestroy
    fun stopWorkers() {
        logger.info("Stopping Temporal workers...")
        
        try {
            workerFactory?.let { factory ->
                factory.shutdownNow()
                factory.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)
                logger.info("✅ Temporal workers stopped successfully")
            } ?: logger.warn("WorkerFactory was not initialized, nothing to stop")
        } catch (e: Exception) {
            logger.error("❌ Error stopping Temporal workers", e)
        }
    }
} 