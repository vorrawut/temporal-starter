# 📜 Diagram for Lesson 8: Activity Retry + Timeout

This diagram visualizes the retry patterns and timeout strategies for resilient activity execution.

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
    
    Note over W,ES: Different Retry Strategies by Activity Type
    
    rect rgb(255, 245, 245)
        Note over W,ES: Critical Activity (Payment)<br/>MaxAttempts: 5, LongTimeout
    end
    
    rect rgb(245, 255, 245)  
        Note over W,ES: Standard Activity (Validation)<br/>MaxAttempts: 3, MediumTimeout
    end
    
    rect rgb(245, 245, 255)
        Note over W,ES: Long-Running Activity (File Processing)<br/>MaxAttempts: 10, Heartbeat
    end
```

> 💡 This sequence diagram shows how Temporal automatically retries failed activities with exponential backoff, and demonstrates different retry strategies for different types of activities based on their criticality and expected duration. 