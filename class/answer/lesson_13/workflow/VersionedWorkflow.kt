package com.temporal.bootcamp.lesson13.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import io.temporal.workflow.Workflow
import java.time.Duration

/**
 * Versioned workflow demonstrating safe migration patterns.
 */
@WorkflowInterface
interface VersionedWorkflow {
    
    @WorkflowMethod
    fun processWithVersioning(request: ProcessingRequest): ProcessingResult
}

/**
 * Implementation showing how to handle workflow versioning safely.
 * 
 * Version evolution:
 * v1: Basic processing (initial implementation)
 * v2: Added validation step (additive change)  
 * v3: Changed processing order (breaking change)
 * v4: Added new activity (additive change)
 */
class VersionedWorkflowImpl : VersionedWorkflow {
    
    companion object {
        const val PROCESSING_VERSION = "ProcessingVersion"
        const val VALIDATION_VERSION = "ValidationVersion"
        const val ORDERING_VERSION = "OrderingVersion"
        const val ENHANCEMENT_VERSION = "EnhancementVersion"
    }
    
    override fun processWithVersioning(request: ProcessingRequest): ProcessingResult {
        val logger = Workflow.getLogger(this::class.java)
        val processingSteps = mutableListOf<String>()
        
        logger.info("Starting versioned processing for: ${request.requestId}")
        
        // Get the main processing version
        val processingVersion = Workflow.getVersion(PROCESSING_VERSION, 1, 4)
        logger.info("Using processing version: $processingVersion")
        
        try {
            // Version 1: Basic processing (always executed)
            val basicResult = performBasicProcessing(request)
            processingSteps.add("Basic processing completed")
            
            // Version 2+: Added validation (additive change)
            if (processingVersion >= 2) {
                val validationVersion = Workflow.getVersion(VALIDATION_VERSION, 1, 2)
                
                if (validationVersion == 1) {
                    // Simple validation (v2.0)
                    performSimpleValidation(request)
                    processingSteps.add("Simple validation completed")
                } else {
                    // Enhanced validation (v2.1+)
                    performEnhancedValidation(request)
                    processingSteps.add("Enhanced validation completed")
                }
            }
            
            // Version 3+: Changed processing order (breaking change)
            val orderingVersion = Workflow.getVersion(ORDERING_VERSION, 1, 2)
            
            if (orderingVersion == 1) {
                // Old order: process then enrich
                val processedResult = performAdvancedProcessing(basicResult)
                val enrichedResult = performDataEnrichment(processedResult)
                processingSteps.add("Legacy processing order: process → enrich")
            } else {
                // New order: enrich then process  
                val enrichedResult = performDataEnrichment(basicResult)
                val processedResult = performAdvancedProcessing(enrichedResult)
                processingSteps.add("New processing order: enrich → process")
            }
            
            // Version 4+: Added new enhancement activity (additive change)
            if (processingVersion >= 4) {
                val enhancementVersion = Workflow.getVersion(ENHANCEMENT_VERSION, 1, 1)
                
                performResultEnhancement(request)
                processingSteps.add("Result enhancement completed")
            }
            
            // Final processing based on priority
            val finalResult = when (request.priority) {
                ProcessingPriority.URGENT -> performUrgentProcessing(request)
                ProcessingPriority.HIGH -> performHighPriorityProcessing(request)
                else -> performStandardProcessing(request)
            }
            
            processingSteps.add("Priority processing completed: ${request.priority}")
            
            logger.info("Versioned processing completed successfully")
            
            return ProcessingResult(
                requestId = request.requestId,
                success = true,
                result = finalResult,
                version = processingVersion,
                processingSteps = processingSteps
            )
            
        } catch (e: Exception) {
            logger.error("Versioned processing failed: ${e.message}")
            
            return ProcessingResult(
                requestId = request.requestId,
                success = false,
                result = null,
                version = processingVersion,
                processingSteps = processingSteps + "Error: ${e.message}"
            )
        }
    }
    
    private fun performBasicProcessing(request: ProcessingRequest): String {
        val logger = Workflow.getLogger(this::class.java)
        logger.info("Performing basic processing for: ${request.requestId}")
        
        Workflow.sleep(Duration.ofSeconds(1))
        return "basic_processed_${request.requestId}"
    }
    
    private fun performSimpleValidation(request: ProcessingRequest) {
        val logger = Workflow.getLogger(this::class.java)
        logger.info("Performing simple validation for: ${request.requestId}")
        
        Workflow.sleep(Duration.ofMillis(500))
        
        if (request.data.isEmpty()) {
            throw IllegalArgumentException("Request data cannot be empty")
        }
    }
    
    private fun performEnhancedValidation(request: ProcessingRequest) {
        val logger = Workflow.getLogger(this::class.java)
        logger.info("Performing enhanced validation for: ${request.requestId}")
        
        Workflow.sleep(Duration.ofSeconds(1))
        
        // Enhanced validation includes more checks
        if (request.data.isEmpty()) {
            throw IllegalArgumentException("Request data cannot be empty")
        }
        
        if (request.requestId.isBlank()) {
            throw IllegalArgumentException("Request ID cannot be blank")
        }
        
        // Validate data structure
        val requiredFields = listOf("type", "source")
        requiredFields.forEach { field ->
            if (!request.data.containsKey(field)) {
                throw IllegalArgumentException("Missing required field: $field")
            }
        }
    }
    
