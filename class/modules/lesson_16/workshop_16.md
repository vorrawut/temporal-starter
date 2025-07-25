---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Workshop 16: Testing + Production Readiness

## Building Production-Grade Temporal Systems

*A comprehensive testing framework and production-ready configuration for Temporal workflows, including unit tests, integration tests, mock activity implementations, worker configuration, and scalability considerations*

---

# What we want to build

A **comprehensive testing framework** and **production-ready configuration** for Temporal workflows, including:

- **Unit tests** for workflows and activities using `TestWorkflowRule`
- **Mock activity implementations** for isolated testing
- **Production worker configuration** with proper scaling
- **Comprehensive error handling** and monitoring patterns

---

# Expecting Result

## A complete testing and deployment solution that includes:

- ✅ **Unit tests** for workflows and activities using `TestWorkflowRule`
- ✅ **Mock activity implementations** for isolated testing
- ✅ **Production worker configuration** with proper scaling
- ✅ **Comprehensive error handling** and monitoring patterns
- ✅ **Performance optimization** strategies
- ✅ **Deployment best practices** and environment setup

---

# Code Steps

## Step 1: Define Comprehensive Data Models

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

**Include detailed result classes for each step that capture both success and failure scenarios**

---

# Step 2: Create Activity Interfaces with Testing in Mind

## Design activity interfaces that are easy to mock and test:

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

**Create separate interfaces for each business domain to enable focused testing**

---

# Step 3: Implement Production-Ready Activity Options

## Configure different activity options for different operation types:

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

---

# Step 4: Create Unit Tests with TestWorkflowRule

```kotlin
class OrderWorkflowTest {
    
    @Rule
    @JvmField
    val testWorkflowRule: TestWorkflowRule = TestWorkflowRule.newBuilder()
        .setWorkflowTypes(OrderWorkflowImpl::class.java)
        .setActivityImplementations(
            MockOrderValidationActivity(),
            MockInventoryActivity(),
            MockPaymentActivity(),
            MockShippingActivity()
        )
        .build()
    
    @Test
    fun testSuccessfulOrderProcessing() {
        val workflow = testWorkflowRule.workflowClient.newWorkflowStub(
            OrderWorkflow::class.java
        )
        
        val orderRequest = OrderRequest(
            orderId = "test-order-123",
            customerId = "customer-456",
            items = listOf(OrderItem("product-1", 2, BigDecimal("29.99"))),
            shippingAddress = Address("123 Test St", "Test City", "TC", "12345", "US"),
            paymentMethod = PaymentMethod.CREDIT_CARD
        )
        
        val result = workflow.processOrder(orderRequest)
        
        assertEquals(OrderStatus.COMPLETED, result.status)
        assertTrue(result.validationResult.isValid)
        assertTrue(result.paymentResult.success)
        assertTrue(result.errors.isEmpty())
    }
}
```

---

# Step 5: Create Mock Activity Implementations

```kotlin
class MockOrderValidationActivity : OrderValidationActivity {
    
    override fun validateOrder(request: OrderRequest): ValidationResult {
        // Simulate validation logic
        return if (request.items.isNotEmpty() && request.customerId.isNotBlank()) {
            ValidationResult.success("Order validation passed")
        } else {
            ValidationResult.failure("Invalid order data")
        }
    }
    
    override fun validateCustomer(customerId: String): Boolean {
        // Mock customer validation
        return !customerId.startsWith("invalid")
    }
    
    override fun validateAddress(address: Address): Boolean {
        // Mock address validation
        return address.zipCode.length == 5
    }
}

class MockPaymentActivity : PaymentActivity {
    
    override fun processPayment(request: PaymentRequest): PaymentResult {
        // Simulate payment processing
        return if (request.amount > BigDecimal.ZERO) {
            PaymentResult.success("txn-${System.currentTimeMillis()}", request.amount)
        } else {
            PaymentResult.failure("Invalid payment amount")
        }
    }
}
```

---

# Step 6: Production Worker Configuration

