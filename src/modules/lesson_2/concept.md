# Concept 2: Kotlin + Spring Boot + Temporal Setup

## Objective

Learn how to integrate Temporal into a Spring Boot application using Kotlin. Understand the key components needed to connect your application to Temporal and prepare the foundation for building workflows.

## Key Concepts

### 1. **Temporal SDK Architecture**

The Temporal SDK has several key components that work together:

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│                 │    │                  │    │                 │
│ Temporal Server │◄──►│ WorkflowClient   │◄──►│ Your Application│
│                 │    │                  │    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         ▲                       ▲                       ▲
         │                       │                       │
         │              ┌──────────────────┐             │
         │              │                  │             │
         └──────────────►│ Worker           │◄────────────┘
                         │                  │
                         └──────────────────┘
```

#### **WorkflowServiceStubs**
- **Purpose**: Low-level connection to Temporal server
- **Think of it as**: The network cable connecting to Temporal
- **Configuration**: Usually points to `localhost:7233` for local development

#### **WorkflowClient** 
- **Purpose**: High-level API to start workflows, send signals, etc.
- **Think of it as**: The remote control for your workflows
- **Usage**: Your application code uses this to interact with workflows

#### **WorkerFactory**
- **Purpose**: Creates and manages workers
- **Think of it as**: A factory that produces workers on demand
- **Management**: Handles lifecycle, resource allocation, and scaling

#### **Worker**
- **Purpose**: Executes workflows and activities
- **Think of it as**: The actual worker bee that does the work
- **Task Queues**: Each worker listens to specific task queues

### 2. **Spring Boot Integration Patterns**

#### **Configuration as Beans**
```kotlin
@Configuration
class TemporalConfig {
    
    @Bean
    fun workflowServiceStubs(): WorkflowServiceStubs {
        // Connection configuration
    }
    
    @Bean  
    fun workflowClient(stubs: WorkflowServiceStubs): WorkflowClient {
        // Client configuration
    }
    
    @Bean
    fun workerFactory(client: WorkflowClient): WorkerFactory {
        // Worker factory configuration
    }
}
```

**Why this pattern?**
- Spring manages bean lifecycle automatically
- Dependency injection handles connections between components
- Easy to test with mocks
- Configuration is centralized and declarative

#### **Lifecycle Management**
```kotlin
@PostConstruct
fun startWorker() {
    // Start workers after all beans are created
    workerFactory.start()
}

@PreDestroy  
fun shutdown() {
    // Gracefully stop workers when app shuts down
    workerFactory.shutdown()
}
```

**Why lifecycle hooks?**
- Ensures workers start only after full initialization
- Provides graceful shutdown for running workflows
- Prevents resource leaks

### 3. **Task Queues**

#### **What are Task Queues?**
Task queues are named channels that connect workflow execution requests with workers.

```kotlin
// Create a worker that listens to a specific queue
val worker = workerFactory.newWorker("my-task-queue")

// Later, start workflows on that same queue
val workflow = client.newWorkflowStub(
    MyWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("my-task-queue")  // Same queue name!
        .build()
)
```

#### **Task Queue Benefits**
- **Load Balancing**: Multiple workers can listen to the same queue
- **Isolation**: Different workflow types can use different queues
- **Scaling**: Add more workers to handle increased load
- **Routing**: Route different workflows to different worker pools

### 4. **Local Development Setup**

#### **Connection Configuration**
```kotlin
// For local development
WorkflowServiceStubs.newLocalServiceStubs()

// For production (example)
WorkflowServiceStubs.newServiceStubs(
    WorkflowServiceStubsOptions.newBuilder()
        .setTarget("temporal.mycompany.com:7233")
        .build()
)
```

#### **Environment-Specific Configuration**
```kotlin
@Value("\${temporal.server.host:localhost}")
private val temporalHost: String

@Value("\${temporal.server.port:7233}")  
private val temporalPort: Int
```

### 5. **Dependencies Explained**

#### **Core Temporal Dependencies**
```kotlin
// Main Temporal SDK
implementation("io.temporal:temporal-sdk:1.22.3")

