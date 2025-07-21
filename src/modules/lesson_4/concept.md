# Concept 4: HelloWorkflow - Your First Workflow & Activity

## Objective

Learn the fundamental building blocks of Temporal by creating your first workflow and activity. Understand how workflows orchestrate activities, the role of each component, and how they work together to create reliable distributed applications.

## Key Concepts

### 1. **Workflow Fundamentals**

#### **What is a Workflow?**
A workflow is the orchestration logic that coordinates multiple steps in a business process. Think of it as the conductor of an orchestra - it doesn't play the instruments, but it coordinates when each section plays.

```kotlin
@WorkflowInterface
interface HelloWorkflow {
    @WorkflowMethod
    fun sayHello(name: String): String
}
```

#### **Workflow Characteristics**
- **Deterministic**: Given the same input, always produces the same output
- **Replayable**: Can be stopped and resumed from any point
- **Orchestration-focused**: Coordinates activities, doesn't do the heavy lifting
- **Long-running**: Can run for days, weeks, or months
- **Fault-tolerant**: Survives worker crashes and restarts

#### **What Workflows Should NOT Do**
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

// ✅ GOOD: Deterministic orchestration only
class GoodWorkflowImpl : MyWorkflow {
    private val activity = Workflow.newActivityStub(...)
    
    override fun process(): String {
        val result = activity.doWork() // Delegate to activity
        return result
    }
}
```

### 2. **Activity Fundamentals**

#### **What is an Activity?**
An activity is a unit of work that performs the actual business logic. It's like a musician in the orchestra - it does the real work when the conductor (workflow) tells it to.

```kotlin
@ActivityInterface
interface GreetingActivity {
    @ActivityMethod
    fun generateGreeting(name: String): String
}
```

#### **Activity Characteristics**
- **Non-deterministic**: Can make external calls, use random numbers, etc.
- **Retryable**: Can be automatically retried if they fail
- **Short-lived**: Typically run for seconds or minutes
- **Stateless**: Don't maintain state between calls
- **Idempotent**: Safe to retry without side effects

#### **What Activities CAN Do**
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

### 3. **Workflow ↔ Activity Communication**

#### **Activity Stubs**
Workflows don't call activities directly. Instead, they use activity stubs (proxies) configured with timeouts and retry policies.

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
    
    override fun process(): String {
        // Call looks like a regular method call
        return myActivity.doWork()
    }
}
```

#### **Data Flow**
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

### 4. **Configuration and Registration**

#### **Worker Registration**
Workers must register both workflow and activity implementations before starting:

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

#### **Task Queues**
Task queues connect workflow execution requests with workers:

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

### 5. **Execution Lifecycle**

#### **Complete Flow Example**
```kotlin
// 1. Client creates workflow stub
val workflow = client.newWorkflowStub(HelloWorkflow::class.java, options)

// 2. Client calls workflow method
val result = workflow.sayHello("John") // This is asynchronous behind the scenes

// 3. Temporal routes to worker
// 4. Worker creates workflow instance
// 5. Workflow calls activity via stub
val greeting = greetingActivity.generateGreeting("John")

// 6. Worker executes activity
// 7. Activity returns result
// 8. Workflow returns result
// 9. Client receives result
```

#### **Timeline in Temporal Web UI**
When you look at a workflow in the Web UI, you'll see:
1. **Workflow Started** - Workflow instance created
2. **Activity Scheduled** - Activity task sent to queue
3. **Activity Started** - Worker picked up activity task
4. **Activity Completed** - Activity finished successfully
5. **Workflow Completed** - Workflow returned final result

### 6. **Error Handling and Retries**

#### **Activity Retry Policies**
```kotlin
private val risky Activity = Workflow.newActivityStub(
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

#### **Timeout Types**
- **ScheduleToCloseTimeout**: Total time for activity (including retries)
- **StartToCloseTimeout**: Time for single activity execution
- **ScheduleToStartTimeout**: Maximum time in queue before execution
- **HeartbeatTimeout**: Maximum time between heartbeats (for long activities)

### 7. **Spring Boot Integration Patterns**

#### **Activity as Spring Beans**
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

#### **Configuration as Spring Configuration**
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

## Best Practices

### ✅ Workflow Design

1. **Keep Workflows Simple**
   ```kotlin
   // Good: Simple orchestration
   override fun processOrder(order: Order): OrderResult {
       val payment = paymentActivity.charge(order.amount)
       val inventory = inventoryActivity.reserve(order.items)
       val shipping = shippingActivity.arrange(order)
       return OrderResult(payment, inventory, shipping)
   }
   ```

2. **Use Meaningful Names**
   ```kotlin
   // Good: Clear, descriptive names
   @WorkflowMethod
   fun processUserOnboarding(userId: String): OnboardingResult
   
   @ActivityMethod
   fun sendWelcomeEmail(userId: String, templateId: String): EmailResult
   ```

3. **Design for Idempotency**
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

### ✅ Configuration Management

1. **Environment-Specific Task Queues**
   ```kotlin
   @Value("\${temporal.task-queue:default-queue}")
   private val taskQueue: String
   ```

2. **Reasonable Timeouts**
   ```kotlin
   // Different timeouts for different activity types
   private val quickActivity = Workflow.newActivityStub(
       QuickActivity::class.java,
       ActivityOptions.newBuilder()
           .setStartToCloseTimeout(Duration.ofSeconds(10))
           .build()
   )
   
   private val slowActivity = Workflow.newActivityStub(
       SlowActivity::class.java,
       ActivityOptions.newBuilder()
           .setStartToCloseTimeout(Duration.ofMinutes(5))
           .build()
   )
   ```

### ❌ Common Mistakes

1. **Non-deterministic Workflows**
   ```kotlin
   // BAD: Will cause replay issues
   override fun badWorkflow(): String {
       if (Random.nextBoolean()) { // Non-deterministic!
           return "option1"
       }
       return "option2"
   }
   ```

2. **Forgetting Activity Registration**
   ```kotlin
   // BAD: Activity not registered
   worker.registerWorkflowImplementationTypes(MyWorkflowImpl::class.java)
   // Forgot to register MyActivityImpl!
   
   // GOOD: Register everything
   worker.registerWorkflowImplementationTypes(MyWorkflowImpl::class.java)
   worker.registerActivitiesImplementations(MyActivityImpl())
   ```

3. **Inappropriate Timeouts**
   ```kotlin
   // BAD: Timeout too short for the operation
   ActivityOptions.newBuilder()
       .setStartToCloseTimeout(Duration.ofSeconds(1)) // Too short for DB operation
       .build()
   ```

---

**You're ready for more!** Now that you understand the workflow-activity pattern, you can build more sophisticated business processes. The next lessons will introduce signals, queries, child workflows, and more advanced patterns! 