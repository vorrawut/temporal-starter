# 📜 Diagram for Lesson 15: External Service Integration

This diagram visualizes the architecture for integrating Temporal workflows with multiple external services using proper encapsulation and error handling patterns.

```mermaid
flowchart TD
    A[ExternalServiceWorkflow] --> B[User Registration Process]
    
    B --> C[Step 1: Profile Creation]
    C --> D[UserProfileService Activity]
    D --> E[External Profile API]
    
    B --> F[Step 2: Payment Processing] 
    F --> G[PaymentService Activity]
    G --> H[Payment Gateway API]
    
    B --> I[Step 3: Database Operations]
    I --> J[DatabaseService Activity] 
    J --> K[Internal Database]
    
    B --> L[Step 4: Notifications]
    L --> M[NotificationService Activity]
    M --> N[Email Service API]
    M --> O[SMS Service API]
    
    subgraph "Service Integration Patterns"
        subgraph "Activity Configuration"
            P[External APIs: 5min timeout, 5 retries]
            Q[Internal Services: 2min timeout, 3 retries]
            R[Critical Operations: Extended timeouts]
        end
        
        subgraph "Error Handling"
            S[Graceful Degradation]
            T[Error Aggregation]
            U[Partial Success Reporting]
            V[Service-Specific Retry Logic]
        end
        
        subgraph "External Services"
            E
            H
            N
            O
        end
        
        subgraph "Internal Services"
            K
        end
    end
    
    E --> W{Profile Created?}
    H --> X{Payment Processed?}
    K --> Y{Data Saved?}
    N --> Z{Email Sent?}
    O --> AA{SMS Sent?}
    
    W --> BB[Collect Results]
    X --> BB
    Y --> BB
    Z --> BB
    AA --> BB
    
    BB --> CC[Return IntegrationResult]
    CC --> DD[Success: All services completed]
    CC --> EE[Partial: Some services failed]
    CC --> FF[Failure: Critical services failed]
    
    style A fill:#e1f5fe
    style DD fill:#e8f5e8
    style EE fill:#fff3e0
    style FF fill:#ffebee
    style E fill:#f3e5f5
    style H fill:#f3e5f5
    style N fill:#f3e5f5
    style O fill:#f3e5f5
```

> 💡 This diagram demonstrates how to integrate workflows with multiple external services using activity encapsulation, different timeout strategies for different service types, and comprehensive error handling that allows for partial success scenarios. 