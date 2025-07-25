---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# 📜 Diagram for Lesson 8: Activity Retry + Timeout

## Visualizing Resilient Activity Execution

*Retry patterns and timeout strategies for resilient activity execution*

---

# Activity Retry Flow with Exponential Backoff

```mermaid
sequenceDiagram
    participant W as Workflow
    participant TS as Temporal Server
    participant A as Activity
    participant ES as External Service
    
    Note over W,ES: Resilient Activity Execution with Retries
    
    W->>TS: Schedule Activity with RetryOptions
    Note over TS: StartToCloseTimeout: 2min<br/>MaxAttempts: 3<br/>InitialInterval: 1s
    
    TS->>A: Execute Activity (Attempt 1)
    A->>ES: Call External Service
    ES-->>A: Timeout/Failure
    A-->>TS: Activity Failed
    
    Note over TS: Wait 1s (InitialInterval)
    
    TS->>A: Execute Activity (Attempt 2)
    A->>ES: Call External Service
    ES-->>A: Timeout/Failure
    A-->>TS: Activity Failed
    
    Note over TS: Wait 2s (Backoff x2)
    
    TS->>A: Execute Activity (Attempt 3)
    A->>ES: Call External Service
    ES->>A: Success Response
    A->>TS: Activity Completed
    TS->>W: Activity Result
```

---

# Retry Strategy by Activity Type

```mermaid
graph TD
    A[Activity Type] --> B{Operation Category}
    
    B -->|Critical| C[Payment Processing]
    B -->|Standard| D[Validation]
    B -->|Long-Running| E[File Processing]
    
    C --> F[MaxAttempts: 5<br/>LongTimeout<br/>Conservative Backoff]
    D --> G[MaxAttempts: 3<br/>MediumTimeout<br/>Standard Backoff]
    E --> H[MaxAttempts: 10<br/>Heartbeat Required<br/>Aggressive Backoff]
    
    style C fill:#ffebee
    style D fill:#e8f5e8
    style E fill:#e3f2fd
    style F fill:#ffcdd2
    style G fill:#c8e6c9
    style H fill:#bbdefb
```

---

# 💡 Key Insights from the Diagram

## **Retry Flow Characteristics:**

- ✅ **Automatic retry handling** by Temporal Server
- ✅ **Exponential backoff** prevents service overload
- ✅ **Configurable max attempts** prevents infinite retries
- ✅ **Success on final attempt** shows resilience in action

## **Strategy Differentiation:**

- 🔴 **Critical activities** (payments) → **Conservative, fewer attempts**
- 🟢 **Standard activities** (validation) → **Balanced approach**
- 🔵 **Long-running activities** (file processing) → **More attempts, heartbeats**

---

# Timeout and Heartbeat Strategy

## **Timeout Hierarchy:**
- **ScheduleToStartTimeout** → Maximum queue wait time
- **StartToCloseTimeout** → Single execution time limit
- **ScheduleToCloseTimeout** → Total time including all retries
- **HeartbeatTimeout** → Progress reporting frequency

## **Benefits:**
- **Prevents stuck workflows** through proper timeout configuration
- **Enables progress monitoring** through heartbeat reporting
- **Balances resilience** with resource efficiency

---

# 🚀 Production Resilience

**This diagram demonstrates production-ready patterns for:**

- ✅ **Service failure recovery** through smart retry policies
- ✅ **Resource protection** via timeout boundaries
- ✅ **System stability** through exponential backoff
- ✅ **Operational visibility** via heartbeat monitoring

**Building fault-tolerant distributed systems! 🎉** 