# 📜 Diagram for Lesson 16: Testing + Production Readiness

This diagram visualizes the comprehensive testing architecture and production deployment patterns for Temporal workflows.

```mermaid
flowchart TD
    subgraph "Testing Strategy"
        A[TestableWorkflow] --> B[Unit Testing Layer]
        A --> C[Integration Testing Layer]
        A --> D[Production Deployment Layer]
        
        subgraph "Unit Testing with TestWorkflowRule"
            B --> E[MockOrderValidationActivity]
            B --> F[MockInventoryActivity] 
            B --> G[MockPaymentActivity]
            B --> H[MockShippingActivity]
            E --> I[Configurable Test Scenarios]
            F --> I
            G --> I 
            H --> I
        end
        
        subgraph "Integration Testing"
            C --> J[Real Temporal Server]
            C --> K[Mock External Services]
            C --> L[End-to-End Workflows]
            J --> M[Performance Testing]
            K --> M
            L --> M
        end
        
        subgraph "Production Deployment"
            D --> N[Production Workers]
            D --> O[Real Activity Implementations] 
            D --> P[Monitoring & Observability]
            N --> Q[Horizontal Scaling]
            O --> Q
            P --> Q
        end
    end
    
    subgraph "Testing Patterns"
        R[Happy Path Testing] --> S[All operations succeed]
        T[Failure Scenario Testing] --> U[Partial failures with compensation]
        V[Load Testing] --> W[Concurrent workflow execution]
        X[Error Recovery Testing] --> Y[Retry and timeout behavior]
    end
    
    subgraph "Production Configuration"
        Z[WorkerOptions Configuration]
        AA[Activity Timeout Strategies]
        BB[Resource Limits & Requests]
        CC[Health Checks & Probes]
        DD[Metrics & Monitoring]
    end
    
    I --> R
    I --> T
    M --> V
    M --> X
    
    Q --> Z
    Q --> AA
    Q --> BB
    Q --> CC
    Q --> DD
    
    style A fill:#e1f5fe
    style B fill:#e8f5e8
    style C fill:#fff3e0
    style D fill:#f3e5f5
    style I fill:#e8f5e8
    style M fill:#fff3e0
    style Q fill:#f3e5f5
```

> 💡 This diagram shows the comprehensive testing strategy from unit tests with mocks through integration testing to production deployment. Each layer has specific patterns and configurations optimized for its purpose, ensuring reliable workflow execution at scale. 