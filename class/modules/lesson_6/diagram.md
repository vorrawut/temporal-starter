# 📜 Diagram for Lesson 6: Workflow & Activity Separation

This diagram visualizes the clean architecture pattern with separated workflow orchestration and activity implementations in the user onboarding process.

```mermaid
flowchart TD
    A[Start UserOnboardingWorkflow] --> B[Call UserValidationActivity]
    B --> C{User validation successful?}
    C -- Yes --> D[Call AccountCreationActivity]
    C -- No --> E[Return validation failure]
    D --> F{Account creation successful?}
    F -- Yes --> G[Call NotificationActivity - Welcome Email]
    F -- No --> H[Return account creation failure]
    G --> I{Email sent successfully?}
    I -- Yes --> J[Return success result]
    I -- No --> K[Log email failure but continue]
    K --> J
    
    subgraph "Clean Architecture"
        subgraph "Workflow Layer - Orchestration"
            A
            C
            F
            I
            J
        end
        
        subgraph "Activity Layer - Business Logic"
            subgraph "UserValidationActivity"
                L[validateEmail]
                M[validatePhoneNumber]
                N[checkDuplicateUser]
            end
            
            subgraph "AccountCreationActivity" 
                O[createUserRecord]
                P[generateUserId]
                Q[setInitialPreferences]
            end
            
            subgraph "NotificationActivity"
                R[sendWelcomeEmail]
                S[sendSMSConfirmation]
            end
        end
    end
    
    B --> L
    D --> O
    G --> R
    
    style A fill:#e1f5fe
    style J fill:#e8f5e8
    style E fill:#ffebee
    style H fill:#ffebee
```

> 💡 This diagram demonstrates the clean separation of concerns: workflows handle orchestration logic while activities contain the actual business logic implementations. Each activity has a focused responsibility and can be tested independently. 