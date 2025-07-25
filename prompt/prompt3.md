Generate Lessons 11 to 16 of a beginner-friendly Temporal Workflow bootcamp using Kotlin and Spring Boot. 
Follow this structure for each lesson by reference and use the guideline from@main_prompt.md.

Place all lesson content under the `/class/` folder.


🧱 For Each Lesson (lesson_11 to lesson_16), generate 3 folders:
- `/class/workshop/lesson_x/` → Kotlin starter scaffold (just enough to start the lesson)
- `/class/answer/lesson_x/` → Complete, runnable solution
- `/class/modules/lesson_x/` → Contains:
    - `workshop_x.md` — Hands-on build guide
    - `concept.md` — Concepts and best practices

📝 `workshop_x.md` should follow this format:
## What we want to build  
Explain the goal of this lesson in simple terms.

## Expecting Result  
Describe the successful outcome of the exercise.

## Code Steps  
Break down the task step-by-step:
- Mention file/class names
- Insert code snippets gradually
- Add explanations alongside each snippet
- Do NOT reveal full solution — that lives in `/answer/lesson_x/`

## How to Run  
Explain how to run only this specific workflow/lesson. Keep it light — no TemporalBootcampApplication or setup duplication.

📘 `concept.md` should include:
## Objective  
Summarize the lesson's purpose.

## Key Concepts  
List Temporal features introduced.

## Best Practices  
Tips, gotchas, and production patterns.

📚 Lesson Focus:

**Lesson 11 – Queries**  
- Implement workflow query handlers  
- Fetch state from running workflows  

**Lesson 12 – Child Workflows & `continueAsNew`**  
- Call child workflows from parent  
- Use `continueAsNew` to avoid history bloat  

**Lesson 13 – Versioning with `Workflow.getVersion()`**  
- Introduce breaking changes safely  
- Use `getVersion()` API to handle migration  

**Lesson 14 – Timers and Cron Workflows**  
- Use `Workflow.sleep()` for delays  
- Define recurring workflows with cron schedules  

**Lesson 15 – External Service Integration**  
- Call an external API or DB via Activity  
- Show how to encapsulate service logic cleanly  

**Lesson 16 – Testing + Production Readiness**  
- Write unit tests for workflows/activities  
- Discuss worker scalability, task queue design, environment setup tips  

📌 General Instructions:
- Kotlin-style, idiomatic coroutine usage  
- Gradle Kotlin DSL  
- Workshop folders must contain only starter code — not complete logic  
- Code must be scoped strictly to each lesson  
- Use latest Temporal Java SDK (Kotlin-compatible)  
- Do NOT include or mention application startup scaffolding (e.g., `TemporalBootcampApplication`)

🎯 Goal:
Deliver Lessons 11–16 as modular learning units in `/class/` — continuing the step-by-step progression from beginner to production-grade Temporal workflows.
