# Concept 13: Versioning with Workflow.getVersion()

## Objective

Master safe workflow evolution patterns using Temporal's versioning system. Learn how to handle breaking changes, manage multiple versions, and implement migration strategies that ensure reliability during workflow updates.

## Key Concepts

### 1. **Why Workflow Versioning Matters**

#### **The Problem Without Versioning**
```kotlin
// Original workflow implementation
class OrderWorkflowImpl : OrderWorkflow {
    override fun processOrder(order: Order): OrderResult {
        validateOrder(order)           // Step 1
        processPayment(order)          // Step 2
        arrangeShipping(order)         // Step 3
        return OrderResult.success()
    }
}

// Updated implementation (DANGEROUS without versioning!)
class OrderWorkflowImpl : OrderWorkflow {
    override fun processOrder(order: Order): OrderResult {
        validateOrder(order)           // Step 1
        checkInventory(order)          // NEW Step 2 (inserted!)
        processPayment(order)          // Step 3 (was Step 2)
        arrangeShipping(order)         // Step 4 (was Step 3)
        return OrderResult.success()
    }
}

// Problem: Running workflows expect payment at step 2,
// but now inventory check is at step 2!
// This will cause workflow history mismatch and failures.
```

#### **The Solution: Workflow Versioning**
```kotlin
class OrderWorkflowImpl : OrderWorkflow {
    override fun processOrder(order: Order): OrderResult {
        val version = Workflow.getVersion("OrderProcessingVersion", 1, 2)
        
        validateOrder(order)
        
        if (version >= 2) {
            checkInventory(order)      // Only in version 2+
        }
        
        processPayment(order)
        arrangeShipping(order)
        return OrderResult.success()
    }
}
```

### 2. **Workflow.getVersion() Deep Dive**

#### **API Signature and Behavior**
```kotlin
fun getVersion(changeId: String, minSupported: Int, maxSupported: Int): Int

// Example usage:
val version = Workflow.getVersion("PaymentVersion", 1, 3)

// Parameters:
// changeId: Unique identifier for this versioning point
// minSupported: Oldest version still supported (cleanup boundary)
// maxSupported: Newest version available (current implementation)

// Returns:
// - For new workflows: maxSupported (3 in example)
// - For existing workflows: their recorded version (1, 2, or 3)
```

#### **Version Recording and Replay**
```kotlin
class VersioningExampleWorkflowImpl : VersioningExampleWorkflow {
    
    override fun processWithVersions(): String {
        val logger = Workflow.getLogger(this::class.java)
        
        // First execution: Records version 2 in workflow history
        val version = Workflow.getVersion("ExampleVersion", 1, 2)
        logger.info("Using version: $version") // Logs: "Using version: 2"
        
        if (version == 1) {
            return "Version 1 logic"
        } else {
            return "Version 2 logic"
        }
        
        // On replay: Always returns 2 (from history)
        // Even if code now supports version 3!
    }
}
```

### 3. **Change Type Patterns**

#### **Additive Changes**
```kotlin
class AdditiveChangesWorkflowImpl : AdditiveChangesWorkflow {
    
    override fun processWithAdditiveChanges(request: Request): Result {
        val version = Workflow.getVersion("AdditiveVersion", 1, 4)
        
        // Always executed (backward compatible)
        val coreResult = performCoreProcessing(request)
        
        // Version 2+: Added optional validation
        if (version >= 2) {
            performValidation(request)
        }
        
        // Version 3+: Added optional enrichment
        if (version >= 3) {
            performDataEnrichment(request)
        }
        
        // Version 4+: Added optional analytics
        if (version >= 4) {
            recordAnalytics(request)
        }
        
        return Result.success(coreResult)
    }
}
```

#### **Breaking Changes**
```kotlin
class BreakingChangesWorkflowImpl : BreakingChangesWorkflow {
    
    override fun processWithBreakingChanges(request: Request): Result {
        val version = Workflow.getVersion("BreakingVersion", 1, 3)
        
        return when (version) {
            1 -> {
                // Original implementation
                val result = legacyProcessing(request)
                Result.fromLegacy(result)
            }
            
            2 -> {
                // Restructured processing (breaking change)
                val preprocessed = preprocess(request)
                val result = newProcessing(preprocessed)
                Result.fromNew(result)
            }
            
            3 -> {
                // Another breaking change: async processing
                val future = Async.function { asyncProcessing(request) }
                val result = future.get()
                Result.fromAsync(result)
            }
            
            else -> throw IllegalStateException("Unsupported version: $version")
        }
    }
}
```

