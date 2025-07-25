---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Child Workflows & continueAsNew

## Lesson 12: Advanced Workflow Orchestration

Master advanced workflow orchestration patterns using child workflows for hierarchical decomposition and `continueAsNew` for managing long-running processes.

---

# Objective

By the end of this lesson, you will understand:

- ✅ **Child Workflows vs Activities** - when to use each pattern
- ✅ **Child workflow patterns** - sequential, parallel, and dynamic
- ✅ **`continueAsNew` mechanism** for long-running processes
- ✅ **Workflow hierarchies** and orchestration strategies
- ✅ **Scaling workflows** through composition
- ✅ **Best practices** for complex workflow systems

---

# 1. **Child Workflows vs Activities**

## **Decision Matrix**

| Aspect | **Child Workflows** | **Activities** |
|--------|-------------------|----------------|
| **Purpose** | Complex business logic | External operations |
| **Durability** | Full workflow semantics | Simple execution model |
| **Visibility** | Own workflow execution | Part of parent execution |
| **Scalability** | Independent scaling | Scales with parent |
| **Complexity** | Can have own signals/queries | Single operation |
| **Testing** | Can be tested independently | Tested with parent |
| **Monitoring** | Separate workflow metrics | Part of parent metrics |

---

# When to Use Child Workflows

```kotlin
// Good use cases for child workflows:

// 1. Complex multi-step processes
@WorkflowInterface
interface PaymentProcessingWorkflow {
    @WorkflowMethod
    fun processPayment(payment: PaymentRequest): PaymentResult
    
    @SignalMethod
    fun updatePaymentMethod(newMethod: PaymentMethod)
    
    @QueryMethod
    fun getPaymentStatus(): PaymentStatus
}

// 2. Independent business domains
@WorkflowInterface
interface InventoryManagementWorkflow {
    @WorkflowMethod
    fun manageInventory(request: InventoryRequest): InventoryResult
}

// 3. Long-running sub-processes
@WorkflowInterface
interface ShippingTrackingWorkflow {
    @WorkflowMethod
    fun trackShipment(shipment: ShipmentInfo): TrackingResult
}
```

---

# When to Use Activities Instead

```kotlin
// Good use cases for activities:

// 1. Simple external calls
@ActivityInterface
interface EmailService {
    @ActivityMethod
    fun sendEmail(email: EmailMessage): Boolean
}

// 2. Database operations
@ActivityInterface
interface OrderRepository {
    @ActivityMethod
    fun saveOrder(order: Order): String
}

// 3. API calls
@ActivityInterface
interface PaymentGateway {
    @ActivityMethod
    fun chargeCard(cardInfo: CardInfo, amount: BigDecimal): ChargeResult
}
```

**Use child workflows for complex business logic, activities for simple operations**

---

# 2. **Child Workflow Patterns**

## **Sequential Child Workflows**

```kotlin
class OrderProcessingWorkflowImpl : OrderProcessingWorkflow {
    
    override fun processOrder(request: OrderRequest): OrderResult {
        // Execute child workflows in sequence
        val paymentResult = paymentWorkflow.processPayment(request.payment)
        
        if (!paymentResult.success) {
            return OrderResult.failed("Payment failed")
        }
        
        val inventoryResult = inventoryWorkflow.reserveItems(request.items)
        
        if (!inventoryResult.success) {
            // Compensate payment
            compensationWorkflow.refundPayment(paymentResult.transactionId)
            return OrderResult.failed("Inventory unavailable")
        }
        
        val shippingResult = shippingWorkflow.arrangeShipping(request.address)
        
        return OrderResult.success(paymentResult, inventoryResult, shippingResult)
    }
}
```

---

# Parallel Child Workflows

```kotlin
class ParallelProcessingWorkflowImpl : ParallelProcessingWorkflow {
    
    override fun processOrderInParallel(request: OrderRequest): OrderResult {
        // Start child workflows in parallel using Async
        val paymentFuture = Async.function { paymentWorkflow.processPayment(request.payment) }
        val inventoryFuture = Async.function { inventoryWorkflow.checkAvailability(request.items) }
        val shippingFuture = Async.function { shippingWorkflow.calculateShipping(request.address) }
        
        // Wait for all to complete
        val paymentResult = paymentFuture.get()
        val inventoryResult = inventoryFuture.get()
        val shippingResult = shippingFuture.get()
        
        // Proceed with sequential steps based on parallel results
        if (paymentResult.success && inventoryResult.available) {
            val finalShipping = shippingWorkflow.arrangeShipping(request.address)
            return OrderResult.success(paymentResult, inventoryResult, finalShipping)
        }
        
        return OrderResult.failed("Parallel validation failed")
    }
}
```

---

# Dynamic Child Workflow Creation

```kotlin
class DynamicOrderWorkflowImpl : DynamicOrderWorkflow {
    
    override fun processOrderDynamically(request: OrderRequest): OrderResult {
        val results = mutableListOf<ProcessingResult>()
        
        // Create child workflows dynamically based on order characteristics
        request.items.forEach { item ->
            when (item.category) {
                ItemCategory.DIGITAL -> {
                    val digitalWorkflow = createDigitalFulfillmentWorkflow(item)
                    results.add(digitalWorkflow.processDigitalItem(item))
                }
                ItemCategory.PHYSICAL -> {
                    val physicalWorkflow = createPhysicalFulfillmentWorkflow(item)
                    results.add(physicalWorkflow.processPhysicalItem(item))
                }
                ItemCategory.SUBSCRIPTION -> {
                    val subscriptionWorkflow = createSubscriptionWorkflow(item)
                    results.add(subscriptionWorkflow.setupSubscription(item))
                }
            }
        }
        
        return OrderResult.fromResults(results)
    }
}
```

---

# 💡 Key Takeaways

## **What You've Learned:**

- ✅ **Child workflows** enable complex hierarchical business logic
- ✅ **Sequential patterns** for dependent operations
- ✅ **Parallel patterns** for independent operations
- ✅ **Dynamic creation** for variable complexity scenarios
- ✅ **Clear decision criteria** for child workflows vs activities

---

# 🚀 Next Steps

**You now understand advanced workflow orchestration!**

## **Lesson 13 will cover:**
- Workflow versioning with `Workflow.getVersion()`
- Safe workflow evolution patterns
- Breaking vs additive changes
- Migration and rollback strategies

**Ready to master workflow evolution? Let's continue! 🎉** 