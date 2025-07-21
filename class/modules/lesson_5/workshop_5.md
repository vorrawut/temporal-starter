# Workshop 5: Adding a Simple Activity

## What we want to build

Create a basic calculator workflow that demonstrates the fundamental workflow-activity pattern. The workflow will take two numbers as input and call an activity to perform the addition, showing how workflows orchestrate activities.

## Expecting Result

By the end of this lesson, you'll have:

- A CalculatorWorkflow that accepts two integers
- A MathActivity that performs the actual addition
- Proper logging showing the flow: workflow → activity → result
- Understanding of how workflows delegate work to activities

## Code Steps

### Step 1: Create the Workflow Interface

Open `class/workshop/lesson_5/workflow/CalculatorWorkflow.kt` and create the interface:

```kotlin
package com.temporal.bootcamp.lesson5.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface CalculatorWorkflow {
    
    @WorkflowMethod
    fun add(a: Int, b: Int): Int
}
```

**Key points:**
- Simple interface with one method
- Takes two integers, returns one integer
- This defines the contract for our workflow

### Step 2: Create the Activity Interface

Open `class/workshop/lesson_5/activity/MathActivity.kt`:

```kotlin
package com.temporal.bootcamp.lesson5.activity

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface MathActivity {
    
    @ActivityMethod
    fun performAddition(a: Int, b: Int): Int
}
```

**Key points:**
- Activities contain the actual business logic
- This activity will do the real work of adding numbers
- Method name is descriptive of what it does

### Step 3: Implement the Activity

Open `class/workshop/lesson_5/activity/MathActivityImpl.kt`:

```kotlin
package com.temporal.bootcamp.lesson5.activity

import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class MathActivityImpl : MathActivity {
    
    private val logger = KotlinLogging.logger {}
    
    override fun performAddition(a: Int, b: Int): Int {
        logger.info { "MathActivity: Adding $a + $b" }
        
        // Simulate some work
        Thread.sleep(100)
        
        val result = a + b
        
        logger.info { "MathActivity: Result = $result" }
        
        return result
    }
}
```

**Key points:**
- `@Component` makes this a Spring-managed bean
- Add logging to see when the activity runs
- The `Thread.sleep(100)` simulates work (database calls, API calls, etc.)

### Step 4: Implement the Workflow

Open `class/workshop/lesson_5/workflow/CalculatorWorkflowImpl.kt`:

```kotlin
package com.temporal.bootcamp.lesson5.workflow

import com.temporal.bootcamp.lesson5.activity.MathActivity
import io.temporal.activity.ActivityOptions
import io.temporal.workflow.Workflow
import java.time.Duration

class CalculatorWorkflowImpl : CalculatorWorkflow {
    
    private val mathActivity = Workflow.newActivityStub(
        MathActivity::class.java,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .build()
    )
    
    override fun add(a: Int, b: Int): Int {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("Calculator workflow started: $a + $b")
        
        val result = mathActivity.performAddition(a, b)
        
        logger.info("Calculator workflow completed: $a + $b = $result")
        
        return result
    }
}
```

**Key points:**
- Create activity stub with timeout configuration
- Use `Workflow.getLogger()` for workflow logging
- Call the activity through the stub (looks like a normal method call)
- Return the result from the activity

## How to Run

To test this lesson's code:

### 1. Register with Worker
Add these to your Temporal worker configuration:
```kotlin
worker.registerWorkflowImplementationTypes(CalculatorWorkflowImpl::class.java)
worker.registerActivitiesImplementations(MathActivityImpl())
```

### 2. Create and Execute Workflow
```kotlin
val workflow = workflowClient.newWorkflowStub(
    CalculatorWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("calculator-queue")
        .setWorkflowId("calc-${System.currentTimeMillis()}")
        .build()
)

val result = workflow.add(5, 3)
println("Result: $result") // Should print: Result: 8
```

### 3. Expected Output
```
Calculator workflow started: 5 + 3
MathActivity: Adding 5 + 3
MathActivity: Result = 8
Calculator workflow completed: 5 + 3 = 8
```

## What You've Learned

- ✅ How workflows call activities through stubs
- ✅ The importance of activity timeouts
- ✅ Proper logging strategies for workflows vs activities
- ✅ The separation of orchestration (workflow) vs execution (activity)
- ✅ How Temporal handles the communication between workflow and activity

This is the fundamental pattern you'll use in all Temporal applications: workflows orchestrate, activities execute! 