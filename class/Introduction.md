---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# 🚀 Welcome to the Temporal Workflow Class!

*Your journey from zero to production-ready distributed workflows starts here*

---

# 👋 Hello, Future Temporal Developer!

Welcome to what might be **the most mind-bending yet practical technology** you'll learn this year.

**If you've ever:**
- Built distributed systems
- Dealt with microservices coordination
- Found yourself in "retry hell"
- Struggled with unreliable service integration

**Temporal is about to change your life.**

---

# 💡 Quick Promise

By the end of this course, you'll be building **fault-tolerant, scalable workflow systems** that can survive:

- ⚡ Server crashes
- 🌐 Network failures  
- 🏢 Entire data center outages

**Sound impossible? Let's dive in!**

---

# 🤔 What is Temporal?

Think of Temporal as **the operating system for your distributed workflows**.

Just like your OS manages:
- 🔄 Processes
- 💾 Memory
- 🔧 Resources

**Temporal manages:**
- 📋 Business processes across services
- 🌍 Multi-server coordination
- ⏰ Time-based workflows

---

# Temporal in Plain English

- **Durable execution platform** - runs code reliably when things go wrong
- **Perfect memory** - remembers exactly where your process was during crashes
- **Smart retries** - handles failures with exponential backoff
- **Time travel** - workflows can sleep for days/weeks and resume perfectly
- **Horizontal scaling** - add workers to handle more load

---

# The Magic Formula

```
Your Code + Temporal = Bulletproof Distributed Systems ✨
```

**Write code that looks synchronous and simple**
**But actually runs across multiple services with:**
- ✅ Built-in retry logic
- ✅ State persistence  
- ✅ Failure recovery

---

# 🎯 Why Temporal Matters: The Problems It Solves

## Before Temporal (The Dark Ages 😅)

**Scenario**: User registration process
1. Validate email with external service
2. Create user account in database
3. Send welcome email
4. Charge credit card
5. Send confirmation SMS

---

# Traditional Problems

- ❌ What if the email service is down? Retry forever?
- ❌ What if you crash after charging but before SMS?
- ❌ How do you track state across services?
- ❌ How do you handle partial failures gracefully?
- ❌ How do you test this complex flow?

**Result**: Spaghetti code, sleepless nights debugging 😴

---

# After Temporal (The Renaissance 🌟)

```kotlin
class UserRegistrationWorkflow {
    fun registerUser(email: String): RegistrationResult {
        val validation = emailService.validate(email)      // Auto-retry
        val user = userService.createAccount(validation)   // Durable
        val welcome = emailService.sendWelcome(user)       // Reliable
        val payment = paymentService.charge(user.card)     // Exactly-once
        val sms = smsService.sendConfirmation(user.phone)  // Fault-tolerant
        
        return RegistrationResult(user, payment, sms)
    }
}
```

---

# What Temporal Handles For You

- ✅ **Automatic retries** with exponential backoff
- ✅ **State persistence** survives crashes and restarts
- ✅ **Exactly-once execution** prevents duplicate charges
- ✅ **Timeout handling** for each step
- ✅ **Comprehensive observability** and debugging
- ✅ **Easy testing** with deterministic time control

---

# 🏢 Who's Using Temporal?

**You're in good company!** Temporal is battle-tested by world-class applications:

- 🎬 **Netflix** - Video processing pipelines (millions of videos)
- 💰 **Coinbase** - Cryptocurrency trading & settlements
- 💳 **Stripe** - Payment orchestration
- 📱 **Snap Inc** - Content delivery (billions of interactions)
- 🏪 **Instacart** - Order fulfillment workflows
- 💼 **HashiCorp** - Infrastructure automation

---

# Common Thread

All these companies needed:
- ✅ **Reliable workflow orchestration**
- ✅ **Observable distributed systems**  
- ✅ **Scalable state management**

They chose Temporal because it **eliminates the complexity** of building distributed state machines from scratch.

---

# 🛠 Real-World Use Cases

## 💳 E-commerce Order Processing
```
Order → Inventory → Payment → Shipping → Notifications → Analytics
```

## 📧 Marketing Campaigns
```
Trigger → Segment → Email → Track → Follow-up → A/B Test
```

## 🏦 Financial Transactions
```
Initiate → Validate → Reserve → Execute → Settle → Report
```

---

# More Use Cases

## 🤖 Human-in-the-Loop Approvals
```
Submit → Auto-Review → [Wait for Human] → Manual Review → Execute
```

## 📊 Data Pipeline Orchestration
```
Extract → Transform → Load → Validate → Backup → Notify
```

**Each step is:**
- Fault-tolerant
- Retryable
- Observable
- Testable

---

# 🎓 What You'll Learn in This Course

## Complete journey: **Beginner → Production-Ready Developer**

**17 Lessons organized in 4 phases:**

1. 🌱 **Foundation** (Lessons 1-4)
2. 🏗 **Building Blocks** (Lessons 5-10)
3. 🚀 **Advanced Patterns** (Lessons 11-14)
4. 🏭 **Production Ready** (Lessons 15-17)

