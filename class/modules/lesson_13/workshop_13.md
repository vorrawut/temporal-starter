# Workshop 13: Versioning with Workflow.getVersion()

## What we want to build

Create a versioned workflow that demonstrates safe evolution and migration patterns using Temporal's `Workflow.getVersion()` API. This lesson shows how to handle breaking changes, additive changes, and complex versioning scenarios while maintaining backward compatibility for running workflows.

## Expecting Result

By the end of this lesson, you'll have:

- A workflow that safely evolves through multiple versions
- Understanding of when and how to use `Workflow.getVersion()`
- Knowledge of different change types (additive vs breaking)
- Best practices for workflow migration and rollback strategies

## Code Steps

### Step 1: Create the Basic Workflow Interface

Open `class/workshop/lesson_13/workflow/VersionedWorkflow.kt` and create the interface:

```kotlin
package com.temporal.bootcamp.lesson13.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface VersionedWorkflow {
    @WorkflowMethod
    fun processWithVersioning(request: ProcessingRequest): ProcessingResult
}
```

### Step 2: Implement Version 1 (Initial Implementation)

Create the basic workflow implementation:

```kotlin
class VersionedWorkflowImpl : VersionedWorkflow {
    
    companion object {
        const val PROCESSING_VERSION = "ProcessingVersion"
    }
    
    override fun processWithVersioning(request: ProcessingRequest): ProcessingResult {
        val logger = Workflow.getLogger(this::class.java)
        val processingSteps = mutableListOf<String>()
        
        // Get version - initially this will be version 1
        val version = Workflow.getVersion(PROCESSING_VERSION, 1, 1)
        
        // Version 1: Basic processing only
        val result = performBasicProcessing(request)
        processingSteps.add("Basic processing completed")
        
        return ProcessingResult(
            requestId = request.requestId,
            success = true,
            result = result,
            version = version,
            processingSteps = processingSteps
        )
    }
    
    private fun performBasicProcessing(request: ProcessingRequest): String {
        Workflow.sleep(Duration.ofSeconds(1))
        return "basic_processed_${request.requestId}"
    }
}
```

**Key points:**
- `Workflow.getVersion(changeId, minSupported, maxSupported)` manages versions
- `changeId` is a unique identifier for this versioning point
- `minSupported` is the oldest version still supported
- `maxSupported` is the newest version available

### Step 3: Add Version 2 (Additive Change)

Extend the workflow to add a validation step:

```kotlin
override fun processWithVersioning(request: ProcessingRequest): ProcessingResult {
    val logger = Workflow.getLogger(this::class.java)
    val processingSteps = mutableListOf<String>()
    
    // Update max version to 2
    val version = Workflow.getVersion(PROCESSING_VERSION, 1, 2)
    
    // Version 1: Basic processing (always executed)
    val result = performBasicProcessing(request)
    processingSteps.add("Basic processing completed")
    
    // Version 2+: Added validation (additive change)
    if (version >= 2) {
        performValidation(request)
        processingSteps.add("Validation completed")
    }
    
    return ProcessingResult(
        requestId = request.requestId,
        success = true,
        result = result,
        version = version,
        processingSteps = processingSteps
    )
}

private fun performValidation(request: ProcessingRequest) {
    if (request.data.isEmpty()) {
        throw IllegalArgumentException("Request data cannot be empty")
    }
}
```

**Key points:**
- Additive changes use `if (version >= X)` checks
- Existing workflows continue with old version until they complete
- New workflows automatically get the latest version

### Step 4: Add Version 3 (Breaking Change)

Implement a breaking change that affects processing order:

```kotlin
override fun processWithVersioning(request: ProcessingRequest): ProcessingResult {
    val logger = Workflow.getLogger(this::class.java)
    val processingSteps = mutableListOf<String>()
    
    // Update max version to 3
    val version = Workflow.getVersion(PROCESSING_VERSION, 1, 3)
    
    // Version 1: Basic processing
    val basicResult = performBasicProcessing(request)
    processingSteps.add("Basic processing completed")
    
    // Version 2+: Validation
    if (version >= 2) {
        performValidation(request)
        processingSteps.add("Validation completed")
    }
    
    // Version 3: Breaking change - different processing order
    val orderingVersion = Workflow.getVersion("OrderingVersion", 1, 2)
    
    if (orderingVersion == 1) {
        // Old order: process then enrich
        val processed = performAdvancedProcessing(basicResult)
        val enriched = performDataEnrichment(processed)
        processingSteps.add("Legacy order: process → enrich")
    } else {
        // New order: enrich then process
        val enriched = performDataEnrichment(basicResult)
        val processed = performAdvancedProcessing(enriched)
        processingSteps.add("New order: enrich → process")
    }
    
    return ProcessingResult(
        requestId = request.requestId,
        success = true,
        result = "final_result",
        version = version,
        processingSteps = processingSteps
    )
}
```

