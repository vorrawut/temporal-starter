# 📜 Diagram for Lesson 17: Deployment & Production Infrastructure

This diagram visualizes the complete deployment architecture from local development to production Kubernetes deployment.

```mermaid
flowchart TD
    subgraph "Local Development - Docker Compose"
        A[PostgreSQL Container] --> B[Temporal Server Container]
        B --> C[Temporal Web UI Container]
        B --> D[Temporal Worker App Container]
        D --> E[Redis Container - Optional]
        
        subgraph "Docker Network"
            A
            B
            C 
            D
            E
        end
    end
    
    subgraph "Production Kubernetes Deployment"
        subgraph "temporal-system Namespace"
            F[PostgreSQL StatefulSet] --> G[Temporal Service]
            G --> H[Temporal Web Deployment]
            G --> I[Worker App Deployment - 3 Replicas]
            
            subgraph "Configuration Management"
                J[ConfigMap - App Config]
                K[Secret - Credentials]
                L[Service Account - RBAC]
            end
            
            subgraph "Networking"
                M[ClusterIP Services]
                N[Ingress Controller]
                O[Network Policies]
            end
            
            subgraph "Monitoring"
                P[Health Checks]
                Q[Prometheus Metrics]
                R[Logging]
            end
        end
    end
    
    subgraph "Container Build Pipeline"
        S[Source Code] --> T[Multi-Stage Dockerfile]
        T --> U[Build Stage - Gradle Build]
        U --> V[Runtime Stage - Optimized JRE]
        V --> W[Security Hardened Container]
        W --> X[Container Registry]
    end
    
    subgraph "Deployment Strategies"
        Y[Docker Compose - Local Dev]
        Z[kubectl - Direct K8s]
        AA[Helm Charts - Package Management]
        BB[CI/CD Pipelines - Automation]
    end
    
    subgraph "Scaling & Management"
        CC[Horizontal Pod Autoscaler]
        DD[Resource Limits & Requests]
        EE[Rolling Updates]
        FF[Health Checks & Probes]
    end
    
    X --> I
    J --> I
    K --> I
    L --> I
    
    I --> CC
    I --> DD
    I --> EE
    I --> FF
    
    Y --> A
    Z --> F
    AA --> F
    BB --> F
    
    style A fill:#e3f2fd
    style B fill:#e8f5e8
    style F fill:#e3f2fd
    style G fill:#e8f5e8
    style I fill:#f3e5f5
    style W fill:#fff3e0
    style CC fill:#fce4ec
```

> 💡 This architecture diagram shows the complete deployment journey from containerized local development with Docker Compose to production-ready Kubernetes deployment with proper scaling, monitoring, and security configurations. 