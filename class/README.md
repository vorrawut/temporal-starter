# 📚 Temporal Workflow Bootcamp: Lessons 5-10

Welcome to the advanced section of the Temporal Workflow bootcamp! This section builds on the foundational concepts from lessons 1-4 and introduces intermediate to advanced Temporal patterns.

## 🎯 Learning Progression

### **Lesson 5: Adding a Simple Activity**
**Focus**: Basic workflow-activity pattern
- Simple calculator workflow calling a math activity
- Activity stubs and timeout configuration
- Basic workflow-activity communication

### **Lesson 6: Workflow & Activity Separation** 
**Focus**: Clean architecture and separation of concerns
- Multi-step user onboarding workflow
- Three distinct activities (validation, account creation, notification)
- Single Responsibility Principle in practice
- Error handling: critical vs non-critical failures

### **Lesson 7: Workflow Input/Output**
**Focus**: Complex data modeling and validation
- Rich input objects with nested data structures
- Comprehensive output objects with detailed results
- Input validation patterns
- Data transformation between workflow and activities

### **Lesson 8: Activity Retry + Timeout**
**Focus**: Resilience and fault tolerance
- Custom retry policies for different operation types
- Exponential backoff and timeout strategies
- Handling different failure types (retriable vs non-retriable)
- Activity heartbeats for long-running operations

### **Lesson 9: Error Handling in Workflows**
**Focus**: Comprehensive error management
- Custom business exceptions
- Try-catch patterns in workflows
- Compensation logic (saga pattern)
- Circuit breaker patterns for external services

### **Lesson 10: Signals**
**Focus**: Interactive and reactive workflows
- Signal and query methods
- Long-running workflows that respond to external events
- Approval workflow patterns
- Multiple signal handlers

## 📁 Folder Structure

Each lesson follows this consistent structure:

```
class/
├── workshop/lesson_x/          # Starter code with TODOs
├── answer/lesson_x/            # Complete working solution
└── modules/lesson_x/
    ├── workshop_x.md          # Step-by-step build instructions
    └── concept.md             # Theory and best practices
```

## 🚀 How to Use This Bootcamp

### For Students:
1. **Start with the workshop code** in `/workshop/lesson_x/`
2. **Follow the instructions** in `/modules/lesson_x/workshop_x.md`
3. **Build incrementally** - don't look at the answer too quickly!
4. **Compare your solution** with `/answer/lesson_x/` when complete
5. **Read the concepts** in `/modules/lesson_x/concept.md` for deeper understanding

### For Instructors:
- Each lesson is self-contained and can be taught separately
- Workshop guides provide step-by-step instructions
- Concept guides explain the theory and best practices
- Answer code demonstrates production-ready patterns

## 🛠️ Prerequisites

Before starting these lessons, ensure you have:

- **Completed lessons 1-4** (basic Temporal setup and HelloWorkflow)
- **Temporal server running locally** (`temporal server start-dev`)
- **Kotlin/Spring Boot development environment** set up
- **Basic understanding** of workflows and activities

## 🔧 Running the Code

### General Pattern:
```kotlin
// 1. Register components with worker
worker.registerWorkflowImplementationTypes(YourWorkflowImpl::class.java)
worker.registerActivitiesImplementations(YourActivityImpl())

// 2. Create workflow stub
val workflow = workflowClient.newWorkflowStub(
    YourWorkflow::class.java,
    WorkflowOptions.newBuilder()
        .setTaskQueue("your-queue")
        .setWorkflowId("your-workflow-id")
        .build()
)

// 3. Execute workflow
val result = workflow.yourMethod(inputData)
```

### Verification:
- Check console output for logging
- Visit Temporal Web UI at http://localhost:8233
- Verify workflow executions and activity details

## 📊 Progressive Complexity

| Lesson | Complexity | Key Concepts | Best For |
|--------|------------|--------------|----------|
| 5 | ⭐ | Basic patterns | Understanding fundamentals |
| 6 | ⭐⭐ | Clean architecture | Production readiness |
| 7 | ⭐⭐ | Data modeling | Complex business logic |
| 8 | ⭐⭐⭐ | Resilience | Fault-tolerant systems |
| 9 | ⭐⭐⭐ | Error handling | Enterprise applications |
| 10 | ⭐⭐⭐⭐ | Interactive workflows | Advanced use cases |

## 🎓 Learning Outcomes

After completing lessons 5-10, you'll be able to:

✅ **Design clean, maintainable workflow architectures**
✅ **Handle complex input/output data models** 
✅ **Implement robust error handling and retry strategies**
✅ **Build resilient, fault-tolerant distributed systems**
✅ **Create interactive workflows with signals and queries**
✅ **Apply production-ready patterns and best practices**

## 🔄 Next Steps

After mastering lessons 5-10, you'll be ready for advanced topics:

- **Child workflows and workflow chaining**
- **Workflow versioning and migration**
- **Timers and cron workflows** 
- **Testing strategies and patterns**
- **Production deployment and scaling**
- **Observability and monitoring**

## 🤝 Getting Help

If you encounter issues:

1. **Check the concept.md** files for detailed explanations
2. **Review the complete answers** in `/answer/` folders
3. **Verify Temporal server** is running (`temporal server start-dev`)
4. **Check the Temporal Web UI** for workflow execution details
5. **Review console logs** for error messages

---

**Happy Learning!** 🚀 These lessons will transform you from a Temporal beginner into a confident workflow architect ready to build production-grade distributed systems.

---

*For questions or contributions, please refer to the main project documentation.* 