---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Workshop 6: Workflow & Activity Separation

## Building Multi-Step User Onboarding

*Create a comprehensive user onboarding workflow that demonstrates clean separation of concerns across multiple activities*

---

# What we want to build

Create a **comprehensive user onboarding workflow** that demonstrates clean separation of concerns across multiple activities. 

This workflow will show how to organize **complex business processes** into maintainable, testable components.

---

# Expecting Result

## By the end of this lesson, you'll have:

- ✅ **Multi-step user onboarding workflow** with proper orchestration
- ✅ **Three distinct activities**, each with a single responsibility
- ✅ **Proper error handling** for critical vs non-critical failures
- ✅ **Different timeout configurations** for different operation types
- ✅ **Clean data modeling** with typed result objects

---

# Code Steps

## Step 1: Create the Workflow Interface and Data Models

Open `class/workshop/lesson_6/workflow/UserOnboardingWorkflow.kt`:

```kotlin
package com.temporal.bootcamp.lesson6.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface UserOnboardingWorkflow {
    
    @WorkflowMethod
    fun onboardUser(email: String): OnboardingResult
}
```

---

# Data Models

```kotlin
data class OnboardingResult(
    val success: Boolean,
    val userId: String?,
    val message: String,
    val steps: List<String>
)
```

**Clean, typed result objects provide clear contracts**

---

# Step 2: Create the Validation Activity

Open `class/workshop/lesson_6/activity/UserValidationActivity.kt`:

```kotlin
package com.temporal.bootcamp.lesson6.activity

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface UserValidationActivity {
    
    @ActivityMethod
    fun validateUser(email: String): ValidationResult
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String?
)
```

---

# Step 3: Implement the Validation Activity

Open `class/workshop/lesson_6/activity/UserValidationActivityImpl.kt`:

```kotlin
package com.temporal.bootcamp.lesson6.activity

import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class UserValidationActivityImpl : UserValidationActivity {
    
    private val logger = KotlinLogging.logger {}
    
    override fun validateUser(email: String): ValidationResult {
        logger.info { "Validating user: $email" }
        
        // Basic email format validation
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!emailRegex.matches(email)) {
            return ValidationResult(false, "Invalid email format")
        }
        // Continued on next slide...
```

---

# Validation Implementation Continued

```kotlin
        // Check for existing users (simulated)
        val existingUsers = setOf("admin@example.com", "test@example.com")
        if (existingUsers.contains(email.lowercase())) {
            return ValidationResult(false, "Email already registered")
        }
        
        logger.info { "✅ User validation passed for: $email" }
        return ValidationResult(true, null)
    }
}
```

**Single responsibility: Only handles user validation logic**

---

# Step 4: Create the Account Creation Activity

Follow the same pattern for `AccountCreationActivity.kt`:

```kotlin
package com.temporal.bootcamp.lesson6.activity

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface AccountCreationActivity {
    
    @ActivityMethod
    fun createAccount(email: String): CreationResult
}

data class CreationResult(
    val success: Boolean,
    val userId: String?,
    val errorMessage: String?
)
```

---

# Step 5: Implement Account Creation

Create `AccountCreationActivityImpl.kt`:

```kotlin
package com.temporal.bootcamp.lesson6.activity

import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class AccountCreationActivityImpl : AccountCreationActivity {
    
    private val logger = KotlinLogging.logger {}
    
    override fun createAccount(email: String): CreationResult {
        logger.info { "Creating account for: $email" }
        
        // Simulate database operation
        Thread.sleep(500)
        
        val userId = "user_${UUID.randomUUID().toString().take(8)}"
        
        logger.info { "✅ Account created with ID: $userId" }
        
        return CreationResult(true, userId, null)
    }
}
```

---

# Step 6: Create the Notification Activity

Create `NotificationActivity.kt`:

```kotlin
package com.temporal.bootcamp.lesson6.activity

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface NotificationActivity {
    
    @ActivityMethod
    fun sendWelcomeEmail(email: String, userId: String): NotificationResult
}

data class NotificationResult(
    val sent: Boolean,
    val errorMessage: String?
)
```

---

# Step 7: Implement Notification

Create `NotificationActivityImpl.kt`:

```kotlin
package com.temporal.bootcamp.lesson6.activity

import mu.KotlinLogging
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class NotificationActivityImpl : NotificationActivity {
    
    private val logger = KotlinLogging.logger {}
    
    override fun sendWelcomeEmail(email: String, userId: String): NotificationResult {
        logger.info { "📧 Sending welcome email to: $email" }
        
        // Simulate email service (with occasional failures)
        Thread.sleep(300)
        
        if (Random.nextDouble() < 0.1) {
            return NotificationResult(false, "Email service unavailable")
        }
        
        logger.info { "✅ Welcome email sent successfully" }
        return NotificationResult(true, null)
    }
}
```

---

