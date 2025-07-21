# 🚀 Temporal Loan Application Demo

> **A complete, production-ready demonstration of Temporal Workflow capabilities using Kotlin and Spring Boot**

This demo showcases a comprehensive loan application system that demonstrates all major Temporal concepts from the bootcamp lessons 1-17. It's designed to be a hands-on capstone project that proves the real-world applicability of Temporal workflows.

## 🎯 What This Demo Demonstrates

### **Temporal Features Covered**
- ✅ **Workflow Orchestration** (Lessons 1-4) - Complex multi-step business processes
- ✅ **Activities with Retry Logic** (Lessons 5, 8) - Resilient external service calls
- ✅ **Clean Architecture** (Lesson 6) - Separated workflows and activities
- ✅ **Input/Output Handling** (Lesson 7) - Complex data structures
- ✅ **Error Handling & Compensation** (Lesson 9) - Saga pattern implementation
- ✅ **Signals** (Lesson 10) - External interaction with running workflows
- ✅ **Queries** (Lesson 11) - Real-time workflow state inspection
- ✅ **Child Workflows** (Lesson 12) - Hierarchical process management
- ✅ **Timers & Scheduling** (Lesson 14) - Long-running follow-up processes
- ✅ **External Service Integration** (Lesson 15) - APIs, databases, notifications
- ✅ **Testing Patterns** (Lesson 16) - Production-ready testing approach
- ✅ **Deployment** (Lesson 17) - Container-ready Spring Boot application

### **Business Process**
A realistic **loan application system** with:
- Document validation with business rules
- External credit bureau integration (with retries)
- Manual approval workflow using signals
- Bank transfer simulation with compensation
- Multi-channel notifications (email + SMS)
- Automated follow-up customer engagement

## 🚀 Quick Start

### **Prerequisites**
- Java 17+
- Docker (for Temporal server)
- curl or Postman (for testing)

### **1. Start Temporal Server**
```bash
temporal server start-dev
```

### **2. Run the Demo Application**
```bash
./gradlew bootRun
```

### **3. Submit a Loan Application**
```bash
curl -X POST http://localhost:8080/api/loan/apply \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "demo-user-001",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1-555-0123",
    "loanAmount": 25000,
    "purpose": "HOME_IMPROVEMENT",
    "annualIncome": 75000,
    "documents": ["ID_CARD", "INCOME_STATEMENT", "BANK_STATEMENT"]
  }'
```

### **4. Check Application Status**
```bash
curl http://localhost:8080/api/loan/status/demo-user-001
```

### **5. Approve the Loan**
```bash
curl -X POST http://localhost:8080/api/loan/approve/demo-user-001 \
  -H "Content-Type: application/json" \
  -d '{
    "approvedBy": "manager-456", 
    "notes": "Application meets all criteria"
  }'
```

### **6. Monitor in Temporal Web UI**
Visit [http://localhost:8233](http://localhost:8233) to see the workflow execution in real-time.

## 📁 Project Structure

```
src/main/kotlin/com/temporal/
├── model/                    # Data models and enums
│   ├── LoanApplication.kt
│   ├── RiskAssessment.kt
│   └── ...
├── workflow/                 # Workflow interfaces and implementations
│   ├── LoanApplicationWorkflow.kt
│   ├── LoanApplicationWorkflowImpl.kt
│   └── FollowUpWorkflowImpl.kt
├── activity/                 # Activity interfaces and implementations
│   ├── DocumentValidationActivity.kt
│   ├── RiskScoringActivity.kt
│   └── ...
├── service/                  # External service integrations
│   ├── CreditBureauService.kt
│   ├── BankService.kt
│   └── ...
├── controller/               # REST API controllers
│   └── LoanApplicationController.kt
└── config/                   # Temporal and Spring configuration
    └── TemporalConfig.kt

demo/                        # Documentation and guides
├── demo-guideline.md        # Complete usage guide
├── demo-diagram.md          # Architecture diagrams
├── demo-result.md           # Expected outcomes
└── README.md               # This file
```

## 🔄 Demo Scenarios

### **Scenario 1: Successful Application**
- Document validation passes
- Risk scoring succeeds (with retries)
- Manual approval granted
- Loan disbursed successfully
- Follow-up workflow started

### **Scenario 2: High-Risk Auto-Rejection**
```bash
curl -X POST http://localhost:8080/api/loan/apply \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "high-risk-user",
    "email": "test.low@example.com",
    "annualIncome": 30000,
    "loanAmount": 100000
  }'
```

### **Scenario 3: Manual Rejection**
```bash
curl -X POST http://localhost:8080/api/loan/reject/demo-user-001 \
  -H "Content-Type: application/json" \
  -d '{
    "rejectedBy": "manager-456",
    "reason": "Insufficient documentation"
  }'
```

## 🔍 Observability

### **Temporal Web UI**
- **URL**: [http://localhost:8233](http://localhost:8233)
- **Features**: Complete workflow history, activity retries, signal events

### **Application Health Checks**
```bash
curl http://localhost:8080/actuator/health
```

### **Real-time Queries**
```bash
curl http://localhost:8080/api/loan/query/demo-user-001/state
```

## 🧪 Testing Failure Scenarios

The demo includes built-in failure simulation:

- **Credit Bureau API**: 30% failure rate (demonstrates retries)
- **Bank Transfer API**: 15% failure rate (demonstrates compensation)
- **Email Service**: 5% failure rate (demonstrates notification resilience)
- **SMS Service**: 10% failure rate (demonstrates multi-channel fallback)

## 📚 Learning Outcomes

After running this demo, you'll understand:

### **Temporal Concepts**
- How workflows orchestrate complex business processes
- How activities encapsulate external service calls
- How signals enable external interaction with running workflows
- How queries provide real-time state inspection
- How child workflows manage hierarchical processes
- How timers enable long-running scheduled operations

### **Production Patterns**
- Retry strategies for unreliable external services
- Compensation patterns for handling failures
- State management across distributed systems
- Error handling and observability
- Clean architecture in workflow systems

### **Practical Skills**
- Building production-ready Temporal applications
- Integrating Temporal with Spring Boot
- Designing resilient distributed systems
- Implementing proper error handling and logging
- Creating comprehensive API interfaces

## 🎓 Next Steps

1. **Explore the Code**: Understand how each Temporal concept is implemented
2. **Modify Scenarios**: Change business rules, add new activities
3. **Scale the System**: Run multiple workers, test high load
4. **Add Features**: Implement additional approval steps, new notification channels
5. **Deploy to Production**: Use the containerization patterns from Lesson 17

## 📖 Related Documentation

- [Demo Guidelines](demo-guideline.md) - Complete usage instructions
- [Architecture Diagrams](demo-diagram.md) - Visual system overview
- [Expected Results](demo-result.md) - Verification guide

## 🤝 Contributing

This demo is part of the Temporal Bootcamp curriculum. Feel free to:
- Report issues or bugs
- Suggest improvements
- Add new features
- Share your extensions

## 📜 License

This project is part of the educational Temporal Bootcamp material.

---

**🎉 Congratulations! You've completed the Temporal Bootcamp and built a production-grade workflow system!**

*Ready to build the future of distributed systems with Temporal?* 🚀