# Workshop 6 Starter Code

This lesson demonstrates clean workflow and activity separation through a user onboarding process with multiple coordinated activities.

## What's Provided

### Workflow
- `workflow/UserOnboardingWorkflow.kt` - Empty workflow interface with TODO comments
- `workflow/UserOnboardingWorkflowImpl.kt` - Empty workflow implementation with TODO comments

### Activities
- `activity/UserValidationActivity.kt` + `UserValidationActivityImpl.kt` - Email validation
- `activity/AccountCreationActivity.kt` + `AccountCreationActivityImpl.kt` - Account creation  
- `activity/NotificationActivity.kt` + `NotificationActivityImpl.kt` - Welcome email

## What You Need To Do

Follow the instructions in `../modules/lesson_6/workshop_6.md` to:

1. Create all the data classes (OnboardingResult, ValidationResult, etc.)
2. Implement each activity interface and implementation
3. Create the workflow that orchestrates all activities
4. Demonstrate clean separation of concerns

## Goal

Create a complete user onboarding workflow that:
- Validates user email format and availability
- Creates a new user account
- Sends a welcome email
- Handles failures gracefully at each step
- Demonstrates proper workflow-activity architecture

## Architecture

```
UserOnboardingWorkflow
├── UserValidationActivity (validate email)
├── AccountCreationActivity (create account)  
└── NotificationActivity (send welcome email)
```

Each activity has a single responsibility and can be tested independently. 