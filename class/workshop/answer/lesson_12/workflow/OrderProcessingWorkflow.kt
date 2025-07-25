package com.temporal.bootcamp.lesson12.workflow

import io.temporal.workflow.*
import io.temporal.common.RetryOptions
import java.time.Duration
import java.math.BigDecimal

/**
 * Parent workflow that coordinates multiple child workflows.
 */
@WorkflowInterface
interface OrderProcessingWorkflow {
    
    @WorkflowMethod
    fun processOrder(orderRequest: OrderRequest): OrderResult
}

/**
 * Child workflow for payment processing.
 */
@WorkflowInterface
interface PaymentWorkflow {
    
    @WorkflowMethod
    fun processPayment(paymentInfo: PaymentInfo): PaymentResult
}

/**
 * Child workflow for inventory management.
 */
@WorkflowInterface
interface InventoryWorkflow {
    
    @WorkflowMethod
    fun reserveInventory(items: List<OrderItem>): InventoryResult
}

/**
 * Child workflow for shipping coordination.
 */
@WorkflowInterface
interface ShippingWorkflow {
    
    @WorkflowMethod
    fun arrangeShipping(address: ShippingAddress, items: List<OrderItem>): ShippingResult
}

/**
 * Parent workflow implementation demonstrating child workflow coordination.
 */
class OrderProcessingWorkflowImpl : OrderProcessingWorkflow {
    
    override fun processOrder(orderRequest: OrderRequest): OrderResult {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Starting order processing for: ${orderRequest.orderId}")
        
        // Create child workflow stubs
        val paymentWorkflow = createPaymentWorkflow(orderRequest.orderId)
        val inventoryWorkflow = createInventoryWorkflow(orderRequest.orderId)
        val shippingWorkflow = createShippingWorkflow(orderRequest.orderId)
        
        try {
            // Execute child workflows in sequence
            logger.info("Step 1: Processing payment")
            val paymentResult = paymentWorkflow.processPayment(orderRequest.paymentInfo)
            
            if (!paymentResult.success) {
                return OrderResult(
                    orderId = orderRequest.orderId,
                    status = OrderStatus.FAILED,
                    paymentResult = paymentResult,
                    inventoryResult = null,
                    shippingResult = null
                )
            }
            
            logger.info("Step 2: Reserving inventory")
            val inventoryResult = inventoryWorkflow.reserveInventory(orderRequest.items)
            
            if (!inventoryResult.success) {
                // Compensate payment if inventory fails
                logger.warn("Inventory reservation failed, compensating payment")
                return OrderResult(
                    orderId = orderRequest.orderId,
                    status = OrderStatus.FAILED,
                    paymentResult = paymentResult,
                    inventoryResult = inventoryResult,
                    shippingResult = null
                )
            }
            
            logger.info("Step 3: Arranging shipping")
            val shippingResult = shippingWorkflow.arrangeShipping(
                orderRequest.shippingAddress,
                orderRequest.items
            )
            
            val finalStatus = if (shippingResult.success) {
                OrderStatus.COMPLETED
            } else {
                OrderStatus.FAILED
            }
            
            logger.info("Order processing completed with status: $finalStatus")
            
            return OrderResult(
                orderId = orderRequest.orderId,
                status = finalStatus,
                paymentResult = paymentResult,
                inventoryResult = inventoryResult,
                shippingResult = shippingResult
            )
            
        } catch (e: Exception) {
            logger.error("Order processing failed: ${e.message}")
            
            return OrderResult(
                orderId = orderRequest.orderId,
                status = OrderStatus.FAILED,
                paymentResult = null,
                inventoryResult = null,
                shippingResult = null
            )
        }
    }
    
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
}

/**
 * Payment workflow implementation.
 */
class PaymentWorkflowImpl : PaymentWorkflow {
    
    override fun processPayment(paymentInfo: PaymentInfo): PaymentResult {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Processing payment: ${paymentInfo.method} for amount: ${paymentInfo.amount}")
        
        // Simulate payment processing
        Workflow.sleep(Duration.ofSeconds(2))
        
        // Simulate success (90% success rate)
        val success = Math.random() > 0.1
        
        return if (success) {
            logger.info("Payment processed successfully")
            PaymentResult(
                success = true,
                transactionId = "txn_${System.currentTimeMillis()}",
                errorMessage = null
            )
        } else {
            logger.warn("Payment processing failed")
            PaymentResult(
                success = false,
                transactionId = null,
                errorMessage = "Payment declined by provider"
            )
        }
    }
}

