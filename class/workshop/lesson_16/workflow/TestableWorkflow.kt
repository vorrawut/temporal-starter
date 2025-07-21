package workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

// TODO: Define test data classes
// data class OrderRequest(...)
// data class OrderResult(...)
// data class TestConfiguration(...)

@WorkflowInterface
interface TestableWorkflow {
    @WorkflowMethod
    fun processOrder(request: String): String // TODO: Use proper types
}

// TODO: Create activity interfaces for testing:
// - OrderValidationActivity
// - InventoryActivity
// - PaymentActivity
// - ShippingActivity

// TODO: Add test helper interfaces:
// - TestWorkflowRunner
// - MockActivityProvider 