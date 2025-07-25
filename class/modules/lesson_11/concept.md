# Concept 11: Queries

## Objective

Master workflow query patterns to enable real-time observability and monitoring of running workflows. Learn how to design queryable workflows that provide visibility into their internal state without affecting execution.

## Key Concepts

### 1. **Queries vs Signals vs Activities**

#### **Comparison Matrix**
| Aspect | Queries | Signals | Activities |
|--------|---------|---------|------------|
| **Purpose** | Read state | Modify state | External operations |
| **Execution** | Synchronous | Asynchronous | Asynchronous |
| **History** | Not recorded | Recorded | Recorded |
| **Side Effects** | None allowed | State changes | External calls |
| **Replay** | Not replayed | Replayed | Replayed |
| **Performance** | Very fast | Fast | Depends on operation |

#### **When to Use Each**
```kotlin
@WorkflowInterface
interface OrderWorkflow {
    // Activities: External operations (database, API calls)
    @WorkflowMethod
    fun processOrder(order: Order): OrderResult
    
    // Signals: External events that change workflow behavior
    @SignalMethod
    fun cancelOrder(reason: String)
    
    // Queries: Read current workflow state
    @QueryMethod
    fun getCurrentStatus(): OrderStatus
}
```

### 2. **Query Design Patterns**

#### **State Snapshot Pattern**
```kotlin
class OrderWorkflowImpl : OrderWorkflow {
    
    private var currentState = OrderState()
    
    @QueryMethod
    override fun getCurrentSnapshot(): OrderSnapshot {
        return OrderSnapshot(
            orderId = currentState.orderId,
            status = currentState.status,
            items = currentState.items.toList(), // Defensive copy
            totalAmount = currentState.totalAmount,
            lastUpdated = currentState.lastUpdated
        )
    }
}
```

#### **Progressive Disclosure Pattern**
```kotlin
class DetailedOrderWorkflowImpl : DetailedOrderWorkflow {
    
    // Basic status query
    @QueryMethod
    override fun getStatus(): OrderStatus = currentStatus
    
    // Detailed information query
    @QueryMethod
    override fun getOrderDetails(): OrderDetails = OrderDetails(
        orderId = orderId,
        customerId = customerId,
        items = orderItems.toList(),
        shippingAddress = shippingAddress,
        paymentInfo = paymentInfo.sanitized() // Remove sensitive data
    )
    
    // Historical information query
    @QueryMethod
    override fun getProcessingHistory(): List<ProcessingEvent> = 
        processingEvents.toList()
    
    // Performance metrics query
    @QueryMethod
    override fun getMetrics(): WorkflowMetrics = WorkflowMetrics(
        startTime = workflowStartTime,
        currentDuration = Duration.between(workflowStartTime, Instant.now()),
        stepsCompleted = completedSteps.size,
        stepsRemaining = remainingSteps.size
    )
}
```

### 3. **Query Performance Optimization**

#### **Caching Expensive Computations**
```kotlin
class OptimizedWorkflowImpl : OptimizedWorkflow {
    
    private var expensiveDataCache: ExpensiveData? = null
    private var cacheLastUpdated: Instant? = null
    private val cacheMaxAge = Duration.ofMinutes(5)
    
    @QueryMethod
    override fun getExpensiveData(): ExpensiveData {
        val now = Workflow.currentTimeMillis()
        val shouldRefreshCache = expensiveDataCache == null || 
            cacheLastUpdated == null ||
            Duration.ofMillis(now - cacheLastUpdated!!.toEpochMilli()) > cacheMaxAge
        
        if (shouldRefreshCache) {
            expensiveDataCache = computeExpensiveData()
            cacheLastUpdated = Instant.ofEpochMilli(now)
        }
        
        return expensiveDataCache!!
    }
    
    private fun computeExpensiveData(): ExpensiveData {
        // Expensive computation here
        return ExpensiveData(/* ... */)
    }
}
```

