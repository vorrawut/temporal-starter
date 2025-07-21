package com.temporal.workshop.lesson_4.config

import org.springframework.context.annotation.Configuration

// TODO: Add all necessary imports

/**
 * TODO: Complete the Temporal configuration
 * 
 * You can copy most of this from Lesson 2, but you'll need to:
 * 1. Register your HelloWorkflow implementation with the worker
 * 2. Register your GreetingActivity implementation with the worker
 * 3. Use a different task queue name (e.g., "lesson4-hello-queue")
 */

@Configuration
class TemporalConfig {
    
    // TODO: Add logger and workerFactory properties
    
    // TODO: Add WorkflowServiceStubs bean
    
    // TODO: Add WorkflowClient bean
    
    // TODO: Add WorkerFactory bean
    
    // TODO: Add GreetingActivity bean
    
    // TODO: Add startWorker method that:
    // - Creates a worker with task queue "lesson4-hello-queue"
    // - Registers HelloWorkflowImpl
    // - Registers GreetingActivity implementation
    // - Starts the worker factory
    
    // TODO: Add shutdown method
    
} 