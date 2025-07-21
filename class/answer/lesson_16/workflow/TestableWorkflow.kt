package workflow

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod
import io.temporal.activity.ActivityOptions
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.common.RetryOptions
import io.temporal.testing.TestWorkflowRule
import io.temporal.worker.Worker
import io.temporal.worker.WorkerOptions
import io.temporal.workflow.Workflow
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.CompletableFuture

// Data classes for comprehensive testing
data class OrderRequest(
    val orderId: String,
    val customerId: String,
    val items: List<OrderItem>,
    val shippingAddress: Address,
    val paymentMethod: PaymentMethod,
    val priority: OrderPriority = OrderPriority.NORMAL,
    val metadata: Map<String, String> = emptyMap()
)

data class OrderItem(
    val productId: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val category: String
)

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String = "US"
)

data class PaymentMethod(
    val type: PaymentType,
    val cardNumber: String? = null,
    val expiryDate: String? = null,
    val accountId: String? = null
)

data class OrderResult(
    val orderId: String,
    val status: OrderStatus,
    val validationResult: ValidationResult,
    val inventoryResult: InventoryResult,
    val paymentResult: PaymentResult,
    val shippingResult: ShippingResult,
    val totalProcessingTime: Long,
    val errors: List<String> = emptyList()
)

data class ValidationResult(
    val isValid: Boolean,
    val validatedAt: Long,
    val issues: List<String> = emptyList()
)

data class InventoryResult(
    val reserved: Boolean,
    val reservationId: String?,
    val availableQuantities: Map<String, Int>,
    val backorderedItems: List<String> = emptyList()
)

data class PaymentResult(
    val processed: Boolean,
    val transactionId: String?,
    val amount: Double,
    val currency: String = "USD",
    val processedAt: Long?
)

data class ShippingResult(
    val scheduled: Boolean,
    val trackingNumber: String?,
    val estimatedDelivery: String?,
    val carrier: String?
)

// Configuration for testing and production
data class TestConfiguration(
    val taskQueue: String = "test-task-queue",
    val namespace: String = "default",
    val workerOptions: WorkerOptions = WorkerOptions.getDefaultInstance(),
    val workflowOptions: WorkflowOptions = WorkflowOptions.getDefaultInstance(),
    val enableMocking: Boolean = true
)

enum class OrderPriority { LOW, NORMAL, HIGH, URGENT }
enum class OrderStatus { CREATED, VALIDATED, INVENTORY_RESERVED, PAYMENT_PROCESSED, SHIPPED, COMPLETED, FAILED }
enum class PaymentType { CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, DIGITAL_WALLET }

// Activity interfaces for comprehensive testing
@ActivityInterface
interface OrderValidationActivity {
    @ActivityMethod
    fun validateOrder(request: OrderRequest): ValidationResult
    
    @ActivityMethod
    fun validateCustomer(customerId: String): Boolean
    
    @ActivityMethod
    fun validateAddress(address: Address): Boolean
}

@ActivityInterface
interface InventoryActivity {
    @ActivityMethod
    fun checkInventory(items: List<OrderItem>): Map<String, Int>
    
    @ActivityMethod
    fun reserveInventory(orderId: String, items: List<OrderItem>): InventoryResult
    
    @ActivityMethod
    fun releaseInventory(reservationId: String): Boolean
}

@ActivityInterface
interface PaymentActivity {
    @ActivityMethod
    fun processPayment(orderId: String, amount: Double, paymentMethod: PaymentMethod): PaymentResult
    
    @ActivityMethod
    fun refundPayment(transactionId: String, amount: Double): PaymentResult
    
    @ActivityMethod
    fun validatePaymentMethod(paymentMethod: PaymentMethod): Boolean
}

@ActivityInterface
interface ShippingActivity {
    @ActivityMethod
    fun scheduleShipping(orderId: String, address: Address, priority: OrderPriority): ShippingResult
    
    @ActivityMethod
    fun trackShipment(trackingNumber: String): String
    
    @ActivityMethod
    fun cancelShipment(trackingNumber: String): Boolean
}

// Main workflow interface
@WorkflowInterface
interface TestableWorkflow {
    @WorkflowMethod
    fun processOrder(request: OrderRequest): OrderResult
}

