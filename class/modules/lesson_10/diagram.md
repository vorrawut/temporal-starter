# 📜 Diagram for Lesson 10: Signals

This diagram visualizes signal handling patterns and how external systems can interact with long-running workflows through signals and queries.

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
    
    ES->>TC: Send approval signal
    TC->>TS: Signal: approve(decision, approver)
    TS->>W: Deliver signal to workflow
    W->>W: Set approvalReceived = true
    W->>W: Set status = APPROVED
    W->>W: Workflow.await() condition met
    
    W->>A: Execute post-approval activity
    A->>W: Activity completed
    W->>W: Complete workflow
    
    rect rgb(245, 255, 245)
        Note over ES,A: Signal Methods - External Events
    end
    
    rect rgb(255, 245, 245)
        Note over ES,A: Query Methods - State Inspection
    end
    
    rect rgb(245, 245, 255)
        Note over ES,A: Workflow.await() - Event-Driven Logic
    end
```

> 💡 This sequence diagram shows how signals enable external systems to interact with long-running workflows. Signals are asynchronous events that can change workflow state, while queries provide real-time inspection of workflow status without affecting execution. 