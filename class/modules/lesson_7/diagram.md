# 📜 Diagram for Lesson 7: Workflow Input/Output

This diagram visualizes complex data flow patterns for workflow input/output handling with rich data structures and validation.

```mermaid
flowchart TD
    A[Client Request] --> B[OrderRequest Data Structure]
    
    subgraph "Rich Input Data Model"
        B --> C[Order ID & Customer ID]
        B --> D[List of OrderItems]
        B --> E[Shipping Address]
        B --> F[Payment Method]
        B --> G[Priority & Metadata]
        
        D --> H[Product ID, Quantity, Price]
        E --> I[Street, City, State, ZIP]
        F --> J[Type, Card Info, Account]
    end
    
    B --> K[OrderProcessingWorkflow.processOrder]
    
    subgraph "Workflow Processing Steps"
        K --> L[Step 1: Input Validation]
        L --> M[Step 2: Order Processing]
        M --> N[Step 3: Result Aggregation]
        
        L --> O[Validate Order Items]
        L --> P[Validate Address Format]
        L --> Q[Validate Payment Method]
        
        M --> R[Calculate Totals]
        M --> S[Process Each Item]
        M --> T[Generate Tracking Info]
    end
    
    N --> U[OrderResult Data Structure]
    
    subgraph "Rich Output Data Model"
        U --> V[Order Metadata]
        U --> W[Processing Steps History]
        U --> X[Tracking Information]
        U --> Y[Final Status]
        
        V --> Z[Order ID, Customer, Timestamps]
        W --> AA[Step Status, Duration, Errors]
        X --> BB[Tracking Number, Carrier, ETA]
        Y --> CC[SUCCESS, PARTIAL, FAILED]
    end
    
    subgraph "Data Validation Patterns"
        DD[Input Validation]
        EE[Business Rule Validation]
        FF[Output Enrichment]
        GG[Error Aggregation]
    end
    
    subgraph "Serialization Considerations"
        HH[JSON Serializable]
        II[Version Compatible]
        JJ[Size Optimized]
        KK[Type Safe]
    end
    
    U --> LL[Client Response]
    
    style A fill:#e1f5fe
    style B fill:#f3e5f5
    style K fill:#e8f5e8
    style U fill:#f3e5f5
    style LL fill:#e1f5fe
    style DD fill:#fff3e0
    style HH fill:#f3e5f5
```

> 💡 This diagram demonstrates how to design rich data structures for workflow input and output, including comprehensive validation, processing steps tracking, and structured result reporting that maintains type safety and serialization compatibility. 