# Lesson 17: Deployment & Production Infrastructure

## What we want to build

A complete production-ready deployment setup for your Temporal Workflow application using modern containerization and orchestration tools. This lesson teaches you how to deploy the entire Temporal stack (server, database, workers) and your Kotlin Spring Boot application using Docker, Docker Compose, and Kubernetes.

## Expecting Result

By the end of this lesson, you will be able to:
- Containerize your Kotlin Spring Boot Temporal worker application
- Deploy the complete Temporal stack locally using Docker Compose
- Deploy to Kubernetes using plain manifests or Helm charts
- Configure environment-specific settings and secrets management
- Monitor and troubleshoot your deployed application
- Scale your workers based on workload demands

## Code Steps

### Step 1: Create a Production-Ready Dockerfile

Build a multi-stage Dockerfile for efficient container images:

```dockerfile
# Build stage
FROM gradle:7.6-jdk17 AS build
WORKDIR /app

# Copy Gradle files first for better layer caching
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle/

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy source and build
COPY src src/
RUN gradle build -x test --no-daemon

# Runtime stage
FROM openjdk:17-jre-slim AS runtime
# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r temporal && useradd -r -g temporal temporal
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown -R temporal:temporal /app
USER temporal

EXPOSE 8080
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
```

**Key considerations:**
- Multi-stage build reduces final image size
- Non-root user improves security
- Health check endpoint for container orchestration
- Configurable JVM options for containerized environments

### Step 2: Configure Docker Compose for Local Development

Create a complete `docker-compose.yaml` with all services:

```yaml
version: '3.8'

services:
  # PostgreSQL for Temporal persistence
  postgresql:
    image: postgres:13
    environment:
      POSTGRES_PASSWORD: temporal
      POSTGRES_USER: temporal
      POSTGRES_DB: temporal
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U temporal"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Temporal server with auto-setup
  temporal:
    image: temporalio/auto-setup:1.22.0
    ports:
      - "7233:7233"  # gRPC
      - "8080:8080"  # HTTP
    environment:
      - DB=postgresql
      - POSTGRES_USER=temporal
      - POSTGRES_PWD=temporal
      - POSTGRES_SEEDS=postgresql
    depends_on:
      postgresql:
        condition: service_healthy

  # Your worker application
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
```

**Key features:**
- Health checks ensure proper startup order
- Environment-specific configuration
- Volume persistence for database
- Network isolation between services

### Step 3: Environment Configuration

Create environment configuration files:

**env.example:**
```bash
# Temporal configuration
TEMPORAL_FRONTEND_ADDRESS=localhost:7233
TEMPORAL_NAMESPACE=default

# Database settings
POSTGRES_USER=temporal
POSTGRES_PASSWORD=temporal
POSTGRES_DB=temporal

# Application settings
SPRING_PROFILES_ACTIVE=docker
SERVER_PORT=8080
LOGGING_LEVEL_ROOT=INFO
```

Copy `env.example` to `.env` and customize for your environment.

### Step 4: Kubernetes Deployment with Plain Manifests

Create Kubernetes manifests for production deployment:

**Namespace:**
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: temporal-system
```

**ConfigMap for application configuration:**
```yaml
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
    spring:
      profiles.active: kubernetes
```

**Secret for sensitive data:**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: temporal-worker-secrets
  namespace: temporal-system
type: Opaque
stringData:
  database-username: temporal
  database-password: temporal
  external-api-key: your-api-key-here
```

**Deployment with proper resource limits and health checks:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: temporal-worker
  namespace: temporal-system
spec:
  replicas: 3
  selector:
    matchLabels:
      app: temporal-worker
  template:
    spec:
      containers:
      - name: temporal-worker
        image: temporal-worker:latest
        ports:
        - containerPort: 8080
        env:
        - name: TEMPORAL_FRONTEND_ADDRESS
          value: "temporal-service:7233"
        resources:
          limits:
            memory: "1Gi"
            cpu: "1000m"
          requests:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
```

### Step 5: Helm Chart for Advanced Deployment

Create a Helm chart for configurable deployments:

**Chart.yaml:**
```yaml
apiVersion: v2
name: temporal-worker
description: Temporal Worker Helm Chart
version: 0.1.0
appVersion: "1.0.0"
dependencies:
  - name: postgresql
    version: "11.9.13"
    repository: https://charts.bitnami.com/bitnami
    condition: postgresql.enabled
```

**values.yaml:**
```yaml
replicaCount: 3

image:
  repository: temporal-worker
  tag: latest
  pullPolicy: IfNotPresent

temporal:
  frontendAddress: "temporal-service:7233"
  namespace: "default"

resources:
  limits:
    cpu: 1000m
    memory: 1Gi
  requests:
    cpu: 500m
    memory: 512Mi

