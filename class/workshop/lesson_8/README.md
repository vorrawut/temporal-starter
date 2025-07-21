# Workshop 8 Starter Code

This lesson focuses on building resilient workflows with sophisticated retry and timeout strategies.

## What's Provided

- `workflow/ResilientWorkflow.kt` - Starter interface with basic data classes
- Template for different retry strategies

## What You Need To Do

Follow the instructions in `../modules/lesson_8/workshop_8.md` to:

1. Implement the ResilientWorkflow interface and implementation
2. Configure different retry policies for different activity types:
   - Aggressive retries for quick validation
   - Conservative retries for external APIs
   - Moderate retries for database operations
3. Implement proper timeout strategies
4. Handle different failure types appropriately

## Goal

Create a workflow that:
- Demonstrates different retry strategies for different operation types
- Uses exponential backoff and jitter appropriately
- Handles retriable vs non-retriable failures correctly
- Implements activity heartbeats for long-running operations
- Provides comprehensive failure information

## Architecture Focus

This lesson emphasizes:
- Retry policy configuration
- Timeout strategy selection
- Failure classification (retriable vs non-retriable)
- Activity heartbeat patterns
- Observability and retry metrics 