**Key points:**
- Breaking changes require separate version identifiers
- Use exact version checks (`== 1` vs `== 2`) for breaking changes
- Both old and new code paths must be maintained

### Step 5: Handle Multiple Versioning Points

Add complex versioning with multiple change points:

```kotlin
override fun processWithVersioning(request: ProcessingRequest): ProcessingResult {
    val processingSteps = mutableListOf<String>()
    
    // Main processing version
    val processingVersion = Workflow.getVersion(PROCESSING_VERSION, 1, 4)
    
    // Basic processing (always executed)
    val basicResult = performBasicProcessing(request)
    processingSteps.add("Basic processing completed")
    
    // Validation versioning (nested versioning)
    if (processingVersion >= 2) {
        val validationVersion = Workflow.getVersion("ValidationVersion", 1, 2)
        
        if (validationVersion == 1) {
            performSimpleValidation(request)
            processingSteps.add("Simple validation")
        } else {
            performEnhancedValidation(request)
            processingSteps.add("Enhanced validation")
        }
    }
    
    // Processing order versioning
    if (processingVersion >= 3) {
        val orderingVersion = Workflow.getVersion("OrderingVersion", 1, 2)
        // ... handle different orders
    }
    
    // New feature versioning
    if (processingVersion >= 4) {
        performNewFeature(request)
        processingSteps.add("New feature completed")
    }
    
    return ProcessingResult(/* ... */)
}
```

### Step 6: Implement Version Cleanup

When you're confident old versions are no longer needed:

```kotlin
// After ensuring no workflows are running old versions:
// Update minSupported version to remove old code paths

val version = Workflow.getVersion(PROCESSING_VERSION, 2, 4) // Dropped support for v1

// Remove old version 1 code paths
if (version >= 2) {
    // Validation is now always executed
    performValidation(request)
}

// Version 3+ processing (version 2 code removed)
if (version >= 3) {
    // Only new processing order supported
    val enriched = performDataEnrichment(basicResult)
    val processed = performAdvancedProcessing(enriched)
}
```

## How to Run

### 1. Deploy Version 1
```kotlin
// Deploy workflow with version 1
val workflow = workflowClient.newWorkflowStub(
    VersionedWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("versioned-queue")
        .setWorkflowId("versioned-${System.currentTimeMillis()}")
        .build()
)

val request = ProcessingRequest(
    requestId = "REQ-001",
    data = mapOf("type" to "test", "source" to "workshop")
)

val result = workflow.processWithVersioning(request)
// Output: version=1, steps=["Basic processing completed"]
```

### 2. Deploy Version 2 (Additive)
```kotlin
// Update code to support version 2, redeploy
// New workflows get version 2, old workflows continue with version 1

val result = workflow.processWithVersioning(request)
// New execution: version=2, steps=["Basic processing completed", "Validation completed"]
```

### 3. Deploy Version 3 (Breaking Change)
```kotlin
// Update code to support version 3, redeploy
// Existing workflows maintain their version, new workflows get version 3

val result = workflow.processWithVersioning(request)
// New execution: version=3 with new processing order
```

### 4. Monitor Version Usage
```kotlin
// Query running workflows to see version distribution
val runningWorkflows = workflowService.listWorkflows()
runningWorkflows.forEach { workflow ->
    val stub = workflowClient.newWorkflowStub(VersionedWorkflow::class.java, workflow.workflowId)
    // Check version through queries or workflow completion
}
```

## What You've Learned

- ✅ How to use `Workflow.getVersion()` for safe workflow evolution
- ✅ Handling additive changes vs breaking changes
- ✅ Managing multiple versioning points in complex workflows
- ✅ Version cleanup strategies and migration planning
- ✅ Best practices for backward compatibility
- ✅ How to safely deploy workflow changes to production

Workflow versioning ensures you can evolve your business logic while maintaining reliability! 