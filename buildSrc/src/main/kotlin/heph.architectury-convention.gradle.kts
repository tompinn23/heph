import java.text.SimpleDateFormat
import java.util.Date
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    id("architectury-plugin")
    id("dev.architectury.loom")
    id("com.github.johnrengelman.shadow")
}



repositories {
    maven("https://maven.parchmentmc.org")
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

//adds the build number to the end of the version string if on a build server
var buildNumber = project.findProperty("BUILD_NUMBER")
if (buildNumber == null) {
    buildNumber = "9999"
}

version = "${specificationVersion}.${buildNumber}"
group = modGroup
base {
    archivesBaseName = "$modId-$minecraftVersion"
}

architectury {
    injectInjectables = false
}


loom {
    silentMojangMappingsLicense()
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.layered{
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${minecraftVersion}:${parchmentDate}")
    })
}

val shadowC by configurations.creating

java {
    withSourcesJar()
}

tasks.withType(ShadowJar::class) {
    this.configurations = listOf(shadowC)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(JavaLanguageVersion.of(17).asInt())
}

tasks.withType<Jar> {
    val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
    manifest {
        attributes(mapOf(
                "Specification-Title" to modName,
                "Specification-Vendor" to modAuthor,
                "Specification-Version" to specificationVersion,
                "Implementation-Title" to name,
                "Implementation-Version" to archiveVersion,
                "Implementation-Vendor" to modAuthor,
                "Implementation-Timestamp" to now,
        ))
    }
}

afterEvaluate {
    tasks.withType(ShadowJar::class) {
        archiveClassifier.set("shadow-dev")
    }

    tasks.withType(net.fabricmc.loom.task.RemapJarTask::class) {
        onlyIf {
            return@onlyIf this.project.name != "common"
        }
        val shadowTask = tasks.getByName("shadowJar") as ShadowJar
        dependsOn(shadowTask)
        input.set(shadowTask.archiveFile)
        archiveAppendix.set("")
    }
}