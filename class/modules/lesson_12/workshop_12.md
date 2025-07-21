# Workshop 12: Child Workflows & continueAsNew

## What we want to build

Create a comprehensive order processing system that demonstrates parent-child workflow patterns and the `continueAsNew` mechanism. The parent workflow will coordinate multiple child workflows (payment, inventory, shipping) and show how to use `continueAsNew` to handle long-running processes without accumulating excessive history.

## Expecting Result

By the end of this lesson, you'll have:

- A parent workflow that coordinates multiple child workflows
- Understanding of child workflow lifecycle and error handling
- Implementation of the `continueAsNew` pattern for long-running processes
- Knowledge of when and how to use workflow hierarchies

## Code Steps

### Step 1: Create Child Workflow Interfaces

Open `class/workshop/lesson_12/workflow/OrderProcessingWorkflow.kt` and define the child workflows:

```kotlin
package com.temporal.bootcamp.lesson12.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface PaymentWorkflow {
    @WorkflowMethod
    fun processPayment(paymentInfo: PaymentInfo): PaymentResult
}

@WorkflowInterface
interface InventoryWorkflow {
    @WorkflowMethod
    fun reserveInventory(items: List<OrderItem>): InventoryResult
}

@WorkflowInterface
interface ShippingWorkflow {
    @WorkflowMethod
    fun arrangeShipping(address: ShippingAddress, items: List<OrderItem>): ShippingResult
}
```

**Key points:**
- Each child workflow has a single responsibility
- Child workflows can be developed and tested independently
- Child workflows return structured result objects

### Step 2: Create the Parent Workflow Interface

Define the parent workflow that coordinates the children:

```kotlin
@WorkflowInterface
interface OrderProcessingWorkflow {
    @WorkflowMethod
    fun processOrder(orderRequest: OrderRequest): OrderResult
}
```

### Step 3: Implement Parent Workflow with Child Coordination

Create the parent workflow implementation:

```kotlin
class OrderProcessingWorkflowImpl : OrderProcessingWorkflow {
    
    override fun processOrder(orderRequest: OrderRequest): OrderResult {
        val logger = Workflow.getLogger(this::class.java)
        
        // Create child workflow stubs
        val paymentWorkflow = createPaymentWorkflow(orderRequest.orderId)
        val inventoryWorkflow = createInventoryWorkflow(orderRequest.orderId)
        val shippingWorkflow = createShippingWorkflow(orderRequest.orderId)
        
        try {
            // Execute child workflows in sequence
            val paymentResult = paymentWorkflow.processPayment(orderRequest.paymentInfo)
            
            if (!paymentResult.success) {
                return OrderResult(/* failed result */)
            }
            
            val inventoryResult = inventoryWorkflow.reserveInventory(orderRequest.items)
            val shippingResult = shippingWorkflow.arrangeShipping(
                orderRequest.shippingAddress,
                orderRequest.items
            )
            
            // Return combined results
            return OrderResult(/* combined results */)
            
        } catch (e: Exception) {
            // Handle child workflow failures
            return OrderResult(/* error result */)
        }
    }
}
```

### Step 4: Create Child Workflow Stubs

Add helper methods to create child workflow stubs:

```kotlin
private fun createPaymentWorkflow(orderId: String): PaymentWorkflow {
    return Workflow.newChildWorkflowStub(
        PaymentWorkflow::class.java,
        ChildWorkflowOptions.newBuilder()
            .setWorkflowId("payment-$orderId")
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setMaximumAttempts(3)
                    .build()
            )
            .build()
    )
}

private fun createInventoryWorkflow(orderId: String): InventoryWorkflow {
    return Workflow.newChildWorkflowStub(
        InventoryWorkflow::class.java,
        ChildWorkflowOptions.newBuilder()
            .setWorkflowId("inventory-$orderId")
            .build()
    )
}

private fun createShippingWorkflow(orderId: String): ShippingWorkflow {
    return Workflow.newChildWorkflowStub(
        ShippingWorkflow::class.java,
        ChildWorkflowOptions.newBuilder()
            .setWorkflowId("shipping-$orderId")
            .build()
    )
}
```

**Key points:**
- Child workflows get unique IDs derived from parent context
- Each child can have different retry policies
- Child workflow options configure behavior and lifecycle

### Step 5: Implement Child Workflows

Create implementations for each child workflow:

