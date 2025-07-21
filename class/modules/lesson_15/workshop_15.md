# Lesson 15: External Service Integration

## What we want to build

A comprehensive user registration workflow that integrates with multiple external services including user profile APIs, payment gateways, databases, and notification systems. This lesson demonstrates how to properly encapsulate external service calls within Temporal activities and handle complex integration scenarios.

## Expecting Result

A working user registration workflow that:
- Creates user profiles in external systems via HTTP APIs
- Processes payments through external payment gateways
- Saves data to databases
- Sends multi-channel notifications (email, SMS)
- Handles failures gracefully with proper error collection
- Uses different timeout and retry configurations for different service types

## Code Steps

### Step 1: Define Data Classes for External Integration

Create data classes in `workflow/ExternalServiceWorkflow.kt` to represent the data structures:

```kotlin
data class UserProfile(
    val userId: String,
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val preferences: Map<String, Any> = emptyMap()
)

data class PaymentRequest(
    val userId: String,
    val amount: Double,
    val currency: String = "USD",
    val paymentMethod: String,
    val description: String
)

data class PaymentResult(
    val transactionId: String,
    val status: String,
    val amount: Double,
    val timestamp: String
)
```

Add notification and integration result classes with proper enums.

### Step 2: Create Activity Interfaces

Define separate activity interfaces for each external service:

```kotlin
@ActivityInterface
interface UserProfileService {
    @ActivityMethod
    fun createUserProfile(userProfile: UserProfile): String
    
    @ActivityMethod
    fun getUserProfile(userId: String): UserProfile?
    
    @ActivityMethod
    fun updateUserProfile(userId: String, updates: Map<String, Any>): Boolean
}

@ActivityInterface
interface PaymentService {
    @ActivityMethod
    fun processPayment(paymentRequest: PaymentRequest): PaymentResult
    
    @ActivityMethod
    fun refundPayment(transactionId: String, amount: Double): PaymentResult
}
```

Continue with `DatabaseService` and `NotificationService` interfaces.

### Step 3: Configure Activity Options

Set up different activity options for different service types:

```kotlin
// For external API calls (longer timeout, more retries)
private val externalApiOptions = ActivityOptions.newBuilder()
    .setStartToCloseTimeout(Duration.ofMinutes(5))
    .setRetryOptions(
        RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(2))
            .setMaximumInterval(Duration.ofMinutes(1))
            .setBackoffCoefficient(2.0)
            .setMaximumAttempts(5)
            .build()
    )
    .build()

// For internal services (shorter timeout, fewer retries)
private val defaultActivityOptions = ActivityOptions.newBuilder()
    .setStartToCloseTimeout(Duration.ofMinutes(2))
    .setRetryOptions(
        RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(1))
            .setMaximumInterval(Duration.ofSeconds(30))
            .setBackoffCoefficient(2.0)
            .setMaximumAttempts(3)
            .build()
    )
    .build()
```

### Step 4: Create Activity Stubs with Proper Configuration

Initialize activity stubs with appropriate configurations:

```kotlin
private val userProfileService = Workflow.newActivityStub(
    UserProfileService::class.java, 
    externalApiOptions
)

private val paymentService = Workflow.newActivityStub(
    PaymentService::class.java, 
    externalApiOptions
)
```

### Step 5: Implement the Integration Workflow

Create the main workflow method with proper error handling:

```kotlin
override fun processUserRegistration(
    email: String,
    userData: Map<String, Any>
): IntegrationResult {
    val userId = generateUserId(email)
    val errors = mutableListOf<String>()
    
    // Step 1: Create user profile
    var profileCreated = false
    try {
        val userProfile = UserProfile(/* ... */)
        userProfileService.createUserProfile(userProfile)
        profileCreated = true
        
        // Log success
        databaseService.logActivity(userId, "PROFILE_CREATED", mapOf(/* ... */))
        
    } catch (e: Exception) {
        errors.add("Failed to create user profile: ${e.message}")
    }
    
    // Continue with payment processing and notifications...
}
```

### Step 6: Implement Service Orchestration

Add logic for conditional processing and error collection:

- Process payment only if payment data is provided
- Send different notifications based on available contact methods
- Collect all errors without failing the entire workflow
- Update final status based on success/failure of individual steps

### Step 7: Add Helper Methods

Include utility methods for ID generation and data transformation:

```kotlin
private fun generateUserId(email: String): String {
    return "user_${email.substringBefore("@")}_${Workflow.currentTimeMillis()}"
}
```

## How to Run

To test this lesson:

1. **Create Test Activity Implementations**: Mock the external services for testing
2. **Set up a Worker**: Configure worker to handle the workflow and activities
3. **Execute the Workflow**: Use WorkflowClient to start the registration process

Example test execution:

```kotlin
val userData = mapOf(
    "fullName" to "John Doe",
    "phoneNumber" to "+1234567890",
    "initialPayment" to mapOf(
        "amount" to 29.99,
        "paymentMethod" to "credit_card"
    ),
    "preferences" to mapOf(
        "newsletter" to true,
        "sms_notifications" to false
    )
)

val result = workflow.processUserRegistration("john.doe@example.com", userData)
println("Registration Result: $result")
```

The workflow will coordinate all external service calls and return a comprehensive result showing what succeeded and what failed. 