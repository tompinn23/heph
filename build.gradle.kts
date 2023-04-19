plugins {
  id("com.diffplug.spotless") version "6.18.0"
}

val modId: String by project
val modName: String by project
val modAuthor: String by project
val modGroup: String by project
val specificationVersion: String by project
val forgeVersionRange: String by project
val loaderVersionRange: String by project
val minecraftVersion: String by project
val minecraftVersionRange: String by project
val parchmentDate: String by project



//  tasks.withType<ProcessResources> {
//    // this will ensure that this task is redone when the versions change.
//    inputs.property("version", version)
//
//    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta", "fabric.mod.json")) {
//      expand(mapOf(
//              "forgeVersionRange" to forgeVersionRange,
//              "loaderVersionRange" to loaderVersionRange,
//              "minecraftVersion" to minecraftVersion,
//              "minecraftVersionRange" to minecraftVersionRange,
//              "modAuthor" to modAuthor,
//              "modName" to modName,
//              "version" to version,
//      ))
//    }
//  }
