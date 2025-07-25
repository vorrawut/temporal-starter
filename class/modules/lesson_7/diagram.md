---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# 📜 Diagram for Lesson 7: Workflow Input/Output

## Visualizing Complex Data Flow Patterns

*Complex data flow patterns for workflow input/output handling with rich data structures and validation*

---

# Order Processing Data Flow

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
    
    style A fill:#e1f5fe
    style B fill:#f3e5f5
    style K fill:#e8f5e8
```

---

# Workflow Processing Pipeline

```mermaid
flowchart TD
    subgraph "Workflow Processing Steps"
        K[OrderProcessingWorkflow] --> L[Step 1: Input Validation]
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
    
    style L fill:#fff3e0
    style M fill:#e8f5e8
    style N fill:#f3e5f5
    style U fill:#e3f2fd
```

---

# Rich Output Data Model

```mermaid
flowchart TD
    subgraph "Rich Output Data Model"
        U[OrderResult] --> V[Order Metadata]
        U --> W[Processing Steps History]
        U --> X[Tracking Information]
        U --> Y[Final Status]
        
        V --> Z[Order ID, Customer, Timestamps]
        W --> AA[Step Status, Duration, Errors]
        X --> BB[Tracking Number, Carrier, ETA]
        Y --> CC[SUCCESS, PARTIAL, FAILED]
    end
    
    U --> LL[Client Response]
    
    style V fill:#e8f5e8
    style W fill:#fff3e0
    style X fill:#f3e5f5
    style Y fill:#ffebee
    style LL fill:#e1f5fe
```

---

# Data Design Patterns

```mermaid
graph TB
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
    
    DD --> HH
    EE --> II
    FF --> JJ
    GG --> KK
    
    style DD fill:#fff3e0
    style HH fill:#f3e5f5
    style EE fill:#e8f5e8
    style II fill:#e3f2fd
```

---

# 💡 Key Data Flow Insights

## **Input Design Patterns:**

- ✅ **Rich data structures** with clear relationships
- ✅ **Nested objects** for complex business domains
- ✅ **Type safety** through data classes and enums
- ✅ **Validation boundaries** at workflow entry points

## **Processing Patterns:**

- ✅ **Step-by-step validation** with clear error reporting
- ✅ **Business logic separation** between validation and processing
- ✅ **Tracking and audit** of all processing steps

---

# Output Design Best Practices

## **Comprehensive Results:**

- ✅ **Status information** for success/failure determination
- ✅ **Processing history** for audit and debugging
- ✅ **Rich metadata** for downstream system integration
- ✅ **Error context** for meaningful failure responses

## **Serialization Requirements:**

- ✅ **JSON compatible** for API integration
- ✅ **Version safe** for schema evolution
- ✅ **Size optimized** for performance
- ✅ **Type safe** for compile-time validation

---

# 🚀 Production Benefits

**This data flow pattern provides:**

- ✅ **Type safety** throughout the entire workflow lifecycle
- ✅ **Rich observability** through comprehensive result tracking
- ✅ **Easy debugging** with detailed processing history
- ✅ **API compatibility** through structured input/output
- ✅ **Schema evolution** support for changing requirements

**Building robust, maintainable data flows! 🎉** 