import java.time.Duration

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.temporal"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	
	// Kotlin Coroutines
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
	
	// Temporal SDK
	implementation("io.temporal:temporal-sdk:1.23.1")
	
	// Logging
	implementation("io.github.microutils:kotlin-logging:3.0.5")
	
	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "mockito-core")
	}
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("io.temporal:temporal-testing:1.23.1")
	testImplementation("io.mockk:mockk:1.13.11")
	testImplementation("junit:junit:4.13.2")
	testImplementation("org.hamcrest:hamcrest:2.2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("temporal.test.enabled", "true")
	
	// Configure test environment
	testLogging {
		events("passed", "skipped", "failed")
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
	}
	
	// Set test timeout - 5 minutes
	timeout.set(Duration.ofMinutes(5))
}

// Configure source sets for modular lessons
sourceSets {
	main {
		kotlin {
			setSrcDirs(listOf("src/main/kotlin"))
		}
		resources {
			setSrcDirs(listOf("src/main/resources"))
		}
	}
	test {
		kotlin {
			setSrcDirs(listOf("src/test/kotlin"))
		}
		resources {
			setSrcDirs(listOf("src/test/resources"))
		}
	}
}
