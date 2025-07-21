# 📊 Temporal Loan Application System - Architecture Diagrams

## 🏗️ System Architecture Overview

```mermaid
graph TB
    subgraph "Client Layer"
        A[REST API Client] 
        B[Temporal Web UI]
    end
    
    subgraph "Application Layer"
        C[Spring Boot App]
        D[Loan Controller]
        E[Status Controller]
        F[Signal Controller]
    end
    
    subgraph "Temporal Platform"
        G[Temporal Server]
        H[Worker Process]
        I[Task Queues]
        J[Workflow Engine]
    end
    
    subgraph "Business Logic"
        K[Loan Workflow]
        L[Follow-up Workflow]
        M[Document Activity]
        N[Risk Scoring Activity]
        O[Disbursement Activity]
        P[Notification Activity]
    end
    
    subgraph "External Services"
        Q[Credit Bureau API]
        R[Email Service]
        S[SMS Gateway]
        T[Bank API]
    end
    
    A --> D
    A --> E
    A --> F
    B --> G
    D --> K
    C --> H
    H --> I
    I --> J
    J --> K
    J --> L
    K --> M
    K --> N
    K --> O
    K --> P
    L --> P
    N --> Q
    P --> R
    P --> S
    O --> T
    
    style A fill:#e1f5fe
    style K fill:#f3e5f5
    style G fill:#e8f5e8
    style Q fill:#fff3e0
```

## 🔄 Loan Application Workflow Sequence

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant LoanWorkflow
    participant DocumentActivity
    participant RiskActivity
    participant DisbursementActivity
    participant NotificationActivity
    participant CreditBureau
    participant BankAPI
    
    Note over Client,BankAPI: Loan Application Process
    
    Client->>Controller: POST /api/loan/apply
    Controller->>LoanWorkflow: Start Workflow
    LoanWorkflow->>LoanWorkflow: 📝 Persist: Application Received
    
    Note over LoanWorkflow,CreditBureau: Step 1: Document Validation
    LoanWorkflow->>DocumentActivity: Validate Documents
    DocumentActivity->>DocumentActivity: Check required docs
    DocumentActivity->>LoanWorkflow: ✅ Documents Valid
    LoanWorkflow->>LoanWorkflow: 📝 Persist: Documents Validated
    
    Note over LoanWorkflow,CreditBureau: Step 2: Risk Scoring (with Retries)
    LoanWorkflow->>RiskActivity: Calculate Risk Score
    RiskActivity->>CreditBureau: Get Credit Score
    CreditBureau-->>RiskActivity: ❌ Service Timeout
    Note over RiskActivity: Auto-retry with backoff (2s)
    RiskActivity->>CreditBureau: Get Credit Score (Retry 1)
    CreditBureau-->>RiskActivity: ❌ Rate Limited
    Note over RiskActivity: Auto-retry with backoff (4s)
    RiskActivity->>CreditBureau: Get Credit Score (Retry 2)
    CreditBureau->>RiskActivity: ✅ Credit Score: 750
    RiskActivity->>LoanWorkflow: ✅ Risk Score: LOW
    LoanWorkflow->>LoanWorkflow: 📝 Persist: Risk Assessment Complete
    
    Note over LoanWorkflow,BankAPI: Step 3: Wait for Approval Signal
    LoanWorkflow->>LoanWorkflow: ⏳ Await Approval Signal
    Controller->>LoanWorkflow: 🔔 Approval Signal
    LoanWorkflow->>LoanWorkflow: 📝 Persist: Application Approved
    
    Note over LoanWorkflow,BankAPI: Step 4: Loan Disbursement
    LoanWorkflow->>DisbursementActivity: Disburse Loan
    DisbursementActivity->>BankAPI: Transfer Funds
    BankAPI->>DisbursementActivity: ✅ Transfer Complete
    DisbursementActivity->>LoanWorkflow: ✅ Loan Disbursed
    LoanWorkflow->>LoanWorkflow: 📝 Persist: Loan Disbursed
    
    Note over LoanWorkflow,BankAPI: Step 5: Notifications & Follow-up
    LoanWorkflow->>NotificationActivity: Send Confirmation
    NotificationActivity->>NotificationActivity: Send Email & SMS
    NotificationActivity->>LoanWorkflow: ✅ Notifications Sent
    LoanWorkflow->>LoanWorkflow: Start Follow-up Workflow
    LoanWorkflow->>Controller: ✅ Application Complete
    Controller->>Client: ✅ Success Response
    
    rect rgb(255, 245, 245)
        Note over Client,BankAPI: 🛡️ Temporal Guarantees:<br/>• State persisted at each step<br/>• Automatic retries with exponential backoff<br/>• Workflow survives crashes and restarts<br/>• Exactly-once execution semantics<br/>• Complete audit trail and observability
    end
```

## 🔀 Signal and Query Interactions

```mermaid
stateDiagram-v2
    [*] --> Submitted: Application Received
    
    Submitted --> DocumentValidation: Start Processing
    DocumentValidation --> RiskScoring: Documents Valid
    DocumentValidation --> Rejected: Invalid Documents
    
    RiskScoring --> AwaitingApproval: Risk Assessment Complete
    RiskScoring --> Rejected: High Risk Score
    
    AwaitingApproval --> Approved: Approval Signal ✅
    AwaitingApproval --> Rejected: Rejection Signal ❌
    AwaitingApproval --> Expired: Timeout (7 days)
    
    Approved --> Disbursing: Begin Disbursement
    Disbursing --> Disbursed: Funds Transferred
    Disbursing --> Failed: Disbursement Error
    
    Failed --> Compensating: Start Rollback
    Compensating --> Rejected: Compensation Complete
    
    Disbursed --> FollowUpScheduled: Schedule Reminders
    FollowUpScheduled --> [*]: Process Complete
    
    Rejected --> [*]: Process Complete
    Expired --> [*]: Process Complete
    
    note right of AwaitingApproval
        Queries Available:
        • getCurrentState()
        • getApplicationDetails()
        • getRiskScore()
        • getProcessingHistory()
    end note
    
    note right of Approved
        Signals Available:
        • approveApplication()
        • rejectApplication()
        • requestMoreInfo()
    end note
