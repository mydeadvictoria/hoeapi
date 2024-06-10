plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.11"
    id("maven-publish")
}

group = "my.deadvictoria"
version = "0.2.1"

val ktorVersion: String by project
val jsoupVersion: String by project
val striktVersion: String by project
val junitVersion: String by project
val serializationVersion: String by project
val benchmarkVersion: String by project

repositories {
    mavenCentral()
}

configurations {
    create("benchmarkImplementation") {
        extendsFrom(configurations.implementation.get())
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("org.jsoup:jsoup:$jsoupVersion")

    // Benchmarking
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:$benchmarkVersion")

    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.strikt:strikt-core:$striktVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    add("benchmarkImplementation", sourceSets.main.get().output + sourceSets.main.get().runtimeClasspath)
}

sourceSets {
    val benchmark by creating {
        kotlin.srcDir("src/benchmark/kotlin")
        resources.srcDir("src/benchmark/resources")
    }
}

benchmark {
    targets {
        register("benchmark")
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "my.deadvictoria"
            artifactId = "hoeapi"
            version = project.version.toString()

            from(components["java"])
        }
    }
}
