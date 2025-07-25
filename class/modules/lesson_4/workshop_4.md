---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Workshop 4: HelloWorkflow

## Your First Temporal Workflow & Activity

*Create your first complete Temporal workflow - the "Hello World" for distributed workflows!*

---

# What we want to build

Create your **first complete Temporal workflow** that demonstrates the core concepts: a workflow that orchestrates an activity to generate a personalized greeting.

This is the classic **"Hello World" for distributed workflows!**

---

# Expecting Result

## By the end of this lesson, you'll have:

- ✅ **Working HelloWorkflow** that takes a name and returns a greeting
- ✅ **GreetingActivity** that generates the actual greeting message
- ✅ **Complete Temporal configuration** that registers both components
- ✅ **Runner** that executes the workflow automatically
- ✅ **Console output** showing successful workflow execution
- ✅ **Your first workflow** visible in the Temporal Web UI

---

# Code Steps

## Step 1: Create the Workflow Interface

Open `src/workshop/lesson_4/workflow/HelloWorkflow.kt` and replace the TODO comments:

```kotlin
package com.temporal.bootcamp.lesson4.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface HelloWorkflow {
    
    @WorkflowMethod
    fun sayHello(name: String): String
}
```

---

# Workflow Interface Key Points

## **Important Annotations:**
- **`@WorkflowInterface`** marks this as a workflow contract
- **`@WorkflowMethod`** marks the main entry point for the workflow

## **Design Principles:**
- Keep it simple: one input parameter, one return value
- This defines what your workflow does, not how it does it

---

# Step 2: Create the Activity Interface

Open `src/workshop/lesson_4/activity/GreetingActivity.kt`:

```kotlin
package com.temporal.bootcamp.lesson4.activity

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface GreetingActivity {
    
    @ActivityMethod
    fun generateGreeting(name: String): String
}
```

---

# Activity Interface Key Points

## **Important Annotations:**
- **`@ActivityInterface`** marks this as an activity contract
- **`@ActivityMethod`** marks the method that will be executed by workers

## **Design Principles:**
- Activities do the actual work (business logic)
- Can make external calls, access databases, etc.

---

# Step 3: Implement the Activity

Open `src/workshop/lesson_4/activity/GreetingActivityImpl.kt`:

```kotlin
package com.temporal.bootcamp.lesson4.activity

import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class GreetingActivityImpl : GreetingActivity {
    
    private val logger = KotlinLogging.logger {}
    
    override fun generateGreeting(name: String): String {
        logger.info { "Generating greeting for: $name" }
        
        // Simulate some processing
        Thread.sleep(100)
        
        val greeting = "Hello, $name! Welcome to Temporal workflows!"
        
        logger.info { "Generated greeting: $greeting" }
        return greeting
    }
}
```

---

# Activity Implementation Key Points

## **Important Features:**
- **`@Component`** makes this a Spring-managed bean
- **Add logging** to see when activities execute
- **Simulate processing** with Thread.sleep()

## **Real-World Usage:**
- This is where real business logic would go
- Database calls, API calls, file processing, etc.

---

# Step 4: Implement the Workflow

Open `src/workshop/lesson_4/workflow/HelloWorkflowImpl.kt`:

```kotlin
package com.temporal.bootcamp.lesson4.workflow

import com.temporal.answer.lesson_4.activity.GreetingActivity
import io.temporal.activity.ActivityOptions
import io.temporal.workflow.Workflow
import java.time.Duration

class HelloWorkflowImpl : HelloWorkflow {
    
    private val greetingActivity = Workflow.newActivityStub(
        GreetingActivity::class.java,
        ActivityOptions.newBuilder()
            .setScheduleToCloseTimeout(Duration.ofMinutes(1))
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .build()
    )
    
    // Continued on next slide...
```

---

# Workflow Implementation Continued

```kotlin
    override fun sayHello(name: String): String {
        val logger = Workflow.getLogger(this::class.java)
        
        logger.info("HelloWorkflow started for: $name")
        
        // Call the activity
        val greeting = greetingActivity.generateGreeting(name)
        
        logger.info("HelloWorkflow completed for: $name")
        
        return greeting
    }
}
```

---

# Workflow Implementation Key Points

## **Important Concepts:**
- **Create activity stub** with timeout configuration
- **Use `Workflow.getLogger()`** for workflow logging
- **Keep workflow logic simple** - just orchestration

## **Activity Stub Configuration:**
- **ScheduleToCloseTimeout**: Total time for activity (including retries)
- **StartToCloseTimeout**: Time for single activity execution

---

# Step 5: Configure Temporal (Part 1)

Open `src/workshop/lesson_4/config/TemporalConfig.kt` and start building the configuration:

```kotlin
package com.temporal.bootcamp.lesson4.config

import com.temporal.answer.lesson_4.activity.GreetingActivityImpl
import com.temporal.answer.lesson_4.workflow.HelloWorkflowImpl
import io.temporal.client.WorkflowClient
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.worker.Worker
import io.temporal.worker.WorkerFactory
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Configuration
class TemporalConfig {
    
    companion object {
        const val TASK_QUEUE = "lesson4-hello-queue"
    }
    
    private val logger = KotlinLogging.logger {}
    private lateinit var workerFactory: WorkerFactory
    // Continued on next slide...
```

---

# Step 5: Configure Temporal (Part 2)

