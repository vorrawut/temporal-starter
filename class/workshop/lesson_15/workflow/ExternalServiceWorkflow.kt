package workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

// TODO: Define data classes for external service integration
// data class UserProfile(...)
// data class PaymentRequest(...)
// data class NotificationRequest(...)
// data class IntegrationResult(...)

@WorkflowInterface
interface ExternalServiceWorkflow {
    @WorkflowMethod
    fun processUserRegistration(
        email: String,
        userData: Map<String, Any>
    ): String // TODO: Return proper result type
}

// TODO: Create activity interfaces for external services:
// - UserProfileService (HTTP API calls)
// - PaymentService (External payment gateway)
// - DatabaseService (Database operations)
// - NotificationService (Email/SMS notifications) 