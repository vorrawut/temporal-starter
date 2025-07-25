Generate **Lesson 17** of a Temporal Workflow Bootcamp using Kotlin and Spring Boot. This final lesson focuses on **deployment**, specifically running Temporal in production-like environments using Docker, Docker Compose, and Kubernetes/Helm.

🧱 Output structure (under `/class/`):

- `/class/workshop/lesson_17/` → Starter files (e.g., `docker-compose.yaml`, Helm chart scaffold, Kubernetes manifests — partial)
- `/class/answer/lesson_17/` → Complete solution with:
    - `docker-compose.yaml`  
    - Dockerfile for Spring Boot app  
    - Sample Helm chart or K8s manifests  
- `/class/modules/lesson_17/`:
    - `workshop_17.md`
    - `concept.md`

📝 `workshop_17.md` should include:
## What we want to build  
Explain that this lesson helps deploy the Temporal stack and student app locally or in the cloud using container tooling.

## Expecting Result  
By the end, users should be able to spin up a local or remote deployment of the Temporal server + Kotlin worker app.

## Code Steps  
Step-by-step instructions:
- Write a `Dockerfile` for Kotlin Spring Boot app  
- Create `docker-compose.yaml` for Temporal + app  
- Optional: Scaffold Helm chart or basic K8s YAMLs  
- Configure connection details in the app (point to containerized Temporal)  
- Build and run the app inside Docker  
- Show logs to validate worker registration and execution

## How to Run  
- `docker compose up` steps  
- Helm chart/kubectl apply examples  
- Port forwarding if needed  
- What logs to watch for success  

📘 `concept.md` should include:
## Objective  
Teach how to deploy Temporal and your app for real-world environments.

## Key Concepts  
- Docker basics for Spring apps  
- Docker Compose for local orchestration  
- Helm vs plain YAML in Kubernetes  
- Container networking with Temporal  

## Best Practices  
- Separate Temporal infra from app container  
- Use environment variables for config  
- Externalize secrets (don’t hardcode!)  
- Consider using Temporal Cloud or managed DBs for scale  

📌 Instructions:
- Do NOT rely on Temporal CLI — this should simulate production
- No need to implement TemporalBootcampApplication
- Use `temporalio/auto-setup` for Docker Compose
- Support local development via `.env` if needed

🎯 Goal:
This lesson gives students the foundational skills to deploy and run Temporal in real environments — making them ready for real-world apps.

