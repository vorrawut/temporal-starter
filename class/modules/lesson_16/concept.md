# Lesson 16 Concepts: Testing + Production Readiness

## Objective

Master comprehensive testing strategies for Temporal workflows and learn production deployment patterns including worker configuration, scalability, monitoring, and operational best practices. This lesson prepares you to deploy and maintain Temporal workflows in real-world production environments.

## Key Concepts

### 1. **Testing Strategies**

#### **Unit Testing with TestWorkflowRule**
- **Isolated Testing**: Test workflow logic without external dependencies
- **Deterministic Execution**: TestWorkflowRule provides deterministic time and execution
- **Mock Activities**: Replace real activities with controllable mock implementations
- **Fast Feedback**: Unit tests run quickly and provide immediate feedback

#### **Integration Testing**
- **Real Temporal Server**: Test against actual Temporal cluster
- **External Service Mocking**: Mock external APIs while using real Temporal infrastructure
- **End-to-End Validation**: Verify complete workflow execution paths
- **Performance Testing**: Measure workflow execution times and resource usage

#### **Load Testing**
- **Concurrent Execution**: Test multiple workflow instances simultaneously
- **Worker Capacity**: Validate worker scaling under load
- **Resource Utilization**: Monitor memory, CPU, and network usage
- **Failure Recovery**: Test system behavior under failure conditions

### 2. **Production Worker Configuration**

#### **Worker Scaling**
```kotlin
val workerOptions = WorkerOptions.newBuilder()
    .setMaxConcurrentActivityExecutions(10)      // Activities per worker
    .setMaxConcurrentWorkflowExecutions(5)       // Workflows per worker
    .setMaxConcurrentLocalActivityExecutions(10) // Local activities per worker
    .build()
```

#### **Task Queue Design**
- **Domain Separation**: Separate task queues for different business domains
- **Priority Handling**: Different queues for high/low priority workflows
- **Resource Isolation**: Isolate resource-intensive workflows
- **Geographic Distribution**: Regional task queues for latency optimization

### 3. **Activity Configuration Patterns**

#### **Timeout Strategies**
- **StartToCloseTimeout**: Maximum time for single activity execution
- **ScheduleToCloseTimeout**: Total time including queuing and retries
- **ScheduleToStartTimeout**: Maximum time in queue before execution
- **HeartbeatTimeout**: For long-running activities with progress reporting

#### **Retry Policies**
```kotlin
RetryOptions.newBuilder()
    .setInitialInterval(Duration.ofSeconds(1))     // First retry delay
    .setMaximumInterval(Duration.ofMinutes(1))     // Maximum retry delay
    .setBackoffCoefficient(2.0)                    // Exponential backoff
    .setMaximumAttempts(5)                         // Total retry attempts
    .setDoNotRetry("NonRetryableException")        // Non-retriable errors
    .build()
```

### 4. **Error Handling and Compensation**

#### **Saga Pattern Implementation**
- **Compensation Activities**: Reverse operations when failures occur
- **Transaction Boundaries**: Define clear rollback points
- **Idempotent Operations**: Ensure activities can be safely retried
- **Error Classification**: Distinguish between retriable and non-retriable failures

#### **Circuit Breaker Pattern**
```kotlin
// In workflow implementation
if (consecutiveFailures > threshold) {
    Workflow.sleep(circuitBreakerDelay)
    // Implement fallback logic or graceful degradation
}
```

### 5. **Monitoring and Observability**

#### **Metrics Collection**
- **Workflow Metrics**: Execution time, success/failure rates
- **Activity Metrics**: Per-activity performance and error rates
- **Worker Metrics**: CPU, memory, and task queue utilization
- **Business Metrics**: Domain-specific KPIs and SLAs

#### **Logging Best Practices**
```kotlin
// Structured logging in activities
logger.info("Processing payment", mapOf(
    "orderId" to orderId,
    "amount" to amount,
    "paymentMethod" to paymentMethod.type,
    "correlationId" to correlationId
))
```

