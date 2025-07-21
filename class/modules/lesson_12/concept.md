# Concept 12: Child Workflows & continueAsNew

## Objective

Master advanced workflow orchestration patterns using child workflows for hierarchical decomposition and `continueAsNew` for managing long-running processes. Learn when and how to use these patterns to build scalable, maintainable workflow systems.

## Key Concepts

### 1. **Child Workflows vs Activities**

#### **Decision Matrix**
| Aspect | Child Workflows | Activities |
|--------|----------------|------------|
| **Purpose** | Complex business logic | External operations |
| **Durability** | Full workflow semantics | Simple execution model |
| **Visibility** | Own workflow execution | Part of parent execution |
| **Scalability** | Independent scaling | Scales with parent |
| **Complexity** | Can have own signals/queries | Single operation |
| **Testing** | Can be tested independently | Tested with parent |
| **Monitoring** | Separate workflow metrics | Part of parent metrics |

#### **When to Use Child Workflows**
```kotlin
// Good use cases for child workflows:

// 1. Complex multi-step processes
@WorkflowInterface
interface PaymentProcessingWorkflow {
    @WorkflowMethod
    fun processPayment(payment: PaymentRequest): PaymentResult
    
    @SignalMethod
    fun updatePaymentMethod(newMethod: PaymentMethod)
    
    @QueryMethod
    fun getPaymentStatus(): PaymentStatus
}

// 2. Independent business domains
@WorkflowInterface
interface InventoryManagementWorkflow {
    @WorkflowMethod
    fun manageInventory(request: InventoryRequest): InventoryResult
}

// 3. Long-running sub-processes
@WorkflowInterface
interface ShippingTrackingWorkflow {
    @WorkflowMethod
    fun trackShipment(shipment: ShipmentInfo): TrackingResult
}
```

#### **When to Use Activities Instead**
```kotlin
// Good use cases for activities:

// 1. Simple external calls
@ActivityInterface
interface EmailService {
    @ActivityMethod
    fun sendEmail(email: EmailMessage): Boolean
}

// 2. Database operations
@ActivityInterface
interface OrderRepository {
    @ActivityMethod
    fun saveOrder(order: Order): String
}

// 3. API calls
@ActivityInterface
interface PaymentGateway {
    @ActivityMethod
    fun chargeCard(cardInfo: CardInfo, amount: BigDecimal): ChargeResult
}
```

### 2. **Child Workflow Patterns**

#### **Sequential Child Workflows**
```kotlin
class OrderProcessingWorkflowImpl : OrderProcessingWorkflow {
    
    override fun processOrder(request: OrderRequest): OrderResult {
        // Execute child workflows in sequence
        val paymentResult = paymentWorkflow.processPayment(request.payment)
        
        if (!paymentResult.success) {
            return OrderResult.failed("Payment failed")
        }
        
        val inventoryResult = inventoryWorkflow.reserveItems(request.items)
        
        if (!inventoryResult.success) {
            // Compensate payment
            compensationWorkflow.refundPayment(paymentResult.transactionId)
            return OrderResult.failed("Inventory unavailable")
        }
        
        val shippingResult = shippingWorkflow.arrangeShipping(request.address)
        
        return OrderResult.success(paymentResult, inventoryResult, shippingResult)
    }
}
```

#### **Parallel Child Workflows**
```kotlin
class ParallelProcessingWorkflowImpl : ParallelProcessingWorkflow {
    
    override fun processOrderInParallel(request: OrderRequest): OrderResult {
        // Start child workflows in parallel using Async
        val paymentFuture = Async.function { paymentWorkflow.processPayment(request.payment) }
        val inventoryFuture = Async.function { inventoryWorkflow.checkAvailability(request.items) }
        val shippingFuture = Async.function { shippingWorkflow.calculateShipping(request.address) }
        
        // Wait for all to complete
        val paymentResult = paymentFuture.get()
        val inventoryResult = inventoryFuture.get()
        val shippingResult = shippingFuture.get()
        
        // Proceed with sequential steps based on parallel results
        if (paymentResult.success && inventoryResult.available) {
            val finalShipping = shippingWorkflow.arrangeShipping(request.address)
            return OrderResult.success(paymentResult, inventoryResult, finalShipping)
        }
        
        return OrderResult.failed("Parallel validation failed")
    }
}
```

