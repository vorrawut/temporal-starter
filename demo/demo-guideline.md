# 🎬 Temporal Bootcamp Demo: Loan Application System

## 📋 Introduction

This is a **capstone demo** showcasing the full power of Temporal Workflows through a realistic **Loan Application System**. It merges lessons 1–17 into a single, production-ready application that demonstrates enterprise-grade workflow orchestration.

## 💡 What This Demo Does

We're simulating a **comprehensive loan application process** with:

### 🏦 **Core Business Process**
- **Multi-step loan application workflow** with document validation
- **Risk scoring with external credit bureau integration** (with retry logic)
- **Human approval process** using Temporal signals
- **Loan disbursement** with compensation rollback capabilities
- **Automated follow-up system** using cron-scheduled workflows
- **Real-time status tracking** via REST API and workflow queries

### 🛠 **Technical Features Demonstrated**
- **Activities with Retry Logic** (Lessons 5, 8) - External API calls with exponential backoff
- **Workflow & Activity Separation** (Lesson 6) - Clean architecture patterns
- **Input/Output Handling** (Lesson 7) - Complex data structures
- **Error Handling & Compensation** (Lesson 9) - Saga pattern for rollbacks
- **Signals** (Lesson 10) - External approval triggers
- **Queries** (Lesson 11) - Real-time workflow state inspection
- **Child Workflows** (Lesson 12) - Hierarchical process management
- **Timers & Cron** (Lesson 14) - Scheduled follow-up reminders
- **External Service Integration** (Lesson 15) - Mock credit bureau and notification services
- **Testing Patterns** (Lesson 16) - Unit tests for workflows and activities
- **Production Deployment** (Lesson 17) - Docker containerization

## 🏗️ System Architecture

### **High-Level Components**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   REST API      │    │   Temporal      │    │   External      │
│   Controllers   │────│   Workflows     │────│   Services      │
│                 │    │   & Activities  │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
    ┌────▼────┐             ┌────▼────┐             ┌────▼────┐
    │ Spring  │             │ Worker  │             │ Credit  │
    │ Boot    │             │ Process │             │ Bureau  │
    │ App     │             │         │             │ API     │
    └─────────┘             └─────────┘             └─────────┘
```

### **Workflow Orchestration**
1. **Main Loan Workflow** - Orchestrates the entire application process
2. **Follow-up Workflow** - Handles scheduled reminders and notifications
3. **Document Processing Child Workflow** - Manages document validation pipeline

## 🛠 How to Run the Demo

### 📦 Prerequisites

- **Java 17+** (OpenJDK or Oracle JDK)
- **Docker & Docker Compose** (for Temporal server)
- **Temporal CLI** (optional, for advanced operations)
- **curl or Postman** (for API testing)

### ▶️ Quick Start

```bash
# 1. Start Temporal Development Server
temporal server start-dev

# 2. In a new terminal, run the Spring Boot application
./gradlew bootRun

# 3. Verify the application is running
curl http://localhost:8080/actuator/health
```

### 🧪 Demo Scenarios

#### **Scenario 1: Successful Loan Application**

```bash
# 1. Submit a new loan application
curl -X POST http://localhost:8080/api/loan/apply \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-123",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1-555-0123",
    "loanAmount": 25000,
    "purpose": "HOME_IMPROVEMENT",
    "annualIncome": 75000,
    "documents": ["ID_CARD", "INCOME_STATEMENT", "BANK_STATEMENT"]
  }'

# Response: {"workflowId": "loan-app-user-123-<timestamp>", "status": "SUBMITTED"}
```

```bash
# 2. Check workflow status
curl http://localhost:8080/api/loan/status/user-123

# Response: Shows current workflow state and progress
```

```bash
# 3. Approve the loan (simulate manual approval)
curl -X POST http://localhost:8080/api/loan/approve/user-123 \
  -H "Content-Type: application/json" \
  -d '{"approvedBy": "manager-456", "notes": "Application meets all criteria"}'
```

```bash
# 4. Check final status
curl http://localhost:8080/api/loan/status/user-123

# Response: Should show "DISBURSED" status with loan details
```

#### **Scenario 2: Application with Retry Logic**

```bash
# Submit an application that will trigger external service retries
curl -X POST http://localhost:8080/api/loan/apply \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-retry-test",
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com",
    "phone": "+1-555-0456",
    "loanAmount": 50000,
    "purpose": "BUSINESS",
    "annualIncome": 100000,
    "documents": ["ID_CARD", "BUSINESS_LICENSE", "TAX_RETURNS"]
  }'

# Watch the logs to see retry attempts for credit scoring
```

#### **Scenario 3: Application Rejection**

```bash
# Submit an application that will be rejected
curl -X POST http://localhost:8080/api/loan/apply \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-reject-test",
    "firstName": "Bob",
    "lastName": "Wilson",
    "email": "bob.wilson@example.com",
    "phone": "+1-555-0789",
    "loanAmount": 100000,
    "purpose": "PERSONAL",
    "annualIncome": 30000,
    "documents": ["ID_CARD"]
  }'