### 6. **Security Considerations**

#### **Credential Management**
- **Environment Variables**: Store secrets outside application code
- **Vault Integration**: Use secret management systems
- **Rotation Policies**: Implement automatic credential rotation
- **Encryption**: Encrypt sensitive data in workflow history

#### **Network Security**
- **TLS Encryption**: Secure communication between workers and Temporal
- **Authentication**: Use proper authentication mechanisms
- **Authorization**: Implement role-based access controls
- **Network Isolation**: Deploy workers in secure network segments

## Best Practices

### 1. **Testing Patterns**

#### **Comprehensive Test Coverage**
```kotlin
class WorkflowTest {
    @Test
    fun `test successful order processing`() {
        val request = createValidOrderRequest()
        val result = workflow.processOrder(request)
        
        assertEquals(OrderStatus.SHIPPED, result.status)
        assertTrue(result.errors.isEmpty())
    }
    
    @Test
    fun `test validation failure handling`() {
        val request = createInvalidOrderRequest()
        val result = workflow.processOrder(request)
        
        assertEquals(OrderStatus.FAILED, result.status)
        assertFalse(result.validationResult.isValid)
    }
    
    @Test
    fun `test compensation logic`() {
        // Test rollback when payment fails after inventory reservation
        val mockPaymentActivity = mockFailingPaymentActivity()
        val result = workflow.processOrder(validRequest)
        
        verify(inventoryActivity).releaseInventory(any())
    }
}
```

#### **Mock Activity Design**
```kotlin
// ✅ Good: Configurable mock behavior
class ConfigurableMockPaymentActivity : PaymentActivity {
    var shouldFail = false
    var failureMessage = "Payment gateway unavailable"
    
    override fun processPayment(request: PaymentRequest): PaymentResult {
        if (shouldFail) {
            throw RuntimeException(failureMessage)
        }
        return PaymentResult(/* success data */)
    }
}

// ❌ Bad: Hardcoded mock behavior
class HardcodedMockPaymentActivity : PaymentActivity {
    override fun processPayment(request: PaymentRequest): PaymentResult {
        // Always returns the same result
        return PaymentResult(processed = true, transactionId = "MOCK_TXN")
    }
}
```

### 2. **Production Configuration**

#### **Environment-Specific Settings**
```kotlin
@Configuration
class TemporalConfig {
    
    @Value("\${temporal.service.target}")
    private lateinit var temporalServiceTarget: String
    
    @Value("\${temporal.namespace}")
    private lateinit var namespace: String
    
    @Bean
    fun workflowServiceStubs(): WorkflowServiceStubs {
        return WorkflowServiceStubs.newServiceStubs(
            WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalServiceTarget)
                .setEnableHttps(true)
                .build()
        )
    }
    
    @Bean
    fun workflowClient(serviceStubs: WorkflowServiceStubs): WorkflowClient {
        return WorkflowClient.newInstance(serviceStubs, 
            WorkflowClientOptions.newBuilder()
                .setNamespace(namespace)
                .build()
        )
    }
}
```

#### **Worker Lifecycle Management**
```kotlin
@Component
class WorkerManager {
    
    @Autowired
    private lateinit var workflowClient: WorkflowClient
    
    private val workers = mutableListOf<Worker>()
    
    @PostConstruct
    fun startWorkers() {
        // Create workers for different task queues
        val orderWorker = createOrderProcessingWorker()
        val notificationWorker = createNotificationWorker()
        
        workers.addAll(listOf(orderWorker, notificationWorker))
        workers.forEach { it.start() }
    }
    
    @PreDestroy
    fun stopWorkers() {
        workers.forEach { it.shutdown() }
    }
}
```

### 3. **Performance Optimization**

