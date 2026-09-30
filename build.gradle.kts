plugins {
	java
	id("org.springframework.boot") version "4.0.8"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "uz.card"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot starters
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// Database
	implementation("org.postgresql:postgresql:42.7.3")
	implementation("org.liquibase:liquibase-core:4.29.1")

	// Telegram
	implementation("org.telegram:telegrambots:6.8.0")


	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// Development
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.testcontainers:testcontainers:1.20.0")
	testImplementation("org.testcontainers:postgresql:1.20.0")
	testImplementation("org.testcontainers:junit-jupiter:1.20.0")
	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
