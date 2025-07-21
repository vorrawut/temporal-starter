# Workshop 3: Run Temporal Locally

## What we want to build

In this lesson, we'll get Temporal server running on your local machine and connect our Spring Boot application to it. Think of this as starting up the engine that will power all our workflows.

## Expecting Result

By the end of this lesson, you'll have:

- Temporal CLI installed and working
- Temporal server running locally on your machine
- Your Spring Boot application successfully connecting to Temporal
- Access to the Temporal Web UI where you can monitor workflows
- Console output showing "✅ Temporal worker started successfully!"

## Code Steps

This lesson doesn't require writing new code, but rather setting up the Temporal infrastructure and testing our existing code.

### Step 1: Install Temporal CLI

The Temporal CLI provides tools to run the server and manage workflows.

#### For macOS (using Homebrew):
```bash
brew install temporal
```

#### For Linux/WSL:
```bash
curl -sSf https://temporal.download/cli.sh | sh
```

#### For Windows:
Download the latest release from: https://github.com/temporalio/cli/releases

#### Verify Installation:
```bash
temporal --version
```

You should see something like:
```
temporal version 0.10.0
```

### Step 2: Start Temporal Server

The `temporal server start-dev` command starts a complete Temporal environment suitable for development.

```bash
temporal server start-dev
```

You should see output like:
```
temporal server start-dev
Starting Temporal Server...

Server:  localhost:7233
UI:      http://localhost:8233
Metrics: http://localhost:7234/metrics

Started Temporal Server.
```

**Keep this terminal window open** - the server runs in the foreground.

#### What `start-dev` Provides:
- Temporal server (orchestration engine)
- Web UI for monitoring workflows
- SQLite database for persistence
- Default namespace configuration

### Step 3: Verify Temporal Web UI

Open your browser and go to: **http://localhost:8233**

You should see the Temporal Web UI with:
- Navigation sidebar
- "No workflows found" message (this is normal - we haven't run any yet)
- Various tabs: Workflows, Schedules, Task Queues, etc.

### Step 4: Test Your Spring Boot Connection

Now let's see if your Lesson 2 application can connect to Temporal!

#### Option 1: Run from IDE
1. Open your Lesson 2 project in your IDE
2. Run `TemporalBootcampApplication.kt`
3. Watch the console output

#### Option 2: Run from Command Line
```bash
./gradlew bootRun --args="--spring.main.sources=com.temporal.workshop.lesson_2.TemporalBootcampApplication"
```

### Step 5: Verify Successful Connection

You should see console output like:
```
Creating Temporal service stubs for local server
Creating Temporal workflow client
Creating Temporal worker factory
Starting Temporal worker...
✅ Temporal worker started successfully! Connected to local Temporal server.
```

If you see errors like "Connection refused", make sure Temporal server is still running in the other terminal.

### Step 6: Check Workers in Web UI

1. Go back to the Temporal Web UI: http://localhost:8233
2. Click on **"Workers"** in the left sidebar
3. You should see your worker listed:
   - **Task Queue**: `lesson2-test-queue`
   - **Identity**: Your computer's hostname + some ID
   - **Status**: Should be green/healthy

### Step 7: Explore the Web UI

Take a few minutes to explore the different sections:

#### **Workflows Tab**
- Currently empty (we haven't created workflows yet)
- This is where you'll see workflow executions

#### **Task Queues Tab**  
- Should show `lesson2-test-queue` with 1 worker
- Shows worker health and activity

#### **Namespaces Tab**
- Shows the `default` namespace we're using
- In production, you might have separate namespaces for different environments

#### **Settings**
- Configuration options for the Web UI
- Data encoding settings

## How to Run

### Daily Development Workflow

For future lessons, this will be your typical workflow:

1. **Start Temporal** (in one terminal):
   ```bash
   temporal server start-dev
   ```

2. **Start your application** (in another terminal):
   ```bash
   ./gradlew bootRun
   ```

3. **Open Web UI** in browser: http://localhost:8233

### Stopping Everything

To stop cleanly:
1. Stop your Spring Boot application (Ctrl+C or stop in IDE)
2. Stop Temporal server (Ctrl+C in the terminal where it's running)

## Troubleshooting

### "Command 'temporal' not found"
- Make sure you installed the CLI correctly
- Try restarting your terminal
- On macOS, try `brew install temporal` again

### "Port 7233 already in use"
- You might have another Temporal server running
- Kill any existing processes: `pkill temporal`
- Try starting again

### "Connection refused" in Spring Boot
- Make sure Temporal server is running (`temporal server start-dev`)
- Check that you see "Started Temporal Server" in the server output
- Verify the server is on localhost:7233

### Spring Boot can't connect but server is running
- Check your `TemporalConfig.kt` configuration
- Make sure you're using `newLocalServiceStubs()` for local development
- Verify all imports are correct

### Web UI doesn't load
- Make sure you're going to http://localhost:8233 (not 7233)
- Try refreshing the page
- Check that no other service is using port 8233

## What You've Accomplished

- ✅ Installed Temporal CLI for managing Temporal environments
- ✅ Started a local Temporal server for development
- ✅ Connected your Spring Boot application to Temporal successfully  
- ✅ Verified the connection using the Temporal Web UI
- ✅ Explored the monitoring and management interface
- ✅ Set up a complete development environment for Temporal workflows

You now have a fully functional Temporal development environment! In Lesson 4, we'll create our first actual workflow and see it running in this setup. 