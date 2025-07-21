# Lesson 17 Concepts: Deployment & Production Infrastructure

## Objective

Master the deployment and infrastructure patterns required to run Temporal workflows in production environments. Learn containerization, orchestration, configuration management, and scaling strategies for robust, production-ready Temporal applications.

## Key Concepts

### 1. **Containerization Fundamentals**

#### **Docker Multi-Stage Builds**
Multi-stage builds optimize container images by separating build and runtime environments:

```dockerfile
# Build stage - includes full development environment
FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --no-daemon
COPY src src/
RUN gradle build -x test --no-daemon

# Runtime stage - minimal production environment
FROM openjdk:17-jre-slim AS runtime
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Benefits:**
- Smaller final image size (only runtime dependencies)
- Better security (no build tools in production)
- Faster deployment and startup times
- Better layer caching for faster rebuilds

#### **Container Security Best Practices**
```dockerfile
# Use non-root user
RUN groupadd -r temporal && useradd -r -g temporal temporal
USER temporal

# Use specific image tags
FROM openjdk:17-jre-slim

# Install only necessary packages
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set secure file permissions
RUN chown -R temporal:temporal /app
```

### 2. **Container Orchestration Patterns**

#### **Docker Compose for Local Development**
Docker Compose simplifies multi-container applications:

```yaml
version: '3.8'
services:
  temporal:
    image: temporalio/auto-setup:1.22.0
    depends_on:
      postgresql:
        condition: service_healthy
    
  postgresql:
    image: postgres:13
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U temporal"]
      interval: 10s
      timeout: 5s
      retries: 5
```

**Key features:**
- **Service Dependencies**: Control startup order with `depends_on`
- **Health Checks**: Ensure services are ready before dependents start
- **Volume Persistence**: Data survives container restarts
- **Network Isolation**: Services communicate through internal networks

#### **Kubernetes for Production**
Kubernetes provides production-grade orchestration:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: temporal-worker
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 1
```

**Kubernetes advantages:**
- **Horizontal Scaling**: Automatically scale based on metrics
- **Rolling Updates**: Zero-downtime deployments
- **Self-Healing**: Automatic restart of failed containers
- **Resource Management**: CPU and memory limits/requests
- **Service Discovery**: Built-in DNS and load balancing

### 3. **Configuration Management**

#### **Environment-Specific Configuration**
Use Spring profiles for different environments:

```yaml
# application-docker.yaml
temporal:
  frontend:
    address: temporal:7233

# application-kubernetes.yaml  
temporal:
  frontend:
    address: ${TEMPORAL_FRONTEND_ADDRESS}
```

#### **Secrets Management**
Separate sensitive configuration from application code:

```yaml
# Kubernetes Secret
apiVersion: v1
kind: Secret
metadata:
  name: temporal-secrets
type: Opaque
stringData:
  database-password: "secure-password"
  api-key: "secret-api-key"
```

**Best practices:**
- Never hardcode secrets in images or config files
- Use environment variables or mounted secret files
- Implement secret rotation policies
- Use external secret management systems (Vault, AWS Secrets Manager)

### 4. **Health Checks and Monitoring**

#### **Application Health Endpoints**
Spring Boot Actuator provides comprehensive health checks:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

#### **Container Health Checks**
Configure health checks at multiple levels:

```dockerfile
# Dockerfile health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
```

```yaml
# Kubernetes health checks
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

**Health check types:**
- **Liveness**: Is the application running?
- **Readiness**: Is the application ready to serve traffic?
- **Startup**: Has the application finished starting up?

### 5. **Scaling Strategies**

#### **Horizontal Pod Autoscaling (HPA)**
Automatically scale based on metrics:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: temporal-worker-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: temporal-worker
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 80
```

#### **Vertical Pod Autoscaling (VPA)**
Automatically adjust resource requests:

```yaml
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: temporal-worker-vpa
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: temporal-worker
  updatePolicy:
    updateMode: "Auto"
```

### 6. **Helm Charts for Package Management**

#### **Chart Structure**
```
helm/
├── Chart.yaml          # Chart metadata
├── values.yaml         # Default configuration values
├── templates/          # Kubernetes manifest templates
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── configmap.yaml
│   └── secret.yaml
└── charts/            # Dependency charts
```

#### **Templating and Values**
```yaml
# values.yaml
replicaCount: 3
image:
  repository: temporal-worker
  tag: latest

# templates/deployment.yaml
spec:
  replicas: {{ .Values.replicaCount }}
  template:
    spec:
      containers:
      - name: temporal-worker
        image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
```

**Helm benefits:**
- **Templating**: Reusable configurations across environments
- **Dependency Management**: Manage complex application dependencies
- **Release Management**: Track and rollback deployments
- **Configuration Validation**: Schema validation for values

## Best Practices

### 1. **Image Optimization**

#### **Minimal Base Images**
```dockerfile
# ✅ Good: Minimal runtime image
FROM openjdk:17-jre-slim

# ❌ Bad: Full JDK with unnecessary tools
FROM openjdk:17-jdk
```

