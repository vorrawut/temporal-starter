---

marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
------------
## Introduction to Temporal

* by Vorrawut Judasri (Wut)
---

## Why This Matters 💡

Before we dive in, think about this:

> What happens when your backend job fails halfway? Who picks up the pieces?

This is where Temporal enters.

---

## Objective 🎯

* Understand what Temporal is and why it exists
* See how it solves painful issues in distributed systems
* Build the foundation to use it in real projects

---

# 🧠 What is Temporal?

A **workflow orchestration platform** for reliable, scalable applications.

> Think of it as an **operating system for distributed systems**.
> It manages workflows, retries, failures, and state so you don’t have to.

---

## Temporal in Real Life 🍳

> If your app is a restaurant, Temporal is the **head chef**:
>
> * Makes sure orders are cooked correctly
> * Handles problems (like running out of ingredients)
> * Tracks what’s done and what’s not

---

# 😖 What Problems Does It Solve?

---

## Without Temporal:

```
🔥 Scattered retry logic
🔥 Manual error handling
🔥 Hard to track long-running processes
🔥 Lost messages & race conditions
🔥 Difficult testing
🔥 No visibility
```

---

## Without Temporal (Example)

```kotlin
fun processOrder(order: Order) {
    try {
        val payment = paymentService.charge(order.amount)
        val inventory = inventoryService.reserve(order.items)
        val shipping = shippingService.arrange(order)
    } catch (e: Exception) {
        // Uh-oh. Now what?
    }
}
```

---

# 💪 How Temporal Helps

---

## With Temporal:

```
📅 Reliable execution (even after failure)
📈 Built-in state management
🔄 Automatic retries
📊 Full visibility
🧰 Easy local testing
🧬 Workflow versioning
```

---

## With Temporal (Example)

```kotlin
@WorkflowInterface
interface OrderWorkflow {
    @WorkflowMethod
    fun processOrder(order: Order): OrderResult
}

class OrderWorkflowImpl : OrderWorkflow {
    override fun processOrder(order: Order): OrderResult {
        val payment = paymentActivity.charge(order.amount)
        val inventory = inventoryActivity.reserve(order.items)
        val shipping = shippingActivity.arrange(order)
        return OrderResult(payment, inventory, shipping)
    }
}
```

---

# 🧩 Temporal Components

---

## Workflows 🎼

* Define business logic (orchestration)
* Deterministic & replayable

## Activities 🎸

* Perform actual work (API, DB, etc.)
* Can fail, retry, and run long

---

## Workers 🏭

* Execute workflows and activities
* Scalable, fault-tolerant

## Temporal Server 🏢

* Schedules, persists, coordinates everything

---

# ✅ When to Use Temporal

Perfect for:

* Multi-step business processes
* Long-running jobs
* Processes across services
* Anything that needs reliability

Not ideal for:

* Simple request/response APIs
* Real-time (\<ms latency) systems

---

# ⚖️ Temporal vs Others

| Approach            | Pros                     | Cons                      |
| ------------------- | ------------------------ | ------------------------- |
| Manual Coordination | Simple, flexible         | Hard to scale & maintain  |
| Message Queues      | Async communication      | No orchestration built-in |
| State Machines      | Clear state tracking     | Poor error handling       |
| **Temporal**        | Scalable, fault-tolerant | Learning curve            |

---

# 💡 Best Practices

---

## Mindset Shifts 🧠

* Design in **workflows**, not service chains
* Keep **non-determinism** out of workflows
* Assume **failures happen**
* Start **simple**, evolve with use

---

## Common Misconceptions 🚫

* "It's just a message queue"
* "Workflows are just functions"
* "Too much complexity"
* "Need to understand everything first"

---

## Mental Model 🧠

Temporal is your app’s:

* 🧠 Memory
* 💾 State manager
* 🧑‍🏫 Coordinator
* 👁️ Observer

---

# 👣 Next Step

> Time to build!
> In the next lesson, we’ll set up a **Kotlin + Spring Boot** project with Temporal SDK.

