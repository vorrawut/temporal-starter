# Workshop 11: Queries

## What we want to build

Create an order tracking workflow that demonstrates how to implement and use query handlers to fetch real-time state information from running workflows. This workflow will process an order through multiple stages while allowing external systems to query its current status and processing history.

## Expecting Result

By the end of this lesson, you'll have:

- A workflow that maintains queryable state throughout its execution
- Multiple query methods for different types of information
- Understanding of when and how to use queries vs signals
- Real-time visibility into workflow progress without interrupting execution

## Code Steps

### Step 1: Create the Workflow Interface with Query Methods

Open `class/workshop/lesson_11/workflow/OrderTrackingWorkflow.kt` and create the interface:

```kotlin
package com.temporal.bootcamp.lesson11.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import io.temporal.workflow.QueryMethod

@WorkflowInterface
interface OrderTrackingWorkflow {
    
    @WorkflowMethod
    fun trackOrder(orderId: String): OrderTrackingResult
    
    @QueryMethod
    fun getCurrentStatus(): OrderStatus
    
    @QueryMethod
    fun getOrderDetails(): OrderDetails
    
    @QueryMethod
    fun getProcessingHistory(): List<ProcessingEvent>
}
```

**Key points:**
- `@QueryMethod` annotation marks methods that can be called to read workflow state
- Query methods should be read-only and not modify workflow state
- Multiple query methods allow different views of the same workflow

### Step 2: Implement the Workflow with Queryable State

Create the workflow implementation:

```kotlin
class OrderTrackingWorkflowImpl : OrderTrackingWorkflow {
    
    // State that can be queried
    private var currentStatus = OrderStatus.CREATED
    private lateinit var orderDetails: OrderDetails
    private val processingHistory = mutableListOf<ProcessingEvent>()
    
    override fun trackOrder(orderId: String): OrderTrackingResult {
        val logger = Workflow.getLogger(this::class.java)
        
        // Initialize order details
        orderDetails = OrderDetails(
            orderId = orderId,
            customerId = "customer-${orderId.takeLast(3)}",
            orderAmount = BigDecimal("299.99"),
            createdAt = Instant.now(),
            estimatedDelivery = LocalDate.now().plusDays(3)
        )
        
        // Process through stages, updating state
        updateStatus(OrderStatus.VALIDATED, "Order validation completed")
        Workflow.sleep(Duration.ofSeconds(2))
        
        updateStatus(OrderStatus.PAYMENT_PROCESSING, "Payment processing started")
        Workflow.sleep(Duration.ofSeconds(3))
        
        // Continue with more stages...
        
        return OrderTrackingResult(/* ... */)
    }
}
```

### Step 3: Implement Query Methods

Add the query method implementations:

```kotlin
override fun getCurrentStatus(): OrderStatus = currentStatus

override fun getOrderDetails(): OrderDetails = orderDetails

override fun getProcessingHistory(): List<ProcessingEvent> = processingHistory.toList()
```

**Key points:**
- Query methods return current state instantly
- Return copies of mutable collections to prevent external modification
- Keep query methods simple and fast

### Step 4: Add State Update Helper

Create a helper method to update workflow state:

```kotlin
private fun updateStatus(newStatus: OrderStatus, details: String) {
    currentStatus = newStatus
    
    val event = ProcessingEvent(
        step = newStatus.name,
        status = "COMPLETED",
        timestamp = Instant.now(),
        details = mapOf("description" to details)
    )
    
    processingHistory.add(event)
    
    val logger = Workflow.getLogger(this::class.java)
    logger.info("Status updated to: $newStatus - $details")
}
```

### Step 5: Complete the Order Processing Logic

Add the remaining order processing stages:

```kotlin
override fun trackOrder(orderId: String): OrderTrackingResult {
    // ... initialization code ...
    
    try {
        updateStatus(OrderStatus.VALIDATED, "Order validation completed")
        Workflow.sleep(Duration.ofSeconds(2))
        
        updateStatus(OrderStatus.PAYMENT_PROCESSING, "Payment processing started")
        Workflow.sleep(Duration.ofSeconds(3))
        
        updateStatus(OrderStatus.PAYMENT_CONFIRMED, "Payment confirmed")
        Workflow.sleep(Duration.ofSeconds(1))
        
        updateStatus(OrderStatus.INVENTORY_RESERVED, "Inventory reserved")
        Workflow.sleep(Duration.ofSeconds(2))
        
        updateStatus(OrderStatus.SHIPPED, "Order shipped")
        Workflow.sleep(Duration.ofSeconds(5))
        
        updateStatus(OrderStatus.DELIVERED, "Order delivered")
        
        return OrderTrackingResult(
            orderId = orderId,
            finalStatus = currentStatus,
            totalProcessingTime = Duration.between(startTime, Instant.now()),
            completedSteps = processingHistory.map { it.step }
        )
        
    } catch (e: Exception) {
        updateStatus(OrderStatus.CANCELLED, "Order cancelled: ${e.message}")
        throw e
    }
}
```

## How to Run

### 1. Start the Workflow
```kotlin
val workflow = workflowClient.newWorkflowStub(
    OrderTrackingWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("order-tracking-queue")
        .setWorkflowId("order-${System.currentTimeMillis()}")
        .build()
)

// Start workflow asynchronously
val execution = WorkflowClient.start(workflow::trackOrder, "ORD-12345")
```

### 2. Query the Workflow While Running
```kotlin
// Create a stub to the running workflow
val queryStub = workflowClient.newWorkflowStub(
    OrderTrackingWorkflow::class.java,
    execution.workflowId
)

// Query current status
val status = queryStub.getCurrentStatus()
println("Current status: $status")

// Query order details
val details = queryStub.getOrderDetails()
println("Order details: $details")

// Query processing history
val history = queryStub.getProcessingHistory()
println("Processing history: ${history.size} events")
```

### 3. Expected Output
```
Current status: PAYMENT_PROCESSING
Order details: OrderDetails(orderId=ORD-12345, customerId=customer-345, ...)
Processing history: 3 events
```

## What You've Learned

- ✅ How to implement query methods in workflows
- ✅ The difference between queries and signals (read vs write)
- ✅ Managing queryable state throughout workflow execution
- ✅ Real-time workflow monitoring without affecting execution
- ✅ Best practices for query method design

Queries provide a powerful way to observe workflow state in real-time! 