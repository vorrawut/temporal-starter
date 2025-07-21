Generate Lessons 5 to 10 of a beginner-friendly Temporal Workflow bootcamp using Kotlin and Spring Boot. Place everything under the `/class/` folder.
Follow this structure for each lesson by reference and use the guideline from@main_prompt.md.

🧱 For Each Lesson (lesson_5 to lesson_10):
Create 3 folders under `/class/`:
- `/class/workshop/lesson_x/` → Kotlin starter scaffold (empty or partial code for students to build on)
- `/class/answer/lesson_x/` → Full, working solution for the lesson
- `/class/modules/lesson_x/` → Contains:
    - `workshop_x.md` (step-by-step instructions to build the solution)
    - `concept.md` (theory and best practices)

📝 For `workshop_x.md`:
Use this format:
## What we want to build
Clear goal of this module (e.g., retry logic, signals, etc.)

## Expecting Result
What the output should be once the lesson is complete

## Code Steps
Guide students step-by-step:
- Include file names and where to place the code
- Explain each code block clearly
- Progress incrementally
- Do NOT include final code (that belongs in `/answer/`)

## How to Run
Explain how to run the workflow/test for this lesson only (skip any application-level boilerplate or TemporalBootcampApplication references)

🧠 For `concept.md`:
Use this format:
## Objective
Describe the purpose of this lesson.

## Key Concepts
List key Temporal features introduced (e.g., retries, signals, queries)

## Best Practices
Tips, edge cases, and guidance for production usage

📚 Lesson Goals:

**Lesson 5 – Adding a Simple Activity**
- Create and call a basic activity from a workflow
- Log the output or return a simple value

**Lesson 6 – Workflow & Activity Separation**
- Refactor workflows and activities into separate files
- Follow clean architecture practices (interfaces, implementation separation)

**Lesson 7 – Workflow Input/Output**
- Accept parameters into workflows
- Return values from workflows

**Lesson 8 – Activity Retry + Timeout**
- Add retry options, timeout settings on activities
- Handle failure gracefully

**Lesson 9 – Error Handling in Workflows**
- Demonstrate try/catch in workflows
- Explain when to fail vs. retry

**Lesson 10 – Signals**
- Send a signal to a long-running workflow
- React to the signal and log or change behavior

📌 Guidelines:
- Kotlin idioms with coroutines
- Use Gradle Kotlin DSL
- No unnecessary boilerplate
- Keep `/workshop/` minimal — just scaffolding
- All runnable code belongs in `/answer/`
- Keep instructions beginner-friendly, clear, and scoped to the lesson
- Do NOT generate or mention any app startup code like `TemporalBootcampApplication`

🎯 Goal:
Scaffold Lessons 5 to 10 under `/class/` in a modular, easy-to-follow format that helps beginners build real understanding of Temporal, one small step at a time.
