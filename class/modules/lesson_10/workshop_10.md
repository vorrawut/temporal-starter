---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Workshop 10: Signals

## Building Interactive Workflow Systems

*Create workflows that can receive and respond to external signals while running*

---

# What we want to build

Create **workflows that can receive and respond to external signals** while running. 

This enables **interactive workflows** that can change behavior based on external events.

---

# Expecting Result

## By the end of this workshop, you'll have:

- ✅ **Long-running workflows** that listen for signals
- ✅ **Signal handlers** that modify workflow behavior
- ✅ **Querying workflow state** from external systems
- ✅ **Interactive approval workflows**

---

# Code Steps

## Step 1: Define Workflow with Signals

```kotlin
@WorkflowInterface
interface ApprovalWorkflow {
    
    @WorkflowMethod
    fun processApprovalRequest(request: ApprovalRequest): ApprovalResult
    
    @SignalMethod
    fun approve(approverComment: String)
    
    @SignalMethod
    fun reject(rejectionReason: String)
    
    @QueryMethod
    fun getStatus(): ApprovalStatus
    
    @QueryMethod
    fun getPendingApprovers(): List<String>
}
```

**Key concepts: `@SignalMethod` for external events, `@QueryMethod` for state reading**

---

# Step 2: Implement Signal Handling

```kotlin
class ApprovalWorkflowImpl : ApprovalWorkflow {
    
    private var status = ApprovalStatus.PENDING
    private var approverComment: String? = null
    private var rejectionReason: String? = null
    private val pendingApprovers = mutableListOf<String>()
    
    override fun processApprovalRequest(request: ApprovalRequest): ApprovalResult {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Starting approval process for: ${request.id}")
        
        // Initialize approvers
        pendingApprovers.addAll(request.requiredApprovers)
        
        // Send notification to approvers
        notificationActivity.notifyApprovers(request.requiredApprovers, request)
        // Continued on next slide...
```

---

# Signal Waiting Pattern

```kotlin
        // Wait for approval or timeout
        val approvalDeadline = Workflow.newTimer(Duration.ofDays(7))
        
        // Use Workflow.await to wait for signals or timeout
        Workflow.await(
            Duration.ofDays(7)
        ) {
            status != ApprovalStatus.PENDING
        }
        
        return when (status) {
            ApprovalStatus.APPROVED -> {
                logger.info("Request approved with comment: $approverComment")
                
                // Execute approved workflow
                val executionResult = executionActivity.executeApprovedRequest(request)
                
                ApprovalResult.approved(
                    requestId = request.id,
                    approverComment = approverComment!!,
                    executionResult = executionResult
                )
            }
            // Continued on next slide...
```

---

# Complete Status Handling

```kotlin
            ApprovalStatus.REJECTED -> {
                logger.info("Request rejected: $rejectionReason")
                
                // Clean up any resources
                cleanupActivity.cleanupRejectedRequest(request)
                
                ApprovalResult.rejected(
                    requestId = request.id,
                    rejectionReason = rejectionReason!!
                )
            }
            
            ApprovalStatus.PENDING -> {
                logger.warn("Approval request timed out")
                
                // Auto-reject due to timeout
                cleanupActivity.cleanupTimedOutRequest(request)
                
                ApprovalResult.timedOut(
                    requestId = request.id,
                    timeoutDays = 7
                )
            }
        }
    }
    // Continued on next slide...
```

---

# Signal Handlers Implementation

```kotlin
    override fun approve(approverComment: String) {
        val logger = Workflow.getLogger(this::class.java)
        logger.info("Approval signal received with comment: $approverComment")
        
        this.approverComment = approverComment
        this.status = ApprovalStatus.APPROVED
    }
    
    override fun reject(rejectionReason: String) {
        val logger = Workflow.getLogger(this::class.java)
        logger.info("Rejection signal received: $rejectionReason")
        
        this.rejectionReason = rejectionReason
        this.status = ApprovalStatus.REJECTED
    }
    
    override fun getStatus(): ApprovalStatus = status
    
    override fun getPendingApprovers(): List<String> = pendingApprovers.toList()
}
```

**Signal handlers update workflow state and trigger condition checks**