#### **Mixed Changes (Complex Scenarios)**
```kotlin
class ComplexVersioningWorkflowImpl : ComplexVersioningWorkflow {
    
    companion object {
        const val MAIN_VERSION = "MainVersion"
        const val PROCESSING_VERSION = "ProcessingVersion"
        const val VALIDATION_VERSION = "ValidationVersion"
        const val OUTPUT_VERSION = "OutputVersion"
    }
    
    override fun processComplex(request: ComplexRequest): ComplexResult {
        // Main workflow evolution
        val mainVersion = Workflow.getVersion(MAIN_VERSION, 1, 3)
        
        // Core processing (with its own versioning)
        val processingVersion = Workflow.getVersion(PROCESSING_VERSION, 1, 2)
        val processedData = if (processingVersion == 1) {
            simpleProcessing(request)
        } else {
            advancedProcessing(request)
        }
        
        // Validation evolution (additive)
        if (mainVersion >= 2) {
            val validationVersion = Workflow.getVersion(VALIDATION_VERSION, 1, 2)
            
            if (validationVersion == 1) {
                basicValidation(processedData)
            } else {
                comprehensiveValidation(processedData)
            }
        }
        
        // Output format evolution (breaking)
        val outputVersion = Workflow.getVersion(OUTPUT_VERSION, 1, 2)
        
        return if (outputVersion == 1) {
            ComplexResult.legacyFormat(processedData)
        } else {
            ComplexResult.newFormat(processedData)
        }
    }
}
```

### 4. **Version Management Strategies**

#### **Progressive Deployment Strategy**
```kotlin
// Phase 1: Introduce new version support
val version = Workflow.getVersion("FeatureVersion", 1, 2)

if (version == 1) {
    // Keep existing logic
    return legacyProcessing()
} else {
    // Add new logic
    return newProcessing()
}

// Phase 2: Monitor version distribution
// Wait for old workflows to complete or migrate

// Phase 3: Remove old version support
val version = Workflow.getVersion("FeatureVersion", 2, 2) // Dropped v1 support

// Now only new logic exists
return newProcessing()
```

#### **Feature Flag Integration**
```kotlin
class FeatureFlagVersioningWorkflowImpl : FeatureFlagVersioningWorkflow {
    
    override fun processWithFeatureFlags(request: Request): Result {
        // Combine versioning with feature flags
        val version = Workflow.getVersion("FeatureVersion", 1, 2)
        
        if (version == 1) {
            return legacyProcessing(request)
        }
        
        // Version 2: Check feature flags for gradual rollout
        if (isFeatureEnabled("NEW_PROCESSING_ALGORITHM", request.customerId)) {
            return newProcessing(request)
        } else {
            return legacyProcessing(request)
        }
    }
    
    private fun isFeatureEnabled(feature: String, customerId: String): Boolean {
        // Deterministic feature flag based on customer ID
        // This ensures same customer always gets same experience
        val hash = customerId.hashCode()
        return (hash % 100) < getFeatureRolloutPercentage(feature)
    }
}
```

### 5. **Migration Patterns**

#### **Data Migration Pattern**
```kotlin
class DataMigrationWorkflowImpl : DataMigrationWorkflow {
    
    override fun processWithDataMigration(request: DataRequest): DataResult {
        val version = Workflow.getVersion("DataMigrationVersion", 1, 2)
        
        val data = if (version == 1) {
            // Load data in old format
            loadLegacyData(request.dataId)
        } else {
            // Try new format first, fallback to migration
            try {
                loadNewFormatData(request.dataId)
            } catch (e: DataNotFoundException) {
                // Migrate data on-demand
                val legacyData = loadLegacyData(request.dataId)
                val migratedData = migrateToNewFormat(legacyData)
                saveNewFormatData(migratedData)
                migratedData
            }
        }
        
        return processData(data)
    }
}
```

#### **Service Migration Pattern**
```kotlin
class ServiceMigrationWorkflowImpl : ServiceMigrationWorkflow {
    
    override fun processWithServiceMigration(request: ServiceRequest): ServiceResult {
        val version = Workflow.getVersion("ServiceMigrationVersion", 1, 3)
        
        return when (version) {
            1 -> {
                // Use old service
                val result = oldServiceActivity.process(request)
                ServiceResult.fromOldService(result)
            }
            
            2 -> {
                // Dual-write pattern: use new service, verify with old
                val newResult = newServiceActivity.process(request)
                
                // Verify consistency (but don't fail on mismatch)
                try {
                    val oldResult = oldServiceActivity.process(request)
                    if (!resultsMatch(newResult, oldResult)) {
                        logger.warn("Service migration: results don't match for ${request.id}")
                    }
                } catch (e: Exception) {
                    logger.warn("Old service verification failed: ${e.message}")
                }
                
                ServiceResult.fromNewService(newResult)
            }
            
            3 -> {
                // Use only new service
                val result = newServiceActivity.process(request)
                ServiceResult.fromNewService(result)
            }
            
            else -> throw IllegalStateException("Unsupported version: $version")
        }
    }
}
```

### 6. **Advanced Versioning Patterns**

