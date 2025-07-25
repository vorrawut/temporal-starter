---
marp: true
theme: default
paginate: true
title: Kotlin + Spring Boot + Temporal Setup
---
## Kotlin + Spring Boot + Temporal Setup

🧠 **Objective**:  
Set up Temporal with Spring Boot + Kotlin, understand how it fits together, and get ready to build real workflows.

---

## ⚙️ Temporal SDK Architecture

```text
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ Temporal Server │◄──►│ WorkflowClient   │◄──►│ Your Application│
└─────────────────┘    └──────────────────┘    └─────────────────┘
         ▲                       ▲                       ▲
         │                       │                       │
         │              ┌──────────────────┐             │
         └─────────────►│ Worker           │◄────────────┘
                        └──────────────────┘
```

🔍 Quick breakdown:
- `WorkflowServiceStubs`: Connects to Temporal server (think: Ethernet cable)
- `WorkflowClient`: Interface to start workflows (like your remote control)
- `WorkerFactory`: Manages the lifecycle of your workers
- `Worker`: Executes workflows and activities

---

## 🔌 Spring Boot Integration

```kotlin
@Configuration
class TemporalConfig {
    @Bean
    fun workflowServiceStubs() = WorkflowServiceStubs.newLocalServiceStubs()

    @Bean
    fun workflowClient(stubs: WorkflowServiceStubs) =
        WorkflowClient.newInstance(stubs)

    @Bean
    fun workerFactory(client: WorkflowClient) =
        WorkerFactory.newInstance(client)
}
```

🪝 **Lifecycle Hooks**
```kotlin
@PostConstruct
fun startWorker() = workerFactory.start()

@PreDestroy
fun shutdown() = workerFactory.shutdown()
```

---

## 📦 Task Queues = Workflow Channels

```kotlin
val worker = workerFactory.newWorker("my-task-queue")
val stub = client.newWorkflowStub(
    MyWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("my-task-queue")
        .build()
)
```

✅ Why task queues matter:
- Horizontal scaling
- Clear separation of responsibility
- Logical routing for workflows

---

## 🧪 Local vs Prod Setup

```kotlin
// Local dev
WorkflowServiceStubs.newLocalServiceStubs()

// Production
WorkflowServiceStubs.newServiceStubs(
    WorkflowServiceStubsOptions.newBuilder()
        .setTarget("temporal.mycompany.com:7233")
        .build()
)
```

🔧 **Spring Config Example**
```properties
temporal.server.host=localhost
temporal.server.port=7233
temporal.namespace=default
```

---

## 📚 Dependencies You’ll Need

```kotlin
implementation("io.temporal:temporal-sdk:1.22.3")
implementation("io.temporal:temporal-kotlin:1.22.3")
testImplementation("io.temporal:temporal-testing:1.22.3")

implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
implementation("io.github.microutils:kotlin-logging:3.0.5")
```

📝 Pro tip: Lock versions and group Temporal stuff together in your build file for clarity.

---

## 🧠 Best Practices

### 🔄 Environment-Specific Beans
```kotlin
@Profile("test")
@Bean fun testStubs() = WorkflowServiceStubs.newLocalServiceStubs()

@Profile("!test")
@Bean fun prodStubs() = WorkflowServiceStubs.newServiceStubs(...)
```

### 🧵 Multiple Workers
```kotlin
val userWorker = workerFactory.newWorker("user-queue")
val orderWorker = workerFactory.newWorker("order-queue")

userWorker.registerWorkflowImplementationTypes(UserWorkflowImpl::class.java)
orderWorker.registerActivitiesImplementations(OrderActivitiesImpl())

workerFactory.start()
```

---

## 🚨 Common Mistakes

### ❌ Register After Start
```kotlin
// ❌ Wrong
workerFactory.start()
worker.registerWorkflowImplementationTypes(...)

// ✅ Right
worker.registerWorkflowImplementationTypes(...)
workerFactory.start()
```

### ❌ Hardcoding Config
```kotlin
// ❌ Wrong
WorkflowServiceStubs.newServiceStubs("prod-temporal:7233")

// ✅ Right
@Value("\${temporal.server.url}")
lateinit var serverUrl: String
```

---

## 🧰 Troubleshooting Cheatsheet

- ❗ **Connection refused** → Is Temporal running locally?
- ❗ **Bean creation failed** → Missing annotations or misconfigured `@Bean`
- ❗ **Worker not running** → Did you call `start()` after registration?

📋 Check logs and use structured logging for quick diagnosis.

---

## 🚀 What’s Next?

You’ve laid the groundwork. Next up:  
Spin up Temporal locally and build your first end-to-end workflow.

_See you in Lesson 3!_