// Production-ready workflow implementation
@Component
class TestableWorkflowImpl : TestableWorkflow {
    
    // Production activity options with proper configurations
    private val standardActivityOptions = ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofMinutes(5))
        .setScheduleToCloseTimeout(Duration.ofMinutes(10))
        .setRetryOptions(
            RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(1))
                .setMaximumInterval(Duration.ofSeconds(30))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(3)
                .build()
        )
        .build()
    
    // Critical operation options (payment processing)
    private val criticalActivityOptions = ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofMinutes(10))
        .setScheduleToCloseTimeout(Duration.ofMinutes(15))
        .setRetryOptions(
            RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(2))
                .setMaximumInterval(Duration.ofMinutes(1))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(5)
                .build()
        )
        .build()
    
    // Long-running operation options (shipping)
    private val longRunningActivityOptions = ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofMinutes(30))
        .setScheduleToCloseTimeout(Duration.ofHours(1))
        .setRetryOptions(
            RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(5))
                .setMaximumInterval(Duration.ofMinutes(5))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(10)
                .build()
        )
        .build()
    
    // Activity stubs with appropriate configurations
    private val orderValidationActivity = Workflow.newActivityStub(
        OrderValidationActivity::class.java,
        standardActivityOptions
    )
    
    private val inventoryActivity = Workflow.newActivityStub(
        InventoryActivity::class.java,
        standardActivityOptions
    )
    
    private val paymentActivity = Workflow.newActivityStub(
        PaymentActivity::class.java,
        criticalActivityOptions
    )
    
    private val shippingActivity = Workflow.newActivityStub(
        ShippingActivity::class.java,
        longRunningActivityOptions
    )
    
    override fun processOrder(request: OrderRequest): OrderResult {
        val startTime = Workflow.currentTimeMillis()
        val errors = mutableListOf<String>()
        
        // Step 1: Order Validation
        val validationResult = try {
            orderValidationActivity.validateOrder(request)
        } catch (e: Exception) {
            errors.add("Validation failed: ${e.message}")
            ValidationResult(false, Workflow.currentTimeMillis(), listOf(e.message ?: "Unknown validation error"))
        }
        
        if (!validationResult.isValid) {
            return OrderResult(
                orderId = request.orderId,
                status = OrderStatus.FAILED,
                validationResult = validationResult,
                inventoryResult = InventoryResult(false, null, emptyMap()),
                paymentResult = PaymentResult(false, null, 0.0, processedAt = null),
                shippingResult = ShippingResult(false, null, null, null),
                totalProcessingTime = Workflow.currentTimeMillis() - startTime,
                errors = errors
            )
        }
        
        // Step 2: Inventory Management
        val inventoryResult = try {
            inventoryActivity.reserveInventory(request.orderId, request.items)
        } catch (e: Exception) {
            errors.add("Inventory reservation failed: ${e.message}")
            InventoryResult(false, null, emptyMap(), request.items.map { it.productId })
        }
        
        if (!inventoryResult.reserved) {
            return OrderResult(
                orderId = request.orderId,
                status = OrderStatus.FAILED,
                validationResult = validationResult,
                inventoryResult = inventoryResult,
                paymentResult = PaymentResult(false, null, 0.0, processedAt = null),
                shippingResult = ShippingResult(false, null, null, null),
                totalProcessingTime = Workflow.currentTimeMillis() - startTime,
                errors = errors
            )
        }
        
        // Step 3: Payment Processing
        val totalAmount = request.items.sumOf { it.quantity * it.pricePerUnit }
        val paymentResult = try {
            paymentActivity.processPayment(request.orderId, totalAmount, request.paymentMethod)
        } catch (e: Exception) {
            errors.add("Payment processing failed: ${e.message}")
            // Compensate by releasing inventory
            try {
                inventoryResult.reservationId?.let { inventoryActivity.releaseInventory(it) }
            } catch (compensationError: Exception) {
                errors.add("Inventory release compensation failed: ${compensationError.message}")
            }
            PaymentResult(false, null, totalAmount, processedAt = null)
        }
        
        if (!paymentResult.processed) {
            return OrderResult(
                orderId = request.orderId,
                status = OrderStatus.FAILED,
                validationResult = validationResult,
                inventoryResult = inventoryResult,
                paymentResult = paymentResult,
                shippingResult = ShippingResult(false, null, null, null),
                totalProcessingTime = Workflow.currentTimeMillis() - startTime,
                errors = errors
            )
        }
        
        // Step 4: Shipping Scheduling
        val shippingResult = try {
            shippingActivity.scheduleShipping(request.orderId, request.shippingAddress, request.priority)
        } catch (e: Exception) {
            errors.add("Shipping scheduling failed: ${e.message}")
            // Note: In production, you might implement compensation logic here
            ShippingResult(false, null, null, null)
        }
        
        // Determine final status
        val finalStatus = when {
            !validationResult.isValid || !inventoryResult.reserved || !paymentResult.processed -> OrderStatus.FAILED
            !shippingResult.scheduled -> OrderStatus.PAYMENT_PROCESSED
            else -> OrderStatus.SHIPPED
        }
        
        return OrderResult(
            orderId = request.orderId,
            status = finalStatus,
            validationResult = validationResult,
            inventoryResult = inventoryResult,
            paymentResult = paymentResult,
            shippingResult = shippingResult,
            totalProcessingTime = Workflow.currentTimeMillis() - startTime,
            errors = errors
        )
    }
}

