---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# HelloWorkflow - Your First Workflow & Activity

## Lesson 4: Understanding the Building Blocks

Learn the fundamental building blocks of Temporal by creating your first workflow and activity.

---

# Objective

By the end of this lesson, you will understand:

- ✅ **How workflows orchestrate activities**
- ✅ **The role of each component**
- ✅ **How they work together** to create reliable distributed applications
- ✅ **Best practices** for workflow and activity design

---

# Key Concepts

## 1. **Workflow Fundamentals**

### **What is a Workflow?**
A workflow is the **orchestration logic** that coordinates multiple steps in a business process. 

Think of it as the **conductor of an orchestra** - it doesn't play the instruments, but it coordinates when each section plays.

---

# Workflow Interface Example

```kotlin
@WorkflowInterface
interface HelloWorkflow {
    @WorkflowMethod
    fun sayHello(name: String): String
}
```

**Simple, clean interface definition!**

---

# Workflow Characteristics

## **Key Properties:**
- ✅ **Deterministic**: Given same input, always produces same output
- ✅ **Replayable**: Can be stopped and resumed from any point
- ✅ **Orchestration-focused**: Coordinates activities, doesn't do heavy lifting
- ✅ **Long-running**: Can run for days, weeks, or months
- ✅ **Fault-tolerant**: Survives worker crashes and restarts

---

# What Workflows Should NOT Do

```kotlin
// ❌ BAD: Non-deterministic operations in workflow
class BadWorkflowImpl : MyWorkflow {
    override fun process(): String {
        val random = Random.nextInt() // Non-deterministic!
        val now = System.currentTimeMillis() // Non-deterministic!
        callExternalAPI() // Should be in an activity!
        return "result"
    }
}
```

**Problem**: These operations can't be replayed consistently!

---

# What Workflows SHOULD Do

```kotlin
// ✅ GOOD: Deterministic orchestration only
class GoodWorkflowImpl : MyWorkflow {
    private val activity = Workflow.newActivityStub(...)
    
    override fun process(): String {
        val result = activity.doWork() // Delegate to activity
        return result
    }
}
```

**Result**: Clean orchestration that can be reliably replayed!

---

# 2. **Activity Fundamentals**

### **What is an Activity?**
An activity is a **unit of work** that performs the actual business logic. 

It's like a **musician in the orchestra** - it does the real work when the conductor (workflow) tells it to.

---

# Activity Interface Example

```kotlin
@ActivityInterface
interface GreetingActivity {
    @ActivityMethod
    fun generateGreeting(name: String): String
}
```

**Activities handle the real business logic!**

---

# Activity Characteristics

## **Key Properties:**
- ✅ **Non-deterministic**: Can make external calls, use random numbers, etc.
- ✅ **Retryable**: Can be automatically retried if they fail
- ✅ **Short-lived**: Typically run for seconds or minutes
- ✅ **Stateless**: Don't maintain state between calls
- ✅ **Idempotent**: Safe to retry without side effects

---

# What Activities CAN Do

```kotlin
@Component
class MyActivityImpl : MyActivity {
    override fun doWork(): String {
        // ✅ All of these are fine in activities:
        val apiResult = httpClient.get("https://api.example.com")
        val dbResult = database.query("SELECT * FROM users")
        val randomValue = Random.nextInt()
        val currentTime = System.currentTimeMillis()
        sendEmail(recipient, message)
        
        return processResults(apiResult, dbResult)
    }
}
```

**Activities can do anything workflows cannot!**

---

# 3. **Workflow ↔ Activity Communication**

## **Activity Stubs**
Workflows don't call activities directly. They use **activity stubs** (proxies) configured with timeouts and retry policies.

**Think of stubs as remote controls for activities!**

---

# Activity Stub Configuration

```kotlin
class MyWorkflowImpl : MyWorkflow {
    
    // Create activity stub with configuration
    private val myActivity = Workflow.newActivityStub(
        MyActivity::class.java,
        ActivityOptions.newBuilder()
            .setScheduleToCloseTimeout(Duration.ofMinutes(5))
            .setStartToCloseTimeout(Duration.ofMinutes(2))
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setMaximumAttempts(3)
                    .build()
            )
            .build()
    )
}
```

