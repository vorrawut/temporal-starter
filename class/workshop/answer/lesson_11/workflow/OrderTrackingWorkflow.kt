package com.temporal.bootcamp.lesson11.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import io.temporal.workflow.QueryMethod
import io.temporal.workflow.Workflow
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.math.BigDecimal

/**
 * Order tracking workflow demonstrating query handlers.
 */
@WorkflowInterface
interface OrderTrackingWorkflow {
    
    /**
     * Main workflow method that tracks an order through its lifecycle.
     */
    @WorkflowMethod
    fun trackOrder(orderId: String): OrderTrackingResult
    
    /**
     * Query to get current order status.
     */
    @QueryMethod
    fun getCurrentStatus(): OrderStatus
    
    /**
     * Query to get detailed order information.
     */
    @QueryMethod
    fun getOrderDetails(): OrderDetails
    
    /**
     * Query to get processing history.
     */
    @QueryMethod
    fun getProcessingHistory(): List<ProcessingEvent>
}

/**
 * Implementation of order tracking workflow with query handling.
 */
class OrderTrackingWorkflowImpl : OrderTrackingWorkflow {
    
    // Workflow state that can be queried
    private var currentStatus = OrderStatus.CREATED
    private lateinit var orderDetails: OrderDetails
    private val processingHistory = mutableListOf<ProcessingEvent>()
    private var startTime: Instant? = null
    
    override fun trackOrder(orderId: String): OrderTrackingResult {
        val logger = Workflow.getLogger(this::class.java)
        startTime = Instant.now()
        
        logger.info("Starting order tracking for: $orderId")
        
        // Initialize order details
        orderDetails = OrderDetails(
            orderId = orderId,
            customerId = "customer-${orderId.takeLast(3)}",
            orderAmount = BigDecimal("299.99"),
            createdAt = startTime!!,
            estimatedDelivery = LocalDate.now().plusDays(3)
        )
        
        // Process order through different stages
        try {
            // Stage 1: Validation
            updateStatus(OrderStatus.VALIDATED, "Order validation completed")
            simulateProcessingTime(Duration.ofSeconds(2))
            
            // Stage 2: Payment Processing
            updateStatus(OrderStatus.PAYMENT_PROCESSING, "Payment processing started")
            simulateProcessingTime(Duration.ofSeconds(3))
            
            updateStatus(OrderStatus.PAYMENT_CONFIRMED, "Payment confirmed")
            simulateProcessingTime(Duration.ofSeconds(1))
            
            // Stage 3: Inventory
            updateStatus(OrderStatus.INVENTORY_RESERVED, "Inventory reserved")
            simulateProcessingTime(Duration.ofSeconds(2))
            
            // Stage 4: Shipping
            updateStatus(OrderStatus.SHIPPED, "Order shipped")
            simulateProcessingTime(Duration.ofSeconds(5))
            
            // Stage 5: Delivery
            updateStatus(OrderStatus.DELIVERED, "Order delivered")
            
            logger.info("Order tracking completed for: $orderId")
            
            return OrderTrackingResult(
                orderId = orderId,
                finalStatus = currentStatus,
                totalProcessingTime = Duration.between(startTime, Instant.now()),
                completedSteps = processingHistory.map { it.step }
            )
            
        } catch (e: Exception) {
            updateStatus(OrderStatus.CANCELLED, "Order cancelled due to error: ${e.message}")
            throw e
        }
    }
    
    override fun getCurrentStatus(): OrderStatus = currentStatus
    
    override fun getOrderDetails(): OrderDetails = orderDetails
    
    override fun getProcessingHistory(): List<ProcessingEvent> = processingHistory.toList()
    
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
    
    private fun simulateProcessingTime(duration: Duration) {
        Workflow.sleep(duration)
    }
}

data class OrderTrackingResult(
    val orderId: String,
    val finalStatus: OrderStatus,
    val totalProcessingTime: Duration,
    val completedSteps: List<String>
)

data class OrderDetails(
    val orderId: String,
    val customerId: String,
    val orderAmount: BigDecimal,
    val createdAt: Instant,
    val estimatedDelivery: LocalDate?
)

data class ProcessingEvent(
    val step: String,
    val status: String,
    val timestamp: Instant,
    val details: Map<String, Any> = emptyMap()
)

enum class OrderStatus {
    CREATED, VALIDATED, PAYMENT_PROCESSING, PAYMENT_CONFIRMED,
    INVENTORY_RESERVED, SHIPPED, DELIVERED, CANCELLED
} 