// Test helper classes for comprehensive testing
class TestWorkflowRunner(private val config: TestConfiguration = TestConfiguration()) {
    
    fun createTestWorkflowRule(): TestWorkflowRule {
        return TestWorkflowRule.newBuilder()
            .setWorkflowTypes(TestableWorkflowImpl::class.java)
            .setActivityImplementations(
                MockOrderValidationActivityImpl(),
                MockInventoryActivityImpl(),
                MockPaymentActivityImpl(),
                MockShippingActivityImpl()
            )
            .setTaskQueue(config.taskQueue)
            .build()
    }
    
    fun runOrderProcessingTest(request: OrderRequest): CompletableFuture<OrderResult> {
        val testRule = createTestWorkflowRule()
        val client = testRule.workflowClient
        val workflow = client.newWorkflowStub(TestableWorkflow::class.java)
        
        return CompletableFuture.supplyAsync {
            workflow.processOrder(request)
        }
    }
}

// Production worker configuration
class ProductionWorkerConfig {
    
    companion object {
        fun createWorker(client: WorkflowClient, taskQueue: String): Worker {
            val workerOptions = WorkerOptions.newBuilder()
                .setMaxConcurrentActivityExecutions(10)
                .setMaxConcurrentWorkflowExecutions(5)
                .setMaxConcurrentLocalActivityExecutions(10)
                .build()
            
            val worker = client.newWorker(taskQueue, workerOptions)
            
            // Register workflow implementations
            worker.registerWorkflowImplementationTypes(TestableWorkflowImpl::class.java)
            
            // Register activity implementations
            worker.registerActivitiesImplementations(
                OrderValidationActivityImpl(),
                InventoryActivityImpl(),
                PaymentActivityImpl(),
                ShippingActivityImpl()
            )
            
            return worker
        }
        
        fun getProductionWorkflowOptions(orderId: String): WorkflowOptions {
            return WorkflowOptions.newBuilder()
                .setWorkflowId("order-processing-$orderId")
                .setTaskQueue("production-order-queue")
                .setWorkflowExecutionTimeout(Duration.ofHours(24))
                .setWorkflowRunTimeout(Duration.ofHours(12))
                .setWorkflowTaskTimeout(Duration.ofMinutes(1))
                .build()
        }
    }
}

// Mock activity implementations for testing
class MockOrderValidationActivityImpl : OrderValidationActivity {
    override fun validateOrder(request: OrderRequest): ValidationResult {
        // Simulate validation logic
        val issues = mutableListOf<String>()
        
        if (request.customerId.isBlank()) issues.add("Customer ID is required")
        if (request.items.isEmpty()) issues.add("Order must contain at least one item")
        if (request.items.any { it.quantity <= 0 }) issues.add("Item quantities must be positive")
        
        return ValidationResult(
            isValid = issues.isEmpty(),
            validatedAt = System.currentTimeMillis(),
            issues = issues
        )
    }
    
    override fun validateCustomer(customerId: String): Boolean {
        return customerId.isNotBlank() && customerId.startsWith("CUST")
    }
    