#### **Lazy Computation Pattern**
```kotlin
class LazyQueryWorkflowImpl : LazyQueryWorkflow {
    
    private var summary: OrderSummary? = null
    
    @QueryMethod
    override fun getOrderSummary(): OrderSummary {
        // Compute summary only when requested
        if (summary == null) {
            summary = OrderSummary(
                totalItems = orderItems.size,
                totalAmount = orderItems.sumOf { it.price * it.quantity },
                estimatedWeight = orderItems.sumOf { it.weight * it.quantity },
                shippingCost = calculateShippingCost()
            )
        }
        return summary!!
    }
    
    // Invalidate cache when state changes
    private fun addOrderItem(item: OrderItem) {
        orderItems.add(item)
        summary = null // Invalidate cache
    }
}
```

### 4. **Query Error Handling**

#### **Safe Query Implementation**
```kotlin
class SafeQueryWorkflowImpl : SafeQueryWorkflow {
    
    @QueryMethod
    override fun getCurrentStatus(): QueryResult<OrderStatus> {
        return try {
            QueryResult.success(currentStatus)
        } catch (e: Exception) {
            QueryResult.error("Failed to get status: ${e.message}")
        }
    }
    
    @QueryMethod
    override fun getProcessingDetails(): ProcessingDetails? {
        // Return null for uninitialized state
        return if (::processingDetails.isInitialized) {
            processingDetails
        } else {
            null
        }
    }
    
    @QueryMethod
    override fun getHealthCheck(): WorkflowHealth {
        return WorkflowHealth(
            isHealthy = !hasErrors,
            lastHeartbeat = lastActivityTime,
            errorCount = errorMessages.size,
            uptime = Duration.between(startTime, Instant.now())
        )
    }
}

sealed class QueryResult<T> {
    data class Success<T>(val data: T) : QueryResult<T>()
    data class Error<T>(val message: String) : QueryResult<T>()
    
    companion object {
        fun <T> success(data: T): QueryResult<T> = Success(data)
        fun <T> error(message: String): QueryResult<T> = Error(message)
    }
}
```

### 5. **Advanced Query Patterns**

#### **Filtered Query Pattern**
```kotlin
class FilterableWorkflowImpl : FilterableWorkflow {
    
    private val events = mutableListOf<WorkflowEvent>()
    
    @QueryMethod
    override fun getEvents(filter: EventFilter): List<WorkflowEvent> {
        return events.filter { event ->
            (filter.eventType == null || event.type == filter.eventType) &&
            (filter.afterTime == null || event.timestamp.isAfter(filter.afterTime)) &&
            (filter.severity == null || event.severity >= filter.severity)
        }
    }
    
    @QueryMethod
    override fun getEventsSince(timestamp: Instant): List<WorkflowEvent> {
        return events.filter { it.timestamp.isAfter(timestamp) }
    }
}
```

#### **Aggregated Query Pattern**
```kotlin
class AggregatedQueryWorkflowImpl : AggregatedQueryWorkflow {
    
    @QueryMethod
    override fun getStatistics(): WorkflowStatistics {
        return WorkflowStatistics(
            totalEvents = events.size,
            eventsByType = events.groupingBy { it.type }.eachCount(),
            averageProcessingTime = calculateAverageProcessingTime(),
            successRate = calculateSuccessRate(),
            errorBreakdown = getErrorBreakdown()
        )
    }
    
    @QueryMethod
    override fun getPerformanceMetrics(): PerformanceMetrics {
        return PerformanceMetrics(
            throughput = calculateThroughput(),
            latencyP50 = calculatePercentile(50),
            latencyP95 = calculatePercentile(95),
            latencyP99 = calculatePercentile(99),
            errorRate = calculateErrorRate()
        )
    }
}
```

### 6. **External Integration with Queries**

