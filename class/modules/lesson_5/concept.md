---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Adding a Simple Activity

## Lesson 5: Understanding Workflow-Activity Pattern

Learn how workflows orchestrate business processes while activities perform the actual work, and how they communicate through Temporal's infrastructure.

---

# Objective

By the end of this lesson, you will understand:

- ✅ **Workflow-Activity relationship** and division of responsibilities
- ✅ **Activity Stubs** - the magic bridge between workflows and activities
- ✅ **Activity Options** and proper timeout configuration
- ✅ **Logging and observability** best practices
- ✅ **Error handling basics** for resilient workflows

---

# 1. **Workflow-Activity Relationship**

## **Division of Responsibilities**

### **Workflow: Orchestration and Coordination**
```kotlin
class CalculatorWorkflowImpl : CalculatorWorkflow {
    override fun add(a: Int, b: Int): Int {
        // Log the operation
        // Call activity
        // Handle result
        return mathActivity.performAddition(a, b)
    }
}
```

**Workflow = Conductor of an Orchestra**

---

# Activity: Actual Work and Business Logic

```kotlin
class MathActivityImpl : MathActivity {
    override fun performAddition(a: Int, b: Int): Int {
        // Do the actual calculation
        // Handle any complex logic
        // Access external systems if needed
        return a + b
    }
}
```

**Activity = Musicians doing the real work**

---

# Why This Separation?

## **Key Benefits:**

- ✅ **Reliability**: If an activity fails, only that step needs to retry
- ✅ **Scalability**: Activities can be distributed across different workers
- ✅ **Maintainability**: Business logic is isolated and testable
- ✅ **Flexibility**: Different activities can have different retry policies

**Clean separation = robust distributed systems**

---

# 2. **Activity Stubs - The Magic Bridge**

## **What is an Activity Stub?**
An activity stub is a **proxy** that makes activity calls look like regular method calls while handling all the Temporal infrastructure behind the scenes.

```kotlin
// Creating a stub
private val mathActivity = Workflow.newActivityStub(
    MathActivity::class.java,
    ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofSeconds(30))
        .build()
)

// Using the stub (looks like a normal method call!)
val result = mathActivity.performAddition(5, 3)
```

---

# What Happens Behind the Scenes

```
Workflow ──► Activity Stub ──► Temporal Server ──► Activity Queue
   ▲                                                      │
   │                                                      ▼
   └── Result ◄── Temporal Server ◄── Worker ◄── Activity Implementation
```

## **The Magic Steps:**
1. **Workflow calls** stub method
2. **Temporal serializes** the call and parameters
3. **Temporal queues** the activity task
4. **Worker picks up** the activity task
5. **Worker executes** the actual activity implementation
6. **Temporal returns** the result to the workflow
7. **Workflow continues** with the result

---

# 3. **Activity Options and Configuration**

## **Essential Timeout Settings**

```kotlin
ActivityOptions.newBuilder()
    .setStartToCloseTimeout(Duration.ofSeconds(30))    // Max execution time
    .setScheduleToCloseTimeout(Duration.ofMinutes(5))  // Max total time (including retries)
    .setScheduleToStartTimeout(Duration.ofMinutes(1))  // Max time in queue
    .build()
```

**Configure timeouts based on your operation's needs!**

---

# Timeout Types Explained

## **Three Critical Timeout Types:**

- **StartToCloseTimeout**: How long the activity can run once it starts
- **ScheduleToCloseTimeout**: Total time allowed (including retries and queue time)
- **ScheduleToStartTimeout**: How long it can wait in the queue before starting

**Think of it as setting expectations for performance!**

---

# Choosing Appropriate Timeouts

```kotlin
// Quick operations
ActivityOptions.newBuilder()
    .setStartToCloseTimeout(Duration.ofSeconds(10))
    .build()

// Database operations
ActivityOptions.newBuilder()
    .setStartToCloseTimeout(Duration.ofSeconds(30))
    .build()

// External API calls
ActivityOptions.newBuilder()
    .setStartToCloseTimeout(Duration.ofMinutes(2))
    .build()

// File processing
ActivityOptions.newBuilder()
    .setStartToCloseTimeout(Duration.ofMinutes(10))
    .build()
```

---

# 4. **Logging and Observability**

## **Workflow Logging**
```kotlin
// In workflows, use Workflow.getLogger()
val logger = Workflow.getLogger(this::class.java)
logger.info("Workflow started with parameters: $a, $b")
```

## **Activity Logging**
```kotlin
// In activities, use regular logging
private val logger = KotlinLogging.logger {}
logger.info { "Activity processing: $a + $b" }
```

---

# Why Different Logging?

## **Key Differences:**

- **Workflow logs** are part of the workflow history and are replayed
- **Activity logs** are immediate and not replayed
- **Temporal Web UI** shows workflow logs in the execution timeline
- **Activity logs** appear in your application logs

**Use the right logging approach for the right component!**

---

# 5. **Error Handling Basics**

## **Activity Failures**