```kotlin
    @Bean
    fun workflowServiceStubs(): WorkflowServiceStubs {
        return WorkflowServiceStubs.newLocalServiceStubs()
    }
    
    @Bean
    fun workflowClient(workflowServiceStubs: WorkflowServiceStubs): WorkflowClient {
        return WorkflowClient.newInstance(workflowServiceStubs)
    }
    
    @Bean
    fun workerFactory(workflowClient: WorkflowClient): WorkerFactory {
        workerFactory = WorkerFactory.newInstance(workflowClient)
        return workerFactory
    }
    
    @Bean
    fun greetingActivity(): GreetingActivityImpl {
        return GreetingActivityImpl()
    }
    // Continued on next slide...
```

---

# Step 5: Configure Temporal (Part 3)

```kotlin
    @Observed
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady(event: ApplicationReadyEvent) {
        val worker: Worker = workerFactory.newWorker(TASK_QUEUE)
        
        // Register workflow and activity
        worker.registerWorkflowImplementationTypes(HelloWorkflowImpl::class.java)
        worker.registerActivitiesImplementations(greetingActivity())
        
        workerFactory.start()
        
        logger.info { "✅ Temporal worker started for HelloWorkflow!" }
    }
    
    @PreDestroy
    fun shutdown() {
        workerFactory.shutdown()
    }
}
```

---

# Configuration Key Points

## **Important Elements:**
- **TASK_QUEUE constant** - keeps queue name consistent
- **Register both workflow and activity** implementations
- **Start worker factory** after registration
- **Graceful shutdown** in @PreDestroy

## **Critical Order:**
1. Create worker
2. Register implementations
3. Start worker factory

---

# Step 6: Create the Workflow Runner

Open `src/workshop/lesson_4/runner/HelloWorkflowRunner.kt`:

```kotlin
package com.temporal.bootcamp.lesson4.runner

import com.temporal.answer.lesson_4.config.TemporalConfig
import com.temporal.answer.lesson_4.workflow.HelloWorkflow
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import mu.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class HelloWorkflowRunner(
    private val workflowClient: WorkflowClient
) : CommandLineRunner {
    
    private val logger = KotlinLogging.logger {}
    // Continued on next slide...
```

---

# Workflow Runner Implementation

```kotlin
    override fun run(vararg args: String?) {
        logger.info { "🚀 Running HelloWorkflow..." }
        
        val workflow = workflowClient.newWorkflowStub(
            HelloWorkflow::class.java,
            WorkflowOptions.newBuilder()
                .setTaskQueue(TemporalConfig.TASK_QUEUE)
                .setWorkflowId("hello-workflow-${System.currentTimeMillis()}")
                .build()
        )

        WorkflowClient.start { 
            val result = workflow.sayHello("Temporal Learner")
    
            // Print the result
            logger.info { "✅ Workflow completed!" }
            logger.info { "   Result: $result" }
    
            // Give some guidance to the user
            logger.info { "" }
            logger.info { "🌐 Check the Temporal Web UI at http://localhost:8233" }
            logger.info { "   You should see your workflow execution in the 'Workflows' tab!" }
        }
    }
}
```

---

# Runner Key Points

## **Important Features:**
- **Implements CommandLineRunner** - runs automatically on startup
- **Creates workflow stub** with options
- **Unique workflow ID** using timestamp
- **Calls workflow method** and gets result

## **Workflow Options:**
- **Task Queue**: Must match worker configuration
- **Workflow ID**: Unique identifier for this execution

---

# How to Run

## **Prerequisites:**

### **1. Temporal server running:**
```bash
temporal server start-dev
```

### **2. Verify Web UI:** 
http://localhost:8233 should load

**Make sure both are working before proceeding!**

---

# Expected Output

```
✅ Temporal worker started for HelloWorkflow!
🚀 Running HelloWorkflow...
Generating greeting for: Temporal Learner
Generated greeting: Hello, Temporal Learner! Welcome to Temporal workflows!
✅ Workflow completed! Result: Hello, Temporal Learner! Welcome to Temporal workflows!
🌐 Check http://localhost:8233 to see your workflow!
```

**🎉 If you see this, your first workflow is working!**

---

# Verify in Web UI

## **Steps to verify:**

1. **Go to http://localhost:8233**
2. **Click "Workflows" tab**
3. **You should see**: `hello-workflow-{timestamp}` with status "Completed"
4. **Click on the workflow** to see execution details
5. **Check the timeline** - you'll see both workflow and activity execution

**Visual confirmation of your workflow execution!**

---

# Troubleshooting

## **"Connection refused"**
- Make sure `temporal server start-dev` is running
- Check that you see "Started Temporal Server" message

## **"Workflow not found" in Web UI**
- Make sure your application ran successfully
- Check console for error messages
- Verify the task queue name matches in config and runner

---

# More Troubleshooting

## **"Activity not registered"**
- Check that `GreetingActivityImpl` is annotated with `@Component`
- Verify the activity is registered in `TemporalConfig.startWorker()`
- Make sure imports are correct

## **Application won't start**
- Check that all imports are added correctly
- Verify Kotlin syntax (especially lambda expressions)
- Make sure Spring can find all classes with `@Component` and `@Configuration`

---

# What You've Accomplished

## ✅ **Major Achievements:**

- ✅ **Created your first workflow** interface and implementation
- ✅ **Created your first activity** interface and implementation
- ✅ **Configured Temporal** to register and run your components
- ✅ **Successfully executed a workflow** and saw the results
- ✅ **Verified execution** in the Temporal Web UI
- ✅ **Understood the basic** workflow → activity pattern

---