    override fun validateAddress(address: Address): Boolean {
        return address.street.isNotBlank() && 
               address.city.isNotBlank() && 
               address.zipCode.matches(Regex("\\d{5}"))
    }
}

class MockInventoryActivityImpl : InventoryActivity {
    override fun checkInventory(items: List<OrderItem>): Map<String, Int> {
        return items.associate { it.productId to (it.quantity + 10) } // Always have extra stock
    }
    
    override fun reserveInventory(orderId: String, items: List<OrderItem>): InventoryResult {
        return InventoryResult(
            reserved = true,
            reservationId = "RES-$orderId-${System.currentTimeMillis()}",
            availableQuantities = items.associate { it.productId to it.quantity }
        )
    }
    
    override fun releaseInventory(reservationId: String): Boolean {
        return true
    }
}

class MockPaymentActivityImpl : PaymentActivity {
    override fun processPayment(orderId: String, amount: Double, paymentMethod: PaymentMethod): PaymentResult {
        return PaymentResult(
            processed = amount > 0,
            transactionId = "TXN-$orderId-${System.currentTimeMillis()}",
            amount = amount,
            processedAt = System.currentTimeMillis()
        )
    }
    
    override fun refundPayment(transactionId: String, amount: Double): PaymentResult {
        return PaymentResult(
            processed = true,
            transactionId = "REFUND-$transactionId",
            amount = -amount,
            processedAt = System.currentTimeMillis()
        )
    }
    
    override fun validatePaymentMethod(paymentMethod: PaymentMethod): Boolean {
        return when (paymentMethod.type) {
            PaymentType.CREDIT_CARD -> !paymentMethod.cardNumber.isNullOrBlank()
            PaymentType.BANK_TRANSFER -> !paymentMethod.accountId.isNullOrBlank()
            else -> true
        }
    }
}

class MockShippingActivityImpl : ShippingActivity {
    override fun scheduleShipping(orderId: String, address: Address, priority: OrderPriority): ShippingResult {
        return ShippingResult(
            scheduled = true,
            trackingNumber = "TRACK-$orderId-${System.currentTimeMillis()}",
            estimatedDelivery = "2024-01-15",
            carrier = "MockCarrier"
        )
    }
    
    override fun trackShipment(trackingNumber: String): String {
        return "In Transit"
    }
    
    override fun cancelShipment(trackingNumber: String): Boolean {
        return true
    }
}

// Production activity implementations (stubbed for example)
class OrderValidationActivityImpl : OrderValidationActivity {
    override fun validateOrder(request: OrderRequest): ValidationResult {
        // Real validation logic would go here
        TODO("Implement with real validation service")
    }
    
    override fun validateCustomer(customerId: String): Boolean {
        TODO("Implement with customer service")
    }
    
    override fun validateAddress(address: Address): Boolean {
        TODO("Implement with address validation service")
    }
}

class InventoryActivityImpl : InventoryActivity {
    override fun checkInventory(items: List<OrderItem>): Map<String, Int> {
        TODO("Implement with inventory management system")
    }
    
    override fun reserveInventory(orderId: String, items: List<OrderItem>): InventoryResult {
        TODO("Implement with inventory management system")
    }
    
    override fun releaseInventory(reservationId: String): Boolean {
        TODO("Implement with inventory management system")
    }
}

class PaymentActivityImpl : PaymentActivity {
    override fun processPayment(orderId: String, amount: Double, paymentMethod: PaymentMethod): PaymentResult {
        TODO("Implement with payment gateway")
    }
    
    override fun refundPayment(transactionId: String, amount: Double): PaymentResult {
        TODO("Implement with payment gateway")
    }
    
    override fun validatePaymentMethod(paymentMethod: PaymentMethod): Boolean {
        TODO("Implement with payment validation service")
    }
}

class ShippingActivityImpl : ShippingActivity {
    override fun scheduleShipping(orderId: String, address: Address, priority: OrderPriority): ShippingResult {
        TODO("Implement with shipping service")
    }
    
    override fun trackShipment(trackingNumber: String): String {
        TODO("Implement with shipping tracking service")
    }
    
    override fun cancelShipment(trackingNumber: String): Boolean {
        TODO("Implement with shipping service")
    }
} 