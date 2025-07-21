package workflow

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod
import io.temporal.activity.ActivityOptions
import io.temporal.common.RetryOptions
import io.temporal.workflow.Workflow
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import org.springframework.stereotype.Component
import java.time.Duration

// Data classes for external service integration
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

data class NotificationRequest(
    val userId: String,
    val type: NotificationType,
    val channel: NotificationChannel,
    val message: String,
    val metadata: Map<String, String> = emptyMap()
)

data class NotificationResult(
    val notificationId: String,
    val status: String,
    val deliveredAt: String?
)

data class IntegrationResult(
    val userId: String,
    val profileCreated: Boolean,
    val paymentProcessed: Boolean,
    val notificationsSent: List<NotificationResult>,
    val errors: List<String> = emptyList()
)

enum class NotificationType { WELCOME, PAYMENT_CONFIRMATION, ACCOUNT_VERIFICATION }
enum class NotificationChannel { EMAIL, SMS, PUSH }

// Activity interfaces for external services
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

@ActivityInterface
interface DatabaseService {
    @ActivityMethod
    fun saveUserRegistration(userId: String, registrationData: Map<String, Any>): Boolean
    
    @ActivityMethod
    fun updateUserStatus(userId: String, status: String): Boolean
    
    @ActivityMethod
    fun logActivity(userId: String, activity: String, metadata: Map<String, Any>): Boolean
}

@ActivityInterface
interface NotificationService {
    @ActivityMethod
    fun sendNotification(request: NotificationRequest): NotificationResult
    
    @ActivityMethod
    fun sendBulkNotifications(requests: List<NotificationRequest>): List<NotificationResult>
}

@WorkflowInterface
interface ExternalServiceWorkflow {
    @WorkflowMethod
    fun processUserRegistration(
        email: String,
        userData: Map<String, Any>
    ): IntegrationResult
}

@Component
class ExternalServiceWorkflowImpl : ExternalServiceWorkflow {
    
    // Configure activity options with proper timeouts and retry policies
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
    
    // Specific options for external API calls (longer timeout)
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
    
    // Create activity stubs with appropriate configurations
    private val userProfileService = Workflow.newActivityStub(
        UserProfileService::class.java, 
        externalApiOptions
    )
    
    private val paymentService = Workflow.newActivityStub(
        PaymentService::class.java, 
        externalApiOptions
    )
    
    private val databaseService = Workflow.newActivityStub(
        DatabaseService::class.java, 
        defaultActivityOptions
    )
    
    private val notificationService = Workflow.newActivityStub(
        NotificationService::class.java, 
        defaultActivityOptions
    )
    
    override fun processUserRegistration(
        email: String,
        userData: Map<String, Any>
    ): IntegrationResult {
        val userId = generateUserId(email)
        val errors = mutableListOf<String>()
        
        // Step 1: Create user profile in external system
        var profileCreated = false
        try {
            val userProfile = UserProfile(
                userId = userId,
                email = email,
                fullName = userData["fullName"] as? String ?: "",
                phoneNumber = userData["phoneNumber"] as? String ?: "",
                preferences = userData["preferences"] as? Map<String, Any> ?: emptyMap()
            )
            
            userProfileService.createUserProfile(userProfile)
            profileCreated = true
            
            // Log the profile creation
            databaseService.logActivity(
                userId, 
                "PROFILE_CREATED", 
                mapOf("email" to email, "timestamp" to Workflow.currentTimeMillis().toString())
            )
            
        } catch (e: Exception) {
            errors.add("Failed to create user profile: ${e.message}")
        }
        
        // Step 2: Process initial payment (if provided)
        var paymentProcessed = false
        if (userData.containsKey("initialPayment")) {
            try {
                val paymentData = userData["initialPayment"] as Map<String, Any>
                val paymentRequest = PaymentRequest(
                    userId = userId,
                    amount = paymentData["amount"] as Double,
                    currency = paymentData["currency"] as? String ?: "USD",
                    paymentMethod = paymentData["paymentMethod"] as String,
                    description = "Initial registration payment"
                )
                
                val paymentResult = paymentService.processPayment(paymentRequest)
                paymentProcessed = paymentResult.status == "SUCCESS"
                
                // Update user status based on payment
                if (paymentProcessed) {
                    databaseService.updateUserStatus(userId, "PAID")
                    databaseService.logActivity(
                        userId, 
                        "PAYMENT_PROCESSED", 
                        mapOf("transactionId" to paymentResult.transactionId, "amount" to paymentResult.amount.toString())
                    )
                } else {
                    errors.add("Payment processing failed: ${paymentResult.status}")
                }
                
            } catch (e: Exception) {
                errors.add("Failed to process payment: ${e.message}")
            }
        }
        
        // Step 3: Send notifications
        val notificationResults = mutableListOf<NotificationResult>()
        
        try {
            // Welcome email
            val welcomeNotification = NotificationRequest(
                userId = userId,
                type = NotificationType.WELCOME,
                channel = NotificationChannel.EMAIL,
                message = "Welcome to our platform!",
                metadata = mapOf("email" to email, "fullName" to (userData["fullName"] as? String ?: ""))
            )
            
            val welcomeResult = notificationService.sendNotification(welcomeNotification)
            notificationResults.add(welcomeResult)
            
            // Payment confirmation (if payment was processed)
            if (paymentProcessed) {
                val paymentNotification = NotificationRequest(
                    userId = userId,
                    type = NotificationType.PAYMENT_CONFIRMATION,
                    channel = NotificationChannel.EMAIL,
                    message = "Your payment has been processed successfully.",
                    metadata = mapOf("amount" to (userData["initialPayment"] as? Map<String, Any>)?.get("amount").toString())
                )
                
                val paymentNotificationResult = notificationService.sendNotification(paymentNotification)
                notificationResults.add(paymentNotificationResult)
            }
            
            // SMS notification (if phone number provided)
            if (userData.containsKey("phoneNumber")) {
                val smsNotification = NotificationRequest(
                    userId = userId,
                    type = NotificationType.ACCOUNT_VERIFICATION,
                    channel = NotificationChannel.SMS,
                    message = "Please verify your account by clicking the link sent to your email.",
                    metadata = mapOf("phoneNumber" to (userData["phoneNumber"] as String))
                )
                
                val smsResult = notificationService.sendNotification(smsNotification)
                notificationResults.add(smsResult)
            }
            
        } catch (e: Exception) {
            errors.add("Failed to send notifications: ${e.message}")
        }
        
        // Step 4: Final database update
        try {
            databaseService.saveUserRegistration(userId, userData + mapOf(
                "profileCreated" to profileCreated,
                "paymentProcessed" to paymentProcessed,
                "notificationsSent" to notificationResults.size,
                "registrationTimestamp" to Workflow.currentTimeMillis()
            ))
            
            val finalStatus = when {
                profileCreated && paymentProcessed -> "COMPLETE"
                profileCreated -> "PARTIAL"
                else -> "FAILED"
            }
            
            databaseService.updateUserStatus(userId, finalStatus)
            
        } catch (e: Exception) {
            errors.add("Failed to save registration data: ${e.message}")
        }
        
        return IntegrationResult(
            userId = userId,
            profileCreated = profileCreated,
            paymentProcessed = paymentProcessed,
            notificationsSent = notificationResults,
            errors = errors
        )
    }
    
    private fun generateUserId(email: String): String {
        return "user_${email.substringBefore("@")}_${Workflow.currentTimeMillis()}"
    }
} 