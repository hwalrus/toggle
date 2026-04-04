plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.http4k.core)
    implementation(libs.http4k.format.jackson)
    implementation(libs.http4k.server.netty)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
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
