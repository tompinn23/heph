plugins {
    id("maven-publish")
    id("heph.loom-convention")
}

val modId = "euthenia"
val modGroup: String by extra
val minecraftVersion: String by extra
val forgeVersion: String by extra
val eutheniaVersion: String by project

//adds the build number to the end of the version string if on a build server
var buildNumber = project.findProperty("BUILD_NUMBER")
if (buildNumber == null) {
    buildNumber = "9999"
}

version = "${eutheniaVersion}.${buildNumber}"
group = modGroup

val baseArchiveName = "$modId-$minecraftVersion"
base { archivesName.set(baseArchiveName) }

val generatedResources = file("src/generated")

sourceSets {
    main {
        resources.srcDirs.add(generatedResources)
    }
}


loom {
    runs {
        create("data") {
            data()
            programArgs("--all", "--mod", modId, "--output", generatedResources.absolutePath)
        }
    }
}

dependencies {
    forge("net.minecraftforge:forge:${forgeVersion}")
}
