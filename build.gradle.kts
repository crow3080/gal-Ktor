plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.example"
version = "0.0.1"


application {
    mainClass = "io.ktor.server.netty.EngineMain"
    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED"
    )
}

dependencies {
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.mongodb.driver.core)
    implementation(libs.mongodb.driver.sync)
    implementation(libs.bson)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.conditional.headers)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.netty)
    implementation("io.ktor:ktor-server-sessions")
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    implementation("io.ktor:ktor-client-auth:3.3.0")
    implementation("io.ktor:ktor-client-resources:3.3.0")
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.12.0")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-thymeleaf")
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.6.1")
    implementation("org.mongodb:bson-kotlinx:5.6.1")

}