---

# Using Activity Stubs

```kotlin
override fun process(): String {
    // Call looks like a regular method call
    return myActivity.doWork()
}
```

## **Magic Behind the Scenes:**
- ✅ **Automatic retries** if activity fails
- ✅ **Timeout handling** if activity takes too long
- ✅ **State persistence** between attempts
- ✅ **Failure recovery** after crashes

---

# Data Flow Overview

```
Client → Workflow Stub → Temporal Server → Worker
                                             ↓
                                        Workflow Instance
                                             ↓
                                        Activity Stub
                                             ↓
                                        Activity Instance
                                             ↓
                                        Return Value
                                             ↓
                         Client ← Temporal Server ← Worker
```

---

# 4. **Configuration and Registration**

## **Worker Registration**
Workers must register both workflow and activity implementations **before starting**:

---

# Registration Example

```kotlin
@PostConstruct
fun startWorker() {
    val worker = workerFactory.newWorker("my-task-queue")
    
    // Register workflow implementations
    worker.registerWorkflowImplementationTypes(
        MyWorkflowImpl::class.java,
        AnotherWorkflowImpl::class.java
    )
    
    // Register activity implementations
    worker.registerActivitiesImplementations(
        MyActivityImpl(),
        AnotherActivityImpl()
    )
    
    workerFactory.start()
}
```

---

# Task Queues Connection

```kotlin
// Worker listens to this queue
val worker = workerFactory.newWorker("hello-queue")

// Client sends work to the same queue
val workflow = client.newWorkflowStub(
    HelloWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("hello-queue") // Same name!
        .build()
)
```

**Task queues connect workflows with workers!**

---

# 5. **Execution Lifecycle**

## **Complete Flow Example:**

1. **Client creates workflow stub**
2. **Client calls workflow method**
3. **Temporal routes to worker**
4. **Worker creates workflow instance**
5. **Workflow calls activity via stub**
6. **Worker executes activity**
7. **Activity returns result**
8. **Workflow returns result**
9. **Client receives result**

---

# Code Example: Complete Flow

```kotlin
// 1. Client creates workflow stub
val workflow = client.newWorkflowStub(HelloWorkflow::class.java, options)

// 2. Client calls workflow method (asynchronous behind scenes)
val result = workflow.sayHello("John") 

// 3-9. Magic happens in Temporal...
// Workflow calls: val greeting = greetingActivity.generateGreeting("John")
// Activity executes and returns result
// Client receives final result
```

---

# Timeline in Temporal Web UI

When you look at a workflow in the Web UI, you'll see:

1. **Workflow Started** - Workflow instance created
2. **Activity Scheduled** - Activity task sent to queue
3. **Activity Started** - Worker picked up activity task
4. **Activity Completed** - Activity finished successfully
5. **Workflow Completed** - Workflow returned final result

**Visual feedback for every step!**

---

# 6. **Error Handling and Retries**

## **Activity Retry Policies**

```kotlin
private val riskyActivity = Workflow.newActivityStub(
    RiskyActivity::class.java,
    ActivityOptions.newBuilder()
        .setRetryOptions(
            RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(1))
                .setMaximumInterval(Duration.ofMinutes(1))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(5)
                .build()
        )
        .build()
)
```

---

# Timeout Types

## **Four Important Timeout Types:**

- **ScheduleToCloseTimeout**: Total time for activity (including retries)
- **StartToCloseTimeout**: Time for single activity execution
- **ScheduleToStartTimeout**: Maximum time in queue before execution
- **HeartbeatTimeout**: Maximum time between heartbeats (for long activities)

**Configure timeouts based on your activity's needs!**

---

# 7. **Spring Boot Integration Patterns**

## **Activity as Spring Beans**

```kotlin
@Component
class GreetingActivityImpl(
    private val userService: UserService,
    private val emailService: EmailService
) : GreetingActivity {
    
    override fun generateGreeting(name: String): String {
        // Use injected dependencies
        val user = userService.findByName(name)
        return "Hello, ${user.fullName}!"
    }
}
```

