# 🧪 Comprehensive Testing Guide - Temporal Loan Application Demo

## 📋 Overview

This guide provides a complete testing strategy for the Temporal Loan Application System demo, covering unit tests, integration tests, and end-to-end testing scenarios. It demonstrates how to test all Temporal concepts learned throughout the bootcamp.

---

## 🎯 Testing Strategy

### **Testing Pyramid**

```
                    E2E Tests
                   /         \
                  /    🔍     \
                 /  Manual     \
                /   Testing     \
               /_________________\
              /                   \
             /  Integration Tests  \
            /     🔗 Temporal       \
           /    Workflow Tests       \
          /___________________________\
         /                             \
        /       Unit Tests              \
       /   🧪 Activities &               \
      /     Business Logic                \
     /_____________________________________\
```

### **Coverage Areas**

| Test Level | Coverage | Tools Used |
|------------|----------|------------|
| **Unit Tests** | Individual activities, business logic, validation rules | JUnit 5, Mockito, Kotlin Test |
| **Integration Tests** | Workflow orchestration, activity interaction, signals | Temporal TestWorkflowRule |
| **API Tests** | REST endpoints, request validation, error handling | MockMvc, TestContainers |
| **E2E Tests** | Complete user journeys, cross-service integration | Manual + Automation |

---

## 🛠 Test Setup & Configuration

### **1. Test Dependencies**

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("io.temporal:temporal-testing:1.23.1")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-kotlin:5.1.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

### **2. Test Configuration**

**`src/test/resources/application-test.properties`**
```properties
# Disable Temporal for unit tests
spring.autoconfigure.exclude=com.temporal.config.TemporalConfig
temporal.enabled=false
logging.level.com.temporal=INFO
```

### **3. Mock Configuration**

**`src/test/kotlin/com/temporal/config/TestConfiguration.kt`**
```kotlin
@TestConfiguration
@Profile("test")
class TestConfiguration {
    @Bean @Primary
    fun testWorkflowClient(): WorkflowClient = mock()
    
    @Bean @Primary  
    fun testCreditBureauService(): CreditBureauService = mock()
    
    // ... other mock beans
}
```

---

## 🧪 Unit Testing

### **Activity Testing**

Test individual activities in isolation with mocked dependencies:

```kotlin
class RiskScoringActivityTest {
    @Mock
    private lateinit var creditBureauService: CreditBureauService
    
    @Test
    fun `should calculate low risk for excellent credit`() {
        // Given: Excellent credit response
        `when`(creditBureauService.getCreditScore(any(), any()))
            .thenReturn(CreditBureauResponse(
                creditScore = 800,
                creditHistory = "Excellent",
                // ... other fields
            ))
        
        // When: Calculate risk
        val assessment = riskScoringActivity.calculateRiskScore(application)
        
        // Then: Should be low risk
        assertEquals(RiskLevel.LOW, assessment.riskLevel)
        assertTrue(assessment.riskFactors.isEmpty())
    }
}
```

### **Business Logic Testing**

Test validation rules, calculations, and decision logic:

```kotlin
@Test
fun `should identify missing documents correctly`() {
    val application = LoanApplication(/* minimal docs */)
    val result = documentActivity.validateDocuments(application)
    
    assertFalse(result.isValid)
    assertTrue(result.missingDocuments.contains(DocumentType.INCOME_STATEMENT))
}
```

### **Edge Cases**

```kotlin
@Test
fun `should handle zero annual income`() {
    val application = LoanApplication(annualIncome = BigDecimal.ZERO)
    val assessment = riskScoringActivity.calculateRiskScore(application)
    
    assertEquals(RiskLevel.VERY_HIGH, assessment.riskLevel)
    assertTrue(assessment.riskFactors.contains("No income reported"))
}
```

---

## 🔗 Integration Testing

### **Workflow Testing with TestWorkflowRule**

Test complete workflow orchestration with real activity interactions:

```kotlin
class LoanApplicationWorkflowTest {
    private val testWorkflowRule = TestWorkflowRule.newBuilder()
        .setWorkflowTypes(LoanApplicationWorkflowImpl::class.java)
        .setDoNotStart(true)
        .build()
    
    @Test
    fun `should process successful loan application end-to-end`() {
        // Given: Mock activities with successful responses
        setupMockActivities()
        
        // When: Start workflow
        val workflowExecution = testWorkflowRule.workflowClient
            .newUntypedWorkflowStub(workflowId)
            .start(workflow::processLoanApplication, application)
        
        // Simulate approval signal
        workflow.approveApplication(approvalDecision)
        
        // Then: Should complete successfully
        val result = workflowExecution.getResult(String::class.java)
        assertTrue(result.startsWith("DISBURSED"))
    }
}
```

### **Signal Testing**

Test workflow signal handling:

```kotlin
@Test
fun `should handle approval signal correctly`() {
    // Start workflow and wait for approval stage
    testWorkflowRule.testEnvironment.sleep(Duration.ofSeconds(2))
    
    // Send approval signal
    workflow.approveApplication(approvalDecision)
    
    // Verify state change
    assertEquals(ApplicationStatus.APPROVED, workflow.getCurrentState())
}
```

