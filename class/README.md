---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# 📚 Temporal Workflow Bootcamp

## Lessons 1–17: From Zero to Production-Ready

Welcome to the comprehensive **Temporal Workflow Bootcamp**! 

This hands-on curriculum takes you from zero to production-ready Temporal development using **Kotlin** and **Spring Boot**.

---

# 🎯 Learning Approach

**Progressive tracks** to help you grow confidently through the Temporal ecosystem:

1. **Foundation** - Core principles
2. **Intermediate** - Advanced features  
3. **Advanced** - Complex patterns
4. **Testing** - Quality assurance
5. **Deployment** - Production readiness

---

# 🚦 Learning Tracks Overview

## 🧱 Lessons 1–6: Foundation & Fundamentals
Core principles of Temporal development: workflows, activities, architecture, and basic patterns.

## 🔁 Lessons 7–12: Intermediate Workflows  
Diving deeper into Temporal's features, including retries, error handling, and signals.

## 🔬 Lessons 13–15: Advanced Use Cases
Building child workflows, timers, and long-running patterns like cron workflows.

---

# Learning Tracks Continued

## 🧪 Lesson 16: Testing Workflows
How to test workflows effectively using Temporal's testing utilities.

## 🚢 Lesson 17: Deployment
Packaging and deploying your Temporal services using Docker, Docker Compose, and Kubernetes.

---

# 📁 Folder Structure

Each lesson uses a **consistent layout** to separate learning materials, starter code, and answers:

```
class/
├── workshop/lesson_x/          # Starter code with TODOs
└── modules/lesson_x/
    ├── workshop_x.md          # Step-by-step build instructions
    ├── concept.md             # Theory and best practices
    └── answer/lesson_x/       # Complete working solution
```

---

# 🧱 Foundation & Fundamentals (Lessons 1–6)

## Lesson 1: Hello Temporal
- Set up Kotlin + Spring Boot with Temporal SDK
- Start Temporal server locally
- Run your first Hello Workflow

## Lesson 2: Your First Workflow  
- Define workflow + activity interfaces and implementations
- Register with worker
- Execute and observe on Temporal Web UI

---

# Foundation Continued

## Lesson 3: Workflow Options
- Configure `WorkflowOptions` (timeouts, IDs, task queues)
- Introduce custom workflow IDs and routing

## Lesson 4: Custom Task Queues
- Create multiple workers with distinct task queues
- Show how queues route traffic to activities

---

# Foundation Continued

## Lesson 5: Adding a Simple Activity
- Calculator example using activity stubs
- Add timeout handling
- Simple workflow-activity communication

## Lesson 6: Workflow & Activity Separation
- User onboarding use case
- Clean architecture: multiple activities
- Error handling (critical vs non-critical failures)

---

# 🔁 Intermediate Workflows (Lessons 7–12)

## Lesson 7: Workflow Input/Output
- Use complex input/output models
- Input validation and transformation
- Return structured result objects

## Lesson 8: Activity Retry + Timeout
- Add custom retry policies and backoff
- Long-running activities with heartbeat
- Handle non-retriable failures

---

# Intermediate Continued

## Lesson 9: Error Handling in Workflows
- Try/catch inside workflows
- Compensation logic with Saga pattern
- Circuit breaker for unstable dependencies

## Lesson 10: Signals
- Implement signal handlers
- Long-running workflows that respond to external input
- Approval workflows, status updates via signal

---

# Intermediate Continued

## Lesson 11: Queries
- Add query methods to inspect workflow state
- Combine signals + queries for interactive flows
- Use cases: real-time dashboards, manual overrides

## Lesson 12: Workflow Versioning
- Demonstrate `WorkflowImplementationOptions`
- Add `Workflow.getVersion()` usage
- Safe refactoring for long-lived workflows

---

# 🔬 Advanced Use Cases (Lessons 13–15)

## Lesson 13: Child Workflows
- Create and invoke child workflows
- Model workflows that represent real-life sub-processes
- Isolate errors and propagate results

## Lesson 14: Workflow Timers
- Schedule with `Workflow.sleep()`
- Build reminder or timeout workflows
- Control time inside unit tests

---

# Advanced Continued

## Lesson 15: Cron Workflows
- Define scheduled (cron) workflows
- Use retry + expiration with cron
- Explore use cases: reports, recurring notifications

---

# 🧪 Testing (Lesson 16)

## Lesson 16: Workflow Testing

- Set up Temporal's test framework in Kotlin
- Write unit tests for activities
- Simulate workflow logic in tests
- Control virtual time and mock dependencies

**Goal**: Ensure your workflows are bulletproof before production

---

# 🚢 Deployment (Lesson 17)

## Lesson 17: Deploying to Production

**Docker Setup:**
- Create Dockerfile for Spring Boot worker app
- Build `docker-compose.yaml` with:
  - Temporal server (auto-setup)
  - PostgreSQL DB  
  - Your worker service

**Kubernetes Deploy:**
- Deploy using Helm or YAML
- Configure secrets, networking, observability

---

# 📊 Progressive Complexity

| Lesson Range | Track        | Complexity | Focus Area                           |
|--------------|--------------|------------|--------------------------------------|
| 1–6          | Foundation   | ⭐–⭐⭐      | Basics, first workflows, setup      |
| 7–12         | Intermediate | ⭐⭐–⭐⭐⭐   | Retry, signals, input/output, errors|
| 13–15        | Advanced     | ⭐⭐⭐⭐     | Child, timers, cron                 |
| 16           | Testing      | ⭐⭐        | Testing and mocks                   |
| 17           | Deployment   | ⭐⭐⭐⭐     | Docker, Kubernetes, Helm            |

---

# 🎓 Learning Outcomes

**After this bootcamp, you will:**

✅ **Understand** core Temporal concepts and architecture  
✅ **Build** production-ready workflows and activities  
✅ **Handle** errors, retries, and stateful logic reliably  
✅ **Use** signals, queries, and timers for interactivity  
✅ **Test** workflows using mocks and time travel  
✅ **Deploy** your workflows with Docker and Kubernetes  

---

# 💡 Key Skills You'll Master

## **Technical Skills:**
- Temporal workflow design patterns
- Kotlin + Spring Boot integration
- Error handling and compensation logic
- Testing strategies for distributed systems
- Container orchestration and deployment

## **Conceptual Understanding:**
- Distributed system reliability patterns
- Event-driven architecture
- Stateful vs stateless processing
- Production deployment best practices

---

# 💬 Getting Help

**If you get stuck:**

- 📖 Read `concept.md` inside the lesson folder
- 💡 Review the `answer/` code  
- 🌐 Check Temporal Web UI: [http://localhost:8233](http://localhost:8233)
- 🐳 Ensure Docker or Temporal CLI is running
- 💬 Ask in the Temporal Slack community

**Remember**: Every expert was once a beginner!

---

# 🚀 Ready to Start?

**Your journey to mastering Temporal begins now!**

## Next Steps:
1. Start with **Lesson 1: Hello Temporal**
2. Follow each lesson in sequence
3. Practice with the workshop exercises
4. Build the complete solution step by step

**Let's build some powerful, fault-tolerant, distributed applications!**

**Happy hacking! 🚀**
