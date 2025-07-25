package com.temporal.bootcamp.lesson9.workflow

// TODO: Add imports for WorkflowInterface, WorkflowMethod, and error handling classes

/**
 * TODO: Create a workflow interface for ErrorHandlingWorkflow
 * 
 * This workflow demonstrates comprehensive error handling patterns:
 * 
 * Requirements:
 * 1. Annotate with @WorkflowInterface
 * 2. Create a method called processOrderWithErrorHandling that:
 *    - Takes an OrderRequest parameter
 *    - Returns an OrderResult
 *    - Is annotated with @WorkflowMethod
 */

// TODO: Define your ErrorHandlingWorkflow interface here

// TODO: Create custom exception classes:
// - InsufficientInventoryException
// - PaymentDeclinedException  
// - ShippingUnavailableException

/**
 * TODO: Create the ErrorHandlingWorkflow implementation
 * 
 * Requirements:
 * 1. Implement comprehensive try-catch blocks
 * 2. Implement compensation logic (saga pattern)
 * 3. Handle different error types appropriately
 * 4. Use circuit breaker patterns for external services
 * 5. Provide graceful degradation for non-critical failures
 */

// TODO: Implement ErrorHandlingWorkflowImpl class here

data class OrderRequest(
    val orderId: String,
    val customerId: String,
    val items: List<OrderItem>,
    val paymentInfo: PaymentInfo,
    val shippingAddress: Address
)

data class OrderItem(
    val productId: String,
    val quantity: Int
)

data class PaymentInfo(
    val cardNumber: String,
    val amount: java.math.BigDecimal
)

data class Address(
    val street: String,
    val city: String,
    val zipCode: String
)

sealed class OrderResult {
    data class Success(
        val orderId: String,
        val confirmationNumber: String
    ) : OrderResult()
    
    data class Failed(
        val orderId: String,
        val reason: String,
        val compensationPerformed: Boolean
    ) : OrderResult()
} 