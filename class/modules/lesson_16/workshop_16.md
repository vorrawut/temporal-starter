# Lesson 16: Testing + Production Readiness

## What we want to build

A comprehensive testing framework and production-ready configuration for Temporal workflows, including unit tests, integration tests, mock activity implementations, worker configuration, and scalability considerations. This final lesson covers everything needed to deploy and maintain Temporal workflows in production.

## Expecting Result

A complete testing and deployment solution that includes:
- Unit tests for workflows and activities using `TestWorkflowRule`
- Mock activity implementations for isolated testing
- Production worker configuration with proper scaling
- Comprehensive error handling and monitoring patterns
- Performance optimization strategies
- Deployment best practices and environment setup

## Code Steps

### Step 1: Define Comprehensive Data Models

Create rich data classes that support both testing and production scenarios:

```kotlin
data class OrderRequest(
    val orderId: String,
    val customerId: String,
    val items: List<OrderItem>,
    val shippingAddress: Address,
    val paymentMethod: PaymentMethod,
    val priority: OrderPriority = OrderPriority.NORMAL,
    val metadata: Map<String, String> = emptyMap()
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
```

Include detailed result classes for each step that capture both success and failure scenarios.

### Step 2: Create Activity Interfaces with Testing in Mind

Design activity interfaces that are easy to mock and test:

```kotlin
@ActivityInterface
interface OrderValidationActivity {
    @ActivityMethod
    fun validateOrder(request: OrderRequest): ValidationResult
    
    @ActivityMethod
    fun validateCustomer(customerId: String): Boolean
    
    @ActivityMethod
    fun validateAddress(address: Address): Boolean
}
```

Create separate interfaces for each business domain to enable focused testing.

### Step 3: Implement Production-Ready Activity Options

Configure different activity options for different operation types:

```kotlin
// Standard operations
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

// Critical operations (payment)
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
```

### Step 4: Build Testable Workflow Implementation

Create workflow implementation with comprehensive error handling and compensation logic:

```kotlin
override fun processOrder(request: OrderRequest): OrderResult {
    val startTime = Workflow.currentTimeMillis()
    val errors = mutableListOf<String>()
    
    // Step 1: Validation with early return on failure
    val validationResult = try {
        orderValidationActivity.validateOrder(request)
    } catch (e: Exception) {
        errors.add("Validation failed: ${e.message}")
        ValidationResult(false, Workflow.currentTimeMillis(), listOf(e.message ?: "Unknown error"))
    }
    
    if (!validationResult.isValid) {
        return createFailureResult(request.orderId, validationResult, errors, startTime)
    }
    
    // Continue with remaining steps with compensation logic...
}
```

### Step 5: Create Mock Activity Implementations

Implement mock activities for testing that simulate various scenarios:

```kotlin
class MockOrderValidationActivityImpl : OrderValidationActivity {
    override fun validateOrder(request: OrderRequest): ValidationResult {
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
}
```

Include mocks that can simulate both success and failure scenarios.

### Step 6: Build Test Framework

Create a comprehensive test runner using `TestWorkflowRule`:

```kotlin
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
```

### Step 7: Configure Production Workers

Set up production-ready worker configuration:

```kotlin
class ProductionWorkerConfig {
    companion object {
        fun createWorker(client: WorkflowClient, taskQueue: String): Worker {
            val workerOptions = WorkerOptions.newBuilder()
                .setMaxConcurrentActivityExecutions(10)
                .setMaxConcurrentWorkflowExecutions(5)
                .setMaxConcurrentLocalActivityExecutions(10)
                .build()
            
            val worker = client.newWorker(taskQueue, workerOptions)
            
            // Register implementations
            worker.registerWorkflowImplementationTypes(TestableWorkflowImpl::class.java)
            worker.registerActivitiesImplementations(/* production activities */)
            
            return worker
        }
    }
}
```

### Step 8: Add Production Workflow Options

Configure workflow options for production use:

```kotlin
fun getProductionWorkflowOptions(orderId: String): WorkflowOptions {
    return WorkflowOptions.newBuilder()
        .setWorkflowId("order-processing-$orderId")
        .setTaskQueue("production-order-queue")
        .setWorkflowExecutionTimeout(Duration.ofHours(24))
        .setWorkflowRunTimeout(Duration.ofHours(12))
        .setWorkflowTaskTimeout(Duration.ofMinutes(1))
        .build()
}
```

### Step 9: Implement Test Cases

Create comprehensive test scenarios covering:

- **Happy Path Testing**: All operations succeed
- **Validation Failures**: Invalid input handling
- **Partial Failures**: Some operations fail, others succeed
- **Compensation Logic**: Rollback scenarios
- **Timeout Testing**: Long-running operation handling
- **Retry Logic**: Activity retry behavior

### Step 10: Add Production Monitoring

Include logging and monitoring patterns:

```kotlin
// In workflow implementation
try {
    val result = paymentActivity.processPayment(orderId, totalAmount, paymentMethod)
    // Log success metrics
    return result
} catch (e: Exception) {
    errors.add("Payment processing failed: ${e.message}")
    // Log failure metrics and attempt compensation
    compensateInventoryReservation(inventoryResult.reservationId)
    throw e
}
```

## How to Run

### Testing
1. **Unit Tests**: Use `TestWorkflowRule` to test workflow logic in isolation
2. **Integration Tests**: Test with real Temporal server and mock external services
3. **Load Testing**: Verify performance under concurrent execution

Example test execution:
```kotlin
val testRunner = TestWorkflowRunner()
val orderRequest = OrderRequest(/* test data */)
val result = testRunner.runOrderProcessingTest(orderRequest).get()

assert(result.status == OrderStatus.SHIPPED)
assert(result.errors.isEmpty())
```

### Production Deployment
1. **Worker Setup**: Configure workers with appropriate scaling
2. **Task Queue Configuration**: Set up dedicated task queues for different workloads
3. **Monitoring**: Implement metrics collection and alerting
4. **Environment Configuration**: Manage secrets and environment-specific settings

Example production startup:
```kotlin
val client = WorkflowClient.newInstance(service)
val worker = ProductionWorkerConfig.createWorker(client, "production-order-queue")
worker.start()
``` 