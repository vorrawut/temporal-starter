# 🚀 Temporal Workflow Bootcamp

Welcome to the **Temporal Workflow Bootcamp**! This repository is designed to help you learn Temporal through hands-on, structured lessons—from basic concepts to advanced production-ready patterns.

---

## 📦 What’s Inside

The bootcamp is organized into progressive tracks:

### 🔰 Beginner & Foundation (Lessons 1–6)

* Introduction to workflows and activities
* Basic project setup
* Execution and result handling

### 🚀 Intermediate (Lessons 7–12)

* Error handling, retry strategies, signals, queries
* Clean architecture, data modeling, heartbeat timeouts

### 🧠 Advanced (Lessons 13–15)

* Child workflows
* Workflow versioning
* Long-running and cron workflows

### 🧪 Testing (Lesson 16)

* Unit testing and integration testing
* Temporal test environment usage

### 📦 Deployment (Lesson 17)

* Docker and docker-compose setup
* Helm and Kubernetes overview for Temporal

---

## 📁 Project Structure

```bash
.
├── class/                  # Lessons 5–10 (clean architecture examples)
│   ├── modules/            # Readme and concept docs
│   └── workshop/           # Starter templates with TODOs
│       ├── answer/         # Completed code per lesson
├── src/                    # Lessons 1–4 and 11–16
├── deploy/                 # Lesson 17: Docker, Docker Compose, Helm
└── README.md               # This file
```

Each lesson includes:

* `workshop/lesson_x/` - Starter code with TODOs
* `workshop/answer/lesson_x/` - Completed reference solution
* `modules/lesson_x/workshop_x.md` - Guided instructions
* `modules/lesson_x/concept.md` - Concepts, theory, and best practices

---

## 🛠️ Getting Started

### 🔧 Prerequisites

* Java 17+
* Kotlin
* Gradle
* Docker (for running Temporal server locally)

### 🚀 Running Temporal Locally

Start a local Temporal server:

```bash
temporal server start-dev
```

Access the UI at [http://localhost:8233](http://localhost:8233)

### ▶️ Running a Lesson

1. Go to `workshop/lesson_x/`
2. Follow `modules/lesson_x/workshop_x.md`
3. Run the code using your IDE or Gradle
4. Compare with `answer/lesson_x/` if needed
5. Review theory in `concept.md`

---

## 👨‍🏫 For Instructors

* Each module is **self-contained**
* Step-by-step workshop guides help walkthrough the coding
* Concepts explain **why**, not just **how**

---

## 🎯 Learning Outcomes

By the end of this bootcamp, you will be able to:

* Design scalable Temporal workflows
* Understand core patterns: signals, retries, heartbeats, etc.
* Handle errors, compensation, and long-running jobs
* Write tests and prepare for production deployments

---

## 🤝 Contributing & Feedback

Feel free to fork this repo, submit pull requests, or use it to run internal workshops.

If you find issues or have suggestions, please open a GitHub Issue.

---

Happy Learning! 🧠✨

> “Code is like humor. When you have to explain it, it’s bad.” – Cory House
