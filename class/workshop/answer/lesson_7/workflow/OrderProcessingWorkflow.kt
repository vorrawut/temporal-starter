package com.temporal.bootcamp.lesson7.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Instant
import java.time.Duration

/**
 * Order processing workflow demonstrating complex input/output patterns.
 */
@WorkflowInterface
interface OrderProcessingWorkflow {
    
    /**
     * Processes a complete order through validation, pricing, payment, and fulfillment.
     * 
     * @param orderRequest Complete order information
     * @return OrderResult with processing details and final status
     */
    @WorkflowMethod
    fun processOrder(orderRequest: OrderRequest): OrderResult
}

/**
 * Complete order request with nested data structures.
 */
data class OrderRequest(
    val customerId: String,
    val items: List<OrderItem>,
    val shippingAddress: Address,
    val paymentMethod: PaymentMethod,
    val metadata: Map<String, String> = emptyMap()
)

data class OrderItem(
    val productId: String,
    val quantity: Int,
    val unitPrice: BigDecimal
)

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String
)

/**
 * Comprehensive order result with rich output data.
 */
data class OrderResult(
    val orderId: String,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val estimatedDelivery: LocalDate?,
    val trackingInfo: TrackingInfo?,
    val processingSteps: List<ProcessingStep>,
    val metadata: OrderMetadata
)

data class ProcessingStep(
    val stepName: String,
    val status: StepStatus,
    val executedAt: Instant,
    val duration: Duration,
    val details: Map<String, Any> = emptyMap()
)

data class TrackingInfo(
    val trackingNumber: String,
    val carrier: String,
    val estimatedDelivery: LocalDate
)

data class OrderMetadata(
    val processingTime: Duration,
    val version: String,
    val systemInfo: String
)

enum class OrderStatus {
    PROCESSING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
}

enum class StepStatus {
    PENDING, IN_PROGRESS, COMPLETED, FAILED, SKIPPED
}

enum class PaymentMethod {
    CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER
} 