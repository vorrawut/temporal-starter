# Workshop 4: HelloWorkflow - Your First Temporal Workflow

## What we want to build

Create your first complete Temporal workflow that demonstrates the core concepts: a workflow that orchestrates an activity to generate a personalized greeting. This is the classic "Hello World" for distributed workflows!

## Expecting Result

By the end of this lesson, you'll have:

- A working HelloWorkflow that takes a name and returns a greeting
- A GreetingActivity that generates the actual greeting message
- Complete Temporal configuration that registers both components
- A runner that executes the workflow automatically
- Console output showing successful workflow execution
- Your first workflow visible in the Temporal Web UI

## Code Steps

### Step 1: Create the Workflow Interface

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

**Key points:**
- `@WorkflowInterface` marks this as a workflow contract
- `@WorkflowMethod` marks the main entry point for the workflow
- Keep it simple: one input parameter, one return value

### Step 2: Create the Activity Interface

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

**Key points:**
- `@ActivityInterface` marks this as an activity contract
- `@ActivityMethod` marks the method that will be executed by workers
- Activities do the actual work (business logic)

### Step 3: Implement the Activity

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

**Key points:**
- `@Component` makes this a Spring-managed bean
- Add logging to see when activities execute
- This is where real business logic would go (database calls, API calls, etc.)

### Step 4: Implement the Workflow

Open `src/workshop/lesson_4/workflow/HelloWorkflowImpl.kt`:

```kotlin
package com.temporal.bootcamp.lesson4.workflow

import com.temporal.bootcamp.lesson4.activity.GreetingActivity
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

**Key points:**
- Create activity stub with timeout configuration
- Use `Workflow.getLogger()` for workflow logging
- Keep workflow logic simple - just orchestration

### Step 5: Configure Temporal

Open `src/workshop/lesson_4/config/TemporalConfig.kt` and build the complete configuration:

```kotlin
package com.temporal.bootcamp.lesson4.config

import com.temporal.bootcamp.lesson4.activity.GreetingActivityImpl
import com.temporal.bootcamp.lesson4.workflow.HelloWorkflowImpl
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.serviceclient.WorkflowServiceStubsOptions
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
    
    @Bean
    fun workflowServiceStubs(): WorkflowServiceStubs {
        return WorkflowServiceStubs.newLocalServiceStubs(
            WorkflowServiceStubsOptions.newBuilder().build()
        )
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
    
    @PostConstruct
    fun startWorker() {
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

### Step 6: Create the Workflow Runner

Open `src/workshop/lesson_4/runner/HelloWorkflowRunner.kt`:

```kotlin
package com.temporal.bootcamp.lesson4.runner

import com.temporal.bootcamp.lesson4.config.TemporalConfig
import com.temporal.bootcamp.lesson4.workflow.HelloWorkflow
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
    
    override fun run(vararg args: String?) {
        logger.info { "🚀 Running HelloWorkflow..." }
        
        val workflow = workflowClient.newWorkflowStub(
            HelloWorkflow::class.java,
            WorkflowOptions.newBuilder()
                .setTaskQueue(TemporalConfig.TASK_QUEUE)
                .setWorkflowId("hello-workflow-${System.currentTimeMillis()}")
                .build()
        )
        
        val result = workflow.sayHello("Temporal Learner")
        
        logger.info { "✅ Workflow completed! Result: $result" }
        logger.info { "🌐 Check http://localhost:8233 to see your workflow!" }
    }
}
```

## How to Run

### Prerequisites
1. **Temporal server running**:
   ```bash
   temporal server start-dev
   ```

2. **Verify Web UI**: http://localhost:8233 should load

### Run Your Workflow
```bash
./gradlew bootRun --args="--spring.main.sources=com.temporal.bootcamp.lesson4.TemporalBootcampApplication"
```

### Expected Output
```
✅ Temporal worker started for HelloWorkflow!
🚀 Running HelloWorkflow...
Generating greeting for: Temporal Learner
Generated greeting: Hello, Temporal Learner! Welcome to Temporal workflows!
✅ Workflow completed! Result: Hello, Temporal Learner! Welcome to Temporal workflows!
🌐 Check http://localhost:8233 to see your workflow!
```

### Verify in Web UI
1. **Go to http://localhost:8233**
2. **Click "Workflows" tab**
3. **You should see**: `hello-workflow-{timestamp}` with status "Completed"
4. **Click on the workflow** to see execution details
5. **Check the timeline** - you'll see both workflow and activity execution

## Troubleshooting

### "Connection refused"
- Make sure `temporal server start-dev` is running
- Check that you see "Started Temporal Server" message

### "Workflow not found" in Web UI
- Make sure your application ran successfully
- Check console for error messages
- Verify the task queue name matches in config and runner

### "Activity not registered"
- Check that `GreetingActivityImpl` is annotated with `@Component`
- Verify the activity is registered in `TemporalConfig.startWorker()`
- Make sure imports are correct

### Application won't start
- Check that all imports are added correctly
- Verify Kotlin syntax (especially lambda expressions)
- Make sure Spring can find all classes with `@Component` and `@Configuration`

## What You've Accomplished

- ✅ Created your first workflow interface and implementation
- ✅ Created your first activity interface and implementation  
- ✅ Configured Temporal to register and run your components
- ✅ Successfully executed a workflow and saw the results
- ✅ Verified execution in the Temporal Web UI
- ✅ Understood the basic workflow → activity pattern

You now have the foundation for building more complex workflows. The next lessons will build on this pattern to show more advanced Temporal features! 