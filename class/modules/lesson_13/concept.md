---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Versioning with Workflow.getVersion()

## Lesson 13: Safe Workflow Evolution

Master safe workflow evolution patterns using Temporal's versioning system. Learn how to handle breaking changes, manage multiple versions, and implement migration strategies.

---

# Objective

By the end of this lesson, you will understand:

- ✅ **Why workflow versioning matters** for production systems
- ✅ **`Workflow.getVersion()` API** and its behavior
- ✅ **Change type patterns** - additive vs breaking changes
- ✅ **Version migration strategies** for safe evolution
- ✅ **Best practices** for versioning workflows
- ✅ **Production deployment** patterns with versioning

---

# 1. **Why Workflow Versioning Matters**

## **The Problem Without Versioning**

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

---

# The Solution: Workflow Versioning

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

**Versioning allows safe evolution of workflow logic without breaking running instances**

---

# 2. **Workflow.getVersion() Deep Dive**

## **API Signature and Behavior**

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

---

# Version Recording and Replay

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

**Versions are permanently recorded in workflow history for deterministic replay**

---

# 3. **Change Type Patterns**

## **Additive Changes (Safe)**

```kotlin
class AdditiveChangesWorkflowImpl : AdditiveChangesWorkflow {
    
    override fun processWithAdditiveChanges(request: ProcessingRequest): ProcessingResult {
        val version = Workflow.getVersion("ProcessingVersion", 1, 3)
        
        // Version 1: Basic processing (always executed)
        val basicResult = performBasicProcessing(request)
        
        // Version 2+: Added validation (additive)
        if (version >= 2) {
            performValidation(request)
        }
        
        // Version 3+: Added audit logging (additive)
        if (version >= 3) {
            performAuditLogging(request, basicResult)
        }
        
        return ProcessingResult.success(basicResult)
    }
}
```

**Additive changes extend functionality without affecting existing logic**

---

# Breaking Changes (Requires Careful Handling)

```kotlin
class BreakingChangesWorkflowImpl : BreakingChangesWorkflow {
    
    override fun processWithBreakingChanges(request: ProcessingRequest): ProcessingResult {
        val version = Workflow.getVersion("BreakingVersion", 1, 2)
        
        if (version == 1) {
            // Version 1: Old implementation
            return performOldProcessing(request)
        } else {
            // Version 2+: Completely different implementation
            return performNewProcessing(request)
        }
    }
    
    private fun performOldProcessing(request: ProcessingRequest): ProcessingResult {
        // Old business logic that must be preserved for running workflows
        val result = legacyProcessingActivity.processOldWay(request)
        return ProcessingResult.fromLegacy(result)
    }
    
    private fun performNewProcessing(request: ProcessingRequest): ProcessingResult {
        // New business logic for new workflows
        val result = modernProcessingActivity.processNewWay(request)
        return ProcessingResult.fromModern(result)
    }
}
```

---

# 💡 Key Takeaways

## **What You've Learned:**

- ✅ **Workflow versioning** prevents history mismatch errors
- ✅ **`Workflow.getVersion()`** provides deterministic version management
- ✅ **Additive changes** are safer than breaking changes
- ✅ **Version recording** ensures consistent replay behavior
- ✅ **Migration strategies** enable safe workflow evolution

---

# 🚀 Next Steps

**You now understand safe workflow evolution!**

## **Lesson 14 will cover:**
- Timers and cron workflows
- Time-based workflow patterns
- Scheduled recurring executions
- Production scheduling strategies

**Ready to master time-based workflows? Let's continue! 🎉** 