# Workshop 7: Workflow Input/Output

## What we want to build

Create workflows that handle complex input parameters and return rich output data structures. This lesson focuses on advanced data modeling and input validation patterns.

## Expecting Result

- A workflow that accepts complex input objects
- Proper input validation before processing
- Rich output objects with detailed results
- Data transformation patterns between workflow and activities

## Code Steps

### Step 1: Create Input Data Classes
```kotlin
data class OrderRequest(
    val customerId: String,
    val items: List<OrderItem>,
    val shippingAddress: Address,
    val paymentMethod: PaymentMethod
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
```

### Step 2: Create Output Data Classes
```kotlin
data class OrderResult(
    val orderId: String,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val estimatedDelivery: LocalDate,
    val trackingInfo: TrackingInfo?,
    val processingSteps: List<ProcessingStep>
)

enum class OrderStatus {
    PROCESSING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
}
```

### Step 3: Implement Order Processing Workflow
```kotlin
@WorkflowInterface
interface OrderProcessingWorkflow {
    @WorkflowMethod
    fun processOrder(orderRequest: OrderRequest): OrderResult
}

class OrderProcessingWorkflowImpl : OrderProcessingWorkflow {
    override fun processOrder(orderRequest: OrderRequest): OrderResult {
        // Validate inputs
        validateOrderRequest(orderRequest)
        
        // Process through activities
        val validation = validationActivity.validateOrder(orderRequest)
        val pricing = pricingActivity.calculateTotal(orderRequest.items)
        val payment = paymentActivity.processPayment(orderRequest.paymentMethod, pricing.total)
        val fulfillment = fulfillmentActivity.createShipment(orderRequest)
        
        return OrderResult(
            orderId = generateOrderId(),
            status = OrderStatus.CONFIRMED,
            totalAmount = pricing.total,
            estimatedDelivery = fulfillment.estimatedDelivery,
            trackingInfo = fulfillment.trackingInfo,
            processingSteps = listOf(validation.step, pricing.step, payment.step, fulfillment.step)
        )
    }
}
```

## How to Run

Test with complex input data:
```kotlin
val orderRequest = OrderRequest(
    customerId = "customer123",
    items = listOf(
        OrderItem("product1", 2, BigDecimal("29.99")),
        OrderItem("product2", 1, BigDecimal("49.99"))
    ),
    shippingAddress = Address("123 Main St", "Anytown", "CA", "12345", "US"),
    paymentMethod = PaymentMethod.CREDIT_CARD
)

val result = workflow.processOrder(orderRequest)
``` 