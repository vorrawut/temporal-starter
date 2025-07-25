---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Workshop 7: Workflow Input/Output

## Advanced Data Modeling Patterns

*Create workflows that handle complex input parameters and return rich output data structures*

---

# What we want to build

Create **workflows that handle complex input parameters** and return **rich output data structures**. 

This lesson focuses on **advanced data modeling** and **input validation patterns**.

---

# Expecting Result

## By the end of this workshop, you'll have:

- ✅ **A workflow that accepts complex input objects**
- ✅ **Proper input validation before processing**
- ✅ **Rich output objects with detailed results**
- ✅ **Data transformation patterns** between workflow and activities

---

# Code Steps

## Step 1: Create Input Data Classes

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
```

**Build structured, complex input objects for realistic business scenarios**

---

# More Input Data Classes

```kotlin
data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String
)

enum class PaymentMethod {
    CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER
}
```

**Use enums and structured objects for type safety**

---

# Step 2: Create Output Data Classes

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

**Rich output objects provide comprehensive result information**

---

# Additional Output Classes

```kotlin
data class TrackingInfo(
    val trackingNumber: String,
    val carrier: String,
    val estimatedDelivery: LocalDate
)

data class ProcessingStep(
    val stepName: String,
    val status: String,
    val timestamp: LocalDateTime,
    val details: Map<String, Any> = emptyMap()
)
```

**Include audit trail and tracking information for observability**

---

# Step 3: Implement Order Processing Workflow

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
        // Continued on next slide...
    }
}
```

---

# Complete Workflow Implementation

```kotlin
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
```

---

# Input Validation Implementation

```kotlin
private fun validateOrderRequest(orderRequest: OrderRequest) {
    require(orderRequest.customerId.isNotBlank()) { 
        "Customer ID is required" 
    }
    require(orderRequest.items.isNotEmpty()) { 
        "Order must contain at least one item" 
    }
    require(orderRequest.items.all { it.quantity > 0 }) { 
        "All items must have positive quantity" 
    }
    require(orderRequest.items.all { it.unitPrice > BigDecimal.ZERO }) { 
        "All items must have positive price" 
    }
}
```

**Always validate inputs early to fail fast with clear error messages**

---

# How to Run

## Test with complex input data:

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
println("Order processed: ${result.orderId}")
println("Status: ${result.status}")
println("Total: $${result.totalAmount}")
println("Estimated delivery: ${result.estimatedDelivery}")
```

---

# Expected Output

```
Order processed: order_abc123456
Status: CONFIRMED
Total: $109.97
Estimated delivery: 2024-01-15
Processing steps:
  1. Validation: COMPLETED at 2024-01-10T10:00:00
  2. Pricing: COMPLETED at 2024-01-10T10:00:05
  3. Payment: COMPLETED at 2024-01-10T10:00:10
  4. Fulfillment: COMPLETED at 2024-01-10T10:00:15
```

**Rich, detailed output provides complete transaction context**

---

# Key Patterns Demonstrated

## ✅ **Input Design:**
- **Structured objects** with clear relationships
- **Type safety** through enums and data classes
- **Early validation** with descriptive error messages

## ✅ **Output Design:**
- **Comprehensive results** with audit trail
- **Status tracking** through the entire process
- **Rich metadata** for debugging and monitoring

## ✅ **Data Transformation:**
- **Clean mapping** between input and activity parameters
- **Result aggregation** from multiple activities

---

# 💡 Key Takeaways

## **What You've Learned:**

- ✅ **Complex input modeling** with nested data structures
- ✅ **Input validation patterns** for robust error handling
- ✅ **Rich output design** with comprehensive result information
- ✅ **Data transformation strategies** between workflows and activities
- ✅ **Type safety** through proper use of data classes and enums
- ✅ **Observability** through detailed processing steps

---

# 🚀 Next Steps

**You now master advanced workflow data patterns!**

## **Ready for:**
- Activity retry and timeout strategies
- Error handling and compensation patterns
- Workflow signals and queries
- Production deployment patterns

**Let's build bulletproof distributed systems! 🎉** 