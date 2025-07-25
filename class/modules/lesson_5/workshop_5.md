---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Workshop 5: Adding a Simple Activity

## Building Your First Workflow-Activity Pattern

*Create a basic calculator workflow that demonstrates the fundamental workflow-activity pattern*

---

# What we want to build

Create a **basic calculator workflow** that demonstrates the fundamental **workflow-activity pattern**. 

The workflow will take two numbers as input and call an activity to perform the addition, showing how **workflows orchestrate activities**.

---

# Expecting Result

## By the end of this lesson, you'll have:

- ✅ **A CalculatorWorkflow** that accepts two integers
- ✅ **A MathActivity** that performs the actual addition
- ✅ **Proper logging** showing the flow: workflow → activity → result
- ✅ **Understanding** of how workflows delegate work to activities

---

# Code Steps

## Step 1: Create the Workflow Interface

Open `class/workshop/lesson_5/workflow/CalculatorWorkflow.kt`:

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

---

# Step 2: Create the Activity Interface

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

---

# Step 3: Implement the Activity

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

---

# Key Implementation Points

## **Activity Implementation:**

- ✅ **`@Component`** makes this a Spring-managed bean
- ✅ **Add logging** to see when the activity runs
- ✅ **`Thread.sleep(100)`** simulates work (database calls, API calls, etc.)
- ✅ **Actual business logic** happens in activities, not workflows

## **Why Activities:**
- **External calls** (database, API, file system)
- **Non-deterministic operations** (random numbers, current time)
- **Business logic** that can be tested independently

---

# Step 4: Implement the Workflow

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
        
        logger.info("CalculatorWorkflow: Starting addition of $a + $b")
        
        val result = mathActivity.performAddition(a, b)
        
        logger.info("CalculatorWorkflow: Final result = $result")
        
        return result
    }
}
```

---

# Workflow Pattern Explained

## **Key Components:**

- ✅ **Activity Stub** → Connection to the activity
- ✅ **Activity Options** → Timeout and retry configuration
- ✅ **Workflow Logger** → Deterministic logging within workflows
- ✅ **Orchestration Logic** → Workflow controls the flow, activity does the work

## **This Pattern:**
- **Workflow** = Coordination and decision making
- **Activity** = Actual work execution
- **Clear separation** of concerns

---

# How to Run

## Register Components

```kotlin
worker.registerWorkflowImplementationTypes(CalculatorWorkflowImpl::class.java)
worker.registerActivitiesImplementations(MathActivityImpl())
```

## Execute the Workflow

```kotlin
val workflow = workflowClient.newWorkflowStub(
    CalculatorWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("calculator-queue")
        .setWorkflowId("calc-${System.currentTimeMillis()}")
        .build()
)

val result = workflow.add(5, 3)
println("Result: $result") // Output: Result: 8
```

---

# Expected Output

```
CalculatorWorkflow: Starting addition of 5 + 3
MathActivity: Adding 5 + 3
MathActivity: Result = 8
CalculatorWorkflow: Final result = 8
Result: 8
```

**Clear execution flow showing workflow orchestration and activity execution**

---

# 💡 Key Takeaways

## **What You've Learned:**

- ✅ **Workflow-Activity Pattern** - fundamental Temporal pattern
- ✅ **Separation of Concerns** - workflows orchestrate, activities execute
- ✅ **Activity Stubs** - how workflows call activities
- ✅ **Timeout Configuration** - basic resilience patterns
- ✅ **Deterministic Logging** - proper logging in workflows

---

# 🚀 Next Steps

**You now understand the fundamental Temporal pattern!**

## **Lesson 6 will cover:**
- Workflow and activity separation
- Clean architecture patterns
- Multiple activities coordination
- Advanced error handling

**Ready to build more complex workflows? Let's continue! 🎉** 