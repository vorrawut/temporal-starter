---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# 📜 Diagram for Lesson 10: Signals

## Visualizing Interactive Workflow Patterns

*Signal handling patterns and how external systems can interact with long-running workflows through signals and queries*

---

# Signal-Based Workflow Interaction

```mermaid
sequenceDiagram
    participant ES as External System
    participant TC as Temporal Client
    participant TS as Temporal Server
    participant W as ApprovalWorkflow
    participant A as Activity
    
    Note over ES,A: Signal-Based Workflow Interaction
    
    TC->>TS: Start ApprovalWorkflow
    TS->>W: Initialize Workflow
    W->>W: Set status = PENDING
    W->>W: Workflow.await(approvalReceived || rejected)
    
    Note over W: Workflow waits for signal...
    
    ES->>TC: Query workflow status
    TC->>TS: Query getCurrentStatus()
    TS->>W: Execute query method
    W->>TS: Return "PENDING"
    TS->>TC: Status response
    TC->>ES: Status: PENDING
```

---

# Signal Delivery and Processing

```mermaid
sequenceDiagram
    participant ES as External System
    participant TC as Temporal Client
    participant TS as Temporal Server
    participant W as ApprovalWorkflow
    participant A as Activity
    
    ES->>TC: Send approval signal
    TC->>TS: Signal: approve(decision, approver)
    TS->>W: Deliver signal to workflow
    W->>W: Set approvalReceived = true
    W->>W: Set status = APPROVED
    W->>W: Workflow.await() condition met
    
    W->>A: Execute post-approval activity
    A->>W: Activity completed
    W->>W: Complete workflow
```

---

# Interaction Pattern Categories

```mermaid
graph TB
    subgraph "Signal Methods - External Events"
        A[Signal Delivery]
        B[State Modification]
        C[Workflow Continuation]
        A --> B --> C
    end
    
    subgraph "Query Methods - State Inspection"
        D[Query Request]
        E[Current State Read]
        F[Immediate Response]
        D --> E --> F
    end
    
    subgraph "Workflow.await() - Event-Driven Logic"
        G[Condition Check]
        H[Block Until Met]
        I[Continue Execution]
        G --> H --> I
    end
    
    style A fill:#e8f5e8
    style D fill:#ffebee
    style G fill:#e3f2fd
```

---

# 💡 Key Insights from Signal Flow

## **Signal Characteristics:**

- ✅ **Asynchronous delivery** - External systems don't wait
- ✅ **Persistent in history** - Signals are replayed during workflow recovery
- ✅ **State modification** - Can trigger workflow logic changes
- ✅ **Event-driven patterns** - Enable reactive workflow behavior

## **Query Characteristics:**

- ✅ **Synchronous response** - Immediate state visibility
- ✅ **No side effects** - Read-only operations
- ✅ **Not persisted** - Don't affect workflow history
- ✅ **Real-time monitoring** - Current state inspection

---

# Workflow.await() Pattern

## **Event-Driven Blocking:**

```mermaid
flowchart TD
    A[Workflow.await() Called] --> B{Condition Met?}
    B --Yes--> C[Continue Execution]
    B --No--> D[Block and Wait]
    D --> E[Signal Received]
    E --> F[Update State]
    F --> B
    
    style A fill:#e3f2fd
    style C fill:#e8f5e8
    style D fill:#fff3e0
    style E fill:#f3e5f5
```

**Enables workflows to wait for external events without consuming resources**

---

# 🚀 Production Benefits

**This signal pattern provides:**

- ✅ **Interactive workflows** that respond to human decisions
- ✅ **Real-time observability** through query methods
- ✅ **Event-driven architecture** for reactive systems
- ✅ **Long-running processes** that wait for external events
- ✅ **Efficient resource usage** through conditional blocking

**Building responsive, interactive distributed systems! 🎉** 