```

## 🏗️ Class Diagram - Domain Model

```mermaid
classDiagram
    class LoanApplication {
        +String workflowId
        +String userId
        +String firstName
        +String lastName
        +String email
        +String phone
        +BigDecimal loanAmount
        +LoanPurpose purpose
        +BigDecimal annualIncome
        +List~DocumentType~ documents
        +ApplicationStatus status
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }
    
    class RiskAssessment {
        +String applicationId
        +Integer creditScore
        +RiskLevel riskLevel
        +BigDecimal debtToIncomeRatio
        +List~String~ riskFactors
        +LocalDateTime assessedAt
    }
    
    class ApprovalDecision {
        +String applicationId
        +String approvedBy
        +ApprovalStatus status
        +String notes
        +LocalDateTime decidedAt
    }
    
    class LoanDisbursement {
        +String applicationId
        +String transactionId
        +BigDecimal amount
        +String bankAccount
        +DisbursementStatus status
        +LocalDateTime disbursedAt
    }
    
    class FollowUpTask {
        +String applicationId
        +FollowUpType type
        +LocalDateTime scheduledAt
        +Boolean completed
        +String notes
    }
    
    LoanApplication ||--|| RiskAssessment
    LoanApplication ||--|| ApprovalDecision
    LoanApplication ||--|| LoanDisbursement
    LoanApplication ||--o{ FollowUpTask
    
    class LoanApplicationWorkflow {
        +processApplication(LoanApplication)
        +handleApprovalSignal(ApprovalDecision)
        +handleRejectionSignal(RejectionDecision)
        +getCurrentState()
        +getApplicationDetails()
    }
    
    class DocumentValidationActivity {
        +validateDocuments(LoanApplication)
    }
    
    class RiskScoringActivity {
        +calculateRiskScore(LoanApplication)
    }
    
    class LoanDisbursementActivity {
        +disburseLoan(LoanApplication)
        +compensateFailedDisbursement(String)
    }
    
    class NotificationActivity {
        +sendApplicationConfirmation(LoanApplication)
        +sendApprovalNotification(LoanApplication)
        +sendRejectionNotification(LoanApplication)
    }
    
    LoanApplicationWorkflow --> DocumentValidationActivity
    LoanApplicationWorkflow --> RiskScoringActivity
    LoanApplicationWorkflow --> LoanDisbursementActivity
    LoanApplicationWorkflow --> NotificationActivity
```

## 🔄 Child Workflow - Follow-up Process

```mermaid
flowchart TD
    A[Loan Disbursed] --> B[Schedule Follow-up Workflow]
    B --> C[Wait 30 Days]
    C --> D[Send First Check-in Email]
    D --> E[Wait 60 Days]
    E --> F[Send Payment Reminder]
    F --> G[Wait 90 Days]
    G --> H[Send Account Review]
    H --> I{Customer Response?}
    I -->|Yes| J[Update Customer Records]
    I -->|No| K[Escalate to Customer Service]
    J --> L[Continue Monitoring]
    K --> M[Manual Outreach]
    L --> N[Schedule Next Follow-up]
    M --> N
    N --> O[Continue as New Workflow]
    
    style A fill:#e8f5e8
    style B fill:#e1f5fe
    style I fill:#fff3e0
    style O fill:#f3e5f5
```

## 📊 Deployment Architecture

```mermaid
graph TB
    subgraph "Load Balancer"
        LB[NGINX/ALB]
    end
    
    subgraph "Application Tier"
        APP1[Spring Boot Instance 1]
        APP2[Spring Boot Instance 2]
        APP3[Spring Boot Instance 3]
    end
    
    subgraph "Temporal Cluster"
        TS1[Temporal Server 1]
        TS2[Temporal Server 2]
        TS3[Temporal Server 3]
    end
    
    subgraph "Worker Tier"
        W1[Worker Pod 1]
        W2[Worker Pod 2]
        W3[Worker Pod 3]
        W4[Worker Pod 4]
    end
    
    subgraph "Data Layer"
        DB[(PostgreSQL)]
        REDIS[(Redis Cache)]
        ES[(Elasticsearch)]
    end
    
    subgraph "External Services"
        CB[Credit Bureau]
        EMAIL[Email Service]
        BANK[Bank API]
    end
    
    LB --> APP1
    LB --> APP2
    LB --> APP3
    
    APP1 --> TS1
    APP2 --> TS2
    APP3 --> TS3
    
    TS1 --> DB
    TS2 --> DB
    TS3 --> DB
    
    W1 --> TS1
    W2 --> TS2
    W3 --> TS3
    W4 --> TS1
    
    W1 --> CB
    W2 --> EMAIL
    W3 --> BANK
    W4 --> REDIS
    
    TS1 --> ES
    TS2 --> ES
    TS3 --> ES
    
    style LB fill:#e1f5fe
    style DB fill:#e8f5e8
    style TS1 fill:#f3e5f5
    style TS2 fill:#f3e5f5
    style TS3 fill:#f3e5f5
```

---

> 💡 **Key Insight**: These diagrams show how Temporal orchestrates complex business processes across multiple services while maintaining reliability, observability, and scalability. The loan application system demonstrates enterprise-grade workflow patterns that can be applied to any domain. 