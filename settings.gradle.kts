pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.minecraftforge.net/")
    maven("https://maven.parchmentmc.org")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.architectury.dev/")
  }
}

val minecraftVersion: String by settings
rootProject.name = "hephaestus-${minecraftVersion}"

include(
        "common",
        "forge",
        "fabric"
)