#### **Worker Tuning**
```kotlin
// High-throughput configuration
val highThroughputOptions = WorkerOptions.newBuilder()
    .setMaxConcurrentActivityExecutions(50)
    .setMaxConcurrentWorkflowExecutions(20)
    .setLocalActivityWorkerOnly(false)
    .build()

// Memory-constrained configuration
val memoryOptimizedOptions = WorkerOptions.newBuilder()
    .setMaxConcurrentActivityExecutions(5)
    .setMaxConcurrentWorkflowExecutions(2)
    .setMaxWorkflowThreads(100)
    .build()
```

#### **Activity Optimization**
```kotlin
// ✅ Good: Efficient batch processing
override fun processBulkNotifications(requests: List<NotificationRequest>): List<NotificationResult> {
    return requests.chunked(100) { batch ->
        // Process in batches to optimize throughput
        externalNotificationService.sendBatch(batch)
    }.flatten()
}

// ❌ Bad: Inefficient individual processing
override fun processNotifications(requests: List<NotificationRequest>): List<NotificationResult> {
    return requests.map { request ->
        // Individual API calls are inefficient
        externalNotificationService.send(request)
    }
}
```

### 4. **Deployment Strategies**

#### **Blue-Green Deployment**
```kotlin
// Version-aware worker deployment
@Component
class VersionedWorkerManager {
    
    fun deployNewVersion(version: String, taskQueue: String) {
        // Start new version workers
        val newWorkers = createWorkersForVersion(version, taskQueue)
        newWorkers.forEach { it.start() }
        
        // Gradually drain old version workers
        gracefullyShutdownOldWorkers()
    }
}
```

#### **Canary Deployment**
```kotlin
// Route percentage of traffic to new version
@Service
class WorkflowRouter {
    
    fun routeWorkflow(request: WorkflowRequest): String {
        return if (shouldRouteToCanary(request)) {
            "canary-task-queue"
        } else {
            "production-task-queue"
        }
    }
}
```

### 5. **Operational Excellence**

#### **Health Checks**
```kotlin
@RestController
class HealthController {
    
    @Autowired
    private lateinit var workflowClient: WorkflowClient
    
    @GetMapping("/health/temporal")
    fun temporalHealth(): ResponseEntity<Map<String, Any>> {
        return try {
            workflowClient.workflowService.getSystemInfo()
            ResponseEntity.ok(mapOf("status" to "healthy"))
        } catch (e: Exception) {
            ResponseEntity.status(503)
                .body(mapOf("status" to "unhealthy", "error" to e.message))
        }
    }
}
```

#### **Alerting and Monitoring**
```kotlin
// Custom metrics for monitoring
@Component
class WorkflowMetrics {
    
    private val workflowCounter = Counter.builder("workflows_started_total")
        .description("Total workflows started")
        .tag("workflow_type", "order_processing")
        .register(meterRegistry)
    
    private val workflowTimer = Timer.builder("workflow_duration_seconds")
        .description("Workflow execution duration")
        .register(meterRegistry)
    
    fun recordWorkflowStart() {
        workflowCounter.increment()
    }
    
    fun recordWorkflowCompletion(duration: Duration) {
        workflowTimer.record(duration)
    }
}
```

### 6. **Common Production Issues**

#### **Memory Management**
- **History Size**: Monitor workflow history size and use `continueAsNew` appropriately
- **Worker Memory**: Tune JVM heap size based on concurrent executions
- **Activity Payloads**: Keep activity inputs/outputs reasonably sized

#### **Performance Bottlenecks**
- **Task Queue Latency**: Monitor task queue lag and worker capacity
- **Database Connections**: Pool and manage database connections in activities
- **External Service Latency**: Implement proper timeouts and circuit breakers

#### **Deployment Challenges**
- **Version Compatibility**: Ensure backward compatibility during deployments
- **State Migration**: Handle data model changes carefully
- **Configuration Management**: Use proper configuration management tools

#### **Troubleshooting Guide**
```kotlin
// Workflow execution debugging
@QueryMethod
fun getExecutionStatus(): Map<String, Any> {
    return mapOf(
        "currentStep" to currentStep,
        "errors" to errors,
        "executionTime" to executionTimeMillis,
        "lastActivityResult" to lastActivityResult
    )
}
``` 