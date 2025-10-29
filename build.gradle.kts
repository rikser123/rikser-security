import org.gradle.api.publish.PublishingExtension

plugins {
	java
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
	`maven-publish`
}


configure<PublishingExtension> {
	publications {
		register<MavenPublication>("gpr") {
			from(components["java"])
		}
	}
	repositories {
		maven {
			name = "GitHubPackagesSecurity"
			url = uri("https://maven.pkg.github.com/rikser123/rikser-security")
			credentials {
				username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
				password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
			}
		}
	}
}

group = "rikser123"
version = "0.0.2"
description = "Security"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven {
		name = "GitHubPackagesBundle"
		url = uri("https://maven.pkg.github.com/rikser123/rikser-bundle")
		credentials {
			username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
			password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
		}
	}
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
	implementation("rikser123:bundle:0.0.6")
}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs("-javaagent:${mockitoAgent.asPath}")
}