/**
 * Inventory workflow implementation.
 */
class InventoryWorkflowImpl : InventoryWorkflow {
    
    override fun reserveInventory(items: List<OrderItem>): InventoryResult {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Reserving inventory for ${items.size} items")
        
        // Simulate inventory check
        Workflow.sleep(Duration.ofSeconds(1))
        
        val reservations = items.map { item ->
            InventoryReservation(
                productId = item.productId,
                quantity = item.quantity,
                reservationId = "res_${item.productId}_${System.currentTimeMillis()}"
            )
        }
        
        logger.info("Inventory reserved successfully")
        
        return InventoryResult(
            success = true,
            reservations = reservations,
            errorMessage = null
        )
    }
}

/**
 * Shipping workflow implementation.
 */
class ShippingWorkflowImpl : ShippingWorkflow {
    
    override fun arrangeShipping(address: ShippingAddress, items: List<OrderItem>): ShippingResult {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Arranging shipping to: ${address.city}, ${address.zipCode}")
        
        // Simulate shipping arrangement
        Workflow.sleep(Duration.ofSeconds(1))
        
        val trackingNumber = "TRACK_${System.currentTimeMillis()}"
        
        logger.info("Shipping arranged with tracking: $trackingNumber")
        
        return ShippingResult(
            success = true,
            trackingNumber = trackingNumber,
            estimatedDelivery = java.time.LocalDate.now().plusDays(3),
            errorMessage = null
        )
    }
}

/**
 * Long-running workflow demonstrating continueAsNew pattern.
 */
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
        
        logger.info("Starting batch $batchNumber, already processed: $processedCount orders")
        
        var currentProcessedCount = processedCount
        
        // Process orders in this batch
        for (i in 1..MAX_ORDERS_PER_WORKFLOW) {
            // Simulate order processing
            processOrder("order_${batchNumber}_$i")
            currentProcessedCount++
            
            // Add some delay
            Workflow.sleep(Duration.ofMillis(100))
        }
        
        logger.info("Completed batch $batchNumber, total processed: $currentProcessedCount")
        
        // Check if we should continue with next batch
        val shouldContinue = shouldContinueProcessing(currentProcessedCount)
        
        if (shouldContinue) {
            logger.info("Continuing with next batch using continueAsNew")
            
            // Use continueAsNew to start fresh with new batch
            Workflow.continueAsNew(batchNumber + 1, currentProcessedCount)
        }
        
        return ProcessingResult(
            batchNumber = batchNumber,
            totalProcessed = currentProcessedCount,
            completed = true
        )
    }
    
    private fun processOrder(orderId: String) {
        val logger = Workflow.getLogger(this::class.java)
        logger.debug("Processing order: $orderId")
        // Simulate order processing logic
    }
    
    private fun shouldContinueProcessing(processedCount: Int): Boolean {
        // Continue processing if we haven't reached the limit
        return processedCount < 1000 // Process up to 1000 orders total
    }
}

// Data classes
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
    val price: BigDecimal
)

data class PaymentInfo(
    val method: String,
    val amount: BigDecimal
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

data class PaymentResult(
    val success: Boolean,
    val transactionId: String?,
    val errorMessage: String?
)

data class InventoryResult(
    val success: Boolean,
    val reservations: List<InventoryReservation>,
    val errorMessage: String?
)

data class InventoryReservation(
    val productId: String,
    val quantity: Int,
    val reservationId: String
)

data class ShippingResult(
    val success: Boolean,
    val trackingNumber: String?,
    val estimatedDelivery: java.time.LocalDate?,
    val errorMessage: String?
)

data class ProcessingResult(
    val batchNumber: Int,
    val totalProcessed: Int,
    val completed: Boolean
)

enum class OrderStatus {
    PROCESSING, COMPLETED, FAILED, CANCELLED
} 