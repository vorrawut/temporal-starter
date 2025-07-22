package com.temporal

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import io.temporal.client.WorkflowClient
import io.mockk.mockk

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["temporal.enabled=false"]
)
@ActiveProfiles("test")
class TemporalStarterApplicationTests {

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun testWorkflowClient(): WorkflowClient = mockk<WorkflowClient>(relaxed = true)
    }

    @Test
    fun contextLoads() {
        // This test validates that the Spring context can load successfully
        // with all our configurations
    }
}