---

# Step 3: Send Signals from External System

```kotlin
// Start the approval workflow
val workflow = workflowClient.newWorkflowStub(
    ApprovalWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("approval-queue")
        .setWorkflowId("approval-${request.id}")
        .build()
)

// Start workflow asynchronously
val workflowExecution = WorkflowClient.start(workflow::processApprovalRequest, request)

// Later, send approval signal
val workflowStub = workflowClient.newWorkflowStub(
    ApprovalWorkflow::class.java,
    workflowExecution.workflowId
)

workflowStub.approve("Looks good to me! Approved by John Doe")

// Query current status
val currentStatus = workflowStub.getStatus()
println("Current approval status: $currentStatus")
```

---

# Step 4: Multiple Signal Handlers

```kotlin
class OrderTrackingWorkflowImpl : OrderTrackingWorkflow {
    
    private var orderStatus = OrderStatus.PROCESSING
    private val statusHistory = mutableListOf<StatusUpdate>()
    
    override fun trackOrder(orderId: String): OrderTrackingResult {
        // Wait for various signals during order lifecycle
        
        Workflow.await { orderStatus == OrderStatus.COMPLETED || orderStatus == OrderStatus.CANCELLED }
        
        return OrderTrackingResult(
            orderId = orderId,
            finalStatus = orderStatus,
            statusHistory = statusHistory.toList()
        )
    }
    
    @SignalMethod
    fun updateShippingStatus(trackingUpdate: TrackingUpdate) {
        statusHistory.add(StatusUpdate.shipping(trackingUpdate))
        if (trackingUpdate.isDelivered) {
            orderStatus = OrderStatus.COMPLETED
        }
    }
    // Continued on next slide...
```

---

# More Signal Handlers

```kotlin
    @SignalMethod
    fun reportIssue(issue: OrderIssue) {
        statusHistory.add(StatusUpdate.issue(issue))
        if (issue.isCritical) {
            orderStatus = OrderStatus.CANCELLED
        }
    }
    
    @SignalMethod
    fun customerUpdate(customerAction: CustomerAction) {
        statusHistory.add(StatusUpdate.customer(customerAction))
        if (customerAction.type == CustomerActionType.CANCEL) {
            orderStatus = OrderStatus.CANCELLED
        }
    }
}
```

**Multiple signal handlers enable rich interaction patterns**

---

# How to Run

## Start workflow and send signals:

```kotlin
// Start the workflow
val approvalRequest = ApprovalRequest(
    id = "req-123",
    description = "Deploy new feature to production",
    requiredApprovers = listOf("manager@company.com", "tech-lead@company.com")
)

val workflowId = "approval-${approvalRequest.id}"
val workflow = workflowClient.newWorkflowStub(
    ApprovalWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("approval-queue")
        .setWorkflowId(workflowId)
        .build()
)

// Start async
WorkflowClient.start(workflow::processApprovalRequest, approvalRequest)

// Query status
val status = workflow.getStatus()
println("Status: $status")

// Send approval
workflow.approve("Deployment approved after security review")
```

---

# Signal vs Query Patterns

## **Signals (Write Operations):**
- ✅ **Modify workflow state** asynchronously
- ✅ **Trigger workflow logic** changes
- ✅ **Persisted in workflow history** for replay
- ✅ **Enable external interaction** with running workflows

## **Queries (Read Operations):**
- ✅ **Read current state** synchronously
- ✅ **No side effects** on workflow execution
- ✅ **Fast response** without persistence
- ✅ **Real-time monitoring** and observability

---

# 💡 Key Takeaways

## **What You've Learned:**

- ✅ **Signals enable interactive workflows** that respond to external events
- ✅ **Workflow.await()** blocks until conditions are met
- ✅ **Signal handlers** update state and trigger logic changes
- ✅ **Queries provide** real-time state visibility
- ✅ **Asynchronous workflow execution** with external interaction

---

# 🚀 Next Steps

**You now understand building interactive workflow systems!**

## **Lesson 11 will cover:**
- Advanced query patterns and optimization
- Real-time workflow observability
- Query design best practices
- Performance considerations

**Ready to master workflow queries? Let's continue! 🎉** 