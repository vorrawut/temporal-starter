---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Workflow & Activity Separation

## Lesson 6: Clean Architecture for Temporal Workflows

Learn how to design and implement clean, maintainable Temporal workflows by properly separating concerns across multiple activities.

---

# Objective

By the end of this lesson, you will understand:

- ✅ **Single Responsibility Principle (SRP)** applied to Temporal workflows
- ✅ **Activity timeout strategies** for different operation types
- ✅ **Error handling patterns** for critical vs non-critical failures
- ✅ **Data modeling best practices** with typed result objects
- ✅ **Workflow orchestration patterns** for complex business processes
- ✅ **Testing strategies** for maintainable code

---

# 1. **Single Responsibility Principle (SRP) in Temporal**

## **Why SRP Matters in Workflows**

Each activity should have **one reason to change**. This makes your system:

- ✅ **Easier to test** - focused components are simpler to unit test
- ✅ **More maintainable** - changes to one concern don't affect others
- ✅ **More scalable** - different activities can have different scaling requirements
- ✅ **More resilient** - failures in one area don't necessarily affect others

---

# Good vs Bad Activity Design

## ❌ **BAD: One activity doing too much**

```kotlin
@ActivityInterface
interface UserProcessingActivity {
    @ActivityMethod
    fun processUser(email: String): String // Validates, creates account, sends email
}
```

**Problem**: Multiple responsibilities in one activity

---

# Good Activity Design

## ✅ **GOOD: Separate concerns**

```kotlin
@ActivityInterface
interface UserValidationActivity {
    @ActivityMethod
    fun validateUser(email: String): ValidationResult
}

@ActivityInterface
interface AccountCreationActivity {
    @ActivityMethod
    fun createAccount(email: String): CreationResult
}

@ActivityInterface
interface NotificationActivity {
    @ActivityMethod
    fun sendWelcomeEmail(email: String, userId: String): NotificationResult
}
```

**Result**: Clear, focused responsibilities for each activity

---

# 2. **Activity Timeout Strategy**

## **Different Operations Need Different Timeouts**

```kotlin
class UserOnboardingWorkflowImpl : UserOnboardingWorkflow {
    
    // Quick validation - short timeout
    private val validationActivity = Workflow.newActivityStub(
        UserValidationActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .build()
    )
    // More activities on next slide...
```

---

# More Timeout Examples

```kotlin
    // Database operations - medium timeout
    private val accountCreationActivity = Workflow.newActivityStub(
        AccountCreationActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .build()
    )
    
    // External services - longer timeout
    private val notificationActivity = Workflow.newActivityStub(
        NotificationActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(2))
            .build()
    )
}
```

---

# Timeout Strategy Guidelines

## **Operation-Based Timeout Recommendations:**

- **In-memory operations**: 5-10 seconds
- **Database queries**: 15-30 seconds
- **Database transactions**: 30-60 seconds
- **External API calls**: 1-5 minutes
- **File processing**: 5-30 minutes
- **Long computations**: 30+ minutes

**Choose timeouts based on the actual operation characteristics!**

---

# 3. **Error Handling Patterns**

## **Critical vs Non-Critical Failures**

```kotlin
override fun onboardUser(email: String): OnboardingResult {
    // Critical failure - stops the entire process
    val validation = validationActivity.validateUser(email)
    if (!validation.isValid) {
        return OnboardingResult(false, null, validation.errorMessage!!, steps)
    }
    
    // Critical failure - user can't be created without this
    val creation = accountCreationActivity.createAccount(email) 
    if (!creation.success) {
        return OnboardingResult(false, null, creation.errorMessage!!, steps)
    }
    // Continued on next slide...
```

---

# Non-Critical Error Handling

```kotlin
    // Non-critical failure - best effort, don't fail the whole process
    try {
        val notification = notificationActivity.sendWelcomeEmail(email, userId)
        // Log but continue even if notification fails
    } catch (e: Exception) {
        logger.warn("Notification failed but user was created successfully: ${e.message}")
    }
    
    return OnboardingResult(true, userId, "Success", steps)
}
```

**Key Principle**: Fail fast for critical operations, gracefully degrade for non-critical ones

---

# Failure Strategy Decision Matrix

| Operation Type | Failure Impact | Strategy |
|----------------|----------------|----------|
| Data Validation | High | Fail fast, stop process |
| Account Creation | High | Fail and rollback |
| Payment Processing | High | Fail and alert |
| Welcome Email | Low | Log and continue |
| Analytics Event | Low | Retry later, don't block |
| Audit Logging | Medium | Retry with backoff |

---

# 4. **Data Modeling Best Practices**

## **Typed Result Objects**

```kotlin
// Clear, specific result types
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String?
)

data class CreationResult(
    val success: Boolean,
    val userId: String?,
    val errorMessage: String?
)

data class NotificationResult(
    val sent: Boolean,
    val errorMessage: String?
)
```

---

# Comprehensive Result Objects

```kotlin
// Comprehensive workflow result
data class OnboardingResult(
    val success: Boolean,
    val userId: String?,
    val message: String,
    val steps: List<String> // Audit trail
)
```

## **Why Typed Results Matter**
- ✅ **Type Safety**: Compile-time checking prevents errors
- ✅ **Clear Contracts**: Each activity's responsibility is explicit
- ✅ **Evolution**: Easy to add fields without breaking existing code
- ✅ **Testing**: Clear expectations for unit tests

---

# 5. **Workflow Orchestration Patterns**

## **Sequential Processing**

