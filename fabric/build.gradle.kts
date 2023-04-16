plugins {
    id("heph.architectury-convention")
}

val fabricLoaderVersion: String by project
val fabricApiVersion: String by project

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

val common: Configuration by configurations.creating
val devFabric = configurations.getByName("developmentFabric")
configurations {
    compileClasspath.get().extendsFrom(common)
    runtimeClasspath.get().extendsFrom(common)
    //loomDevelopmentDependencies.extendsFrom
    devFabric.extendsFrom(common)
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    modApi("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    devFabric(project(path = ":common")) { isTransitive = false }
    shadowC(project(path = ":common", configuration = "transformProductionFabric")) { isTransitive = false }
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand("version" to version)
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        inputs.property("version", version)
    }

    shadowJar {
        exclude("architectury.common.json")

        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        injectAccessWidener.set(true)
        input.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
    }

    jar {
        archiveClassifier.set("dev")
    }

    sourcesJar {
        val commonSources = project(":common").tasks.getByName("sourcesJar") as Jar
        dependsOn(commonSources)
        from(commonSources.archiveFile.map { zipTree(it) })
    }

}