# Reject the application
curl -X POST http://localhost:8080/api/loan/reject/user-reject-test \
  -H "Content-Type: application/json" \
  -d '{"rejectedBy": "manager-456", "reason": "Insufficient income"}'
```

### 🔍 Observability & Monitoring

#### **Temporal Web UI**
- **URL**: [http://localhost:8233](http://localhost:8233)
- **Features**: View workflow executions, inspect event history, monitor task queues

#### **Application Health Checks**
```bash
# Application health
curl http://localhost:8080/actuator/health

# Temporal worker status
curl http://localhost:8080/actuator/temporal

# Metrics endpoint
curl http://localhost:8080/actuator/metrics
```

#### **Workflow Queries**
```bash
# Get current workflow state
curl http://localhost:8080/api/loan/query/user-123/state

# Get workflow history
curl http://localhost:8080/api/loan/query/user-123/history

# Get current activity status
curl http://localhost:8080/api/loan/query/user-123/activities
```

## ✅ Expected Behavior

### **Successful Flow**
1. **Application Submission** → Workflow starts, assigns unique ID
2. **Document Validation** → Validates required documents (with mock delay)
3. **Risk Scoring** → Calls external credit bureau API (with retry on failure)
4. **Approval Wait** → Workflow pauses, waiting for manual approval signal
5. **Loan Disbursement** → Upon approval, disburses loan amount
6. **Follow-up Scheduling** → Schedules periodic check-ins and reminders
7. **Completion** → Workflow completes, all activities logged

### **Failure Scenarios**
- **Network failures** → Automatic retries with exponential backoff
- **Service timeouts** → Graceful timeout handling and fallback logic
- **Rejection** → Compensation activities (cleanup, notifications)
- **System crashes** → Workflow resumes from last persisted state

### **Real-time Features**
- **Progress tracking** → Query workflow state at any time
- **Signal handling** → Send approval/rejection signals while workflow runs
- **Event logging** → All activities logged with timestamps and outcomes

## 📚 Lessons Demonstrated

| Feature | Lesson | Implementation |
|---------|--------|----------------|
| Basic Workflow | 1-4 | Main loan application workflow |
| Simple Activities | 5 | Document validation, risk scoring |
| Clean Architecture | 6 | Separated interfaces and implementations |
| Input/Output | 7 | Complex loan application data structures |
| Retry & Timeout | 8 | Credit bureau API calls with retry |
| Error Handling | 9 | Compensation logic for failed disbursements |
| Signals | 10 | Approval/rejection signals |
| Queries | 11 | Real-time status and progress queries |
| Child Workflows | 12 | Document processing sub-workflows |
| Versioning | 13 | Safe workflow evolution (optional) |
| Timers & Cron | 14 | Follow-up reminder scheduling |
| External Services | 15 | Credit bureau and notification APIs |
| Testing | 16 | Unit tests for all components |
| Deployment | 17 | Docker containerization and deployment |

## 🧠 Learning Opportunities

### **Try These Experiments**
1. **Break the external API** → Observe retry behavior
2. **Restart the application** → See workflow resume from checkpoint
3. **Send signals at different times** → Test signal handling
4. **Query workflow state** → Inspect real-time progress
5. **Scale workers** → Run multiple worker instances
6. **Load test** → Submit multiple applications simultaneously

### **Extension Ideas**
1. **Add more approval steps** → Multi-level approval workflow
2. **Integrate real databases** → Replace in-memory storage
3. **Add email notifications** → Real SMTP integration
4. **Implement audit logging** → Track all workflow decisions
5. **Add user dashboard** → Web UI for applicant status
6. **Connect to real credit APIs** → Replace mocks with actual service integrations

## 🎯 Key Takeaways

This demo proves that **Temporal enables building production-grade distributed systems** with:

- ✅ **Reliability** → Automatic retry and recovery
- ✅ **Observability** → Complete visibility into process execution
- ✅ **Scalability** → Horizontal scaling of workers
- ✅ **Maintainability** → Clean separation of business logic
- ✅ **Testability** → Deterministic testing of complex workflows
- ✅ **Flexibility** → Easy to modify and extend workflows

**You've learned to build systems that Fortune 500 companies rely on for their most critical business processes!** 🚀

---

## 🔗 Next Steps

1. **Deploy to production** → Use the Docker configurations from Lesson 17
2. **Add monitoring** → Integrate with Prometheus and Grafana
3. **Scale horizontally** → Run multiple worker instances
4. **Enhance security** → Add authentication and authorization
5. **Connect to real systems** → Replace mocks with actual service integrations

**Congratulations on completing the Temporal Bootcamp!** 🎉 