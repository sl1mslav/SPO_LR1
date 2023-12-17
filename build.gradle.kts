plugins {
    kotlin("jvm") version "1.9.21"
    id("org.jetbrains.compose") version "1.5.11"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(compose.desktop.currentOs)
    implementation("cafe.adriel.bonsai:bonsai-core:1.2.0")
    implementation("cafe.adriel.bonsai:bonsai-file-system:1.2.0")
    implementation("cafe.adriel.bonsai:bonsai-json:1.2.0")
    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.1.1")
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(18)
}