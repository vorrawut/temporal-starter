---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Lesson 17: Deployment & Production Infrastructure

## Workshop Guide

*Deploy your Temporal applications to production-like environments using Docker, Docker Compose, and Kubernetes*

---

# Overview

This **final lesson** of the Temporal Workflow bootcamp focuses on deploying your Temporal applications to **production-like environments** using:

- ✅ **Docker** for containerization
- ✅ **Docker Compose** for local development 
- ✅ **Kubernetes** for production scaling

You'll learn containerization, configuration management, and deployment at scale.

---

# What You'll Learn

## **Production Deployment Skills:**

- ✅ **Containerizing Spring Boot applications** with Docker
- ✅ **Multi-stage builds** for optimized container images
- ✅ **Local development** with Docker Compose
- ✅ **Production deployment** with Kubernetes
- ✅ **Configuration management** and secrets handling
- ✅ **Health checks and monitoring** setup
- ✅ **Scaling strategies** and best practices

---

# Your Task

## Complete the production deployment setup by implementing:

1. ✅ **Production-Ready Dockerfile**: Multi-stage build with security best practices
2. ✅ **Docker Compose Configuration**: Complete stack with Temporal server, database, and your application
3. ✅ **Kubernetes Manifests**: Deployment, Service, ConfigMap, and Secret configurations
4. ✅ **Environment Configuration**: Proper separation of environment-specific settings
5. ✅ **Health Checks**: Application and container health monitoring
6. ✅ **Helm Chart** (Optional): Package management for Kubernetes deployments

---

# Key Requirements

## **Production Standards:**

- ✅ **Use multi-stage Docker builds** for efficiency
- ✅ **Implement proper security practices** (non-root user, minimal base images)
- ✅ **Configure health checks** at application and container levels
- ✅ **Separate configuration from secrets**
- ✅ **Include resource limits and requests**
- ✅ **Implement proper scaling strategies**
- ✅ **Follow container orchestration** best practices

---

# Files to Work With

## **Implementation Guide:**

- ✅ `docker-compose.yaml` - Complete the Docker Compose configuration
- ✅ `Dockerfile` - Build a production-ready container image
- ✅ `k8s/` directory - Complete the Kubernetes manifests
- ✅ `env.example` - Environment configuration template
- ✅ **Follow the TODO comments** to implement each section

---

# Success Criteria

## Your implementation should demonstrate:

- ✅ **Working Docker Compose stack** with all services
- ✅ **Optimized Docker image** with security best practices
- ✅ **Complete Kubernetes deployment** manifests
- ✅ **Proper configuration and secrets** management
- ✅ **Health checks and monitoring** endpoints
- ✅ **Resource management and scaling** configuration
- ✅ **Production-ready deployment** patterns

---

# Getting Started

## **Prerequisites:**

Make sure you have installed:
- ✅ **Docker and Docker Compose**
- ✅ **kubectl** (for Kubernetes deployment)
- ✅ **Helm** (optional, for Helm chart deployment)
- ✅ **Access to a Kubernetes cluster** (local or cloud)

---

# Step-by-Step Approach

## **Implementation Sequence:**

1. ✅ **Start with Docker**: Complete the Dockerfile with multi-stage build
2. ✅ **Local Development**: Set up Docker Compose for the complete stack
3. ✅ **Kubernetes Basics**: Create basic deployment and service manifests
4. ✅ **Configuration**: Add ConfigMaps and Secrets for configuration management
5. ✅ **Advanced Features**: Add health checks, resource limits, and scaling
6. ✅ **Monitoring**: Configure Prometheus metrics and logging
7. ✅ **Helm Chart** (Optional): Package everything in a Helm chart

---

# Testing Your Implementation

## **Docker Compose Testing:**
```bash
# Build and start the stack
docker compose up -d

# Verify all services are running
docker compose ps

# Check application health
curl http://localhost:8081/actuator/health

# Access Temporal Web UI
open http://localhost:8088

# View logs
docker compose logs temporal-worker
```

---

# Kubernetes Testing

```bash
# Apply all manifests
kubectl apply -f k8s/ -n temporal-system

# Check deployment status
kubectl get pods -n temporal-system
kubectl get svc -n temporal-system

# Port forward to access application
kubectl port-forward svc/temporal-worker-service 8080:8080 -n temporal-system

# Check application health
curl http://localhost:8080/actuator/health
```

---

# Common Issues and Solutions

## **Docker Issues:**
- ✅ **Build failures**: Check Gradle wrapper permissions and dependencies
- ✅ **Connection refused**: Ensure services are healthy before dependents start
- ✅ **Port conflicts**: Check if ports 7233, 8080, 8081, 8088 are available

## **Kubernetes Issues:**
- ✅ **ImagePullBackOff**: Ensure Docker image is built and tagged correctly
- ✅ **CrashLoopBackOff**: Check application logs and health check configuration
- ✅ **Service not accessible**: Verify service selectors match pod labels

---

# Production Considerations

## When implementing, consider:

| Aspect | Focus | Implementation |
|--------|--------|----------------|
| **Security** | Use non-root users, scan images | Security contexts, image scanning |
| **Performance** | Optimize JVM settings for containers | Resource limits, JVM tuning |
| **Monitoring** | Include comprehensive health checks | Actuator endpoints, Prometheus |
| **Scalability** | Design for horizontal scaling | HPA, resource requests |
| **Reliability** | Implement proper restart policies | Liveness/readiness probes |
| **Maintainability** | Use clear naming conventions | Documentation, IaC |

---

# 🎉 Final Achievement

## After completing this lesson, you'll have:

- ✅ **Complete understanding** of containerized Temporal deployments
- ✅ **Production-ready infrastructure** as code
- ✅ **Experience with container orchestration** platforms
- ✅ **Knowledge of deployment best practices** and security considerations

---

# 🏆 Bootcamp Mastery Complete!

**Congratulations! You've now mastered the complete Temporal Workflow development lifecycle from basic concepts to production deployment!**

## **Your journey included:**
- Temporal fundamentals and architecture
- Workflow and activity patterns
- Error handling and reliability
- Testing and production readiness
- Advanced patterns and scaling
- **Full production deployment**

**You're ready to build and deploy enterprise-grade distributed systems! 🚀** 