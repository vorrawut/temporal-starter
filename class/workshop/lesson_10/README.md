# Workshop 10 Starter Code

This lesson focuses on interactive workflows using signals and queries for real-time communication.

## What's Provided

- `workflow/ApprovalWorkflow.kt` - Starter interface with signal/query templates
- Basic data classes for approval requests and results

## What You Need To Do

Follow the instructions in `../modules/lesson_10/workshop_10.md` to:

1. Implement the ApprovalWorkflow interface with signals and queries
2. Create workflow implementation that maintains state
3. Handle signal methods for approve/reject actions
4. Implement query methods for status checking
5. Add timeout handling and external integration patterns

## Goal

Create a workflow that:
- Responds to external signals in real-time
- Provides queryable state information
- Handles long-running approval processes
- Supports timeout and escalation patterns
- Integrates with external systems via REST APIs

## Architecture Focus

This lesson emphasizes:
- Signal vs query patterns
- Long-running workflow management
- External system integration
- State management and caching
- Interactive workflow patterns 