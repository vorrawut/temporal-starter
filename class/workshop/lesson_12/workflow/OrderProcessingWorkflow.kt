package com.temporal.bootcamp.lesson12.workflow

// TODO: Add imports for WorkflowInterface, WorkflowMethod, and child workflow classes

/**
 * TODO: Create a workflow interface for OrderProcessingWorkflow
 * 
 * This workflow demonstrates parent-child workflow patterns:
 * 
 * Requirements:
 * 1. Annotate with @WorkflowInterface
 * 2. Create a @WorkflowMethod called processOrder that:
 *    - Takes an OrderRequest parameter
 *    - Returns an OrderResult
 * 3. This will be the parent workflow that coordinates child workflows
 */

// TODO: Define your OrderProcessingWorkflow interface here

/**
 * TODO: Create child workflow interfaces:
 * 
 * 1. PaymentWorkflow - handles payment processing
 * 2. InventoryWorkflow - handles inventory management  
 * 3. ShippingWorkflow - handles shipping coordination
 */

// TODO: Define your child workflow interfaces here

/**
 * TODO: Create the OrderProcessingWorkflow implementation
 * 
 * Requirements:
 * 1. Implement the OrderProcessingWorkflow interface
 * 2. Use Workflow.newChildWorkflowStub() to create child workflow stubs
 * 3. Coordinate execution of child workflows
 * 4. Handle child workflow results and errors
 * 5. Demonstrate continueAsNew for long-running processes
 */

// TODO: Implement OrderProcessingWorkflowImpl class here

data class OrderRequest(
    val orderId: String,
    val customerId: String,
    val items: List<OrderItem>,
    val paymentInfo: PaymentInfo,
    val shippingAddress: ShippingAddress
)

data class OrderItem(
    val productId: String,
    val quantity: Int,
    val price: java.math.BigDecimal
)

data class PaymentInfo(
    val method: String,
    val amount: java.math.BigDecimal
)

data class ShippingAddress(
    val street: String,
    val city: String,
    val zipCode: String
)

data class OrderResult(
    val orderId: String,
    val status: OrderStatus,
    val paymentResult: PaymentResult?,
    val inventoryResult: InventoryResult?,
    val shippingResult: ShippingResult?
)

enum class OrderStatus {
    PROCESSING, COMPLETED, FAILED, CANCELLED
} 