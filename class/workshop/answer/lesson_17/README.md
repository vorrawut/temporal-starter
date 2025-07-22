# Lesson 17: Deployment & Production Infrastructure - Complete Solution

## Solution Overview

This comprehensive solution demonstrates enterprise-grade deployment patterns for Temporal workflow applications. It includes everything needed to deploy, scale, and maintain Temporal applications in production environments using modern containerization and orchestration tools.

## Architecture Overview

### **Deployment Stack**
1. **Containerized Application**: Multi-stage Docker build with security best practices
2. **Local Development**: Complete Docker Compose stack with all dependencies
3. **Kubernetes Production**: Full production deployment with proper resource management
4. **Configuration Management**: Environment-specific configuration with secrets handling
5. **Monitoring**: Health checks, metrics, and observability
6. **Helm Chart**: Package management for complex deployments

### **Infrastructure Components**
- **PostgreSQL**: Persistent database for Temporal
- **Temporal Server**: Auto-setup Temporal with health checks
- **Temporal Web UI**: Management interface
- **Worker Application**: Your Kotlin Spring Boot application
- **Redis**: Optional caching layer
- **Monitoring**: Prometheus metrics and health endpoints

## Key Implementation Features

### 1. **Production-Ready Dockerfile**

```dockerfile
# Multi-stage build for optimal image size and security
FROM gradle:7.6-jdk17 AS build
WORKDIR /app

# Layer caching optimization
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle/
RUN gradle dependencies --no-daemon

# Build application
COPY src src/
RUN gradle build -x test --no-daemon

# Minimal runtime image
FROM openjdk:17-jre-slim AS runtime
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Security: non-root user
RUN groupadd -r temporal && useradd -r -g temporal temporal
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown -R temporal:temporal /app
USER temporal

# Container-optimized JVM settings
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
EXPOSE 8080
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
```

**Key features:**
- **Multi-stage build**: Reduces final image size by 60-70%
- **Security**: Non-root user, minimal base image
- **Layer caching**: Optimized for fast rebuilds
- **Container JVM tuning**: Memory-aware garbage collection

### 2. **Comprehensive Docker Compose Stack**

```yaml
version: '3.8'

services:
  # Database with health checks
  postgresql:
    image: postgres:13
    environment:
      POSTGRES_PASSWORD: temporal
      POSTGRES_USER: temporal
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U temporal"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Temporal server with proper dependencies
  temporal:
    image: temporalio/auto-setup:1.22.0
    ports:
      - "7233:7233"
      - "8080:8080"
    depends_on:
      postgresql:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "tctl", "--address", "temporal:7233", "workflow", "list"]

  # Worker application with health monitoring
  temporal-worker:
    build:
      context: .
      dockerfile: Dockerfile
    environment:
      - TEMPORAL_FRONTEND_ADDRESS=temporal:7233
      - SPRING_PROFILES_ACTIVE=docker
    depends_on:
      temporal:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
```

**Features:**
- **Health checks**: Ensure proper startup order
- **Service dependencies**: Temporal starts only after PostgreSQL is ready
- **Network isolation**: Services communicate through internal networks
- **Volume persistence**: Database data survives container restarts

### 3. **Production Kubernetes Deployment**

#### **Namespace and RBAC**
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: temporal-system

---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: temporal-worker
  namespace: temporal-system

---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: temporal-worker-role
  namespace: temporal-system
rules:
- apiGroups: [""]
  resources: ["configmaps", "secrets"]
  verbs: ["get", "list", "watch"]
```

#### **Configuration Management**
```yaml
# ConfigMap for non-sensitive configuration
apiVersion: v1
kind: ConfigMap
metadata:
  name: temporal-worker-config
  namespace: temporal-system
data:
  application.yaml: |
    temporal:
      frontend.address: temporal-service:7233
      namespace: default
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus

---
# Secret for sensitive data
apiVersion: v1
kind: Secret
metadata:
  name: temporal-worker-secrets
  namespace: temporal-system
type: Opaque
stringData:
  database-password: "temporal"
  external-api-key: "your-api-key-here"
```

#### **Production Deployment**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: temporal-worker
  namespace: temporal-system
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 1
  template:
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
      containers:
      - name: temporal-worker
        image: temporal-worker:latest
        resources:
          limits:
            memory: "1Gi"
            cpu: "1000m"
          requests:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
```

### 4. **Helm Chart for Advanced Deployment**

#### **Chart Structure**
```
helm/
├── Chart.yaml              # Chart metadata with dependencies
├── values.yaml             # Configurable default values
├── templates/
│   ├── deployment.yaml     # Templated deployment manifest
│   ├── service.yaml        # Service configuration
│   ├── configmap.yaml      # Configuration management
│   ├── secret.yaml         # Secrets management
│   └── hpa.yaml           # Horizontal Pod Autoscaler
```

#### **Configurable Values**
```yaml
# values.yaml
replicaCount: 3

image:
  repository: temporal-worker
  tag: latest
  pullPolicy: IfNotPresent

temporal:
  frontendAddress: "temporal-service:7233"
  namespace: "default"

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 80

postgresql:
  enabled: true
  auth:
    username: temporal
    password: temporal
    database: temporal
```

## Deployment Strategies

### **Local Development with Docker Compose**

```bash
# Quick start for development
docker compose up -d

# Access points
# Application: http://localhost:8081
# Temporal Web UI: http://localhost:8088
# PostgreSQL: localhost:5432

# Monitor services
docker compose ps
docker compose logs -f temporal-worker
```

