---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Deployment & Production Infrastructure

## Lesson 17: Production-Ready Temporal Systems

Master the deployment and infrastructure patterns required to run Temporal workflows in production environments. Learn containerization, orchestration, configuration management, and scaling strategies for robust, production-ready Temporal applications.

---

# Objective

By the end of this lesson, you will understand:

- ✅ **Containerization patterns** with Docker multi-stage builds
- ✅ **Container orchestration** with Docker Compose and Kubernetes
- ✅ **Production configuration** management and security
- ✅ **Scaling strategies** for high-availability systems
- ✅ **Monitoring and observability** for production workflows
- ✅ **Deployment pipelines** and operational best practices

---

# 1. **Containerization Fundamentals**

## **Docker Multi-Stage Builds**

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

---

# Multi-Stage Build Benefits

## **Production Advantages:**

- ✅ **Smaller final image size** (only runtime dependencies)
- ✅ **Better security** (no build tools in production)
- ✅ **Faster deployment** and startup times
- ✅ **Better layer caching** for faster rebuilds

## **Security Best Practices:**

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

---

# 2. **Container Orchestration Patterns**

## **Docker Compose for Local Development**

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

**Key features: Service dependencies, health checks, volume persistence, network isolation**

---

# Kubernetes for Production

## **Production-Grade Orchestration:**

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

## **Kubernetes Advantages:**
- ✅ **Horizontal Scaling**: Automatically scale based on metrics
- ✅ **Rolling Updates**: Zero-downtime deployments
- ✅ **Self-Healing**: Automatic restart of failed containers
- ✅ **Load Balancing**: Built-in service discovery and load balancing

---

# Production Deployment Patterns

## **Environment Configuration:**

| Environment | Configuration | Purpose |
|-------------|---------------|---------|
| **Development** | Docker Compose | Local development and testing |
| **Staging** | Kubernetes | Production-like validation |
| **Production** | Kubernetes + Helm | Scalable production deployment |

## **Scaling Strategy:**
- **Horizontal Pod Autoscaler** for dynamic scaling
- **Resource requests and limits** for predictable performance
- **Multiple availability zones** for high availability

---

# 💡 Key Deployment Concepts

## **What You've Learned:**

- ✅ **Multi-stage Docker builds** optimize production images
- ✅ **Container orchestration** manages complex deployments
- ✅ **Security best practices** protect production systems
- ✅ **Kubernetes patterns** enable scalable production deployments
- ✅ **Environment-specific** configurations support the full SDLC

---

# 🎉 Congratulations!

**You've completed the Temporal Workflow Bootcamp!**

## **You now master:**
- Temporal fundamentals and architecture
- Workflow and activity patterns
- Error handling and reliability
- Testing and production readiness
- Advanced patterns and scaling

**You're ready to build production-grade distributed systems with Temporal! 🚀** 