# Step 8: Implement the Main Workflow (Part 1)

Open `class/workshop/lesson_6/workflow/UserOnboardingWorkflowImpl.kt`:

```kotlin
package com.temporal.bootcamp.lesson6.workflow

import com.temporal.bootcamp.lesson6.activity.*
import io.temporal.activity.ActivityOptions
import io.temporal.workflow.Workflow
import java.time.Duration

class UserOnboardingWorkflowImpl : UserOnboardingWorkflow {
    
    // Different timeouts for different operations
    private val validationActivity = Workflow.newActivityStub(
        UserValidationActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .build()
    )
    // Continued on next slide...
```

---

# Main Workflow Implementation (Part 2)

```kotlin
    private val accountCreationActivity = Workflow.newActivityStub(
        AccountCreationActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .build()
    )
    
    private val notificationActivity = Workflow.newActivityStub(
        NotificationActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(1))
            .build()
    )
    // Continued on next slide...
```

**Notice different timeouts based on operation characteristics**

---

# Main Workflow Logic (Part 3)

```kotlin
    override fun onboardUser(email: String): OnboardingResult {
        val logger = Workflow.getLogger(this::class.java)
        val steps = mutableListOf<String>()
        
        logger.info("Starting onboarding for: $email")
        
        // Step 1: Validate
        val validation = validationActivity.validateUser(email)
        steps.add("Validation: ${if (validation.isValid) "Passed" else "Failed"}")
        
        if (!validation.isValid) {
            return OnboardingResult(false, null, validation.errorMessage!!, steps)
        }
        // Continued on next slide...
```

---

# Main Workflow Logic (Part 4)

```kotlin
        // Step 2: Create Account
        val creation = accountCreationActivity.createAccount(email)
        steps.add("Account: ${if (creation.success) "Created" else "Failed"}")
        
        if (!creation.success) {
            return OnboardingResult(false, null, creation.errorMessage!!, steps)
        }
        
        // Step 3: Send Notification (best effort)
        try {
            val notification = notificationActivity.sendWelcomeEmail(email, creation.userId!!)
            steps.add("Email: ${if (notification.sent) "Sent" else "Failed"}")
        } catch (e: Exception) {
            steps.add("Email: Failed (non-critical)")
        }
        
        logger.info("Onboarding completed successfully")
        
        return OnboardingResult(true, creation.userId, "User onboarded successfully", steps)
    }
}
```

---

# Key Design Patterns

## **Error Handling Strategy:**
- ✅ **Critical failures** (validation, account creation) → **Stop process**
- ✅ **Non-critical failures** (email notification) → **Log and continue**

## **Timeout Strategy:**
- ✅ **Quick validation**: 10 seconds
- ✅ **Database operations**: 30 seconds  
- ✅ **External services**: 1 minute

## **Single Responsibility:**
- ✅ Each activity has **one focused job**

---

# How to Run

## 1. Register All Components

```kotlin
worker.registerWorkflowImplementationTypes(UserOnboardingWorkflowImpl::class.java)
worker.registerActivitiesImplementations(
    UserValidationActivityImpl(),
    AccountCreationActivityImpl(), 
    NotificationActivityImpl()
)
```

**Register workflow and all activity implementations**

---

# 2. Execute the Workflow

```kotlin
val workflow = workflowClient.newWorkflowStub(
    UserOnboardingWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("onboarding-queue")
        .setWorkflowId("onboard-${System.currentTimeMillis()}")
        .build()
)

val result = workflow.onboardUser("newuser@example.com")
println("Result: $result")
```

**Create workflow stub and execute with test data**

---

# 3. Expected Output

```
Starting onboarding for: newuser@example.com
Validating user: newuser@example.com  
✅ User validation passed for: newuser@example.com
Creating account for: newuser@example.com
✅ Account created with ID: user_abc12345
📧 Sending welcome email to: newuser@example.com
✅ Welcome email sent successfully
Onboarding completed successfully
```

**Clean execution flow with detailed logging**

---

# What You've Learned

## ✅ **Key Achievements:**

- ✅ **How to organize complex workflows** with multiple activities
- ✅ **Single Responsibility Principle** in workflow design
- ✅ **Different timeout strategies** for different operation types
- ✅ **Error handling**: critical vs non-critical failures
- ✅ **Clean data modeling** with typed result objects
- ✅ **Best effort operations** (notifications can fail without breaking the flow)

---

# 🚀 Production-Ready Pattern

**This demonstrates a production-ready pattern for complex business processes!**

## **Key Principles Applied:**
- **Clean Architecture** - Clear separation of concerns
- **Fault Tolerance** - Graceful degradation for non-critical failures
- **Maintainability** - Each component has a single responsibility
- **Observability** - Rich logging and step tracking
- **Type Safety** - Strongly typed interfaces and results

**Ready for more advanced patterns? Let's continue! 🎉** 