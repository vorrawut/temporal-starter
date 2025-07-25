---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Workflow Input/Output

## Lesson 7: Advanced Data Modeling Patterns

Master advanced data modeling patterns for Temporal workflows, including complex input validation, rich output structures, and data transformation strategies.

---

# Objective

By the end of this lesson, you will understand:

- ✅ **Complex input data modeling** with structured objects
- ✅ **Input validation patterns** for robust workflows
- ✅ **Rich output data structures** with comprehensive results
- ✅ **Data transformation strategies** between workflows and activities
- ✅ **Serialization considerations** for Temporal compatibility
- ✅ **Error context in results** for better debugging

---

# 1. **Complex Input Data Modeling**

## **Structured Input Objects**

```kotlin
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
    val unitPrice: BigDecimal,
    val customizations: List<ProductCustomization> = emptyList()
)
```

**Build complex, structured data models for realistic business scenarios**

---

# Input Validation Patterns

```kotlin
class OrderProcessingWorkflowImpl : OrderProcessingWorkflow {
    
    override fun processOrder(orderRequest: OrderRequest): OrderResult {
        // Validate inputs before processing
        validateOrderRequest(orderRequest)
        
        // Process with validated data
        return processValidatedOrder(orderRequest)
    }
    
    private fun validateOrderRequest(request: OrderRequest) {
        require(request.customerId.isNotBlank()) { "Customer ID is required" }
        require(request.items.isNotEmpty()) { "Order must contain at least one item" }
        require(request.items.all { it.quantity > 0 }) { "All items must have positive quantity" }
        require(request.items.all { it.unitPrice > BigDecimal.ZERO }) { "All items must have positive price" }
    }
}
```

---

# Why Validate Early?

## **Benefits of Early Validation:**

- ✅ **Fail fast** - Don't waste time on invalid data
- ✅ **Clear error messages** - Specific validation feedback
- ✅ **Resource efficiency** - Don't consume workflow/activity resources
- ✅ **Better debugging** - Know exactly what went wrong
- ✅ **User experience** - Immediate feedback on problems

**Always validate in workflows before calling activities**

---

# 2. **Rich Output Data Structures**

## **Comprehensive Result Objects**

```kotlin
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
```

---

# More Result Structures

```kotlin
data class OrderMetadata(
    val processingTime: Duration,
    val version: String,
    val systemInfo: SystemInfo
)
```

## **Why Rich Results Matter:**

- ✅ **Audit trail** - Track what happened and when
- ✅ **Debugging** - Understand processing flow
- ✅ **Monitoring** - Performance metrics and timing
- ✅ **Business intelligence** - Rich data for analysis
- ✅ **User feedback** - Detailed status information

---

# 3. **Data Transformation Strategies**

## **Workflow-to-Activity Data Flow**

```kotlin
class OrderProcessingWorkflowImpl : OrderProcessingWorkflow {
    
    override fun processOrder(orderRequest: OrderRequest): OrderResult {
        // Transform request data for different activities
        
        val validationInput = ValidationInput.from(orderRequest)
        val validationResult = validationActivity.validateOrder(validationInput)
        
        val pricingInput = PricingInput.from(orderRequest, validationResult)
        val pricingResult = pricingActivity.calculatePricing(pricingInput)
        
        val paymentInput = PaymentInput.from(orderRequest, pricingResult)
        val paymentResult = paymentActivity.processPayment(paymentInput)
        
        // Aggregate results into comprehensive output
        return OrderResult.builder()
            .withOrderRequest(orderRequest)
            .withValidation(validationResult)
            .withPricing(pricingResult)
            .withPayment(paymentResult)
            .build()
    }
}
```

---

# Activity-Specific Data Models

```kotlin
// Validation activity input/output
data class ValidationInput(
    val customerId: String,
    val items: List<ItemToValidate>,
    val shippingAddress: Address
) {
    companion object {
        fun from(orderRequest: OrderRequest): ValidationInput {
            return ValidationInput(
                customerId = orderRequest.customerId,
                items = orderRequest.items.map { ItemToValidate.from(it) },
                shippingAddress = orderRequest.shippingAddress
            )
        }
    }
}
```

---

# More Data Transformation

```kotlin
// Pricing activity input/output
data class PricingInput(
    val items: List<PricingItem>,
    val customerId: String,
    val promotionCodes: List<String>
) {
    companion object {
        fun from(orderRequest: OrderRequest, validationResult: ValidationResult): PricingInput {
            return PricingInput(
                items = orderRequest.items.map { PricingItem.from(it) },
                customerId = orderRequest.customerId,
                promotionCodes = validationResult.applicablePromotions
            )
        }
    }
}
```

**Each activity gets exactly the data it needs in the format it expects**

---

# 4. **Serialization Considerations**

## **Temporal-Safe Data Types**

```kotlin
// Good: Temporal-serializable types
data class OrderRequest(
    val customerId: String,                    // ✅ String
    val orderDate: LocalDateTime,              // ✅ LocalDateTime
    val totalAmount: BigDecimal,              // ✅ BigDecimal
    val items: List<OrderItem>,               // ✅ List of data classes
    val metadata: Map<String, String>         // ✅ Map with serializable types
)
```

**Stick to standard, serializable types for reliable data transfer**

---

# Avoid Non-Serializable Types

```kotlin
// Avoid: Non-serializable types
data class BadOrderRequest(
    val customerId: String,
    val callback: () -> Unit,                 // ❌ Function
    val inputStream: InputStream,             // ❌ Stream
    val complexObject: SomeComplexClass       // ❌ Non-serializable class
)
```