// Kotlin-specific helpers
implementation("io.temporal:temporal-kotlin:1.22.3")

// Testing support
testImplementation("io.temporal:temporal-testing:1.22.3")
```

#### **Supporting Dependencies**
```kotlin
// Coroutines for async programming
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

// Logging
implementation("io.github.microutils:kotlin-logging:3.0.5")
```

## Best Practices

### ✅ Configuration Best Practices

1. **Use Spring Profiles**
   ```kotlin
   @Profile("!test")
   @Bean
   fun workflowServiceStubs(): WorkflowServiceStubs {
       // Real Temporal connection
   }
   
   @Profile("test")
   @Bean
   fun testWorkflowServiceStubs(): WorkflowServiceStubs {
       // Test environment setup
   }
   ```

2. **Externalize Configuration**
   ```properties
   # application.properties
   temporal.server.host=localhost
   temporal.server.port=7233
   temporal.namespace=default
   ```

3. **Health Checks**
   ```kotlin
   @Component
   class TemporalHealthCheck(
       private val workflowClient: WorkflowClient
   ) : HealthIndicator {
       
       override fun health(): Health {
           return try {
               workflowClient.workflowService.getSystemInfo()
               Health.up().build()
           } catch (e: Exception) {
               Health.down(e).build()
           }
       }
   }
   ```

### ✅ Worker Management

1. **One Worker Per Task Queue**
   ```kotlin
   // Good: Clear separation of concerns
   val userWorker = workerFactory.newWorker("user-workflows")
   val orderWorker = workerFactory.newWorker("order-workflows")
   ```

2. **Register Before Starting**
   ```kotlin
   // Always register workflows and activities before starting
   worker.registerWorkflowImplementationTypes(MyWorkflowImpl::class.java)
   worker.registerActivitiesImplementations(MyActivityImpl())
   
   // Then start
   workerFactory.start()
   ```

3. **Resource Limits**
   ```kotlin
   val worker = workerFactory.newWorker(
       "my-queue",
       WorkerOptions.newBuilder()
           .setMaxConcurrentWorkflowExecutionSize(100)
           .setMaxConcurrentActivityExecutionSize(200)
           .build()
   )
   ```

### ❌ Common Mistakes

1. **Starting Workers Too Early**
   ```kotlin
   // BAD: Worker starts before workflows are registered
   workerFactory.start()
   worker.registerWorkflowImplementationTypes(MyWorkflow::class.java)
   
   // GOOD: Register first, then start
   worker.registerWorkflowImplementationTypes(MyWorkflow::class.java) 
   workerFactory.start()
   ```

2. **Forgetting Graceful Shutdown**
   ```kotlin
   // BAD: No cleanup
   @PostConstruct
   fun start() {
       workerFactory.start()
   }
   
   // GOOD: Proper lifecycle management
   @PostConstruct
   fun start() {
       workerFactory.start()
   }
   
   @PreDestroy
   fun stop() {
       workerFactory.shutdown()
   }
   ```

3. **Hardcoded Configuration**
   ```kotlin
   // BAD: Hardcoded values
   WorkflowServiceStubs.newServiceStubs("prod-temporal:7233")
   
   // GOOD: Configurable
   @Value("\${temporal.server.url}")
   private val serverUrl: String
   ```

### 🔧 Troubleshooting Tips

**"Connection refused"**
- Check if Temporal server is running
- Verify host and port configuration
- Check firewall/network settings

**"Bean creation failed"**
- Ensure all dependencies are imported
- Check that beans are properly annotated
- Verify Spring component scanning includes your config package

**"Worker not starting"**
- Make sure `workerFactory.start()` is called
- Check for exceptions in startup logs
- Verify task queue names are consistent

---

**Ready for the next step?** Now that you understand how to set up Temporal with Spring Boot, let's learn how to run Temporal server locally in Lesson 3! 