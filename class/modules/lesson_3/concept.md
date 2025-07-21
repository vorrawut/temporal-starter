# Concept 3: Run Temporal Locally

## Objective

Understand Temporal's server architecture, learn how to run Temporal locally for development, and explore the tools available for monitoring and debugging workflows.

## Key Concepts

### 1. **Temporal Server Architecture**

Temporal server consists of several key components working together:

```
┌─────────────────────────────────────────────────────────────┐
│                    Temporal Server                          │
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │   Frontend  │  │   History   │  │      Matching       │ │
│  │   Service   │  │   Service   │  │       Service       │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │   Worker    │  │  Internal   │  │      Database       │ │
│  │   Service   │  │  Frontend   │  │   (SQLite/MySQL/    │ │
│  │             │  │             │  │   PostgreSQL)       │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                               │
                               ▼
                    ┌─────────────────┐
                    │    Web UI       │
                    │ (Port 8233)     │
                    └─────────────────┘
```

#### **Core Services**

**Frontend Service** (Port 7233)
- **Purpose**: Main API endpoint for workflow clients and workers
- **Responsibilities**: Authentication, request routing, rate limiting
- **Client Connection**: This is where your Spring Boot app connects

**History Service**
- **Purpose**: Stores and manages workflow execution history
- **Responsibilities**: Event persistence, workflow state management
- **Critical Role**: Enables workflow replay and recovery

**Matching Service**
- **Purpose**: Routes workflow tasks to available workers
- **Responsibilities**: Task queue management, load balancing
- **Smart Routing**: Ensures tasks go to appropriate workers

**Worker Service**
- **Purpose**: Internal worker for system workflows
- **Responsibilities**: System maintenance, cleanup tasks
- **Background**: Handles Temporal's own internal processes

### 2. **Development vs Production Setup**

#### **Development Setup (`temporal server start-dev`)**
```bash
temporal server start-dev
```

**What it provides:**
- Single-node Temporal server
- Built-in SQLite database
- Web UI on port 8233
- Default namespace configuration
- No authentication required
- Perfect for learning and local development

**Pros:**
- ✅ Zero configuration required
- ✅ Fast startup
- ✅ No external dependencies
- ✅ Built-in Web UI

**Cons:**
- ❌ Not suitable for production
- ❌ Limited scalability
- ❌ No high availability
- ❌ Data stored locally only

#### **Production Setup**
```yaml
# docker-compose.yml example
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

**Production features:**
- Multi-node clustering
- External database (PostgreSQL, MySQL, Cassandra)
- Authentication and authorization
- Metrics and monitoring
- High availability
- Horizontal scaling

### 3. **Temporal CLI Commands**

#### **Server Management**
```bash
# Start development server
temporal server start-dev

# Start with custom ports
temporal server start-dev --port 7234 --http-port 8234

# Start with specific database
temporal server start-dev --db-filename temporal.db
```

#### **Namespace Management**
```bash
# List namespaces
temporal operator namespace list

# Create namespace
temporal operator namespace create my-namespace

# Describe namespace
temporal operator namespace describe default
```

#### **Workflow Operations**
```bash
# List workflows
temporal workflow list

# Show workflow details
temporal workflow show --workflow-id my-workflow-id

# Terminate workflow
temporal workflow terminate --workflow-id my-workflow-id
```

### 4. **Temporal Web UI Features**

#### **Workflows Section**
- **Execution List**: All workflow executions with status
- **Filters**: Search by workflow type, status, time range
- **Details View**: Complete execution history and timeline
- **Retry**: Ability to retry failed workflows

#### **Task Queues Section**
- **Worker Health**: See which workers are connected
- **Queue Statistics**: Task processing rates and backlogs
- **Partitioning**: View task queue partitions and distribution

#### **Workers Section**
- **Worker Registry**: All connected workers
- **Capabilities**: What workflows/activities each worker can handle
- **Resource Usage**: Worker capacity and current load

#### **Schedules Section**
- **Recurring Workflows**: Cron-like scheduled executions
- **Schedule Management**: Create, pause, and modify schedules
- **Execution History**: Past and upcoming scheduled runs

### 5. **Local Development Best Practices**

#### **Environment Setup**
```bash
# Create a startup script
#!/bin/bash
echo "Starting Temporal development environment..."
temporal server start-dev --log-level info
```

#### **Data Persistence**
```bash
# Use custom database file for persistence
temporal server start-dev --db-filename ./dev-temporal.db

# This preserves your data between restarts
```

#### **Multiple Namespaces**
```bash
# Create separate namespaces for different projects
temporal operator namespace create project-a
temporal operator namespace create project-b
```

#### **Configuration Files**
```yaml
# .temporal/config.yaml
version: 1
contexts:
  local:
    address: localhost:7233
    namespace: default
  staging:
    address: staging.temporal.company.com:7233
    namespace: staging
```

### 6. **Monitoring and Debugging**

#### **Health Checks**
```bash
# Check server health
curl http://localhost:7233/health

# Check web UI health  
curl http://localhost:8233/health
```

#### **Logs and Metrics**
```bash
# Start with debug logging
temporal server start-dev --log-level debug

# Metrics endpoint
curl http://localhost:7234/metrics
```

#### **Database Inspection**
```bash
# For SQLite database
sqlite3 temporal.db ".tables"
sqlite3 temporal.db "SELECT * FROM executions LIMIT 5;"
```

## Best Practices

### ✅ Development Environment

1. **Consistent Startup**
   ```bash
   # Create an alias for easy startup
   alias temporal-dev='temporal server start-dev --log-level info'
   ```

2. **Data Backup**
   ```bash
   # Backup your development database periodically
   cp temporal.db temporal-backup-$(date +%Y%m%d).db
   ```

3. **Port Management**
   ```bash
   # Check what's running on Temporal ports
   lsof -i :7233  # Server port
   lsof -i :8233  # Web UI port
   ```

### ✅ Workflow Development

1. **Use Meaningful Workflow IDs**
   ```kotlin
   WorkflowOptions.newBuilder()
       .setWorkflowId("user-onboarding-${userId}-${timestamp}")
       .build()
   ```

2. **Monitor via Web UI**
   - Always check the Web UI when debugging
   - Use workflow search and filters effectively
   - Review execution timelines for performance issues

3. **Clean Development Data**
   ```bash
   # Reset development environment
   rm temporal.db
   temporal server start-dev
   ```

### ❌ Common Mistakes

1. **Forgetting to Start Server**
   - Always start Temporal server before your application
   - Consider using Docker Compose for complex setups

2. **Wrong Port Configuration**
   - Server: 7233 (for client connections)
   - Web UI: 8233 (for browser access)
   - Don't confuse these ports

3. **Database Corruption**
   - Don't kill the server forcefully
   - Always use Ctrl+C for graceful shutdown

### 🔧 Troubleshooting Guide

#### **Server Won't Start**
```bash
# Check for port conflicts
lsof -i :7233
lsof -i :8233

# Kill conflicting processes
kill $(lsof -t -i :7233)
```

#### **Database Issues**
```bash
# Remove corrupted database
rm temporal.db

# Start fresh
temporal server start-dev
```

#### **Connection Issues**
```bash
# Test server connectivity
curl -f http://localhost:7233/health

# Check firewall settings
telnet localhost 7233
```

---

**Ready for workflows?** Now that you have Temporal running locally and understand the development environment, let's create your first workflow in Lesson 4! 