---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# 📜 Diagram for Lesson 6: Workflow & Activity Separation

## Visualizing Clean Architecture Patterns

*Clean architecture pattern with separated workflow orchestration and activity implementations in the user onboarding process*

---

# User Onboarding Workflow Architecture

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
    
    style A fill:#e1f5fe
    style J fill:#e8f5e8
    style E fill:#ffebee
    style H fill:#ffebee
```

---

# Clean Architecture Layers

```mermaid
flowchart TD
    subgraph "Clean Architecture"
        subgraph "Workflow Layer - Orchestration"
            A[Start UserOnboardingWorkflow]
            C{User validation successful?}
            F{Account creation successful?}
            I{Email sent successfully?}
            J[Return success result]
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
    
    A --> L
    A --> O
    A --> R
```

---

# 💡 Key Architecture Insights

## **Clean Separation of Concerns:**

- ✅ **Workflows handle orchestration logic** - decision points and flow control
- ✅ **Activities contain actual business logic** - focused, testable implementations
- ✅ **Each activity has focused responsibility** - single purpose, easy to maintain
- ✅ **Activities can be tested independently** - unit testing friendly
- ✅ **Clear boundaries** between coordination and execution

---

# Error Handling Strategy

## **Critical vs Non-Critical Failures:**

- 🔴 **Critical**: Validation and Account Creation → **Stop Process**
- 🟡 **Non-Critical**: Email Notification → **Log and Continue**

## **Benefits:**
- **Graceful degradation** for non-essential features
- **Clear failure boundaries** with specific recovery actions
- **User experience** isn't blocked by secondary failures
- **System resilience** through intelligent error handling

---

# 🚀 Production Benefits

**This architecture pattern provides:**

- ✅ **Maintainability** - Clear separation makes changes easier
- ✅ **Testability** - Each component can be tested in isolation
- ✅ **Scalability** - Activities can scale independently
- ✅ **Reliability** - Failure isolation prevents cascading issues
- ✅ **Observability** - Clear flow visibility for debugging

**Perfect foundation for enterprise workflows! 🎉** 