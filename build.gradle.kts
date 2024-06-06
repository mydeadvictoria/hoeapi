plugins {
    kotlin("jvm") version "1.9.23"
    id("maven-publish")
}

group = "my.deadvictoria"
version = "0.1.2"

val ktorVersion: String by project
val jsoupVersion: String by project
val striktVersion: String by project
val junitVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("org.jsoup:jsoup:$jsoupVersion")

    testImplementation("io.strikt:strikt-core:$striktVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
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
