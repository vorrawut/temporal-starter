Generate the first 4 lessons of a beginner-friendly Temporal Workflow bootcamp using Kotlin and Spring Boot. 
Follow this structure for each lesson by reference and use the guideline from @main_prompt.md.

🧱 For Each Lesson (lesson_1 to lesson_4):
Create 3 folders under `/class/`:
- `/workshop/lesson_x/` → Starter Kotlin + Spring Boot code scaffold
- `/answer/lesson_x/` → Full working solution
- `/modules/lesson_x/` → Contains 2 Markdown files:
    - `workshop_x.md`
    - `concept.md`

📝 For `workshop_x.md`:
Use this format:
## What we want to build  
Describe what this lesson helps the student build.

## Expecting Result  
Clearly describe the output of the finished code.

## Code Steps  
Step-by-step instructions to build the code manually:
- Mention file names
- Add code in blocks with explanations
- Be progressive and incremental
- Do NOT include final code here

## How to Run  
Explain how to run this specific lesson.

🧠 For `concept.md`:
Use this format:
## Objective  
What this lesson aims to teach.

## Key Concepts  
List new Temporal ideas introduced.

## Best Practices  
Mention any common pitfalls, tips, or rules to follow.

📚 Lesson Goals:

**Lesson 1 – Intro to Temporal**  
- What is Temporal? Why is it useful for distributed systems?
- No coding yet — just high-level understanding

**Lesson 2 – Kotlin + Spring Boot + Temporal Setup**  
- Create a minimal Spring Boot app in Kotlin  
- Add Temporal SDK as a dependency  
- Scaffold the initial project and show folder structure

**Lesson 3 – Run Temporal Locally**  
- Teach how to run the Temporal server using Docker  
- Use `temporalio docker` or `temporalite`  
- Run the Spring Boot app and connect to local Temporal  
- Confirm worker is running

**Lesson 4 – HelloWorkflow**  
- Create the first simple workflow and activity  
- Log output when workflow is complete  
- Run workflow manually from a CLI runner or simple main class

💡 Notes:
- Keep code beginner-friendly
- Use Kotlin idioms
- Use Gradle Kotlin DSL
- Avoid complexity
- Use coroutines where possible
- Output should match the structure defined in `prompt.md`

🎯 Goal:
Scaffold Lessons 1 to 4 under `/src/` in the proper format and structure. Lessons should be incremental and easy to follow.