#### **Dynamic Child Workflow Creation**
```kotlin
class DynamicOrderWorkflowImpl : DynamicOrderWorkflow {
    
    override fun processOrderDynamically(request: OrderRequest): OrderResult {
        val results = mutableListOf<ProcessingResult>()
        
        // Create child workflows dynamically based on order characteristics
        request.items.forEach { item ->
            when (item.category) {
                ItemCategory.DIGITAL -> {
                    val digitalWorkflow = createDigitalFulfillmentWorkflow(item)
                    results.add(digitalWorkflow.processDigitalItem(item))
                }
                ItemCategory.PHYSICAL -> {
                    val physicalWorkflow = createPhysicalFulfillmentWorkflow(item)
                    results.add(physicalWorkflow.processPhysicalItem(item))
                }
                ItemCategory.SUBSCRIPTION -> {
                    val subscriptionWorkflow = createSubscriptionWorkflow(item)
                    results.add(subscriptionWorkflow.setupSubscription(item))
                }
            }
        }
        
        return OrderResult.fromResults(results)
    }
    
    private fun createDigitalFulfillmentWorkflow(item: OrderItem): DigitalFulfillmentWorkflow {
        return Workflow.newChildWorkflowStub(
            DigitalFulfillmentWorkflow::class.java,
            ChildWorkflowOptions.newBuilder()
                .setWorkflowId("digital-${item.id}")
                .build()
        )
    }
}
```

### 3. **continueAsNew Deep Dive**

#### **Why Use continueAsNew?**
```kotlin
// Problem: Workflow history grows indefinitely
class BadLongRunningWorkflowImpl : BadLongRunningWorkflow {
    
    override fun processForever(): String {
        var count = 0
        
        while (true) { // This will eventually fail!
            // Process items
            count++
            Workflow.sleep(Duration.ofMinutes(1))
            
            // History keeps growing: 1MB, 10MB, 100MB, 1GB...
            // Eventually hits history size limits and fails
        }
        
        return "Never reaches here"
    }
}

// Solution: Use continueAsNew to reset history
class GoodLongRunningWorkflowImpl : GoodLongRunningWorkflow {
    
    companion object {
        const val MAX_ITERATIONS_PER_EXECUTION = 1000
    }
    
    override fun processWithContinueAsNew(iterationCount: Int): String {
        var currentCount = iterationCount
        
        repeat(MAX_ITERATIONS_PER_EXECUTION) {
            // Process items
            currentCount++
            Workflow.sleep(Duration.ofMinutes(1))
        }
        
        // Check if we should continue
        if (shouldContinueProcessing(currentCount)) {
            // Reset history and continue with new execution
            Workflow.continueAsNew(currentCount)
        }
        
        return "Completed processing $currentCount iterations"
    }
}
```

#### **continueAsNew Patterns**

**Batch Processing Pattern**
```kotlin
class BatchProcessorWorkflowImpl : BatchProcessorWorkflow {
    
    override fun processBatch(batchId: String, offset: Int, batchSize: Int): BatchResult {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Processing batch $batchId starting at offset $offset")
        
        val items = fetchItemsActivity.fetchItems(offset, batchSize)
        
        if (items.isEmpty()) {
            return BatchResult.completed(offset)
        }
        
        // Process items in this batch
        val results = items.map { item ->
            processItemActivity.processItem(item)
        }
        
        val newOffset = offset + items.size
        
        // Continue with next batch if there are more items
        if (items.size == batchSize) {
            logger.info("Continuing with next batch at offset $newOffset")
            Workflow.continueAsNew(batchId, newOffset, batchSize)
        }
        
        return BatchResult.success(results, newOffset)
    }
}
```