**Spring dependency injection works seamlessly!**

---

# Configuration as Spring Configuration

```kotlin
@Configuration
class TemporalConfig {
    
    @Bean
    fun workflowClient(): WorkflowClient { ... }
    
    @PostConstruct
    fun startWorker() {
        // Register Spring-managed activity beans
        worker.registerActivitiesImplementations(greetingActivity)
    }
}
```

**Clean separation of concerns with Spring!**

---

# Best Practices

## ✅ **Workflow Design**

### **1. Keep Workflows Simple**

```kotlin
// Good: Simple orchestration
override fun processOrder(order: Order): OrderResult {
    val payment = paymentActivity.charge(order.amount)
    val inventory = inventoryActivity.reserve(order.items)
    val shipping = shippingActivity.arrange(order)
    return OrderResult(payment, inventory, shipping)
}
```

**Workflows should orchestrate, not implement!**

---

# More Best Practices

### **2. Use Meaningful Names**

```kotlin
// Good: Clear, descriptive names
@WorkflowMethod
fun processUserOnboarding(userId: String): OnboardingResult

@ActivityMethod
fun sendWelcomeEmail(userId: String, templateId: String): EmailResult
```

### **3. Design for Idempotency**
Activities should be safe to retry multiple times!

---

# Idempotency Example

```kotlin
// Activities should be safe to retry
override fun createUserAccount(userData: UserData): String {
    // Check if user already exists
    if (userRepository.existsByEmail(userData.email)) {
        return userRepository.findByEmail(userData.email).id
    }
    
    // Create new user
    return userRepository.create(userData).id
}
```

**Always check before creating!**

---

# Configuration Management

### **1. Environment-Specific Task Queues**

```kotlin
@Value("\${temporal.task-queue:default-queue}")
private val taskQueue: String
```

### **2. Reasonable Timeouts**

```kotlin
// Different timeouts for different activity types
private val quickActivity = Workflow.newActivityStub(
    QuickActivity::class.java,
    ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofSeconds(10))
        .build()
)
```

---

# ❌ Common Mistakes

### **1. Non-deterministic Workflows**

```kotlin
// BAD: Will cause replay issues
override fun badWorkflow(): String {
    if (Random.nextBoolean()) { // Non-deterministic!
        return "option1"
    }
    return "option2"
}
```

**Random operations break replay functionality!**

---

# More Common Mistakes

### **2. Forgetting Activity Registration**

```kotlin
// BAD: Activity not registered
worker.registerWorkflowImplementationTypes(MyWorkflowImpl::class.java)
// Forgot to register MyActivityImpl!

// GOOD: Register everything
worker.registerWorkflowImplementationTypes(MyWorkflowImpl::class.java)
worker.registerActivitiesImplementations(MyActivityImpl())
```

---

# Even More Common Mistakes

### **3. Inappropriate Timeouts**

```kotlin
// BAD: Timeout too short for the operation
ActivityOptions.newBuilder()
    .setStartToCloseTimeout(Duration.ofSeconds(1)) // Too short for DB!
    .build()
```

**Set realistic timeouts based on actual operation duration!**

---

# 💡 Key Takeaways

## **What You've Learned:**
- ✅ **Workflows orchestrate**, activities execute
- ✅ **Deterministic vs non-deterministic** operations
- ✅ **Activity stubs** provide configuration and retries
- ✅ **Registration and task queues** connect everything
- ✅ **Spring Boot integration** is seamless
- ✅ **Common mistakes** to avoid

---

# 🚀 You're Ready for More!

**Now that you understand the workflow-activity pattern**, you can build more sophisticated business processes.

## **Next lessons will introduce:**
- 📡 **Signals** - External interaction with running workflows
- 🔍 **Queries** - Inspecting workflow state
- 👶 **Child workflows** - Hierarchical workflow composition
- ⏰ **Timers** - Time-based workflow logic

**Let's keep building! 🎉** 