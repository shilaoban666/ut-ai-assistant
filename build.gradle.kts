plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"
    id("jacoco")  // Add JaCoCo plugin for coverage
}

group = "com.honghu.ut.test.ai.assigment"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2024.1.7")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.intellij.java"))
}

dependencies {
    // JaCoCo dependencies for coverage analysis
    implementation("org.jacoco:org.jacoco.core:0.8.11")
    implementation("org.jacoco:org.jacoco.report:0.8.11")
    
    // JUnit dependencies for testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    
    // Add any other dependencies needed for test generation
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("243.*")
        changeNotes.set("""
            <p>Initial release of UT AI Assistant plugin</p>
            <ul>
                <li>JaCoCo coverage checking with real-time display</li>
                <li>Detailed coverage reports generation</li>
                <li>AI-powered unit test generation at method, class, or project level</li>
                <li>Automatic test execution and error correction</li>
                <li>Integration with IDEA's test running workflow</li>
            </ul>
        """)
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
    
    // Configure test task
    test {
        useJUnitPlatform()
        finalizedBy("jacocoTestReport")
    }
    
    // JaCoCo configuration
    jacoco {
        toolVersion = "0.8.11"
    }
    
    // Custom task to create coverage report
    register("generateCoverageReport") {
        dependsOn("test", "jacocoTestReport")
        doLast {
            println("Coverage report generated at build/reports/jacoco/test/html/index.html")
        }
    }
    
    buildPlugin {
        dependsOn("test")
    }
    
    runIde {
        jvmArgs = listOf("-Xmx2g")
        // Disable plugin version check to avoid network issues
        systemProperty("idea.plugin.version.check", "false")
    }
}