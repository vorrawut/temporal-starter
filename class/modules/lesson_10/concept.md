---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Signals

## Lesson 10: Interactive Workflow Patterns

Master interactive workflow patterns using Temporal signals and queries to build responsive, long-running workflows that can react to external events and provide real-time status information.

---

# Objective

By the end of this lesson, you will understand:

- ✅ **Signals vs Queries** - when to use each pattern
- ✅ **Signal handling patterns** for event-driven workflows
- ✅ **Interactive approval workflows** with external decision makers
- ✅ **Long-running workflow patterns** with external events
- ✅ **State management** in signal-driven workflows
- ✅ **Best practices** for responsive workflow design

---

# 1. **Signals vs Queries**

## **Signals: Changing Workflow State**

```kotlin
@WorkflowInterface
interface OrderTrackingWorkflow {
    @WorkflowMethod
    fun trackOrder(orderId: String): OrderResult
    
    // Signals modify workflow state
    @SignalMethod
    fun updateShippingStatus(update: ShippingUpdate)
    
    @SignalMethod
    fun processCustomerRequest(request: CustomerRequest)
    
    @SignalMethod
    fun handleException(exception: OrderException)
}
```

**Signals enable external systems to send events to running workflows**

---

# Queries: Reading Workflow State

```kotlin
@WorkflowInterface
interface OrderTrackingWorkflow {
    // Queries read current state without modification
    @QueryMethod
    fun getCurrentStatus(): OrderStatus
    
    @QueryMethod
    fun getShippingHistory(): List<ShippingEvent>
    
    @QueryMethod
    fun getEstimatedDelivery(): LocalDateTime?
    
    @QueryMethod
    fun getCustomerRequests(): List<CustomerRequest>
}
```

**Queries provide real-time visibility into workflow state without affecting execution**

---

# Key Differences

| Aspect | **Signals** | **Queries** |
|--------|-------------|-------------|
| **Purpose** | Modify workflow state | Read workflow state |
| **Durability** | Persisted in workflow history | Not persisted |
| **Timing** | Asynchronous | Synchronous |
| **Replay** | Executed during replay | Not executed during replay |
| **Side Effects** | Can trigger activities | Read-only operations |

**Choose signals for state changes, queries for state inspection**

---

# 2. **Signal Handling Patterns**

## **Event-Driven State Machine**

```kotlin
class ApprovalWorkflowImpl : ApprovalWorkflow {
    
    private var state = ApprovalState.PENDING
    private var approvals = mutableListOf<Approval>()
    private var rejections = mutableListOf<Rejection>()
    private val requiredApprovers = mutableSetOf<String>()
    
    override fun processApprovalRequest(request: ApprovalRequest): ApprovalResult {
        val logger = Workflow.getLogger(this::class.java)
        
        // Initialize state
        requiredApprovers.addAll(request.requiredApprovers)
        
        // Send notifications
        notificationActivity.notifyApprovers(request)
        
        // Wait for signals or timeout
        val approved = Workflow.await(Duration.ofDays(7)) {
            when (state) {
                ApprovalState.APPROVED -> true
                ApprovalState.REJECTED -> true
                ApprovalState.PENDING -> false
            }
        }
        // Continued on next slide...
```

---

# Approval State Handling

```kotlin
        return when (state) {
            ApprovalState.APPROVED -> {
                val executionResult = executionActivity.executeRequest(request)
                ApprovalResult.approved(request.id, approvals, executionResult)
            }
            ApprovalState.REJECTED -> {
                cleanupActivity.cleanup(request.id)
                ApprovalResult.rejected(request.id, rejections)
            }
            ApprovalState.PENDING -> {
                cleanupActivity.cleanup(request.id)
                ApprovalResult.timedOut(request.id)
            }
        }
    }
    
    @SignalMethod
    override fun approve(approverEmail: String, comment: String) {
        if (state != ApprovalState.PENDING) return
        
        val approval = Approval(approverEmail, comment, Instant.now())
        approvals.add(approval)
        requiredApprovers.remove(approverEmail)
        
        // Check if all required approvals received
        if (requiredApprovers.isEmpty()) {
            state = ApprovalState.APPROVED
        }
    }
    // Continued on next slide...
```

---

# Signal Handler Implementation

```kotlin
    @SignalMethod
    override fun reject(approverEmail: String, reason: String) {
        if (state != ApprovalState.PENDING) return
        
        val rejection = Rejection(approverEmail, reason, Instant.now())
        rejections.add(rejection)
        state = ApprovalState.REJECTED
    }
}
```

## **Key Signal Patterns:**
- ✅ **State validation** in signal handlers
- ✅ **Conditional logic** based on current state
- ✅ **Collection management** for tracking multiple events
- ✅ **State transitions** triggered by signals

---

# Signal Buffering and Ordering

```kotlin
class OrderProcessingWorkflowImpl : OrderProcessingWorkflow {
    
    private val pendingUpdates = mutableListOf<StatusUpdate>()
    private var isProcessingUpdates = false
    
    override fun processOrder(orderId: String): OrderResult {
        // Main workflow logic
        val result = executeOrderProcessing(orderId)
        
        // Process any buffered updates
        processPendingUpdates()
        
        return result
    }
    
    @SignalMethod
    override fun updateOrderStatus(update: StatusUpdate) {
        // Buffer updates if workflow is busy
        pendingUpdates.add(update)
        
        // Trigger processing if not already running
        if (!isProcessingUpdates) {
            isProcessingUpdates = true
            processUpdatesAsync()
        }
    }
    // Continued on next slide...
```

---

# Async Signal Processing

```kotlin
    private fun processUpdatesAsync() {
        // Process updates in a separate workflow "thread"
        Async.procedure {
            while (pendingUpdates.isNotEmpty()) {
                val update = pendingUpdates.removeFirst()
                processStatusUpdate(update)
            }
            isProcessingUpdates = false
        }
    }
}
```

## **Signal Processing Patterns:**
- ✅ **Buffering** for high-frequency signals
- ✅ **Async processing** to avoid blocking main workflow
- ✅ **Ordering guarantees** for dependent signals
- ✅ **State consistency** during concurrent updates

---

# 💡 Key Takeaways

## **What You've Learned:**

- ✅ **Signals enable interactive workflows** that respond to external events
- ✅ **Queries provide real-time observability** without affecting execution
- ✅ **Event-driven state machines** handle complex approval flows
- ✅ **Signal buffering** manages high-frequency updates
- ✅ **Workflow.await()** blocks until conditions are met

---

# 🚀 Next Steps

**You now understand building interactive workflow systems!**

## **Lesson 11 will cover:**
- Advanced query patterns and optimization
- Real-time workflow observability
- Query design best practices
- Performance considerations for large-scale systems

**Ready to master workflow queries? Let's continue! 🎉** 