---

# 🌱 Foundation Phase (Lessons 1-4)

- **Temporal fundamentals** and core concepts
- **Your first workflow** and activity
- **Development environment** setup
- **Temporal ecosystem** understanding

**Goal**: Get comfortable with basic concepts

---

# 🏗 Building Blocks Phase (Lessons 5-10)

- **Activities**: Encapsulating business logic
- **Clean Architecture**: Proper separation of concerns
- **Input/Output**: Complex data handling
- **Retry & Timeout**: Building resilient systems
- **Error Handling**: Graceful failure recovery
- **Signals**: External workflow interaction

**Goal**: Master the core building blocks

---

# 🚀 Advanced Patterns (Lessons 11-14)

- **Queries**: Real-time workflow state inspection
- **Child Workflows**: Building hierarchical systems
- **Versioning**: Safe production workflow evolution
- **Timers & Cron**: Time-based and recurring workflows

**Goal**: Handle complex real-world scenarios

---

# 🏭 Production Ready (Lessons 15-17)

- **External Services**: API and database integration
- **Testing Strategies**: Unit, mocks, and integration tests
- **Deployment**: Docker, Kubernetes, production infrastructure

**Goal**: Deploy reliable systems to production

---

# 🎯 Your Learning Outcomes

By lesson 17, you'll be able to:

- ✅ Design complex, fault-tolerant workflows
- ✅ Handle failures with proper retry strategies
- ✅ Build horizontally scalable systems
- ✅ Test workflow logic comprehensively
- ✅ Deploy Temporal applications to production
- ✅ Debug and monitor distributed workflows
- ✅ Apply Temporal patterns to real business problems

---

# 👨‍💻 Who This Course is For

## ✅ Perfect If You Are:

- **Kotlin Developer** with basic Spring Boot experience
- **Curious about distributed systems** but intimidated by complexity
- **Tired of building custom retry logic** and state management
- **Working with microservices** that need coordination
- **Building reliable systems** that need fault-tolerance
- **Interested in modern workflow orchestration**

---

# ✅ No Prior Experience Needed

- **Zero Temporal knowledge** - we start from basics
- **No complex distributed systems background** - we explain everything
- **No DevOps expertise required** - step-by-step deployment

## 🎯 Ideal Background:
- Basic Kotlin code and classes/functions
- Simple Spring Boot application (even just REST API)
- Understanding of databases and external API calls
- Command line and basic Docker comfort

---

# 📋 Nice-to-Know Concepts

## 🔄 Kotlin Coroutines (Basic)
- Asynchronous programming in Kotlin
- Temporal workflows benefit from async understanding
- **Don't worry**: We explain relevant parts

## 🏗 Spring Boot Structure
- Configuration, components, dependencies organization
- **Don't worry**: We show exactly what goes where

## 🔄 REST vs Async
- **REST**: Request-response, immediate results
- **Async**: Fire-and-forget, eventual results, event-driven

---

# 🧠 Mental Model Shift

**Instead of thinking:**
```
"When user clicks submit, immediately return success/failure"
```

**You'll think:**
```
"When user clicks submit, start a durable workflow 
that will eventually complete, with progress tracking"
```

**This paradigm shift is the biggest learning curve!**

---

# 🗺 Your Learning Journey

## 📖 Introduction (You are here! 👆)
- Understanding what Temporal is and why it matters
- Getting excited about possibilities
- Setting mental context

## 🛠 Lessons 1-4: Foundation
- Development environment setup
- First "Hello World" workflow
- Basic Temporal concepts
- Hands-on coding

---

# Learning Journey Continued

## 🏗 Lessons 5-10: Core Skills
- Master Temporal building blocks
- Learn production reliability patterns
- Build confidence with exercises

## 🚀 Lessons 11-17: Advanced & Production
- Complex real-world scenarios
- Testing and deployment strategies
- Graduate as production-ready developer

---

# 🎉 Ready to Begin Your Temporal Journey?

You're about to learn something that will **fundamentally change** how you think about building distributed systems.

**Temporal isn't just another tool** - it's a **new paradigm** that makes impossible-seeming reliability guarantees actually achievable.

---

# 🔥 What Makes This Exciting

- **Solve real problems** that plagued distributed systems for decades
- **More reliable code** than systems built by entire teams
- **Join a growing community** of workflow orchestration developers
- **Gain a superpower** for building systems that work when everything goes wrong

---

# 🎯 Your Next Step

1. **Take a deep breath** and get comfortable
2. **Trust the process** - every expert was once a beginner
3. **Dive into Lesson 1** - write your first Temporal workflow
4. **By lesson 17** - you'll be amazed at what you can build!

---

# 💪 Remember

**Temporal might feel different** from what you're used to, but that's exactly why it's so powerful.

**Every expert was once a beginner.**

Let's build something amazing together!

**Ready? Let's go! 🚀**

---
