plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

repositories {
    mavenCentral()
}

val e2eTest by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

val e2eTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

dependencies {
    implementation(libs.http4k.core)
    implementation(libs.http4k.format.jackson)
    implementation(libs.http4k.server.netty)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)

    e2eTestImplementation(libs.kotest.runner.junit5)
    e2eTestImplementation(libs.kotest.assertions.core)
    e2eTestImplementation(libs.okhttp)
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass = "io.hwalrus.toggle.AppKt"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

val e2eTestTask = tasks.register<Test>("e2eTest") {
    testClassesDirs = e2eTest.output.classesDirs
    classpath = e2eTest.runtimeClasspath
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn(e2eTestTask)
}
