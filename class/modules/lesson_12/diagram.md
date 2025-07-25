---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# 📜 Diagram for Lesson 12

## Child Workflows & continueAsNew

*Visualizing hierarchical workflow orchestration with child workflows and the continueAsNew pattern for managing workflow history*

---

# Hierarchical Workflow Orchestration

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
    
    style A fill:#e1f5fe
    style C fill:#f3e5f5
    style D fill:#f3e5f5
    style E fill:#f3e5f5
    style Q fill:#e8f5e8
```

---

# continueAsNew Pattern

```mermaid
flowchart TD
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
    
    style X fill:#fff3e0
    style Z fill:#fff3e0
```

**continueAsNew prevents workflow history from growing too large by starting a new workflow run while preserving state**

---

# Child Workflow Benefits

```mermaid
graph TB
    subgraph "Child Workflow Benefits"
        AA[Independent Scaling]
        BB[Isolated Failure Domains]
        CC[Reusable Components]
        DD[Parallel Execution]
    end
    
    style AA fill:#e8f5e8
    style BB fill:#e8f5e8
    style CC fill:#e8f5e8
    style DD fill:#e8f5e8
```

## **Key Advantages:**
- ✅ **Independent Scaling**: Each child can scale based on its workload
- ✅ **Isolated Failure Domains**: Child failures don't crash parent
- ✅ **Reusable Components**: Child workflows can be reused across parents
- ✅ **Parallel Execution**: Multiple children execute simultaneously

---

# Orchestration Patterns

## **Parent-Child Coordination:**

| Pattern | Use Case | Benefits |
|---------|----------|----------|
| **Sequential** | Dependent operations | Clear ordering, simple error handling |
| **Parallel** | Independent operations | Faster execution, better resource usage |
| **Dynamic** | Variable complexity | Flexible, adaptive to requirements |

## **History Management:**
- **continueAsNew** for long-running processes
- **State preservation** across workflow runs
- **Memory optimization** through history reset

---

# 💡 Key Orchestration Insights

## **What This Diagram Shows:**

- ✅ **Parent workflows** orchestrate multiple child workflows in parallel
- ✅ **Each child** handles a specific domain of business logic
- ✅ **continueAsNew pattern** prevents workflow history from growing too large
- ✅ **Hierarchical decomposition** enables complex business process management
- ✅ **Independent scaling** and failure isolation improve system resilience

---

# 🚀 Production Benefits

**This orchestration pattern provides:**

- ✅ **Scalable architecture** through child workflow composition
- ✅ **Fault tolerance** with isolated failure domains
- ✅ **Resource optimization** through independent scaling
- ✅ **Maintainable complexity** with clear separation of concerns
- ✅ **Long-running reliability** through continueAsNew

**Building robust, scalable distributed systems! 🎉** 