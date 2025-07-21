# Concept 10: Signals

## Objective

Master interactive workflow patterns using Temporal signals and queries to build responsive, long-running workflows that can react to external events and provide real-time status information.

## Key Concepts

### 1. **Signals vs Queries**

#### **Signals: Changing Workflow State**
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

#### **Queries: Reading Workflow State**
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

#### **Key Differences**
| Aspect | Signals | Queries |
|--------|---------|---------|
| **Purpose** | Modify workflow state | Read workflow state |
| **Durability** | Persisted in workflow history | Not persisted |
| **Timing** | Asynchronous | Synchronous |
| **Replay** | Executed during replay | Not executed during replay |
| **Side Effects** | Can trigger activities | Read-only operations |

### 2. **Signal Handling Patterns**

#### **Event-Driven State Machine**
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
    
    @SignalMethod
    override fun reject(approverEmail: String, reason: String) {
        if (state != ApprovalState.PENDING) return
        
        val rejection = Rejection(approverEmail, reason, Instant.now())
        rejections.add(rejection)
        state = ApprovalState.REJECTED
    }
}
```

#### **Signal Buffering and Ordering**
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

### 3. **Long-Running Workflow Patterns**

#### **Persistent Session Pattern**
```kotlin
class CustomerSupportSessionWorkflowImpl : CustomerSupportSessionWorkflow {
    
    private var sessionState = SessionState.ACTIVE
    private val messages = mutableListOf<Message>()
    private var assignedAgent: String? = null
    private var lastActivity = Instant.now()
    
    override fun startSession(customerId: String): SessionResult {
        val logger = Workflow.getLogger(this::class.java)
        
        // Initialize session
        val sessionId = generateSessionId()
        logger.info("Starting support session: $sessionId for customer: $customerId")
        
        // Wait for messages, agent assignment, or timeout
        while (sessionState == SessionState.ACTIVE) {
            val hasActivity = Workflow.await(Duration.ofMinutes(30)) {
                val now = Workflow.currentTimeMillis()
                now - lastActivity.toEpochMilli() < Duration.ofMinutes(5).toMillis()
            }
            
            if (!hasActivity) {
                // Session timeout due to inactivity
                sessionState = SessionState.TIMED_OUT
                cleanupActivity.archiveSession(sessionId)
                break
            }
        }
        
        return SessionResult(
            sessionId = sessionId,
            finalState = sessionState,
            messageCount = messages.size,
            duration = Duration.between(Instant.now(), lastActivity)
        )
    }
    
    @SignalMethod
    override fun sendMessage(message: Message) {
        messages.add(message)
        lastActivity = Instant.now()
        
        // Process message asynchronously
        Async.procedure {
            messageProcessingActivity.processMessage(message)
        }
    }
    
    @SignalMethod
    override fun assignAgent(agentId: String) {
        assignedAgent = agentId
        lastActivity = Instant.now()
        
        // Notify agent assignment
        Async.procedure {
            notificationActivity.notifyAgentAssignment(agentId, messages)
        }
    }
    
    @SignalMethod
    override fun closeSession(reason: SessionCloseReason) {
        sessionState = SessionState.CLOSED
        
        // Perform cleanup
        Async.procedure {
            cleanupActivity.finalizeSession(generateSessionSummary(), reason)
        }
    }
}
```

### 4. **Signal-Based Coordination**

#### **Multi-Workflow Coordination**
```kotlin
class OrderCoordinatorWorkflowImpl : OrderCoordinatorWorkflow {
    
    private val subWorkflowStates = mutableMapOf<String, WorkflowState>()
    private val completedWorkflows = mutableSetOf<String>()
    
    override fun coordinateOrder(order: OrderRequest): CoordinationResult {
        val logger = Workflow.getLogger(this::class.java)
        
        // Start sub-workflows
        val paymentWorkflowId = startPaymentWorkflow(order)
        val inventoryWorkflowId = startInventoryWorkflow(order)
        val shippingWorkflowId = startShippingWorkflow(order)
        
        subWorkflowStates[paymentWorkflowId] = WorkflowState.RUNNING
        subWorkflowStates[inventoryWorkflowId] = WorkflowState.RUNNING
        subWorkflowStates[shippingWorkflowId] = WorkflowState.RUNNING
        
        // Wait for all workflows to complete or any to fail
        val allCompleted = Workflow.await(Duration.ofMinutes(30)) {
            completedWorkflows.size == 3 || 
            subWorkflowStates.values.any { it == WorkflowState.FAILED }
        }
        
        return if (allCompleted && completedWorkflows.size == 3) {
            CoordinationResult.success(order.orderId, collectResults())
        } else {
            // Handle partial completion
            cancelIncompleteWorkflows()
            CoordinationResult.failed(order.orderId, getFailureReasons())
        }
    }
    
    @SignalMethod
    override fun reportWorkflowCompletion(workflowId: String, result: WorkflowResult) {
        subWorkflowStates[workflowId] = if (result.success) {
            WorkflowState.COMPLETED
        } else {
            WorkflowState.FAILED
        }
        
        if (result.success) {
            completedWorkflows.add(workflowId)
        }
    }
}
```

### 5. **Query Optimization**

#### **Efficient State Queries**
```kotlin
class OrderTrackingWorkflowImpl : OrderTrackingWorkflow {
    
    // Cached computed state for efficient queries
    private var cachedStatus: OrderStatus? = null
    private var statusLastComputed: Instant? = null
    private val statusCacheTTL = Duration.ofSeconds(30)
    
