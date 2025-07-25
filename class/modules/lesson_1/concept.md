---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Introduction to Temporal

## Lesson 1: Building the Foundation

*by Vorrawut Judasri (Wut)*

---

# Why This Matters 💡

Before we dive in, think about this:

> **What happens when your backend job fails halfway?**
> **Who picks up the pieces?**

This is where **Temporal** enters.

---

# Objective 🎯

By the end of this lesson, you will:

- ✅ **Understand** what Temporal is and why it exists
- ✅ **See** how it solves painful issues in distributed systems  
- ✅ **Build** the foundation to use it in real projects

---

# 🧠 What is Temporal?

A **workflow orchestration platform** for reliable, scalable applications.

> Think of it as an **operating system for distributed systems**.
> 
> It manages workflows, retries, failures, and state so you don't have to.

---

# Temporal in Real Life 🍳

> **If your app is a restaurant, Temporal is the head chef:**
>
> - 👨‍🍳 Makes sure orders are cooked correctly
> - 🛠️ Handles problems (like running out of ingredients)  
> - 📋 Tracks what's done and what's not
> - 🔄 Coordinates the entire kitchen workflow

---

# 😖 What Problems Does It Solve?

## Without Temporal:

- 🔥 **Scattered retry logic**
- 🔥 **Manual error handling**
- 🔥 **Hard to track long-running processes**
- 🔥 **Lost messages & race conditions**
- 🔥 **Difficult testing**
- 🔥 **No visibility**

---

# Example: Without Temporal

```kotlin
fun processOrder(order: Order) {
    try {
        val payment = paymentService.charge(order.amount)
        val inventory = inventoryService.reserve(order.items)
        val shipping = shippingService.arrange(order)
    } catch (e: Exception) {
        // Uh-oh. Now what?
        // How do we recover?
        // What state are we in?
        // Should we retry everything?
    }
}
```

**Problem**: No coordination, no recovery, no visibility!

---

# 💪 How Temporal Helps

## With Temporal:

- ✅ **Reliable execution** (even after failure)
- ✅ **Built-in state management**
- ✅ **Automatic retries**
- ✅ **Full visibility**
- ✅ **Easy local testing**
- ✅ **Workflow versioning**

---

# Example: With Temporal

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

**Result**: Clean, reliable, observable workflow!

---

# 🧩 Temporal Components

## Workflows 🎼
- **Define business logic** (orchestration)
- **Deterministic & replayable**
- Control the flow of execution

## Activities 🎸  
- **Perform actual work** (API, DB, etc.)
- **Can fail, retry, and run long**
- Handle non-deterministic operations

---

# Components Continued

## Workers 🏭
- **Execute workflows and activities**
- **Scalable, fault-tolerant**
- Run your business logic

## Temporal Server 🏢
- **Schedules, persists, coordinates everything**
- **Manages state and execution**
- The brain of the operation

---

# ✅ When to Use Temporal

## **Perfect for:**
- ✅ Multi-step business processes
- ✅ Long-running jobs  
- ✅ Processes across services
- ✅ Anything that needs reliability

## **Not ideal for:**
- ❌ Simple request/response APIs
- ❌ Real-time (<ms latency) systems
- ❌ Basic CRUD operations

---

# ⚖️ Temporal vs Other Approaches

| Approach            | Pros                     | Cons                      |
|---------------------|--------------------------|---------------------------|
| Manual Coordination | Simple, flexible         | Hard to scale & maintain  |
| Message Queues      | Async communication      | No orchestration built-in |
| State Machines      | Clear state tracking     | Poor error handling       |
| **Temporal**        | Scalable, fault-tolerant | Learning curve            |

---

# 💡 Best Practices

## Mindset Shifts 🧠

- ✅ Design in **workflows**, not service chains
- ✅ Keep **non-determinism** out of workflows  
- ✅ Assume **failures happen**
- ✅ Start **simple**, evolve with use

---

# Common Misconceptions 🚫

## **Wrong Assumptions:**
- ❌ "It's just a message queue"
- ❌ "Workflows are just functions"  
- ❌ "Too much complexity"
- ❌ "Need to understand everything first"

## **Reality:**
- ✅ It's a durable execution platform
- ✅ Start simple, learn by doing

---

# Mental Model 🧠

## **Temporal is your app's:**

- 🧠 **Memory** - Never forgets where you left off
- 💾 **State manager** - Handles complex state transitions
- 🧑‍🏫 **Coordinator** - Orchestrates distributed processes  
- 👁️ **Observer** - Provides complete visibility

---

# 👣 Next Steps

> **Time to build!**
> 
> In the next lesson, we'll set up a **Kotlin + Spring Boot** project with Temporal SDK.

## **You're ready to:**
- Start building real workflows
- See Temporal in action
- Experience the power of durable execution

**Let's start coding! 🚀**