**State Machine Pattern**
```kotlin
class StateMachineWorkflowImpl : StateMachineWorkflow {
    
    override fun runStateMachine(currentState: ProcessingState, context: ProcessingContext): StateMachineResult {
        val logger = Workflow.getLogger(this::class.java)
        
        when (currentState) {
            ProcessingState.INIT -> {
                val newContext = initializationActivity.initialize(context)
                Workflow.continueAsNew(ProcessingState.PROCESSING, newContext)
            }
            
            ProcessingState.PROCESSING -> {
                val result = processingActivity.process(context)
                
                val nextState = if (result.hasMoreWork) {
                    ProcessingState.PROCESSING
                } else {
                    ProcessingState.FINALIZING
                }
                
                Workflow.continueAsNew(nextState, result.updatedContext)
            }
            
            ProcessingState.FINALIZING -> {
                val finalResult = finalizationActivity.finalize(context)
                return StateMachineResult.completed(finalResult)
            }
        }
        
        // This should never be reached due to continueAsNew calls
        throw IllegalStateException("Invalid state: $currentState")
    }
}
```

**Cron-like Pattern**
```kotlin
class CronWorkflowImpl : CronWorkflow {
    
    override fun runCronJob(lastRunTime: Instant?, config: CronConfig): CronResult {
        val now = Workflow.currentTimeMillis()
        val currentTime = Instant.ofEpochMilli(now)
        
        // Execute the job
        val jobResult = cronJobActivity.executeJob(config)
        
        // Calculate next run time
        val nextRunTime = calculateNextRunTime(currentTime, config.schedule)
        
        // Sleep until next run time
        val sleepDuration = Duration.between(currentTime, nextRunTime)
        if (sleepDuration.isPositive) {
            Workflow.sleep(sleepDuration)
        }
        
        // Continue with next execution
        Workflow.continueAsNew(currentTime, config)
        
        // This return is never reached due to continueAsNew
        return CronResult.completed(jobResult)
    }
}
```

### 4. **Child Workflow Configuration**

#### **Comprehensive Child Workflow Options**
```kotlin
private fun createAdvancedChildWorkflow(): AdvancedChildWorkflow {
    return Workflow.newChildWorkflowStub(
        AdvancedChildWorkflow::class.java,
        ChildWorkflowOptions.newBuilder()
            // Unique workflow ID
            .setWorkflowId("child-${UUID.randomUUID()}")
            
            // Task queue (can be different from parent)
            .setTaskQueue("specialized-task-queue")
            
            // Execution timeout
            .setWorkflowExecutionTimeout(Duration.ofHours(2))
            
            // Run timeout (includes retries)
            .setWorkflowRunTimeout(Duration.ofMinutes(30))
            
            // Task timeout
            .setWorkflowTaskTimeout(Duration.ofMinutes(1))
            
            // Retry policy
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(10))
                    .setMaximumInterval(Duration.ofMinutes(5))
                    .setBackoffCoefficient(2.0)
                    .setMaximumAttempts(5)
                    .build()
            )
            
            // Parent close policy
            .setParentClosePolicy(ParentClosePolicy.ABANDON)
            
            // Workflow ID reuse policy
            .setWorkflowIdReusePolicy(WorkflowIdReusePolicy.ALLOW_DUPLICATE)
            
            .build()
    )
}
```

#### **Parent Close Policies**
```kotlin
enum class ParentClosePolicy {
    // Child continues running after parent completes
    ABANDON,
    
    // Child is cancelled when parent completes
    REQUEST_CANCEL,
    
    // Child is terminated when parent completes
    TERMINATE
}

// Usage examples:
class ParentWorkflowImpl : ParentWorkflow {
    
    // Fire-and-forget child workflow
    private fun createAbandonedChild(): FireAndForgetWorkflow {
        return Workflow.newChildWorkflowStub(
            FireAndForgetWorkflow::class.java,
            ChildWorkflowOptions.newBuilder()
                .setParentClosePolicy(ParentClosePolicy.ABANDON)
                .build()
        )
    }
    
    // Child that should be cancelled with parent
    private fun createCancellableChild(): CancellableWorkflow {
        return Workflow.newChildWorkflowStub(
            CancellableWorkflow::class.java,
            ChildWorkflowOptions.newBuilder()
                .setParentClosePolicy(ParentClosePolicy.REQUEST_CANCEL)
                .build()
        )
    }
}
```

