# 📜 Diagram for Lesson 12: Child Workflows & continueAsNew

This diagram visualizes the hierarchical workflow orchestration with child workflows and the continueAsNew pattern for managing workflow history.

```mermaid
flowchart TD
    A[OrderProcessingWorkflow - Parent] --> B[Start Child Workflows]
    
    subgraph "Parallel Child Workflow Execution"
        B --> C[PaymentWorkflow - Child 1]
        B --> D[InventoryWorkflow - Child 2] 
        B --> E[ShippingWorkflow - Child 3]
        
        C --> F[Validate Payment Method]
        C --> G[Process Payment]
        C --> H[Payment Complete]
        
        D --> I[Check Inventory]
        D --> J[Reserve Items]
        D --> K[Inventory Reserved]
        
        E --> L[Calculate Shipping]
        E --> M[Schedule Pickup]
        E --> N[Shipping Scheduled]
    end
    
    H --> O[Collect Child Results]
    K --> O
    N --> O
    
    O --> P{All Children Successful?}
    P -- Yes --> Q[Complete Order]
    P -- No --> R[Handle Partial Failure]
    
    subgraph "continueAsNew Pattern"
        S[LongRunningWorkflow] --> T[Process Batch 1]
        T --> U[Process Batch 2]
        U --> V[Process Batch N]
        V --> W{History Too Large?}
        W -- Yes --> X[continueAsNew with current state]
        W -- No --> Y[Continue Processing]
        X --> Z[New Workflow Run with Reset History]
        Z --> S
    end
    
    subgraph "Child Workflow Benefits"
        AA[Independent Scaling]
        BB[Isolated Failure Domains]
        CC[Reusable Components]
        DD[Parallel Execution]
    end
    
    style A fill:#e1f5fe
    style C fill:#f3e5f5
    style D fill:#f3e5f5
    style E fill:#f3e5f5
    style Q fill:#e8f5e8
    style X fill:#fff3e0
    style Z fill:#fff3e0
```

> 💡 This diagram shows how parent workflows can orchestrate multiple child workflows in parallel, each handling a specific domain of business logic. The continueAsNew pattern prevents workflow history from growing too large by starting a new workflow run while preserving state. 