postgresql:
  enabled: true
  auth:
    username: temporal
    password: temporal
    database: temporal
```

### Step 6: Application Configuration for Containers

Update your Spring Boot application configuration to work with containers:

**application-docker.yaml:**
```yaml
server:
  port: 8080

temporal:
  frontend:
    address: ${TEMPORAL_FRONTEND_ADDRESS:localhost:7233}
  namespace: ${TEMPORAL_NAMESPACE:default}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

**application-kubernetes.yaml:**
```yaml
server:
  port: 8080

temporal:
  frontend:
    address: ${TEMPORAL_FRONTEND_ADDRESS}
  namespace: ${TEMPORAL_NAMESPACE}

spring:
  datasource:
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
```

### Step 7: Build and Test Your Container

Build and test your container locally:

```bash
# Build the Docker image
docker build -t temporal-worker:latest .

# Test the container locally
docker run -p 8080:8080 --name temporal-worker-test \
  -e TEMPORAL_FRONTEND_ADDRESS=host.docker.internal:7233 \
  temporal-worker:latest

# Check health endpoint
curl http://localhost:8080/actuator/health
```

### Step 8: Deploy with Docker Compose

Start the complete stack locally:

```bash
# Start all services
docker compose up -d

# Check service status
docker compose ps

# View logs
docker compose logs temporal-worker

# Access Temporal Web UI
open http://localhost:8088

# Access your application
curl http://localhost:8081/actuator/health
```

### Step 9: Deploy to Kubernetes

Deploy using kubectl:

```bash
# Create namespace
kubectl apply -f k8s/namespace.yaml

# Apply configuration
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml

# Deploy application
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Check deployment status
kubectl get pods -n temporal-system
kubectl logs -f deployment/temporal-worker -n temporal-system

# Port forward to access application
kubectl port-forward svc/temporal-worker-service 8080:8080 -n temporal-system
```

Or deploy using Helm:

```bash
# Install dependencies
helm dependency update ./helm

# Install the chart
helm install temporal-worker ./helm \
  --namespace temporal-system \
  --create-namespace \
  --values ./helm/values.yaml

# Check deployment
helm status temporal-worker -n temporal-system
kubectl get pods -n temporal-system
```

## How to Run

### Local Development with Docker Compose

1. **Copy environment file:**
   ```bash
   cp env.example .env
   # Edit .env with your configuration
   ```

2. **Start the stack:**
   ```bash
   docker compose up -d
   ```

3. **Verify services are running:**
   ```bash
   docker compose ps
   # All services should show "Up" status
   ```

4. **Check application health:**
   ```bash
   curl http://localhost:8081/actuator/health
   ```

5. **Access Temporal Web UI:**
   ```bash
   open http://localhost:8088
   ```

6. **Monitor logs:**
   ```bash
   docker compose logs -f temporal-worker
   ```

### Production Deployment with Kubernetes

1. **Prepare cluster:**
   ```bash
   # Create namespace
   kubectl create namespace temporal-system
   ```

2. **Deploy using kubectl:**
   ```bash
   kubectl apply -f k8s/ -n temporal-system
   ```

3. **Or deploy using Helm:**
   ```bash
   helm install temporal-worker ./helm -n temporal-system --create-namespace
   ```

4. **Verify deployment:**
   ```bash
   kubectl get pods -n temporal-system
   kubectl get svc -n temporal-system
   ```

5. **Access application:**
   ```bash
   kubectl port-forward svc/temporal-worker-service 8080:8080 -n temporal-system
   curl http://localhost:8080/actuator/health
   ```

### What Logs to Watch

**Successful startup logs to look for:**

```log
# Temporal connection
INFO - Successfully connected to Temporal at temporal:7233

# Worker registration
INFO - Worker started for task queue: default-task-queue

# Spring Boot startup
INFO - Started TemporalStarterApplication in X.XXX seconds

# Health check
INFO - Health check endpoint available at /actuator/health
```

**Troubleshooting common issues:**

- **Connection refused:** Check if Temporal server is running and accessible
- **Database connection issues:** Verify PostgreSQL is healthy and credentials are correct
- **Image pull errors:** Ensure Docker image is built and tagged correctly
- **Health check failures:** Check if application is binding to correct port (8080)

### Scaling Your Deployment

**Docker Compose scaling:**
```bash
docker compose up -d --scale temporal-worker=3
```

**Kubernetes manual scaling:**
```bash
kubectl scale deployment temporal-worker --replicas=5 -n temporal-system
```

**Kubernetes auto-scaling:**
```bash
kubectl autoscale deployment temporal-worker \
  --min=3 --max=10 --cpu-percent=80 \
  -n temporal-system
``` 