## Best Practices

### ✅ Child Workflow Design

1. **Single Responsibility**
   ```kotlin
   // Good: Focused child workflow
   @WorkflowInterface
   interface PaymentProcessingWorkflow {
       @WorkflowMethod
       fun processPayment(payment: PaymentRequest): PaymentResult
   }
   
   // Bad: Multiple responsibilities
   @WorkflowInterface
   interface OrderManagementWorkflow {
       @WorkflowMethod
       fun processEverything(order: Order): OrderResult // Too broad!
   }
   ```

2. **Clear Input/Output Contracts**
   ```kotlin
   data class PaymentRequest(
       val orderId: String,
       val amount: BigDecimal,
       val paymentMethod: PaymentMethod,
       val customerId: String
   )
   
   data class PaymentResult(
       val success: Boolean,
       val transactionId: String?,
       val errorCode: String?,
       val errorMessage: String?
   )
   ```

3. **Appropriate Task Queue Assignment**
   ```kotlin
   // Separate task queues for different child types
   private fun createPaymentWorkflow(): PaymentWorkflow {
       return Workflow.newChildWorkflowStub(
           PaymentWorkflow::class.java,
           ChildWorkflowOptions.newBuilder()
               .setTaskQueue("payment-processing-queue") // Specialized queue
               .build()
       )
   }
   ```

### ✅ continueAsNew Best Practices

1. **Regular State Checkpointing**
   ```kotlin
   override fun processLongRunning(checkpoint: ProcessingCheckpoint): ProcessingResult {
       var currentCheckpoint = checkpoint
       
       repeat(BATCH_SIZE) {
           // Process item
           currentCheckpoint = processNextItem(currentCheckpoint)
           
           // Update progress
           if (currentCheckpoint.itemsProcessed % 100 == 0) {
               logger.info("Processed ${currentCheckpoint.itemsProcessed} items")
           }
       }
       
       // Continue with updated checkpoint
       if (hasMoreWork(currentCheckpoint)) {
           Workflow.continueAsNew(currentCheckpoint)
       }
       
       return ProcessingResult.completed(currentCheckpoint)
   }
   ```

2. **Avoid Large State Objects**
   ```kotlin
   // Good: Minimal state in continueAsNew
   data class ProcessingCheckpoint(
       val batchNumber: Int,
       val itemsProcessed: Int,
       val lastProcessedId: String
   )
   
   // Bad: Large state object
   data class BadCheckpoint(
       val batchNumber: Int,
       val itemsProcessed: Int,
       val allProcessedItems: List<ProcessedItem>, // Don't pass large collections!
       val detailedHistory: Map<String, ProcessingDetail>
   )
   ```

### ❌ Common Mistakes

1. **Not Using continueAsNew for Long-Running Workflows**
   ```kotlin
   // Bad: Infinite loop without continueAsNew
   override fun processForever(): String {
       while (true) {
           processItems() // History grows forever!
           Workflow.sleep(Duration.ofHours(1))
       }
   }
   
   // Good: Use continueAsNew
   override fun processWithReset(iteration: Int): String {
       processItems()
       
       if (iteration < MAX_ITERATIONS) {
           Workflow.continueAsNew(iteration + 1)
       }
       
       return "Completed"
   }
   ```

2. **Overusing Child Workflows**
   ```kotlin
   // Bad: Child workflow for simple operation
   val childWorkflow = Workflow.newChildWorkflowStub(
       SimpleCalculationWorkflow::class.java
   )
   val result = childWorkflow.add(2, 3) // Use activity instead!
   
   // Good: Activity for simple operation
   val result = calculationActivity.add(2, 3)
   ```

---

**Next**: Lesson 13 will explore workflow versioning and safe migration strategies! 