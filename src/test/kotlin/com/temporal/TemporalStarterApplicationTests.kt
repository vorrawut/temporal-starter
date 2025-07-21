package com.temporal

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import io.temporal.client.WorkflowClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TemporalStarterApplicationTests {

	@MockBean
	private lateinit var workflowClient: WorkflowClient

	@Test
	fun contextLoads() {
		// Test that Spring context loads successfully with mocked dependencies
	}
}
