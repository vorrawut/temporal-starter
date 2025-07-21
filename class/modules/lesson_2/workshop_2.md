# Workshop 2: Kotlin + Spring Boot + Temporal Setup

## What we want to build

A basic Spring Boot application in Kotlin that connects to Temporal. Think of this as creating the foundation - we're setting up the plumbing that will allow our application to communicate with Temporal, but we won't create any workflows yet.

## Expecting Result

By the end of this lesson, you'll have:

- A Spring Boot application that starts successfully
- Temporal SDK dependencies properly configured
- A Temporal client that connects to a local Temporal server
- A worker that's ready to execute workflows (even though we haven't written any yet)
- Console output showing "✅ Temporal worker started successfully!"

## Code Steps

### Step 1: Understand the Project Structure

Look at the starter code in `/src/workshop/lesson_2/`:
- `TemporalBootcampApplication.kt` - Standard Spring Boot main class
- `config/TemporalConfig.kt` - Empty configuration class with hints

The Temporal dependencies are already added to `build.gradle.kts` for you.

### Step 2: Configure Temporal Connection

Open `src/workshop/lesson_2/config/TemporalConfig.kt` and add the necessary imports:

```kotlin
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowClientOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.serviceclient.WorkflowServiceStubsOptions
import io.temporal.worker.WorkerFactory
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
```

### Step 3: Add Basic Properties

Inside the `TemporalConfig` class, add:

```kotlin
private val logger = KotlinLogging.logger {}
private lateinit var workerFactory: WorkerFactory
```

### Step 4: Create WorkflowServiceStubs Bean

This creates the connection to Temporal server:

```kotlin
@Bean
fun workflowServiceStubs(): WorkflowServiceStubs {
    logger.info { "Creating Temporal service stubs for local server" }
    return WorkflowServiceStubs.newLocalServiceStubs(
        WorkflowServiceStubsOptions.newBuilder()
            .build()
    )
}
```

### Step 5: Create WorkflowClient Bean

This provides the API to interact with Temporal:

```kotlin
@Bean
fun workflowClient(workflowServiceStubs: WorkflowServiceStubs): WorkflowClient {
    logger.info { "Creating Temporal workflow client" }
    return WorkflowClient.newInstance(
        workflowServiceStubs,
        WorkflowClientOptions.newBuilder()
            .build()
    )
}
```

### Step 6: Create WorkerFactory Bean

This manages workers that execute workflows:

```kotlin
@Bean
fun workerFactory(workflowClient: WorkflowClient): WorkerFactory {
    logger.info { "Creating Temporal worker factory" }
    workerFactory = WorkerFactory.newInstance(workflowClient)
    return workerFactory
}
```

### Step 7: Start the Worker

Add a method to start the worker after Spring finishes initializing:

```kotlin
@PostConstruct
fun startWorker() {
    logger.info { "Starting Temporal worker..." }
    
    // Create a worker for a test task queue
    val worker = workerFactory.newWorker("lesson2-test-queue")
    
    // Start the worker factory
    workerFactory.start()
    
    logger.info { "✅ Temporal worker started successfully! Connected to local Temporal server." }
}
```

### Step 8: Add Graceful Shutdown

Add cleanup when the application stops:

```kotlin
@PreDestroy
fun shutdown() {
    logger.info { "Shutting down Temporal worker..." }
    workerFactory.shutdown()
    logger.info { "❌ Temporal worker stopped" }
}
```

## How to Run

**Important**: This lesson requires Temporal server to be running locally. Since we haven't covered that yet, expect connection errors for now. That's completely normal!

### Option 1: From IDE
1. Open `TemporalBootcampApplication.kt`
2. Click the green arrow next to the `main` function
3. You should see the application start (but fail to connect to Temporal)

### Option 2: From Command Line
```bash
./gradlew bootRun --args="--spring.main.sources=com.temporal.workshop.lesson_2.TemporalBootcampApplication"
```

### Expected Output (Without Temporal Server)
```
Creating Temporal service stubs for local server
Creating Temporal workflow client  
Creating Temporal worker factory
Starting Temporal worker...
ERROR: Connection refused (Temporal server not running)
```

### Expected Output (With Temporal Server - Lesson 3)
```
Creating Temporal service stubs for local server
Creating Temporal workflow client
Creating Temporal worker factory  
Starting Temporal worker...
✅ Temporal worker started successfully! Connected to local Temporal server.
```

## Troubleshooting

**"Connection refused"** - This is expected! You need Temporal server running (covered in Lesson 3)

**"Class not found"** - Make sure all imports are correct

**"Bean creation failed"** - Check that all `@Bean` methods are properly annotated

## What You've Accomplished

- ✅ Set up the basic Temporal infrastructure in Spring Boot
- ✅ Created the connection configuration to Temporal server
- ✅ Prepared a worker that's ready to execute workflows
- ✅ Added proper logging and lifecycle management

The foundation is ready! In Lesson 3, we'll start the Temporal server and see this connection come alive. 