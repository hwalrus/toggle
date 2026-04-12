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
    implementation(libs.mongodb.kotlin.sync)
    implementation(libs.typesafe.config)
    runtimeOnly(libs.slf4j.simple)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.testcontainers.mongodb)

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

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

val e2eTestTask = tasks.register<Test>("e2eTest") {
    testClassesDirs = e2eTest.output.classesDirs
    classpath = e2eTest.runtimeClasspath
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn(e2eTestTask)
}

val buildFrontend = tasks.register<Exec>("buildFrontend") {
    workingDir = file("../web")
    commandLine("sh", "-c", "npm run build")
    environment("PATH", System.getenv("PATH") ?: "/usr/local/bin:/usr/bin:/bin")
    inputs.dir("../web/src")
    inputs.files("../web/package.json", "../web/vite.config.ts", "../web/tsconfig.json")
    outputs.dir("../web/dist")
}

val copyFrontend = tasks.register<Copy>("copyFrontend") {
    dependsOn(buildFrontend)
    from("../web/dist")
    into(layout.buildDirectory.dir("generated/frontend/public"))
}

sourceSets.main.get().resources.srcDir(layout.buildDirectory.dir("generated/frontend"))

tasks.named("processResources") {
    dependsOn(copyFrontend)
}