## **Safe Data Types for Temporal:**
- ✅ **Primitives**: String, Int, Long, Double, Boolean
- ✅ **Collections**: List, Set, Map (with serializable contents)
- ✅ **Time**: LocalDate, LocalDateTime, Instant, Duration
- ✅ **Numbers**: BigDecimal, BigInteger
- ✅ **Data classes**: With serializable properties

---

# Version-Safe Evolution

```kotlin
data class OrderRequest(
    val customerId: String,
    val items: List<OrderItem>,
    val shippingAddress: Address,
    
    // Safe to add optional fields
    val priority: OrderPriority = OrderPriority.NORMAL,
    val giftMessage: String? = null,
    val requestedDeliveryDate: LocalDate? = null
) {
    // Version handling
    companion object {
        const val CURRENT_VERSION = "2.1"
    }
}
```

**Add new fields as optional with defaults to maintain backward compatibility**

---

# 5. **Error Context in Results**

## **Rich Error Information**

```kotlin
sealed class OrderResult {
    abstract val orderId: String
    abstract val processingSteps: List<ProcessingStep>
    
    data class Success(
        override val orderId: String,
        val totalAmount: BigDecimal,
        val estimatedDelivery: LocalDate,
        override val processingSteps: List<ProcessingStep>
    ) : OrderResult()
    
    data class Failure(
        override val orderId: String,
        val errorType: ErrorType,
        val errorMessage: String,
        val failedStep: String,
        val recoveryActions: List<RecoveryAction>,
        override val processingSteps: List<ProcessingStep>
    ) : OrderResult()
```

---

# More Result Types

```kotlin
    data class PartialSuccess(
        override val orderId: String,
        val completedSteps: List<String>,
        val failedSteps: List<FailedStep>,
        val canRetry: Boolean,
        override val processingSteps: List<ProcessingStep>
    ) : OrderResult()
}
```

**Use sealed classes to represent different outcome scenarios clearly**

---

# Best Practices

## ✅ **Input Design**

### **1. Use Data Classes**

```kotlin
// Good: Immutable data classes
data class CreateUserRequest(
    val email: String,
    val name: String,
    val preferences: UserPreferences
)

// Bad: Mutable classes
class CreateUserRequest {
    var email: String? = null
    var name: String? = null
}
```

**Immutable data classes provide thread safety and clear contracts**

---

# More Input Best Practices

### **2. Validate Early**

```kotlin
override fun processOrder(request: OrderRequest): OrderResult {
    // Validate in workflow before calling activities
    validateOrderRequest(request)
    
    // Now safe to process
    return processValidatedOrder(request)
}
```

### **3. Use Builders for Complex Objects**

```kotlin
class OrderRequestBuilder {
    private var customerId: String? = null
    private var items: MutableList<OrderItem> = mutableListOf()
    
    fun customerId(id: String) = apply { this.customerId = id }
    fun addItem(item: OrderItem) = apply { this.items.add(item) }
    
    fun build(): OrderRequest {
        requireNotNull(customerId) { "Customer ID is required" }
        require(items.isNotEmpty()) { "At least one item required" }
        
        return OrderRequest(customerId = customerId!!, items = items.toList())
    }
}
```

---

# ✅ Output Design

### **1. Include Processing Context**

```kotlin
data class ProcessingResult(
    val success: Boolean,
    val data: ResultData?,
    val error: ErrorInfo?,
    val processingTime: Duration,
    val stepResults: List<StepResult>
)
```

### **2. Use Sealed Classes for Status**

```kotlin
sealed class OrderStatus {
    object Processing : OrderStatus()
    object Confirmed : OrderStatus()
    data class Shipped(val trackingNumber: String) : OrderStatus()
    data class Failed(val reason: String) : OrderStatus()
}
```

---

# More Output Best Practices

### **3. Provide Audit Trail**

```kotlin
data class OrderResult(
    val orderId: String,
    val finalStatus: OrderStatus,
    val auditTrail: List<AuditEvent>
)

data class AuditEvent(
    val timestamp: Instant,
    val action: String,
    val actor: String,
    val details: Map<String, Any>
)
```

**Rich audit trails enable debugging and compliance**

---

# ❌ Common Anti-Patterns

### **1. Overly Complex Input Objects**

```kotlin
// Bad: Too many nested levels
data class OverlyComplexRequest(
    val level1: Level1Data,
    val level2: Map<String, Level2Data>,
    val level3: List<Map<String, List<Level3Data>>>
)
```

### **2. Stringly Typed Data**

```kotlin
// Bad: Everything as strings
data class BadRequest(
    val amount: String,           // Should be BigDecimal
    val date: String,            // Should be LocalDate
    val status: String           // Should be enum
)
```

---

# Final Anti-Pattern

### **3. Missing Error Context**

```kotlin
// Bad: Minimal error info
data class BadResult(
    val success: Boolean,
    val error: String?
)

// Good: Rich error context
data class GoodResult(
    val success: Boolean,
    val data: ResultData?,
    val error: DetailedError?
)
```

**Always provide enough context for debugging and user feedback**

---

# 💡 Key Takeaways

## **What You've Learned:**

- ✅ **Structured input objects** with proper validation
- ✅ **Rich output structures** with audit trails and metadata
- ✅ **Data transformation patterns** for activity-specific models
- ✅ **Serialization best practices** for Temporal compatibility
- ✅ **Error-rich results** with detailed context
- ✅ **Version-safe evolution** for long-term maintainability

---

# 🚀 Next Steps

**You now master workflow data modeling!**

## **Lesson 8 will cover:**
- Activity retry and timeout strategies
- Building resilient fault-tolerant workflows
- Handling external service failures
- Advanced retry patterns and circuit breakers

**Ready to build bulletproof workflows? Let's continue! 🎉** 