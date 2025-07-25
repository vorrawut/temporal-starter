---
marp: true
theme: default
paginate: true
title: Concept 3: Run Temporal Locally
---

# Concept 3
## Run Temporal Locally

🎯 **Objective:**  
Understand Temporal server architecture, run Temporal locally for development, and explore monitoring and debugging tools.

---

## 🏗️ Temporal Server Architecture Overview

```text
┌─────────────────────────────────────────────────────────────┐
│                       Temporal Server                       │
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ Frontend    │  │ History     │  │ Matching            │  │
│  │ Service     │  │ Service     │  │ Service             │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ Worker      │  │ Internal    │  │ Database            │  │
│  │ Service     │  │ Frontend    │  │ (SQLite/MySQL/Postgres)│
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                               │
                               ▼
                    ┌─────────────────┐
                    │    Web UI       │
                    │  (Port 8233)    │
                    └─────────────────┘
```

### Core Services at a Glance

- **Frontend Service (Port 7233):** Main API endpoint for clients and workers. Handles routing and authentication.
- **History Service:** Stores workflow event history for replay and recovery.
- **Matching Service:** Routes tasks to workers and manages task queues with load balancing.
- **Worker Service:** Runs internal Temporal system workflows.
- **Database:** Persistent storage layer (SQLite for dev, MySQL/Postgres for prod).

---

## ⚙️ Development vs Production

### Development Setup

```bash
temporal server start-dev
```

- Single-node server with embedded SQLite DB
- Web UI available on port 8233
- Zero config, no auth—perfect for local dev and learning

### Production Setup (Example with Docker Compose)

```yaml
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

- Multi-node clustering
- External DB (Postgres, MySQL, Cassandra)
- Authentication, monitoring, high availability, and scaling

---

## 🔧 Temporal CLI Quick Commands

### Server

```bash
temporal server start-dev
temporal server start-dev --port 7234 --http-port 8234
temporal server start-dev --db-filename temporal.db
```

### Namespace Management

```bash
temporal operator namespace list
temporal operator namespace create my-namespace
temporal operator namespace describe default
```

### Workflow Operations

```bash
temporal workflow list
temporal workflow show --workflow-id my-workflow-id
temporal workflow terminate --workflow-id my-workflow-id
```

---

## 🌐 Web UI Highlights

- **Workflows:** Browse executions, filter, retry failures
- **Task Queues:** Monitor worker health, queue backlog, partitions
- **Workers:** See registered workers and their capabilities
- **Schedules:** Manage recurring workflows and cron schedules

---

## 🛠️ Best Practices for Local Development

- Use startup scripts or aliases for easy server launch
- Persist your dev data with `--db-filename` option
- Use separate namespaces per project
- Maintain environment configs via `.temporal/config.yaml`

---

## 🛠️ Monitoring & Debugging Tips

- Check health endpoints:  
  `curl http://localhost:7233/health`  
  `curl http://localhost:8233/health`
- Enable debug logging for verbose output
- Inspect SQLite DB directly if needed

---

## ✅ Tips & Common Pitfalls

- Always start Temporal server before your app
- Keep server and Web UI ports straight: 7233 and 8233
- Shutdown gracefully to avoid DB corruption
- Backup dev DB regularly

---

## 🔍 Troubleshooting

- Check port conflicts:  
  `lsof -i :7233`  
  `lsof -i :8233`
- Remove corrupted DB file and restart if needed
- Verify network connectivity with `curl` or `telnet`

---

# 🚀 Next Steps
Ready to create your first workflow and run it locally? Let’s dive into Lesson 4!