```kotlin
// Steps must happen in order
val validation = validationActivity.validateUser(email)
if (!validation.isValid) return failure(validation.errorMessage)

val creation = accountCreationActivity.createAccount(email)
if (!creation.success) return failure(creation.errorMessage)

val notification = notificationActivity.sendWelcomeEmail(email, userId)
```

**When to use**: When each step depends on the previous one

---

# Parallel Processing

## **Parallel Processing** (for future lessons)

```kotlin
// Independent operations can run in parallel
val validationFuture = Async.function { validationActivity.validateUser(email) }
val configFuture = Async.function { configActivity.setupDefaults(email) }

val validation = validationFuture.get()
val config = configFuture.get()
```

**When to use**: When operations are independent and can run concurrently

---

# Conditional Processing

## **Conditional Processing**

```kotlin
// Different paths based on business rules
val userType = classificationActivity.classifyUser(email)

when (userType) {
    UserType.PREMIUM -> {
        premiumOnboardingActivity.setupPremiumFeatures(userId)
    }
    UserType.STANDARD -> {
        standardOnboardingActivity.setupBasicFeatures(userId)
    }
    UserType.TRIAL -> {
        trialOnboardingActivity.setupTrialFeatures(userId)
    }
}
```

**When to use**: When business logic requires different paths

---

# 6. **Testing Strategy**

## **Unit Testing Activities**

```kotlin
@Test
fun `should validate correct email format`() {
    val activity = UserValidationActivityImpl()
    
    val result = activity.validateUser("test@example.com")
    
    assertThat(result.isValid).isTrue()
    assertThat(result.errorMessage).isNull()
}

@Test
fun `should reject invalid email format`() {
    val activity = UserValidationActivityImpl()
    
    val result = activity.validateUser("invalid-email")
    
    assertThat(result.isValid).isFalse()
    assertThat(result.errorMessage).isEqualTo("Invalid email format")
}
```

---

# Integration Testing Workflows

```kotlin
@Test
fun `should complete full user onboarding`() {
    val testEnv = TestWorkflowEnvironment.newInstance()
    val worker = testEnv.newWorker("test-queue")
    
    worker.registerWorkflowImplementationTypes(UserOnboardingWorkflowImpl::class.java)
    worker.registerActivitiesImplementations(
        UserValidationActivityImpl(),
        AccountCreationActivityImpl(),
        NotificationActivityImpl()
    )
    
    testEnv.start()
    
    val workflow = testEnv.workflowClient.newWorkflowStub(
        UserOnboardingWorkflow::class.java
    )
    
    val result = workflow.onboardUser("test@example.com")
    
    assertThat(result.success).isTrue()
    assertThat(result.userId).isNotNull()
    assertThat(result.steps).hasSize(3)
}
```

---

# Best Practices

## ✅ **Activity Organization**

### **1. Domain-Driven Design**

```kotlin
// Group by business domain
com.company.user.validation.UserValidationActivity
com.company.user.account.AccountCreationActivity
com.company.notification.EmailNotificationActivity
com.company.billing.PaymentActivity
```

**Organize by business domain, not technical concerns**

---

# More Organization Best Practices

### **2. Interface Segregation**

```kotlin
// Specific interfaces, not generic ones
interface UserValidationActivity {
    fun validateUser(email: String): ValidationResult
}

// Not this:
interface GenericActivity {
    fun process(data: Any): Any
}
```

### **3. Dependency Injection**

```kotlin
@Component
class UserValidationActivityImpl(
    private val userRepository: UserRepository,
    private val emailValidator: EmailValidator
) : UserValidationActivity {
    // Use injected dependencies
}
```

---

# ✅ Error Handling Best Practices

### **1. Fail Fast for Critical Errors**

```kotlin
if (!validation.isValid) {
    // Stop immediately, don't waste resources
    return OnboardingResult(false, null, validation.errorMessage!!, steps)
}
```

### **2. Graceful Degradation for Non-Critical**

```kotlin
try {
    notificationActivity.sendWelcomeEmail(email, userId)
} catch (e: Exception) {
    // Log but don't fail the workflow
    logger.warn("Email failed but user was created: ${e.message}")
}
```

---

# More Error Handling

### **3. Detailed Error Context**

```kotlin
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String?,
    val errorCode: String? = null,
    val failedField: String? = null
)
```

**Provide enough context for debugging and user feedback**

---

# ❌ Common Anti-Patterns

### **1. God Activities**

```kotlin
// Bad: One activity doing everything
interface UserManagementActivity {
    fun processCompleteUserLifecycle(data: UserData): Result
}
```

### **2. Shared Mutable State**

```kotlin
// Bad: Activities sharing state
class BadActivityImpl {
    companion object {
        var sharedCounter = 0 // Don't do this!
    }
}
```

---

# Final Anti-Pattern

### **3. No Error Differentiation**

```kotlin
// Bad: All failures treated the same
if (validation.failed || creation.failed || notification.failed) {
    return failure("Something went wrong")
}
```

**Always differentiate between critical and non-critical failures**

---

# 💡 Key Takeaways

## **What You've Learned:**

- ✅ **Single Responsibility Principle** creates maintainable activities
- ✅ **Timeout strategies** should match operation characteristics
- ✅ **Error handling** requires differentiating critical vs non-critical failures
- ✅ **Typed result objects** provide clear contracts and type safety
- ✅ **Orchestration patterns** handle different business scenarios
- ✅ **Testing strategies** ensure reliable, maintainable code

---

# 🚀 Next Steps

**You now understand clean architecture for Temporal workflows!**

## **Lesson 7 will dive deeper into:**
- Workflow input/output patterns
- Complex data handling scenarios
- Advanced parameter validation
- State management techniques

**Ready to master workflow data flow? Let's continue! 🎉** 