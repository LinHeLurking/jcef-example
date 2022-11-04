import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

plugins {
    kotlin("jvm") version "1.7.20"
}

group = "online.ruin_of_future"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val jcefReleaseTag = project.property("jcefReleaseTag").toString()

dependencies {
    val osName = System.getProperty("os.name").toLowerCaseAsciiOnly()
    if (osName.contains("windows")) {
        implementation("me.friwi:jcef-natives-windows-amd64:${jcefReleaseTag}")
    }
    implementation("org.jogamp.gluegen:gluegen-rt-main:2.3.2")
    implementation("org.jogamp.jogl:jogl-all-main:2.3.2")
    implementation("org.apache.commons:commons-compress:1.22")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}