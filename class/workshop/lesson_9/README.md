# Workshop 9 Starter Code

This lesson focuses on comprehensive error handling strategies including compensation patterns and circuit breakers.

## What's Provided

- `workflow/ErrorHandlingWorkflow.kt` - Starter interface with basic data classes
- Template for custom exception classes

## What You Need To Do

Follow the instructions in `../modules/lesson_9/workshop_9.md` to:

1. Create custom business exception classes
2. Implement comprehensive try-catch error handling
3. Build compensation logic (saga pattern)
4. Implement circuit breaker patterns
5. Create graceful degradation strategies

## Goal

Create a workflow that:
- Handles different error types with appropriate strategies
- Implements compensation logic for partial failures
- Uses circuit breakers to prevent cascading failures
- Provides graceful degradation for non-critical services
- Includes rich error context and debugging information

## Architecture Focus

This lesson emphasizes:
- Custom exception design
- Compensation patterns (saga)
- Circuit breaker implementation
- Error classification and handling strategies
- Graceful degradation patterns 