### **Query Testing**

Test real-time workflow state queries:

```kotlin
@Test
fun `should return correct workflow state via queries`() {
    // Start workflow
    startWorkflow()
    
    // Query state during execution
    val currentState = workflow.getCurrentState()
    val history = workflow.getProcessingHistory()
    
    assertNotNull(currentState)
    assertFalse(history.isEmpty())
}
```

### **Retry Logic Testing**

Test activity retry behavior:

```kotlin
@Test
fun `should retry failed external service calls`() {
    // Mock service to fail first two calls, succeed on third
    `when`(creditBureauService.getCreditScore(any(), any()))
        .thenThrow(RuntimeException("Service timeout"))
        .thenThrow(RuntimeException("Rate limited"))
        .thenReturn(successfulResponse)
    
    // Should eventually succeed after retries
    val assessment = riskScoringActivity.calculateRiskScore(application)
    assertEquals(RiskLevel.LOW, assessment.riskLevel)
    
    // Verify retry attempts
    verify(creditBureauService, times(3)).getCreditScore(any(), any())
}
```

### **Compensation Testing**

Test failure handling and compensation logic:

```kotlin
@Test
fun `should compensate failed disbursement correctly`() {
    // Mock disbursement to fail
    `when`(disbursementActivity.disburseLoan(any()))
        .thenThrow(RuntimeException("Bank transfer failed"))
    
    // Should trigger compensation
    assertThrows<Exception> { workflow.processLoanApplication(application) }
    
    // Verify compensation was called
    verify(disbursementActivity).compensateFailedDisbursement(any())
}
```

---

## 🌐 API Integration Testing

### **REST Endpoint Testing**

Test HTTP API endpoints with MockMvc:

```kotlin
@WebMvcTest(LoanApplicationController::class)
class LoanApplicationControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Test
    fun `should submit loan application successfully`() {
        mockMvc.perform(post("/api/loan/apply")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validApplicationJson))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("SUBMITTED"))
            .andExpect(jsonPath("$.workflowId").exists())
    }
    
    @Test
    fun `should handle invalid request data`() {
        mockMvc.perform(post("/api/loan/apply")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidApplicationJson))
            .andExpect(status().isBadRequest)
    }
}
```

### **Error Handling Testing**

```kotlin
@Test
fun `should handle workflow client failures gracefully`() {
    `when`(workflowClient.newWorkflowStub(any(), any()))
        .thenThrow(RuntimeException("Temporal unavailable"))
    
    mockMvc.perform(get("/api/loan/status/user-123"))
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value(containsString("Failed to get status")))
}
```

---

## 🚀 End-to-End Testing

### **Manual Testing Scenarios**

#### **Scenario 1: Successful Loan Approval**

```bash
# 1. Start services
temporal server start-dev
./gradlew bootRun

# 2. Submit application
curl -X POST http://localhost:8080/api/loan/apply \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "e2e-user-001",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@test.com",
    "phone": "+1-555-0123",
    "loanAmount": 25000,
    "purpose": "HOME_IMPROVEMENT",
    "annualIncome": 75000,
    "documents": ["ID_CARD", "INCOME_STATEMENT", "BANK_STATEMENT"]
  }'

# Expected: {"workflowId": "loan-app-e2e-user-001-...", "status": "SUBMITTED"}

# 3. Check status
curl http://localhost:8080/api/loan/status/e2e-user-001

# Expected: Status shows "AWAITING_APPROVAL"

# 4. Approve loan
curl -X POST http://localhost:8080/api/loan/approve/e2e-user-001 \
  -H "Content-Type: application/json" \
  -d '{"approvedBy": "e2e-manager", "notes": "E2E test approval"}'

# Expected: {"message": "Loan approved successfully"}

# 5. Verify final status
curl http://localhost:8080/api/loan/status/e2e-user-001

# Expected: Status shows "DISBURSED"
```

#### **Scenario 2: High-Risk Auto-Rejection**

```bash
curl -X POST http://localhost:8080/api/loan/apply \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "high-risk-user",
    "email": "test.low@example.com",
    "annualIncome": 25000,
    "loanAmount": 75000,
    "documents": ["ID_CARD"]
  }'

# Expected: Workflow completes with "REJECTED" status
```

#### **Scenario 3: Manual Rejection**

```bash
# Submit valid application, then reject
curl -X POST http://localhost:8080/api/loan/reject/e2e-user-002 \
  -H "Content-Type: application/json" \
  -d '{
    "rejectedBy": "e2e-manager",
    "reason": "Policy violation",
    "notes": "E2E test rejection"
  }'
```

### **Temporal Web UI Verification**