```kotlin
class PaymentWorkflowImpl : PaymentWorkflow {
    override fun processPayment(paymentInfo: PaymentInfo): PaymentResult {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Processing payment: ${paymentInfo.method}")
        
        // Simulate payment processing
        Workflow.sleep(Duration.ofSeconds(2))
        
        return PaymentResult(
            success = true,
            transactionId = "txn_${System.currentTimeMillis()}",
            errorMessage = null
        )
    }
}

class InventoryWorkflowImpl : InventoryWorkflow {
    override fun reserveInventory(items: List<OrderItem>): InventoryResult {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Reserving inventory for ${items.size} items")
        
        val reservations = items.map { item ->
            InventoryReservation(
                productId = item.productId,
                quantity = item.quantity,
                reservationId = "res_${item.productId}_${System.currentTimeMillis()}"
            )
        }
        
        return InventoryResult(
            success = true,
            reservations = reservations,
            errorMessage = null
        )
    }
}
```

### Step 6: Implement continueAsNew Pattern

Create a long-running workflow that uses `continueAsNew`:

```kotlin
@WorkflowInterface
interface LongRunningOrderWorkflow {
    @WorkflowMethod
    fun processOrdersWithContinueAsNew(batchNumber: Int, processedCount: Int): ProcessingResult
}

class LongRunningOrderWorkflowImpl : LongRunningOrderWorkflow {
    
    companion object {
        const val MAX_ORDERS_PER_WORKFLOW = 100
    }
    
    override fun processOrdersWithContinueAsNew(batchNumber: Int, processedCount: Int): ProcessingResult {
        val logger = Workflow.getLogger(this::class.java)
        
        var currentProcessedCount = processedCount
        
        // Process orders in this batch
        for (i in 1..MAX_ORDERS_PER_WORKFLOW) {
            processOrder("order_${batchNumber}_$i")
            currentProcessedCount++
            Workflow.sleep(Duration.ofMillis(100))
        }
        
        // Check if we should continue
        if (shouldContinueProcessing(currentProcessedCount)) {
            logger.info("Continuing with next batch using continueAsNew")
            
            // Use continueAsNew to start fresh
            Workflow.continueAsNew(batchNumber + 1, currentProcessedCount)
        }
        
        return ProcessingResult(
            batchNumber = batchNumber,
            totalProcessed = currentProcessedCount,
            completed = true
        )
    }
    
    private fun shouldContinueProcessing(processedCount: Int): Boolean {
        return processedCount < 1000 // Process up to 1000 orders total
    }
}
```

**Key points:**
- `continueAsNew` resets workflow history to prevent bloat
- Pass state as parameters to the new workflow execution
- Use for long-running or recurring workflows

## How to Run

### 1. Start Parent Workflow
```kotlin
val workflow = workflowClient.newWorkflowStub(
    OrderProcessingWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("order-processing-queue")
        .setWorkflowId("order-${System.currentTimeMillis()}")
        .build()
)

val orderRequest = OrderRequest(
    orderId = "ORD-12345",
    customerId = "CUST-67890",
    items = listOf(
        OrderItem("PROD-1", 2, BigDecimal("29.99")),
        OrderItem("PROD-2", 1, BigDecimal("99.99"))
    ),
    paymentInfo = PaymentInfo("CREDIT_CARD", BigDecimal("159.97")),
    shippingAddress = ShippingAddress("123 Main St", "Anytown", "12345")
)

val result = workflow.processOrder(orderRequest)
```

### 2. Start Long-Running Workflow with continueAsNew
```kotlin
val longRunningWorkflow = workflowClient.newWorkflowStub(
    LongRunningOrderWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("long-running-queue")
        .setWorkflowId("batch-processor")
        .build()
)

// Start with batch 1, 0 processed orders
val result = longRunningWorkflow.processOrdersWithContinueAsNew(1, 0)
```

### 3. Expected Output
```
Parent workflow: order-ORD-12345 started
Child workflow: payment-ORD-12345 processing payment
Child workflow: inventory-ORD-12345 reserving inventory
Child workflow: shipping-ORD-12345 arranging shipping
Order processing completed successfully

Batch processor: Starting batch 1
Batch processor: Completed batch 1, continuing with batch 2
Batch processor: Using continueAsNew for batch 2
```

## What You've Learned

- ✅ How to coordinate multiple child workflows from a parent
- ✅ Child workflow configuration and lifecycle management
- ✅ Error handling and compensation in workflow hierarchies
- ✅ Using `continueAsNew` to prevent workflow history bloat
- ✅ When to use child workflows vs activities
- ✅ Best practices for workflow orchestration patterns

Child workflows and `continueAsNew` are powerful patterns for building scalable, maintainable workflow systems! 