#### **Conditional Versioning**
```kotlin
class ConditionalVersioningWorkflowImpl : ConditionalVersioningWorkflow {
    
    override fun processConditionally(request: ConditionalRequest): ConditionalResult {
        // Different versioning based on request characteristics
        val version = if (request.isLegacyCustomer) {
            // Legacy customers: keep on old version longer
            Workflow.getVersion("ConditionalVersion", 1, 1)
        } else {
            // New customers: get latest version
            Workflow.getVersion("ConditionalVersion", 1, 3)
        }
        
        return when (version) {
            1 -> processLegacy(request)
            2 -> processStandard(request)
            3 -> processAdvanced(request)
            else -> throw IllegalStateException("Unsupported version: $version")
        }
    }
}
```

#### **Nested Workflow Versioning**
```kotlin
class NestedVersioningWorkflowImpl : NestedVersioningWorkflow {
    
    override fun processNested(request: NestedRequest): NestedResult {
        val mainVersion = Workflow.getVersion("MainVersion", 1, 2)
        
        if (mainVersion == 1) {
            // Version 1: Simple processing
            return processSimple(request)
        }
        
        // Version 2: Complex processing with child workflows
        val childVersion = Workflow.getVersion("ChildVersion", 1, 2)
        
        val childWorkflow = Workflow.newChildWorkflowStub(
            ProcessingChildWorkflow::class.java,
            ChildWorkflowOptions.newBuilder()
                .setWorkflowId("child-${request.id}")
                .build()
        )
        
        val childRequest = if (childVersion == 1) {
            ChildRequest.legacyFormat(request)
        } else {
            ChildRequest.newFormat(request)
        }
        
        val childResult = childWorkflow.processChild(childRequest)
        return NestedResult.fromChild(childResult)
    }
}
```

## Best Practices

### ✅ Version Management

1. **Use Meaningful Change IDs**
   ```kotlin
   // Good: Descriptive change IDs
   val version = Workflow.getVersion("PaymentProviderMigration", 1, 2)
   val version = Workflow.getVersion("OrderValidationEnhancement", 1, 3)
   
   // Bad: Generic change IDs
   val version = Workflow.getVersion("version", 1, 2)
   val version = Workflow.getVersion("v1", 1, 2)
   ```

2. **Plan Version Lifecycle**
   ```kotlin
   // Plan: v1 → v2 → v3 → cleanup v1 → cleanup v2
   
   // Phase 1: Support v1-v2
   val version = Workflow.getVersion("ProcessingVersion", 1, 2)
   
   // Phase 2: Support v1-v3
   val version = Workflow.getVersion("ProcessingVersion", 1, 3)
   
   // Phase 3: Drop v1, support v2-v3
   val version = Workflow.getVersion("ProcessingVersion", 2, 3)
   
   // Phase 4: Drop v2, support only v3
   val version = Workflow.getVersion("ProcessingVersion", 3, 3)
   ```

3. **Monitor Version Distribution**
   ```kotlin
   // Add metrics to track version usage
   override fun processVersioned(request: Request): Result {
       val version = Workflow.getVersion("ProcessingVersion", 1, 3)
       
       // Record version metrics
       metricsActivity.recordVersionUsage("ProcessingVersion", version)
       
       // Process based on version
       return when (version) {
           1 -> processV1(request)
           2 -> processV2(request)
           3 -> processV3(request)
           else -> throw IllegalStateException("Unsupported version: $version")
       }
   }
   ```

### ✅ Change Management

1. **Additive Changes First**
   ```kotlin
   // Prefer additive changes when possible
   if (version >= 2) {
       performAdditionalValidation() // Safe addition
   }
   
   // Avoid breaking changes unless necessary
   if (version == 1) {
       processOldWay()
   } else {
       processNewWay() // Breaking change - requires careful management
   }
   ```

2. **Test All Version Paths**
   ```kotlin
   @Test
   fun testAllVersions() {
       // Test version 1 behavior
       testWithVersion(1) { processVersioned(request) }
       
       // Test version 2 behavior
       testWithVersion(2) { processVersioned(request) }
       
       // Test version 3 behavior
       testWithVersion(3) { processVersioned(request) }
   }
   ```

### ❌ Common Mistakes

1. **Changing minSupported Too Quickly**
   ```kotlin
   // Bad: Dropping support too quickly
   val version = Workflow.getVersion("Version", 3, 3) // Dropped v1,v2 immediately!
   
   // Good: Gradual deprecation
   val version = Workflow.getVersion("Version", 1, 3) // Still support all versions
   // ... wait for old workflows to complete ...
   val version = Workflow.getVersion("Version", 2, 3) // Drop v1 support
   // ... wait more ...
   val version = Workflow.getVersion("Version", 3, 3) // Drop v2 support
   ```

2. **Inconsistent Version Logic**
   ```kotlin
   // Bad: Inconsistent version handling
   if (version >= 2) {
       doSomething()
   }
   if (version == 3) { // Should be >= 3 for consistency
       doSomethingElse()
   }
   
   // Good: Consistent version logic
   if (version >= 2) {
       doSomething()
   }
   if (version >= 3) {
       doSomethingElse()
   }
   ```

---

**Next**: Lesson 14 will explore timers and cron workflows for time-based processing! 