1. **Navigate to**: [http://localhost:8233](http://localhost:8233)
2. **Search for workflow**: Use workflow ID from API response
3. **Verify execution**: Check event history, activity executions, signals
4. **Inspect retries**: Look for retry attempts in activity executions
5. **Check state**: Verify current workflow state matches API response

---

## 📊 Test Coverage & Validation

### **Coverage Checklist**

#### ✅ **Workflow Logic**
- [ ] Happy path execution
- [ ] Document validation flow
- [ ] Risk scoring with retries
- [ ] Manual approval waiting
- [ ] Disbursement process
- [ ] Follow-up workflow initiation

#### ✅ **Signal Handling**
- [ ] Approval signals
- [ ] Rejection signals
- [ ] Multiple signals handling
- [ ] Signal timing variations

#### ✅ **Query Methods**
- [ ] Real-time state queries
- [ ] Application details retrieval
- [ ] Risk assessment queries
- [ ] Processing history access

#### ✅ **Error Scenarios**
- [ ] Invalid document validation
- [ ] High-risk auto-rejection
- [ ] External service failures
- [ ] Disbursement failures
- [ ] Compensation logic

#### ✅ **Edge Cases**
- [ ] Zero/negative amounts
- [ ] Missing required data
- [ ] Extreme risk factors
- [ ] Network timeouts
- [ ] Concurrent operations

### **Performance Testing**

```kotlin
@Test
@Timeout(value = 30, unit = TimeUnit.SECONDS)
fun `should complete workflow within acceptable time`() {
    val startTime = System.currentTimeMillis()
    
    val result = workflow.processLoanApplication(application)
    
    val duration = System.currentTimeMillis() - startTime
    assertTrue(duration < 10000, "Workflow took too long: ${duration}ms")
}
```

### **Load Testing**

```kotlin
@Test
fun `should handle multiple concurrent applications`() {
    val futures = (1..10).map { i ->
        CompletableFuture.supplyAsync {
            val app = createApplication("user-$i")
            workflow.processLoanApplication(app)
        }
    }
    
    val results = CompletableFuture.allOf(*futures.toTypedArray()).get()
    assertEquals(10, futures.size)
}
```

---

## 🔍 Testing Tools & Observability

### **Temporal Web UI**
- **Workflow Executions**: Monitor real-time execution
- **Event History**: Detailed step-by-step progression
- **Activity Details**: Retry attempts, timeouts, failures
- **Signal Events**: External interactions

### **Application Monitoring**
```bash
# Health checks
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Custom temporal metrics
curl http://localhost:8080/actuator/metrics/temporal.workflow.execution
```

### **Logging Verification**
```bash
# Watch application logs
tail -f logs/application.log | grep "TEMPORAL"

# Check for specific events
grep "Risk assessment completed" logs/application.log
grep "Loan successfully disbursed" logs/application.log
```

---

## 🐛 Debugging Failed Tests

### **Common Issues & Solutions**

#### **1. Temporal Connection Failures**
```
Error: WorkflowServiceException: UNAVAILABLE
```
**Solution**: Ensure Temporal server is running
```bash
temporal server start-dev
```

#### **2. Test Timeouts**
```
Error: Test timed out after 30 seconds
```
**Solution**: Increase timeout or check for infinite loops
```kotlin
@Test
@Timeout(value = 60, unit = TimeUnit.SECONDS)
```

#### **3. Mock Configuration Issues**
```
Error: No qualifying bean of type 'WorkflowClient'
```
**Solution**: Ensure test configuration excludes Temporal config
```properties
spring.autoconfigure.exclude=com.temporal.config.TemporalConfig
```

#### **4. Signal Timing Issues**
```
Error: Signal not received by workflow
```
**Solution**: Add delays before sending signals
```kotlin
testWorkflowRule.testEnvironment.sleep(Duration.ofSeconds(2))
workflow.approveApplication(approval)
```

---

## 📈 Test Execution

### **Running Tests**

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "LoanApplicationWorkflowTest"

# Run with specific profile
./gradlew test -Dspring.profiles.active=test

# Run with coverage
./gradlew test jacocoTestReport
```

### **Continuous Integration**

```yaml
# .github/workflows/test.yml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Start Temporal
        run: temporal server start-dev &
      - name: Run tests
        run: ./gradlew test
```

---

## 🎯 Summary

This comprehensive testing strategy ensures your Temporal demo is:

- ✅ **Functionally Correct**: All business logic works as expected
- ✅ **Resilient**: Handles failures and retries appropriately  
- ✅ **Observable**: Provides complete visibility into execution
- ✅ **Performant**: Meets acceptable response time requirements
- ✅ **Maintainable**: Tests are clear, reliable, and easy to extend

### **Key Testing Principles**

1. **Test the Happy Path**: Ensure normal operations work correctly
2. **Test Failure Scenarios**: Verify error handling and recovery
3. **Test External Integrations**: Mock and validate service interactions
4. **Test Temporal Features**: Signals, queries, retries, compensation
5. **Test API Contracts**: Validate request/response formats
6. **Test Performance**: Ensure acceptable response times
7. **Test Observability**: Verify logging and monitoring

**You now have a production-ready testing strategy that validates every aspect of your Temporal workflow system!** 🚀 