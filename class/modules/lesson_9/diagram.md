# 📜 Diagram for Lesson 9: Error Handling in Workflows

This diagram visualizes the error handling patterns including the Saga pattern with compensation logic and circuit breaker implementation.

```mermaid
flowchart TD
    A[Start ErrorHandlingWorkflow] --> B[Try: Validate Order]
    B --> C{Validation Success?}
    C -- Yes --> D[Try: Reserve Inventory]
    C -- No --> E[Throw OrderValidationException]
    
    D --> F{Inventory Reserved?}
    F -- Yes --> G[Try: Process Payment]
    F -- No --> H[Throw InventoryException]
    
    G --> I{Payment Success?}
    I -- Yes --> J[Try: Schedule Shipping]
    I -- No --> K[Compensation: Release Inventory]
    
    J --> L{Shipping Scheduled?}
    L -- Yes --> M[Complete Successfully]
    L -- No --> N[Compensation: Refund Payment]
    
    K --> O[Throw PaymentException]
    N --> P[Compensation: Release Inventory]
    P --> Q[Throw ShippingException]
    
    subgraph "Error Handling Patterns"
        subgraph "Saga Pattern - Compensation"
            R[Success Path: Forward Operations]
            S[Failure Path: Compensation Operations]
            R --> S
        end
        
        subgraph "Circuit Breaker Pattern"
            T[Track Consecutive Failures]
            U{Failure Count > Threshold?}
            U -- Yes --> V[Open Circuit - Fast Fail]
            U -- No --> W[Continue Normal Operation]
            T --> U
        end
        
        subgraph "Exception Hierarchy"
            X[BusinessException - Retriable]
            Y[CriticalException - Non-Retriable]
            Z[ApplicationFailure - Custom]
        end
    end
    
    style A fill:#e1f5fe
    style M fill:#e8f5e8
    style E fill:#ffebee
    style H fill:#ffebee
    style O fill:#ffebee
    style Q fill:#ffebee
    style K fill:#fff3e0
    style N fill:#fff3e0
    style P fill:#fff3e0
```

> 💡 This flowchart demonstrates the Saga pattern where each forward operation has a corresponding compensation operation, ensuring data consistency even when failures occur partway through a complex business process. The circuit breaker pattern prevents cascading failures by fast-failing when error thresholds are exceeded. 