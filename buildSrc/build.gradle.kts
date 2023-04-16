plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
    maven("https://maven.minecraftforge.net/")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.architectury.dev/")
}


dependencies {
    implementation("architectury-plugin:architectury-plugin.gradle.plugin:3.4-SNAPSHOT")
    implementation("dev.architectury.loom:dev.architectury.loom.gradle.plugin:1.1-SNAPSHOT")
    implementation("com.github.johnrengelman.shadow:com.github.johnrengelman.shadow.gradle.plugin:7.1.2")
}