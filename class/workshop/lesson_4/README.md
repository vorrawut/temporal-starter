# Workshop 4 Starter Code

This folder contains the starter code for creating your first Temporal workflow and activity.

## What's Provided

- `workflow/HelloWorkflow.kt` - Empty workflow interface with TODO comments
- `workflow/HelloWorkflowImpl.kt` - Empty workflow implementation with TODO comments  
- `activity/GreetingActivity.kt` - Empty activity interface with TODO comments
- `activity/GreetingActivityImpl.kt` - Empty activity implementation with TODO comments
- `config/TemporalConfig.kt` - Empty configuration with TODO comments
- `runner/HelloWorkflowRunner.kt` - Empty runner with TODO comments

## What You Need To Do

Follow the instructions in `../modules/lesson_4/workshop_4.md` to:

1. Create the workflow interface and implementation
2. Create the activity interface and implementation
3. Configure Temporal to register your workflow and activity
4. Create a runner to execute the workflow
5. Test the complete flow

## Expected Result

You should see output like:
```
✅ Temporal worker started successfully!
🚀 Running HelloWorkflow...
Workflow result: Hello, Temporal Learner! Welcome to workflows!
```

And in the Temporal Web UI, you should see your workflow execution under the "Workflows" tab. 