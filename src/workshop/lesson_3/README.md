# Workshop 3 - Run Temporal Locally

This lesson focuses on running Temporal server locally and testing the connection with our Spring Boot application from Lesson 2.

## What's Provided

Since this lesson is primarily about running external tools and testing connections, there's no new code to write. Instead, you'll:

1. Install and run Temporal CLI
2. Start a local Temporal server  
3. Test the connection with your Lesson 2 application
4. Explore the Temporal Web UI

## Prerequisites

Make sure you've completed Lesson 2 and have a working Spring Boot application with Temporal configuration.

## Next Steps

Follow the instructions in `../modules/lesson_3/workshop_3.md` to:

1. Install Temporal CLI
2. Start Temporal server
3. Run your application and see the successful connection
4. Explore the Temporal Web UI

## Testing Your Setup

After completing the workshop, you should be able to:

- Run `temporal server start-dev` successfully
- See the Temporal Web UI at http://localhost:8233
- Run your Spring Boot app and see "✅ Temporal worker started successfully!"
- View your worker in the Temporal Web UI 