### 🧨 Temporal Workflow Kotlin Bootcamp (16 Lessons) — Full Cursor Prompt

```
🎯 Objective:
Create a complete, beginner-friendly, modular learning course to teach Temporal Workflow using Kotlin and Spring Boot. The course should walk learners through Temporal concepts step-by-step in **16 manageable lessons**, from absolute basics to production-level practices.

📁 Project Structure:
Organize all materials under `/src`. Each lesson is broken into three parts:
- `/workshop/lesson_x/` → Starter code needed for the student to begin the workshop  
- `/answer/lesson_x/` → Full solution based on the workshop instructions  
- `/modules/lesson_x/`
    - `workshop_x.md` → Hands-on, step-by-step build guide
    - `concept.md` → Explanation of concepts, theory, and best practices

🗂️ Final Layout:
```

/src/ /workshop/ /lesson\_1/ - (Empty or partial Kotlin/Spring files to be filled in) /answer/ /lesson\_1/ - Full, runnable Kotlin code for the lesson /modules/ /lesson\_1/ - workshop\_1.md - concept.md

```

🧠 Curriculum — 16 Lesson Breakdown:
Design the learning flow based on increasing complexity. Each lesson builds on the last but remains beginner-friendly.

1. Intro to Temporal: What it is, why it exists
2. Set up Kotlin + Spring Boot + Temporal SDK
3. Running a local Temporal server with CLI
4. Your first simple workflow (HelloWorkflow)
5. Adding a simple activity
6. Splitting workflow and activity logic
7. Workflow input/output
8. Retry logic and timeout for activities
9. Error handling in workflows
10. Signals: Interacting with running workflows
11. Queries: Fetching workflow state
12. Child workflows and `continueAsNew`
13. Versioning with `Workflow.getVersion()`
14. Timers and cron workflows
15. Connecting to external services via activities (e.g., DB or APIs)
16. Testing workflows, scaling workers, and production readiness

📝 Markdown File Templates (inside `/modules/lesson_x/`):

🔹 `workshop_x.md`
```

## What we want to build

Briefly describe the goal of this lesson in plain language.

## Expecting Result

What should happen if the student completes this successfully?

## Code Steps

Guide students step-by-step:

- Describe what file to create or modify
- Insert code snippets incrementally
- Explain each block clearly, in order
- Avoid giving the full answer — that's what `/answer/lesson_x/` is for

## How to Run

Show how to run the module (via IDE or CLI), what to expect in output.

```

🔹 `concept.md`
```

## Objective

What this lesson teaches at a high level.

## Key Concepts

New Temporal concepts introduced (e.g., WorkflowInterface, RetryOptions).

## Best Practices

Any edge cases, anti-patterns to avoid, or pro tips for implementation.

```

📌 Development Guidelines:
- Kotlin idioms, use coroutines where appropriate
- Use Gradle Kotlin DSL
- No unnecessary complexity
- Keep each `/workshop/lesson_x/` minimal — just starter scaffolding
- Full working code should only live in `/answer/lesson_x/`
- Docs must be written in clear, conversational tone — easy for beginners
- Lessons should be runnable individually
- Use the latest compatible Temporal Java SDK (with Kotlin)

🎓 Target Audience:
Developers familiar with Kotlin and Spring Boot, but completely new to Temporal.

🏁 Final Output:
A clear, modular, hands-on Temporal Bootcamp for Kotlin developers — ideal for self-learners or instructor-led workshops. Students can follow the `workshop_x.md`, write code in `/workshop`, compare with `/answer`, and read theory in `concept.md`.

BONUS: Add a top-level `README.md` summarizing the course, setup instructions, lesson index, and contribution guide.
```

