# 📜 Diagram for Lesson 11: Queries

This diagram visualizes query patterns for inspecting workflow state and how queries differ from signals and activities.

```mermaid
sequenceDiagram
    participant C as Client
    participant TS as Temporal Server
    participant W as OrderTrackingWorkflow
    participant A as Activity
    participant ES as External System
    
    Note over C,ES: Workflow State Inspection via Queries
    
    C->>TS: Start OrderTrackingWorkflow
    TS->>W: Initialize workflow
    W->>W: Set initial state
    W->>A: Execute validation activity
    A->>W: Validation complete
    W->>W: Update order status = VALIDATED
    
    Note over C,ES: Real-time State Queries
    
    C->>TS: Query: getCurrentStatus()
    TS->>W: Execute query method
    W->>TS: Return current status
    TS->>C: Status: VALIDATED
    
    W->>A: Execute inventory activity
    A->>ES: Check inventory
    ES->>A: Inventory available
    A->>W: Inventory reserved
    W->>W: Update status = INVENTORY_RESERVED
    
    C->>TS: Query: getOrderDetails()
    TS->>W: Execute query method
    W->>TS: Return detailed order info
    TS->>C: Order details with items
    
    C->>TS: Query: getProcessingHistory()
    TS->>W: Execute query method
    W->>TS: Return processing steps
    TS->>C: Step-by-step history
    
    rect rgb(245, 255, 245)
        Note over C,ES: Queries - Read-only, No side effects
    end
    
    rect rgb(255, 245, 245)
        Note over C,ES: Signals - Write operations, Change state
    end
    
    rect rgb(245, 245, 255)
        Note over C,ES: Activities - External operations, Side effects
    end
```

> 💡 This sequence diagram demonstrates how queries provide real-time, read-only access to workflow state without affecting execution. Unlike signals (which change state) or activities (which have side effects), queries are purely for inspection and monitoring. 