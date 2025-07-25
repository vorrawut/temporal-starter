---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Lesson 15: External Service Integration

## Workshop Guide

*Learn how to integrate Temporal workflows with external services like APIs, databases, payment gateways, and notification systems*

---

# Overview

In this lesson, you'll learn how to integrate Temporal workflows with external services like **APIs**, **databases**, **payment gateways**, and **notification systems**. 

This is **critical for real-world applications** where workflows need to orchestrate multiple external dependencies.

---

# What You'll Learn

## **Core Integration Patterns:**

- ✅ **How to encapsulate external service calls** within Temporal activities
- ✅ **Different timeout and retry strategies** for different service types
- ✅ **Error handling patterns** for graceful degradation
- ✅ **Service-specific activity configurations**
- ✅ **Production-ready integration patterns**

---

# Your Task

## Complete the `ExternalServiceWorkflow` implementation to:

1. ✅ **Define comprehensive data classes** for external service integration
2. ✅ **Create activity interfaces** for different service types (UserProfile, Payment, Database, Notification)
3. ✅ **Configure appropriate activity options** for external vs internal services
4. ✅ **Implement the workflow logic** with proper error handling and service orchestration
5. ✅ **Handle partial failures** gracefully while collecting detailed error information

---

# Key Requirements

## **Integration Standards:**

- ✅ **All external service calls must happen within activities**
- ✅ **Use different timeout/retry configurations** for different service types
- ✅ **Implement graceful error handling** that doesn't fail the entire workflow
- ✅ **Return comprehensive results** showing what succeeded and what failed
- ✅ **Include proper logging** and activity correlation

---

# Files to Work With

## **Implementation Guide:**

- ✅ `workflow/ExternalServiceWorkflow.kt` - Main workflow interface and implementation
- ✅ **Follow the TODO comments** to implement each section
- ✅ **Reference the concept documentation** for best practices
- ✅ **Test with mock external services**

---

# Success Criteria

## Your implementation should:

- ✅ **Handle multiple external service integrations**
- ✅ **Use appropriate timeouts** for different service types
- ✅ **Collect and return all errors** without failing early
- ✅ **Demonstrate proper activity configuration** patterns
- ✅ **Show real-world integration scenarios**

---

# Service Integration Patterns

## **Service Categories:**

| Service Type | Timeout | Retries | Strategy |
|--------------|---------|---------|----------|
| **External APIs** | 5+ minutes | 5+ attempts | Conservative |
| **Payment Gateways** | 10+ minutes | 2 attempts | Careful |
| **Internal Services** | 2 minutes | 3 attempts | Standard |
| **Notifications** | 1 minute | 5 attempts | Aggressive |

---

# 🚀 Next Steps

**After completing this lesson, you'll be ready for Lesson 16** where we'll cover:

- **Testing strategies** for complex workflows
- **Production deployment** considerations 
- **Monitoring and observability** patterns
- **Scaling external integrations**

**Ready to integrate with the world? Let's build! 🎉** 