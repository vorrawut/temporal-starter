package com.temporal.bootcamp.lesson7.workflow

import java.math.BigDecimal
import java.time.LocalDate

// TODO: Add imports for WorkflowInterface and WorkflowMethod

/**
 * TODO: Create a workflow interface for OrderProcessingWorkflow
 * 
 * This workflow demonstrates complex input/output patterns:
 * 
 * Requirements:
 * 1. Annotate with @WorkflowInterface
 * 2. Create a method called processOrder that:
 *    - Takes an OrderRequest parameter (you'll create this data class)
 *    - Returns an OrderResult (you'll create this data class)
 *    - Is annotated with @WorkflowMethod
 */

// TODO: Define your OrderProcessingWorkflow interface here

// TODO: Create data classes for input:
// - OrderRequest (customerId, items, shippingAddress, paymentMethod)
// - OrderItem (productId, quantity, unitPrice)
// - Address (street, city, state, zipCode, country)

// TODO: Create data classes for output:
// - OrderResult (orderId, status, totalAmount, estimatedDelivery, processingSteps)
// - ProcessingStep (stepName, status, executedAt, duration)

enum class OrderStatus {
    PROCESSING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
}

enum class PaymentMethod {
    CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER
} 