#### **REST API Integration**
```kotlin
@RestController
@RequestMapping("/api/workflows")
class WorkflowQueryController(
    private val workflowClient: WorkflowClient
) {
    
    @GetMapping("/order/{workflowId}/status")
    fun getOrderStatus(@PathVariable workflowId: String): ResponseEntity<OrderStatusDto> {
        return try {
            val workflow = workflowClient.newWorkflowStub(
                OrderTrackingWorkflow::class.java,
                workflowId
            )
            
            val status = workflow.getCurrentStatus()
            val details = workflow.getOrderDetails()
            
            val dto = OrderStatusDto(
                status = status.name,
                orderId = details.orderId,
                customerId = details.customerId,
                lastUpdated = Instant.now()
            )
            
            ResponseEntity.ok(dto)
            
        } catch (e: WorkflowNotFoundException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }
    
    @GetMapping("/order/{workflowId}/history")
    fun getProcessingHistory(@PathVariable workflowId: String): ResponseEntity<List<ProcessingEventDto>> {
        return try {
            val workflow = workflowClient.newWorkflowStub(
                OrderTrackingWorkflow::class.java,
                workflowId
            )
            
            val history = workflow.getProcessingHistory()
            val dtos = history.map { ProcessingEventDto.from(it) }
            
            ResponseEntity.ok(dtos)
            
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }
}
```

## Best Practices

### ✅ Query Design

1. **Keep Queries Fast and Simple**
   ```kotlin
   // Good: Simple, fast query
   @QueryMethod
   override fun getCurrentStatus(): OrderStatus = currentStatus
   
   // Bad: Complex computation in query
   @QueryMethod
   override fun getComplexAnalytics(): Analytics {
       // Don't do heavy computation here!
       return performComplexAnalysis() // This will block query execution
   }
   ```

2. **Return Immutable Data**
   ```kotlin
   // Good: Return defensive copies
   @QueryMethod
   override fun getOrderItems(): List<OrderItem> = orderItems.toList()
   
   @QueryMethod
   override fun getOrderDetails(): OrderDetails = orderDetails.copy()
   
   // Bad: Return mutable references
   @QueryMethod
   override fun getOrderItems(): MutableList<OrderItem> = orderItems // Don't do this!
   ```

3. **Handle Uninitialized State**
   ```kotlin
   @QueryMethod
   override fun getProcessingDetails(): ProcessingDetails? {
       return if (::processingDetails.isInitialized) {
           processingDetails
       } else {
           null
       }
   }
   ```

### ✅ Performance Optimization

1. **Cache Expensive Computations**
   ```kotlin
   private var cachedSummary: OrderSummary? = null
   private var summaryLastComputed: Instant? = null
   
   @QueryMethod
   override fun getOrderSummary(): OrderSummary {
       if (shouldRefreshCache()) {
           cachedSummary = computeSummary()
           summaryLastComputed = Instant.now()
       }
       return cachedSummary!!
   }
   ```

2. **Use Progressive Disclosure**
   ```kotlin
   // Start with basic queries
   @QueryMethod fun getBasicStatus(): OrderStatus
   
   // Add detailed queries as needed
   @QueryMethod fun getDetailedStatus(): DetailedOrderStatus
   
   // Provide summary queries for dashboards
   @QueryMethod fun getSummary(): OrderSummary
   ```

### ❌ Common Mistakes

1. **Modifying State in Queries**
   ```kotlin
   // Bad: Modifying state in query
   @QueryMethod
   override fun getCurrentStatusAndMarkAsRead(): OrderStatus {
       markAsRead() // Don't modify state in queries!
       return currentStatus
   }
   
   // Good: Read-only query
   @QueryMethod
   override fun getCurrentStatus(): OrderStatus = currentStatus
   ```

2. **Expensive Operations in Queries**
   ```kotlin
   // Bad: Heavy computation in query
   @QueryMethod
   override fun getAnalytics(): Analytics {
       return performHeavyAnalysis() // This blocks the query thread!
   }
   
   // Good: Cache or pre-compute
   @QueryMethod
   override fun getAnalytics(): Analytics = cachedAnalytics
   ```

3. **Returning Sensitive Information**
   ```kotlin
   // Bad: Exposing sensitive data
   @QueryMethod
   override fun getPaymentInfo(): PaymentInfo = paymentInfo
   
   // Good: Return sanitized data
   @QueryMethod
   override fun getPaymentInfo(): PaymentInfo = paymentInfo.sanitized()
   ```

---