# 📜 Diagram for Lesson 5: Adding a Simple Activity

This diagram visualizes the core architecture and communication flow between a workflow and its activities in this lesson.

```mermaid
flowchart TD
    A[Client starts CalculatorWorkflow] --> B[Workflow receives input: a, b, operation]
    B --> C{Workflow validates input}
    C -- Valid --> D[Call MathActivity.calculate]
    C -- Invalid --> E[Return error result]
    D --> F[Activity performs calculation]
    F --> G[Activity returns result]
    G --> H[Workflow receives result]
    H --> I[Workflow returns final result]
    
    subgraph "Worker Process"
        subgraph "Workflow Execution"
            B
            C
            H
            I
        end
        
        subgraph "Activity Execution"
            D
            F
            G
        end
    end
    
    style A fill:#e1f5fe
    style D fill:#f3e5f5
    style F fill:#f3e5f5
    style I fill:#e8f5e8
```

> 💡 This diagram shows the fundamental pattern of Temporal workflows: the workflow orchestrates business logic by calling activities, which perform the actual work and return results back to the workflow. 