    private fun performAdvancedProcessing(inputData: String): String {
        val logger = Workflow.getLogger(this::class.java)
        logger.info("Performing advanced processing on: $inputData")
        
        Workflow.sleep(Duration.ofSeconds(2))
        return "advanced_processed_$inputData"
    }
    
    private fun performDataEnrichment(inputData: String): String {
        val logger = Workflow.getLogger(this::class.java)
        logger.info("Performing data enrichment on: $inputData")
        
        Workflow.sleep(Duration.ofSeconds(1))
        return "enriched_$inputData"
    }
    
    private fun performResultEnhancement(request: ProcessingRequest) {
        val logger = Workflow.getLogger(this::class.java)
        logger.info("Performing result enhancement for: ${request.requestId}")
        
        Workflow.sleep(Duration.ofMillis(800))
        // Add metadata, analytics, etc.
    }
    
    private fun performUrgentProcessing(request: ProcessingRequest): String {
        val logger = Workflow.getLogger(this::class.java)
        logger.info("Performing urgent processing for: ${request.requestId}")
        
        Workflow.sleep(Duration.ofMillis(500)) // Faster processing
        return "urgent_result_${request.requestId}"
    }
    
    private fun performHighPriorityProcessing(request: ProcessingRequest): String {
        val logger = Workflow.getLogger(this::class.java)
        logger.info("Performing high priority processing for: ${request.requestId}")
        
        Workflow.sleep(Duration.ofSeconds(1))
        return "high_priority_result_${request.requestId}"
    }
    
    private fun performStandardProcessing(request: ProcessingRequest): String {
        val logger = Workflow.getLogger(this::class.java)
        logger.info("Performing standard processing for: ${request.requestId}")
        
        Workflow.sleep(Duration.ofSeconds(2))
        return "standard_result_${request.requestId}"
    }
}

/**
 * Example of a workflow with complex versioning scenarios.
 */
@WorkflowInterface
interface ComplexVersionedWorkflow {
    
    @WorkflowMethod
    fun processComplexVersioning(request: ComplexRequest): ComplexResult
}

class ComplexVersionedWorkflowImpl : ComplexVersionedWorkflow {
    
    companion object {
        const val MAIN_VERSION = "MainVersion"
        const val FEATURE_A_VERSION = "FeatureAVersion"
        const val FEATURE_B_VERSION = "FeatureBVersion"
        const val INTEGRATION_VERSION = "IntegrationVersion"
    }
    
    override fun processComplexVersioning(request: ComplexRequest): ComplexResult {
        val logger = Workflow.getLogger(this::class.java)
        
        // Main workflow version
        val mainVersion = Workflow.getVersion(MAIN_VERSION, 1, 3)
        
        when (mainVersion) {
            1 -> {
                // Original implementation
                return processVersionOne(request)
            }
            2 -> {
                // Added feature A
                val featureAVersion = Workflow.getVersion(FEATURE_A_VERSION, 1, 2)
                return processVersionTwo(request, featureAVersion)
            }
            3 -> {
                // Added feature B and integration changes
                val featureAVersion = Workflow.getVersion(FEATURE_A_VERSION, 1, 2)
                val featureBVersion = Workflow.getVersion(FEATURE_B_VERSION, 1, 1)
                val integrationVersion = Workflow.getVersion(INTEGRATION_VERSION, 1, 1)
                
                return processVersionThree(request, featureAVersion, featureBVersion, integrationVersion)
            }
            else -> {
                throw IllegalStateException("Unsupported version: $mainVersion")
            }
        }
    }
    
    private fun processVersionOne(request: ComplexRequest): ComplexResult {
        // Original simple processing
        return ComplexResult(
            requestId = request.requestId,
            version = 1,
            features = listOf("basic_processing"),
            result = "v1_result"
        )
    }
    
    private fun processVersionTwo(request: ComplexRequest, featureAVersion: Int): ComplexResult {
        val features = mutableListOf("basic_processing")
        
        // Feature A processing
        if (featureAVersion == 1) {
            // Simple feature A
            features.add("feature_a_simple")
        } else {
            // Enhanced feature A
            features.add("feature_a_enhanced")
        }
        
        return ComplexResult(
            requestId = request.requestId,
            version = 2,
            features = features,
            result = "v2_result"
        )
    }
    
    private fun processVersionThree(
        request: ComplexRequest,
        featureAVersion: Int,
        featureBVersion: Int,
        integrationVersion: Int
    ): ComplexResult {
        val features = mutableListOf("basic_processing")
        
        // Feature A (inherited from v2)
        if (featureAVersion == 1) {
            features.add("feature_a_simple")
        } else {
            features.add("feature_a_enhanced")
        }
        
        // Feature B (new in v3)
        features.add("feature_b")
        
        // Integration changes
        if (integrationVersion == 1) {
            features.add("new_integration")
        }
        
        return ComplexResult(
            requestId = request.requestId,
            version = 3,
            features = features,
            result = "v3_result"
        )
    }
}

// Data classes
data class ProcessingRequest(
    val requestId: String,
    val data: Map<String, Any>,
    val priority: ProcessingPriority = ProcessingPriority.NORMAL
)

data class ProcessingResult(
    val requestId: String,
    val success: Boolean,
    val result: String?,
    val version: Int,
    val processingSteps: List<String>
)

data class ComplexRequest(
    val requestId: String,
    val data: Map<String, Any>
)

data class ComplexResult(
    val requestId: String,
    val version: Int,
    val features: List<String>,
    val result: String
)

enum class ProcessingPriority {
    LOW, NORMAL, HIGH, URGENT
} 