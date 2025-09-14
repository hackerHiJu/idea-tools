plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.16.1"
}

group = "io.github.easy"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.intellij:platform-api:2024.2.5")
    implementation("com.intellij:psi:2024.2.5")
    implementation("com.intellij:util:2024.2.5")
    implementation("com.intellij:openapi:2024.2.5")
    implementation("com.intellij:platform-editor:2024.2.5")
    implementation("org.apache.velocity:velocity-engine-core:2.3")
    implementation("cn.hutool:hutool-core:5.8.24")
    implementation("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// 使用idea-IC-2024.2.5版本作为IntelliJ平台
intellij {
    version.set("2024.2.5")
    type.set("IC")
    plugins.set(listOf())
}