```kotlin
@Configuration
class TemporalWorkerConfiguration {
    
    @Bean
    fun temporalWorker(
        workflowClient: WorkflowClient,
        orderValidationActivity: OrderValidationActivity,
        inventoryActivity: InventoryActivity,
        paymentActivity: PaymentActivity,
        shippingActivity: ShippingActivity
    ): Worker {
        
        val workerFactory = WorkerFactory.newInstance(workflowClient)
        
        val worker = workerFactory.newWorker(
            "order-processing-queue",
            WorkerOptions.newBuilder()
                .setMaxConcurrentActivityExecutions(20)
                .setMaxConcurrentWorkflowExecutions(10)
                .setMaxConcurrentLocalActivityExecutions(10)
                .build()
        )
        
        // Register workflows
        worker.registerWorkflowImplementationTypes(
            OrderWorkflowImpl::class.java
        )
        
        // Register activities
        worker.registerActivitiesImplementations(
            orderValidationActivity,
            inventoryActivity,
            paymentActivity,
            shippingActivity
        )
        
        workerFactory.start()
        
        return worker
    }
}
```

---

# Production Configuration Patterns

## **Worker Scaling Guidelines:**

| Metric | Recommendation | Reasoning |
|--------|----------------|-----------|
| **Max Concurrent Activities** | 20-50 per worker | Balance throughput with resource usage |
| **Max Concurrent Workflows** | 10-20 per worker | Workflows are lightweight |
| **Workers per Instance** | 1-3 | Avoid resource contention |
| **Task Queue Strategy** | Domain-specific | Separate queues for different workflow types |

## **Environment-Specific Settings:**
- **Development**: Lower concurrency, verbose logging
- **Staging**: Production-like settings, extended timeouts
- **Production**: Optimized settings, minimal logging

---

# Step 7: Integration Testing

```kotlin
@SpringBootTest
@TestPropertySource(properties = ["temporal.enabled=false"])
class OrderWorkflowIntegrationTest {
    
    private lateinit var testWorkflowRule: TestWorkflowRule
    
    @BeforeEach
    fun setUp() {
        testWorkflowRule = TestWorkflowRule.newBuilder()
            .setWorkflowTypes(OrderWorkflowImpl::class.java)
            .setActivityImplementations(
                RealOrderValidationActivity(),  // Real implementations
                RealInventoryActivity(),
                MockPaymentActivity(),          // Mock external services
                MockShippingActivity()
            )
            .build()
    }
    
    @Test
    fun testEndToEndOrderProcessing() {
        val workflow = testWorkflowRule.workflowClient.newWorkflowStub(
            OrderWorkflow::class.java
        )
        
        // Test with realistic data
        val result = workflow.processOrder(createRealisticOrderRequest())
        
        // Verify end-to-end flow
        assertNotNull(result)
        assertEquals(OrderStatus.COMPLETED, result.status)
        assertTrue(result.totalProcessingTime > 0)
    }
}
```

---

# Testing Strategies Summary

## **Testing Pyramid:**

- ✅ **Unit Tests**: Fast, isolated, mock all dependencies
- ✅ **Integration Tests**: Real activities, mock external services
- ✅ **End-to-End Tests**: Full system with real Temporal cluster
- ✅ **Load Tests**: Performance validation under stress

## **Mock Strategy:**
- **Mock external services** (payment gateways, APIs)
- **Use real business logic** in activities when possible
- **Test error scenarios** with controlled failures
- **Validate retry behavior** and timeout handling

---

# 💡 Key Testing & Production Patterns

## **What You've Learned:**

- ✅ **Comprehensive testing** with `TestWorkflowRule`
- ✅ **Mock activity implementations** for isolated testing
- ✅ **Production worker configuration** with proper scaling
- ✅ **Integration testing** strategies
- ✅ **Performance optimization** approaches
- ✅ **Deployment best practices** for production systems

---

# 🚀 Final Achievement

**You now master production-ready Temporal development!**

## **Lesson 17 will cover:**
- Deployment strategies and environments
- Monitoring and observability
- Production operational patterns
- Advanced scaling and optimization

**Ready for production deployment mastery? Let's finish strong! 🎉** 