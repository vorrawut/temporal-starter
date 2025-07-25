---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Kotlin + Spring Boot + Temporal Setup

## Lesson 2: Building Your Foundation

🧠 **Objective**: Set up Temporal with Spring Boot + Kotlin, understand how it fits together, and get ready to build real workflows.

---

# ⚙️ Temporal SDK Architecture

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

---

# Quick Architecture Breakdown

## 🔍 Core Components:

- **WorkflowServiceStubs**: Connects to Temporal server (think: Ethernet cable)
- **WorkflowClient**: Interface to start workflows (like your remote control)
- **WorkerFactory**: Manages the lifecycle of your workers
- **Worker**: Executes workflows and activities

---

# 🔌 Spring Boot Integration

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

**Simple and clean Spring configuration!**

---

# 🪝 Lifecycle Hooks

```kotlin
@PostConstruct
fun startWorker() = workerFactory.start()

@PreDestroy
fun shutdown() = workerFactory.shutdown()
```

## **Why This Matters:**
- ✅ **Proper startup** - Workers start after beans are initialized
- ✅ **Graceful shutdown** - Clean cleanup when app stops
- ✅ **Spring lifecycle** - Integrates with Spring Boot lifecycle

---

# 📦 Task Queues = Workflow Channels

```kotlin
val worker = workerFactory.newWorker("my-task-queue")
val stub = client.newWorkflowStub(
    MyWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("my-task-queue")
        .build()
)
```

## **Why Task Queues Matter:**
- ✅ **Horizontal scaling** - Add more workers as needed
- ✅ **Clear separation** - Different queues for different responsibilities
- ✅ **Logical routing** - Route workflows to appropriate workers

---

# 🧪 Local vs Production Setup

## **Local Development:**
```kotlin
WorkflowServiceStubs.newLocalServiceStubs()
```

## **Production:**
```kotlin
WorkflowServiceStubs.newServiceStubs(
    WorkflowServiceStubsOptions.newBuilder()
        .setTarget("temporal.mycompany.com:7233")
        .build()
)
```

---

# 🔧 Spring Configuration Example

```properties
# application.properties
temporal.server.host=localhost
temporal.server.port=7233
temporal.namespace=default
```

```kotlin
@ConfigurationProperties(prefix = "temporal")
data class TemporalProperties(
    val server: Server = Server()
) {
    data class Server(
        val host: String = "localhost",
        val port: Int = 7233
    )
}
```

---

# 📚 Dependencies You'll Need

```kotlin
// build.gradle.kts
implementation("io.temporal:temporal-sdk:1.22.3")
implementation("io.temporal:temporal-kotlin:1.22.3")
testImplementation("io.temporal:temporal-testing:1.22.3")

implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
implementation("io.github.microutils:kotlin-logging:3.0.5")
```

## 📝 Pro tip: 
Lock versions and group Temporal dependencies together for clarity.

---

# 🧠 Best Practices

## 🔄 Environment-Specific Beans

```kotlin
@Profile("test")
@Bean 
fun testStubs() = WorkflowServiceStubs.newLocalServiceStubs()

@Profile("!test")
@Bean 
fun prodStubs() = WorkflowServiceStubs.newServiceStubs(...)
```

**Result**: Different configurations for different environments

---

# 🧵 Multiple Workers Example

```kotlin
@PostConstruct
fun startWorkers() {
    val userWorker = workerFactory.newWorker("user-queue")
    val orderWorker = workerFactory.newWorker("order-queue")

    userWorker.registerWorkflowImplementationTypes(UserWorkflowImpl::class.java)
    orderWorker.registerActivitiesImplementations(OrderActivitiesImpl())

    workerFactory.start()
}
```

**Benefit**: Separate concerns with dedicated task queues

---

# 🚨 Common Mistakes

## ❌ Register After Start

```kotlin
// ❌ Wrong - Workers already started!
workerFactory.start()
worker.registerWorkflowImplementationTypes(...)

// ✅ Right - Register then start
worker.registerWorkflowImplementationTypes(...)
workerFactory.start()
```

---

# More Common Mistakes

## ❌ Hardcoding Configuration

```kotlin
// ❌ Wrong - No flexibility
WorkflowServiceStubs.newServiceStubs("prod-temporal:7233")

// ✅ Right - Configurable
@Value("\${temporal.server.url}")
lateinit var serverUrl: String
```

**Always use externalized configuration!**

---

# 🧰 Troubleshooting Cheatsheet

## Common Issues:

- ❗ **Connection refused** → Is Temporal running locally?
- ❗ **Bean creation failed** → Missing annotations or misconfigured `@Bean`
- ❗ **Worker not running** → Did you call `start()` after registration?

## 📋 Quick Fix:
Check logs and use structured logging for quick diagnosis.

---

# 💡 Key Takeaways

## **What You've Learned:**
- ✅ How to integrate Temporal with Spring Boot
- ✅ Configuration patterns for different environments
- ✅ Worker lifecycle management
- ✅ Task queue concepts
- ✅ Common pitfalls to avoid

---

# 🚀 What's Next?

**You've laid the groundwork!**

## Next up in Lesson 3:
- Spin up Temporal locally
- Build your first end-to-end workflow
- See everything working together

**Ready to run Temporal locally? Let's go! 🎉**
