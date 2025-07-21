package com.temporal.bootcamp.lesson11.workflow

// TODO: Add imports for WorkflowInterface, WorkflowMethod, QueryMethod

/**
 * TODO: Create a workflow interface for OrderTrackingWorkflow
 * 
 * This workflow demonstrates query handlers for fetching state:
 * 
 * Requirements:
 * 1. Annotate with @WorkflowInterface
 * 2. Create a @WorkflowMethod called trackOrder that:
 *    - Takes an orderId String parameter
 *    - Returns an OrderTrackingResult
 * 3. Create @QueryMethod methods for:
 *    - getCurrentStatus(): OrderStatus
 *    - getOrderDetails(): OrderDetails
 *    - getProcessingHistory(): List<ProcessingEvent>
 */

// TODO: Define your OrderTrackingWorkflow interface here

/**
 * TODO: Create the OrderTrackingWorkflow implementation
 * 
 * Requirements:
 * 1. Implement the OrderTrackingWorkflow interface
 * 2. Maintain workflow state (current status, order details, processing history)
 * 3. Implement query methods to return current state
 * 4. Process order through different stages
 * 5. Update internal state as processing progresses
 */

// TODO: Implement OrderTrackingWorkflowImpl class here

data class OrderTrackingResult(
    val orderId: String,
    val finalStatus: OrderStatus,
    val totalProcessingTime: java.time.Duration,
    val completedSteps: List<String>
)

data class OrderDetails(
    val orderId: String,
    val customerId: String,
    val orderAmount: java.math.BigDecimal,
    val createdAt: java.time.Instant,
    val estimatedDelivery: java.time.LocalDate?
)

data class ProcessingEvent(
    val step: String,
    val status: String,
    val timestamp: java.time.Instant,
    val details: Map<String, Any> = emptyMap()
)

enum class OrderStatus {
    CREATED, VALIDATED, PAYMENT_PROCESSING, PAYMENT_CONFIRMED,
    INVENTORY_RESERVED, SHIPPED, DELIVERED, CANCELLED
} 