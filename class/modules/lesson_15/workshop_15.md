---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Workshop 15: External Service Integration

## Building Production Integration Patterns

*A comprehensive user registration workflow that integrates with multiple external services including user profile APIs, payment gateways, databases, and notification systems*

---

# What we want to build

A **comprehensive user registration workflow** that integrates with multiple external services including:

- **User profile APIs** via HTTP 
- **Payment gateways** for processing
- **Databases** for data persistence
- **Notification systems** (email, SMS)

This demonstrates **proper encapsulation** of external service calls within Temporal activities.

---

# Expecting Result

## A working user registration workflow that:

- ✅ **Creates user profiles** in external systems via HTTP APIs
- ✅ **Processes payments** through external payment gateways
- ✅ **Saves data** to databases
- ✅ **Sends multi-channel notifications** (email, SMS)
- ✅ **Handles failures gracefully** with proper error collection
- ✅ **Uses different timeout and retry configurations** for different service types

---

# Code Steps

## Step 1: Define Data Classes for External Integration

Create data classes in `workflow/ExternalServiceWorkflow.kt`:

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

---

# Step 2: Create Activity Interfaces

## Define separate activity interfaces for each external service:

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

**Separate interfaces enable focused responsibility and easier testing**

---

# Database and Notification Interfaces

```kotlin
@ActivityInterface
interface DatabaseService {
    @ActivityMethod
    fun saveUserData(userId: String, data: Map<String, Any>): Boolean
    
    @ActivityMethod
    fun getUserData(userId: String): Map<String, Any>?
}

@ActivityInterface
interface NotificationService {
    @ActivityMethod
    fun sendWelcomeEmail(email: String, userName: String): Boolean
    
    @ActivityMethod
    fun sendSMS(phoneNumber: String, message: String): Boolean
}
```

---

# Step 3: Configure Activity Options

## Set up different activity options for different service types:

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
            .setBackoffCoefficient(1.5)
            .setMaximumAttempts(3)
            .build()
    )
    .build()
```

---

# Workflow Implementation with Service Integration

```kotlin
class UserRegistrationWorkflowImpl : UserRegistrationWorkflow {
    
    private val userProfileService = Workflow.newActivityStub(
        UserProfileService::class.java, externalApiOptions
    )
    
    private val paymentService = Workflow.newActivityStub(
        PaymentService::class.java, externalApiOptions
    )
    
    private val databaseService = Workflow.newActivityStub(
        DatabaseService::class.java, defaultActivityOptions
    )
    
    private val notificationService = Workflow.newActivityStub(
        NotificationService::class.java, defaultActivityOptions
    )
    
    override fun registerUser(registrationRequest: UserRegistrationRequest): RegistrationResult {
        val logger = Workflow.getLogger(this::class.java)
        val errors = mutableListOf<String>()
        var userProfileCreated = false
        var paymentProcessed = false
        
        // Step 1: Create user profile (critical)
        try {
            val userId = userProfileService.createUserProfile(registrationRequest.userProfile)
            userProfileCreated = true
            logger.info("User profile created: $userId")
        } catch (e: Exception) {
            errors.add("Profile creation failed: ${e.message}")
            return RegistrationResult.failed(errors)
        }
        // Continued on next slide...
```

---

# Service Integration Continued

```kotlin
        // Step 2: Process payment (critical)
        try {
            val paymentResult = paymentService.processPayment(registrationRequest.paymentRequest)
            paymentProcessed = true
            logger.info("Payment processed: ${paymentResult.transactionId}")
        } catch (e: Exception) {
            errors.add("Payment processing failed: ${e.message}")
            // Cleanup: Remove user profile if payment fails
            if (userProfileCreated) {
                try {
                    cleanupUserProfile(registrationRequest.userProfile.userId)
                } catch (cleanupError: Exception) {
                    logger.error("Cleanup failed: ${cleanupError.message}")
                }
            }
            return RegistrationResult.failed(errors)
        }
        
        // Step 3: Save to database (best effort)
        try {
            databaseService.saveUserData(registrationRequest.userProfile.userId, 
                mapOf("registrationDate" to System.currentTimeMillis()))
        } catch (e: Exception) {
            errors.add("Database save failed: ${e.message}")
            // Continue - not critical
        }
        
        // Step 4: Send notifications (best effort)
        try {
            notificationService.sendWelcomeEmail(
                registrationRequest.userProfile.email,
                registrationRequest.userProfile.fullName
            )
        } catch (e: Exception) {
            errors.add("Email notification failed: ${e.message}")
            // Continue - not critical
        }
        
        return RegistrationResult.success(registrationRequest.userProfile.userId, errors)
    }
}
```

---

# Service Integration Patterns

## **Error Handling Strategy:**

| Service Type | Failure Impact | Strategy |
|--------------|----------------|----------|
| **User Profile** | Critical | Fail workflow, no cleanup needed |
| **Payment** | Critical | Fail workflow, cleanup user profile |
| **Database** | Non-critical | Log error, continue workflow |
| **Notifications** | Non-critical | Log error, continue workflow |

## **Timeout Strategy:**
- **External APIs**: Longer timeouts (5+ minutes)
- **Internal services**: Shorter timeouts (2 minutes)
- **Payment gateways**: Conservative retries to avoid duplicate charges

---

# Activity Implementation Examples

```kotlin
@Component
class UserProfileServiceImpl : UserProfileService {
    
    private val httpClient: RestTemplate = RestTemplate()
    
    override fun createUserProfile(userProfile: UserProfile): String {
        val logger = LoggerFactory.getLogger(this::class.java)
        
        try {
            val response = httpClient.postForEntity(
                "https://api.userservice.com/profiles",
                userProfile,
                CreateProfileResponse::class.java
            )
            
            if (response.statusCode.is2xxSuccessful && response.body != null) {
                logger.info("User profile created successfully: ${response.body!!.userId}")
                return response.body!!.userId
            } else {
                throw RuntimeException("Failed to create user profile: ${response.statusCode}")
            }
            
        } catch (e: Exception) {
            logger.error("User profile creation failed", e)
            throw e
        }
    }
}
```

---

# 💡 Key Integration Patterns

## **What You've Learned:**

- ✅ **Service encapsulation** in activities with proper interfaces
- ✅ **Different timeout strategies** for different service types
- ✅ **Error classification** - critical vs non-critical failures
- ✅ **Graceful degradation** for non-essential services
- ✅ **Compensation patterns** for cleanup operations
- ✅ **Production-ready** external service integration

---

# 🚀 Next Steps

**You now understand external service integration!**

## **Lesson 16 will cover:**
- Comprehensive testing strategies
- Production readiness patterns
- Worker configuration and scaling
- Monitoring and observability

**Ready to deploy to production? Let's continue! 🎉** 