    @QueryMethod
    override fun getCurrentStatus(): OrderStatus {
        val now = Workflow.currentTimeMillis()
        
        // Return cached status if still valid
        if (cachedStatus != null && statusLastComputed != null) {
            val cacheAge = Duration.ofMillis(now - statusLastComputed!!.toEpochMilli())
            if (cacheAge < statusCacheTTL) {
                return cachedStatus!!
            }
        }
        
        // Recompute status
        cachedStatus = computeCurrentStatus()
        statusLastComputed = Instant.ofEpochMilli(now)
        
        return cachedStatus!!
    }
    
    @QueryMethod
    override fun getDetailedProgress(): ProgressDetails {
        return ProgressDetails(
            orderId = orderId,
            currentStep = currentStep,
            completedSteps = completedSteps.toList(),
            remainingSteps = remainingSteps.toList(),
            estimatedCompletion = estimateCompletion(),
            lastUpdate = lastUpdateTime
        )
    }
    
    private fun computeCurrentStatus(): OrderStatus {
        return when {
            completedSteps.contains("shipped") -> OrderStatus.SHIPPED
            completedSteps.contains("payment") -> OrderStatus.PROCESSING
            completedSteps.contains("validation") -> OrderStatus.VALIDATED
            else -> OrderStatus.PENDING
        }
    }
}
```

### 6. **External Integration Patterns**

#### **REST API Integration**
```kotlin
@RestController
@RequestMapping("/api/workflows")
class WorkflowController(
    private val workflowClient: WorkflowClient
) {
    
    @PostMapping("/approval/{workflowId}/approve")
    fun approveRequest(
        @PathVariable workflowId: String,
        @RequestBody approvalRequest: ApprovalRequestDto
    ): ResponseEntity<ApiResponse> {
        
        return try {
            val workflow = workflowClient.newWorkflowStub(
                ApprovalWorkflow::class.java,
                workflowId
            )
            
            workflow.approve(approvalRequest.approverEmail, approvalRequest.comment)
            
            ResponseEntity.ok(ApiResponse.success("Approval recorded"))
            
        } catch (e: WorkflowNotFoundException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to process approval: ${e.message}"))
        }
    }
    
    @GetMapping("/order/{workflowId}/status")
    fun getOrderStatus(@PathVariable workflowId: String): ResponseEntity<OrderStatusDto> {
        
        return try {
            val workflow = workflowClient.newWorkflowStub(
                OrderTrackingWorkflow::class.java,
                workflowId
            )
            
            val status = workflow.getCurrentStatus()
            val progress = workflow.getDetailedProgress()
            
            val statusDto = OrderStatusDto.from(status, progress)
            ResponseEntity.ok(statusDto)
            
        } catch (e: WorkflowNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
}
```

## Best Practices

### ✅ Signal Design

1. **Make Signals Idempotent**
   ```kotlin
   @SignalMethod
   override fun updateInventory(update: InventoryUpdate) {
       // Check if update already processed
       if (processedUpdates.contains(update.updateId)) {
           return // Safe to call multiple times
       }
       
       processedUpdates.add(update.updateId)
       applyInventoryUpdate(update)
   }
   ```

2. **Validate Signal Parameters**
   ```kotlin
   @SignalMethod
   override fun approve(approverEmail: String, comment: String) {
       require(approverEmail.isNotBlank()) { "Approver email required" }
       require(comment.isNotBlank()) { "Approval comment required" }
       require(state == ApprovalState.PENDING) { "Cannot approve non-pending request" }
       
       processApproval(approverEmail, comment)
   }
   ```

3. **Use Structured Signal Data**
   ```kotlin
   data class StatusUpdate(
       val updateId: String,
       val timestamp: Instant,
       val updateType: UpdateType,
       val data: Map<String, Any>,
       val source: String
   )
   
   @SignalMethod
   override fun updateStatus(update: StatusUpdate) {
       // Process structured update
   }
   ```

### ✅ Query Design

1. **Cache Expensive Computations**
   ```kotlin
   private var expensiveResultCache: ExpensiveResult? = null
   private var cacheTimestamp: Long = 0
   
   @QueryMethod
   override fun getExpensiveData(): ExpensiveResult {
       val now = Workflow.currentTimeMillis()
       if (expensiveResultCache == null || (now - cacheTimestamp) > CACHE_TTL_MS) {
           expensiveResultCache = computeExpensiveResult()
           cacheTimestamp = now
       }
       return expensiveResultCache!!
   }
   ```

2. **Return Immutable Objects**
   ```kotlin
   @QueryMethod
   override fun getProcessingSteps(): List<ProcessingStep> {
       return processingSteps.toList() // Return copy, not mutable original
   }
   ```

### ❌ Common Mistakes

1. **Modifying State in Queries**
   ```kotlin
   // Bad: Query modifying state
   @QueryMethod
   override fun getAndMarkAsRead(): List<Message> {
       markAllAsRead() // Don't do this!
       return messages
   }
   
   // Good: Query only reads
   @QueryMethod
   override fun getUnreadMessages(): List<Message> {
       return messages.filter { !it.isRead }
   }
   ```

2. **Ignoring Signal Ordering**
   ```kotlin
   // Bad: Not handling out-of-order signals
   @SignalMethod
   override fun updateStep(stepNumber: Int, status: String) {
       stepStatuses[stepNumber] = status // Could overwrite newer status!
   }
   
   // Good: Check timestamps/versions
   @SignalMethod
   override fun updateStep(update: StepUpdate) {
       val current = stepStatuses[update.stepNumber]
       if (current == null || update.timestamp > current.timestamp) {
           stepStatuses[update.stepNumber] = update
       }
   }
   ```

---

**Congratulations!** You've completed the intermediate-to-advanced Temporal bootcamp. You're now ready to build production-grade, resilient distributed systems with Temporal! 