```kotlin
@Component
class MathActivityImpl : MathActivity {
    override fun performAddition(a: Int, b: Int): Int {
        // Simulate a failure condition
        if (a < 0 || b < 0) {
            throw IllegalArgumentException("Negative numbers not supported")
        }
        
        return a + b
    }
}
```

**Activities can fail, and that's okay!**

---

# Workflow Error Handling

```kotlin
class CalculatorWorkflowImpl : CalculatorWorkflow {
    override fun add(a: Int, b: Int): Int {
        return try {
            mathActivity.performAddition(a, b)
        } catch (e: Exception) {
            // Workflow can handle activity failures
            logger.warn("Addition failed: ${e.message}")
            0 // Default value or alternative logic
        }
    }
}
```

**Workflows can gracefully handle activity failures!**

---

# Best Practices

## ✅ **Activity Design**

### **1. Keep Activities Focused**

```kotlin
// Good: Single responsibility
@ActivityMethod
fun calculateSum(numbers: List<Int>): Int

@ActivityMethod  
fun validateInput(data: String): Boolean

// Bad: Multiple responsibilities
@ActivityMethod
fun calculateSumAndValidateAndLog(data: String): Int
```

---

# More Activity Best Practices

### **2. Make Activities Idempotent**

```kotlin
override fun createUser(userData: UserData): String {
    // Check if user already exists
    val existing = userRepository.findByEmail(userData.email)
    if (existing != null) {
        return existing.id // Safe to call multiple times
    }
    
    return userRepository.create(userData).id
}
```

**Activities should be safe to retry!**

---

# Even More Activity Best Practices

### **3. Use Descriptive Method Names**

```kotlin
// Good: Clear what the activity does
fun performAddition(a: Int, b: Int): Int
fun validateCreditCard(cardNumber: String): ValidationResult
fun sendWelcomeEmail(userId: String): EmailResult

// Bad: Vague names
fun doWork(data: Any): Any
fun process(input: String): String
```

**Make your intent crystal clear!**

---

# ✅ Workflow Design

### **1. Keep Workflows Simple**

```kotlin
// Good: Simple orchestration
override fun processOrder(order: Order): OrderResult {
    val payment = paymentActivity.processPayment(order)
    val fulfillment = fulfillmentActivity.shipOrder(order)
    return OrderResult(payment, fulfillment)
}

// Bad: Complex business logic in workflow
override fun processOrder(order: Order): OrderResult {
    val discountedPrice = order.price * 0.9 // This should be in an activity
    // ...
}
```

**Workflows orchestrate, activities execute!**

---

# Workflow Timeout Strategy

### **2. Use Meaningful Timeouts**

```kotlin
// Consider the actual operation when setting timeouts
private val quickActivity = Workflow.newActivityStub(
    QuickActivity::class.java,
    ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofSeconds(5))
        .build()
)

private val slowActivity = Workflow.newActivityStub(
    SlowActivity::class.java,
    ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofMinutes(5))
        .build()
)
```

---

# ❌ Common Mistakes

### **1. Calling Activities Directly**

```kotlin
// Bad: Direct instantiation
class BadWorkflowImpl : MyWorkflow {
    override fun process(): String {
        val activity = MathActivityImpl() // Wrong!
        return activity.performAddition(1, 2)
    }
}

// Good: Use stub
class GoodWorkflowImpl : MyWorkflow {
    private val activity = Workflow.newActivityStub(...)
    
    override fun process(): String {
        return activity.performAddition(1, 2) // Correct!
    }
}
```

---

# More Common Mistakes

### **2. No Timeout Configuration**

```kotlin
// Bad: No timeouts specified
private val activity = Workflow.newActivityStub(
    MyActivity::class.java
)

// Good: Explicit timeouts
private val activity = Workflow.newActivityStub(
    MyActivity::class.java,
    ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofSeconds(30))
        .build()
)
```

**Always specify timeouts explicitly!**

---

# Final Common Mistake

### **3. Complex Logic in Workflows**

```kotlin
// Bad: Business logic in workflow
override fun calculateDiscount(order: Order): Double {
    var discount = 0.0
    if (order.amount > 100) {
        discount = 0.1
    }
    // Complex calculations should be in activities
    return discount
}

// Good: Delegate to activity
override fun calculateDiscount(order: Order): Double {
    return discountActivity.calculateDiscount(order)
}
```

---

# 💡 Key Takeaways

## **What You've Learned:**

- ✅ **Workflows orchestrate**, activities execute business logic
- ✅ **Activity stubs** handle all Temporal infrastructure magic
- ✅ **Timeout configuration** is critical for reliability
- ✅ **Proper logging** provides visibility into execution
- ✅ **Error handling** enables graceful failure recovery
- ✅ **Best practices** ensure maintainable code

---

# 🚀 Next Steps

**You now understand the fundamental workflow-activity pattern!**

## **Lesson 6 will show you:**
- How to organize workflows and activities into clean, maintainable structures
- Software engineering best practices for Temporal applications
- Complex multi-activity workflows
- Advanced error handling patterns

**Ready to build more sophisticated workflows? Let's continue! 🎉** 