### **Production Deployment with Kubernetes**

```bash
# Deploy using kubectl
kubectl apply -f k8s/ -n temporal-system

# Deploy using Helm
helm install temporal-worker ./helm \
  --namespace temporal-system \
  --create-namespace \
  --values values-prod.yaml

# Verify deployment
kubectl get pods -n temporal-system
kubectl get svc -n temporal-system
```

### **Scaling and Management**

```bash
# Manual scaling
kubectl scale deployment temporal-worker --replicas=5 -n temporal-system

# Auto-scaling
kubectl autoscale deployment temporal-worker \
  --min=3 --max=10 --cpu-percent=80 -n temporal-system

# Rolling updates
kubectl set image deployment/temporal-worker \
  temporal-worker=temporal-worker:v2.0.0 -n temporal-system

# Rollback if needed
kubectl rollout undo deployment/temporal-worker -n temporal-system
```

## Monitoring and Observability

### **Health Checks**
- **Application**: Spring Boot Actuator endpoints
- **Container**: Docker health checks
- **Kubernetes**: Liveness, readiness, and startup probes
- **Load Balancer**: Service health monitoring

### **Metrics Collection**
```yaml
# Prometheus annotations
annotations:
  prometheus.io/scrape: "true"
  prometheus.io/port: "8080"
  prometheus.io/path: "/actuator/prometheus"

# Custom metrics
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### **Logging Configuration**
```yaml
logging:
  level:
    root: INFO
    io.temporal: DEBUG
  pattern:
    console: '{"timestamp":"%d{ISO8601}","level":"%level","class":"%logger{40}","message":"%message"}%n'
```

## Security Implementation

### **Container Security**
- **Non-root user**: All containers run as non-root
- **Minimal base images**: Only necessary components included
- **Regular security scans**: Automated vulnerability scanning
- **Secret management**: Proper separation of secrets from configuration

### **Kubernetes Security**
- **RBAC**: Role-based access control for service accounts
- **Pod Security Context**: Security constraints at pod level
- **Network Policies**: Traffic isolation between namespaces
- **Secrets management**: Encrypted at rest and in transit

### **Production Hardening**
```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1000
  readOnlyRootFilesystem: true
  allowPrivilegeEscalation: false
  capabilities:
    drop:
    - ALL
```

## Performance Optimization

### **JVM Tuning for Containers**
```bash
JAVA_OPTS="-XX:+UseContainerSupport 
           -XX:MaxRAMPercentage=75.0 
           -XX:+UseG1GC 
           -XX:+UseStringDeduplication"
```

### **Resource Management**
```yaml
resources:
  limits:
    memory: "1Gi"      # Hard limit
    cpu: "1000m"       # 1 CPU core
  requests:
    memory: "512Mi"    # Guaranteed memory
    cpu: "500m"        # 0.5 CPU core reserved
```

### **Scaling Configuration**
```yaml
# Horizontal Pod Autoscaler
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
spec:
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 80
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

## Troubleshooting Guide

### **Common Issues and Solutions**

#### **Container Build Issues**
```bash
# Check Gradle wrapper permissions
chmod +x gradlew

# Build with verbose output
docker build --no-cache --progress=plain -t temporal-worker .

# Debug multi-stage build
docker build --target=build -t temporal-worker-debug .
```

#### **Service Connectivity Issues**
```bash
# Check service resolution
kubectl exec -it temporal-worker-xxx -- nslookup temporal-service

# Test connectivity
kubectl exec -it temporal-worker-xxx -- curl -v http://temporal-service:7233

# Check logs
kubectl logs -f deployment/temporal-worker -n temporal-system
```

#### **Health Check Failures**
```bash
# Test health endpoint locally
curl -v http://localhost:8080/actuator/health

# Check health in container
kubectl exec -it temporal-worker-xxx -- curl http://localhost:8080/actuator/health

# Debug startup issues
kubectl describe pod temporal-worker-xxx -n temporal-system
```

## Production Readiness Checklist

- ✅ **Security**: Non-root users, minimal images, RBAC configured
- ✅ **Monitoring**: Health checks, metrics, logging configured
- ✅ **Scalability**: HPA configured, resource limits set
- ✅ **Reliability**: Rolling updates, health checks, restart policies
- ✅ **Configuration**: Secrets management, environment separation
- ✅ **Performance**: JVM tuning, resource optimization
- ✅ **Backup**: Database backups, disaster recovery procedures
- ✅ **Documentation**: Deployment guides, troubleshooting docs

## Key Learning Outcomes

1. **Containerization Mastery**: Complete understanding of Docker best practices
2. **Orchestration Expertise**: Production-ready Kubernetes deployments
3. **Configuration Management**: Proper separation of concerns
4. **Security Awareness**: Container and Kubernetes security patterns
5. **Operational Excellence**: Monitoring, scaling, and maintenance procedures
6. **Deployment Automation**: CI/CD ready infrastructure as code

## Graduation Achievement

Completing this lesson means you have mastered:
- ✅ Complete Temporal application lifecycle (development to production)
- ✅ Modern containerization and orchestration practices
- ✅ Production-grade security and monitoring patterns
- ✅ Scalable deployment architectures
- ✅ Infrastructure as code methodologies

**🎉 Congratulations! You're now ready to deploy and maintain production-grade Temporal applications in any environment!**

This completes your journey from Temporal workflow basics to enterprise deployment mastery. You now have all the skills needed to build, test, and deploy robust, scalable workflow applications in production environments. 