#### **Layer Caching Optimization**
```dockerfile
# ✅ Good: Copy dependencies first for better caching
COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --no-daemon
COPY src src/
RUN gradle build --no-daemon

# ❌ Bad: Copy everything, then download dependencies
COPY . .
RUN gradle build --no-daemon
```

#### **Security Scanning**
```bash
# Scan images for vulnerabilities
docker scan temporal-worker:latest

# Use minimal base images
FROM gcr.io/distroless/java17-debian11
```

### 2. **Resource Management**

#### **Proper Resource Limits**
```yaml
resources:
  limits:
    memory: "1Gi"       # Maximum memory allowed
    cpu: "1000m"        # Maximum CPU (1 core)
  requests:
    memory: "512Mi"     # Reserved memory
    cpu: "500m"         # Reserved CPU (0.5 core)
```

#### **JVM Tuning for Containers**
```dockerfile
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
```

**Container-aware JVM options:**
- `UseContainerSupport`: Recognize container memory limits
- `MaxRAMPercentage`: Set heap size as percentage of container memory
- `UseG1GC`: Low-latency garbage collector

### 3. **Configuration Management**

#### **Environment-Specific Values**
```yaml
# values-dev.yaml
replicaCount: 1
resources:
  requests:
    memory: "256Mi"
    cpu: "250m"

# values-prod.yaml
replicaCount: 3
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
```

#### **External Configuration Sources**
```yaml
# Use ConfigMaps for non-sensitive configuration
envFrom:
- configMapRef:
    name: temporal-worker-config

# Use Secrets for sensitive data
envFrom:
- secretRef:
    name: temporal-worker-secrets
```

### 4. **Monitoring and Observability**

#### **Prometheus Metrics**
```yaml
# Pod annotations for Prometheus scraping
annotations:
  prometheus.io/scrape: "true"
  prometheus.io/port: "8080"
  prometheus.io/path: "/actuator/prometheus"
```

#### **Structured Logging**
```yaml
logging:
  pattern:
    console: '{"timestamp":"%d{ISO8601}","level":"%level","thread":"%thread","class":"%logger{40}","message":"%message"}%n'
```

#### **Distributed Tracing**
```yaml
management:
  tracing:
    sampling:
      probability: 0.1
  zipkin:
    tracing:
      endpoint: http://zipkin:9411/api/v2/spans
```

### 5. **Deployment Strategies**

#### **Rolling Updates**
```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxUnavailable: 25%
    maxSurge: 25%
```

#### **Blue-Green Deployment**
```bash
# Deploy to green environment
helm install temporal-worker-green ./helm --values values-green.yaml

# Switch traffic to green
kubectl patch service temporal-worker-service \
  -p '{"spec":{"selector":{"version":"green"}}}'

# Remove blue environment
helm uninstall temporal-worker-blue
```

#### **Canary Deployment**
```yaml
# Canary service (10% traffic)
apiVersion: v1
kind: Service
metadata:
  name: temporal-worker-canary
spec:
  selector:
    app: temporal-worker
    version: canary
```

### 6. **Security Hardening**

#### **Pod Security Context**
```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1000
  readOnlyRootFilesystem: true
  allowPrivilegeEscalation: false
```

#### **Network Policies**
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: temporal-worker-netpol
spec:
  podSelector:
    matchLabels:
      app: temporal-worker
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: temporal-web
```

#### **RBAC (Role-Based Access Control)**
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: temporal-worker-role
rules:
- apiGroups: [""]
  resources: ["configmaps", "secrets"]
  verbs: ["get", "list", "watch"]
```

### 7. **Production Considerations**

#### **Database Considerations**
- **Use managed databases** in production (AWS RDS, Google Cloud SQL)
- **Implement connection pooling** for database connections
- **Configure backup and recovery** strategies
- **Monitor database performance** and optimize queries

#### **Temporal Server Deployment**
- **Use Temporal Cloud** for managed Temporal service
- **Deploy Temporal server clusters** for high availability
- **Configure proper data retention** policies
- **Implement disaster recovery** procedures

#### **Networking and Load Balancing**
- **Use ingress controllers** for external access
- **Implement service mesh** for advanced traffic management
- **Configure SSL/TLS termination** at load balancer
- **Set up proper DNS** and service discovery

#### **Backup and Disaster Recovery**
- **Backup application data** and configuration
- **Test recovery procedures** regularly
- **Implement cross-region replication** for critical workloads
- **Document incident response** procedures

### 8. **Common Anti-Patterns to Avoid**

❌ **Hardcoded Configuration**: Never embed environment-specific values in container images
❌ **Root User**: Avoid running containers as root user
❌ **Large Images**: Don't include unnecessary tools in production images
❌ **Missing Health Checks**: Always implement proper health checks
❌ **No Resource Limits**: Set appropriate CPU and memory limits
❌ **Ignoring Security**: Don't skip security scanning and hardening
❌ **Poor Logging**: Avoid unstructured or excessive logging
❌ **Manual Deployment**: Automate deployment processes with CI/CD 