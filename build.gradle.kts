plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "de.mylabs"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:4.4.0")
    testImplementation(kotlin("test"))
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("net.java.dev.jna:jna:4.3.0")
    implementation("com.google.code.gson:gson:2.10.1")
}
tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
application {
    mainClass.set("de.mylabs.MainKt")
}
