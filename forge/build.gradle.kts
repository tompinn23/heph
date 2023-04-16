import org.gradle.jvm.tasks.Jar

plugins {
    id("heph.architectury-convention")
}

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)
    forge {
        convertAccessWideners.set(true)
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
    }
}

val minecraftVersion: String by project
val forgeVersion: String by project

val common: Configuration by configurations.creating
val devForge = configurations.getByName("developmentForge")
configurations {
    compileClasspath.get().extendsFrom(common)
    runtimeClasspath.get().extendsFrom(common)
    devForge.extendsFrom(common)
}


dependencies {
    forge("net.minecraftforge:forge:${forgeVersion}")

    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    //devForge(project(path = ":common")) { isTransitive = false }
    shadowC(project(path = ":common", configuration = "transformProductionForge")) { isTransitive = false }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("META-INF/mods.toml") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        exclude("fabric.mod.json", "architectury.common.json")
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        input.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
        archiveClassifier.set("")
    }

    jar {
        archiveClassifier.set("dev")
    }

    sourcesJar {
        val commonSources: Jar = project(":common").tasks.getByName("sourcesJar") as Jar
        dependsOn(commonSources)
        from(commonSources.archiveFile.map { zipTree(it) })
    }
}