package com.temporal.bootcamp.lesson4.runner

// TODO: Add necessary imports for:
// - HelloWorkflow interface
// - WorkflowClient, WorkflowOptions
// - Spring @Component and CommandLineRunner

/**
 * TODO: Create a workflow runner
 * 
 * Requirements:
 * 1. Implement CommandLineRunner
 * 2. Inject WorkflowClient via constructor
 * 3. Annotate with @Component
 * 4. In the run method:
 *    - Create a workflow stub for HelloWorkflow
 *    - Set the task queue to "lesson4-hello-queue"
 *    - Set a unique workflow ID
 *    - Execute the workflow with a name (e.g., "Temporal Learner")
 *    - Print the result
 */

// TODO: Implement HelloWorkflowRunner class here 