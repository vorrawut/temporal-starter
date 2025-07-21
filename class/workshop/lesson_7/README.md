# Workshop 7 Starter Code

This lesson focuses on complex workflow input/output patterns and rich data modeling.

## What's Provided

- `workflow/OrderProcessingWorkflow.kt` - Starter interface with data class templates
- Basic enums for OrderStatus and PaymentMethod

## What You Need To Do

Follow the instructions in `../modules/lesson_7/workshop_7.md` to:

1. Create comprehensive input data classes (OrderRequest, OrderItem, Address)
2. Create rich output data classes (OrderResult, ProcessingStep, TrackingInfo)
3. Implement the OrderProcessingWorkflow interface and implementation
4. Add proper input validation and data transformation patterns

## Goal

Create a workflow that:
- Accepts complex nested input objects
- Validates input data early
- Transforms data appropriately for different activities
- Returns comprehensive output with processing details
- Demonstrates proper data modeling for Temporal workflows

## Architecture Focus

This lesson emphasizes:
- Complex data structures and serialization
- Input validation patterns
- Data transformation between workflow and activities  
- Rich output objects with audit trails
- Type-safe data modeling approaches 