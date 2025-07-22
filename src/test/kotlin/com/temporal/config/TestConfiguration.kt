package com.temporal.config

import com.temporal.service.*
import io.temporal.client.WorkflowClient
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.worker.WorkerFactory
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@TestConfiguration
@Profile("test")
class TestConfiguration {
    
    @Bean
    @Primary
    fun testWorkflowServiceStubs(): WorkflowServiceStubs = mockk<WorkflowServiceStubs>(relaxed = true)
    
    @Bean
    @Primary
    fun testWorkflowClient(): WorkflowClient = mockk<WorkflowClient>(relaxed = true)
    
    @Bean
    @Primary
    fun testWorkerFactory(): WorkerFactory = mockk<WorkerFactory>(relaxed = true)
    
    @Bean
    @Primary
    fun testCreditBureauService(): CreditBureauService = mockk<CreditBureauService>(relaxed = true)
    
    @Bean
    @Primary
    fun testBankService(): BankService = mockk<BankService>(relaxed = true)
    
    @Bean
    @Primary
    fun testEmailService(): EmailService = mockk<EmailService>(relaxed = true)
    
    @Bean
    @Primary
    fun testSmsService(): SmsService = mockk<SmsService>(relaxed = true)
} 