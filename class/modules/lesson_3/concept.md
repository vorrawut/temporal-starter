---
marp: true
theme: gaia
paginate: true
backgroundColor: #1e1e2f
color: white
---

# Run Temporal Locally

## Lesson 3: Setting Up Your Development Environment

🎯 **Objective**: Understand Temporal server architecture, run Temporal locally for development, and explore monitoring and debugging tools.

---

# 🏗️ Temporal Server Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│                    Temporal Server                          │
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ Frontend    │  │ History     │  │ Matching            │  │
│  │ Service     │  │ Service     │  │ Service             │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ Worker      │  │ Internal    │  │ Database            │  │
│  │ Service     │  │ Frontend    │  │ (SQLite/MySQL/etc.) │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                               │
                               ▼
                    ┌─────────────────┐
                    │    Web UI       │
                    │  (Port 8233)    │
                    └─────────────────┘
```

---

# Core Services Overview

## **Frontend Service (Port 7233)**
- Main API endpoint for clients and workers
- Handles routing and authentication

## **History Service**
- Stores workflow event history for replay and recovery

## **Matching Service**
- Routes tasks to workers
- Manages task queues with load balancing

---

# More Core Services

## **Worker Service**
- Runs internal Temporal system workflows

## **Database**
- Persistent storage layer
- SQLite for dev, MySQL/Postgres for production

## **Web UI (Port 8233)**
- Visual interface for monitoring and debugging

---

# ⚙️ Development vs Production

## **Development Setup**

```bash
temporal server start-dev
```

### **Features:**
- ✅ Single-node server with embedded SQLite DB
- ✅ Web UI available on port 8233
- ✅ Zero config, no auth
- ✅ Perfect for local dev and learning

---

# Production Setup Example

```yaml
# docker-compose.yml
version: '3.8'
services:
  temporal:
    image: temporalio/auto-setup:latest
    environment:
      - DB=postgresql
      - DB_PORT=5432
      - POSTGRES_USER=temporal
      - POSTGRES_PWD=temporal
    ports:
      - 7233:7233
    depends_on:
      - postgresql
```

---

# Production Features

### **Production Setup Includes:**
- ✅ Multi-node clustering
- ✅ External DB (Postgres, MySQL, Cassandra)
- ✅ Authentication
- ✅ Monitoring
- ✅ High availability
- ✅ Scaling capabilities

---

# 🔧 Temporal CLI Quick Commands

## **Server Operations**

```bash
# Start basic dev server
temporal server start-dev

# Custom ports
temporal server start-dev --port 7234 --http-port 8234

# Persistent database
temporal server start-dev --db-filename temporal.db
```

---

# CLI: Namespace Management

```bash
# List namespaces
temporal operator namespace list

# Create namespace
temporal operator namespace create my-namespace

# Describe namespace
temporal operator namespace describe default
```

## **Why Namespaces Matter:**
- 🏢 **Multi-tenancy** - Separate different applications
- 🔒 **Isolation** - Keep environments separate
- 📊 **Organization** - Group related workflows

---

🧠 What is namespace temporal-system?
It’s the default internal namespace Temporal uses to run its own workflows — not yours.

Think of it like Temporal’s “admin” namespace — 
it’s where it handles system-level workflows, heartbeats, and background tasks that keep Temporal itself alive and healthy.

---

# CLI: Workflow Operations

```bash
# List all workflows
temporal workflow list

# Show specific workflow
temporal workflow show --workflow-id my-workflow-id

# Terminate workflow
temporal workflow terminate --workflow-id my-workflow-id
```

**Pro tip**: Use these commands for debugging and monitoring!

---

# 🌐 Web UI Highlights

## **Key Features:**

- **📋 Workflows**: Browse executions, filter, retry failures
- **⚡ Task Queues**: Monitor worker health, queue backlog, partitions
- **👷 Workers**: See registered workers and their capabilities
- **📅 Schedules**: Manage recurring workflows and cron schedules

**Access at**: `http://localhost:8233`

---

# 🛠️ Best Practices for Local Development

## **Development Tips:**

- ✅ **Use startup scripts** or aliases for easy server launch
- ✅ **Persist your dev data** with `--db-filename` option
- ✅ **Use separate namespaces** per project
- ✅ **Maintain environment configs** via `.temporal/config.yaml`

---

# Startup Script Example

```bash
#!/bin/bash
# start-temporal.sh

echo "🚀 Starting Temporal development server..."
temporal server start-dev \
  --port 7233 \
  --http-port 8233 \
  --db-filename ./temporal-dev.db \
  --namespace default

echo "✅ Temporal started!"
echo "📊 Web UI: http://localhost:8233"
echo "🔌 gRPC API: localhost:7233"
```

---

# 🛠️ Monitoring & Debugging Tips

## **Health Checks:**

```bash
# Check server health
🔥 Temporal server by command runs on gRPC, not HTTP
❌ curl http://localhost:7233/health
❌ curl: (1) Received HTTP/0.9 when not allowed

tctl cluster health 

install ctl: https://docs.temporal.io/tctl-v1#install

# Check Web UI health
curl http://localhost:8233/health


```

## **Additional Tips:**
- ✅ Enable debug logging for verbose output
- ✅ Inspect SQLite DB directly if needed
- ✅ Use Web UI for visual debugging

---

# ✅ Development Tips & Common Pitfalls

## **Best Practices:**
- ✅ **Always start Temporal server** before your app
- ✅ **Keep server and Web UI ports straight**: 7233 and 8233
- ✅ **Shutdown gracefully** to avoid DB corruption
- ✅ **Backup dev DB regularly**

## **Common Issues:**
- ❌ Starting app before server
- ❌ Port conflicts
- ❌ Corrupted database files

---

# 🔍 Troubleshooting Guide

## **Port Conflicts:**

```bash
# Check if ports are in use
lsof -i :7233
lsof -i :8233
```

## **Database Issues:**
- Remove corrupted DB file and restart if needed
- Backup important dev data regularly

## **Network Issues:**
- Verify connectivity with `curl` or `telnet`

---

# Quick Troubleshooting Commands

```bash
# Kill processes on Temporal ports
lsof -ti:7233 | xargs kill -9
lsof -ti:8233 | xargs kill -9

# Clean start
temporal server start-dev
```

**When in doubt, clean slate!**

---

# 💡 Key Takeaways

## **What You've Learned:**
- ✅ Temporal server architecture and components
- ✅ Development vs production setup differences
- ✅ Essential CLI commands
- ✅ Web UI capabilities
- ✅ Local development best practices
- ✅ Troubleshooting techniques

---

# 🚀 Next Steps

**Your Temporal server is ready!**
