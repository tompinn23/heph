import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("net.minecraftforge.gradle")
    id("org.parchmentmc.librarian.forgegradle")
    id("java")
    id("maven-publish")
}

val forgeVersion: String by extra

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
    withSourcesJar()
}

//repositories {
//    maven("https://maven.parchmentmc.org")
//    maven("https://maven.minecraftforge.net")
//}

minecraft {
    mappings("parchment", "2023.03.12-1.19.3")
}

dependencies {
    minecraft("net.minecraftforge:forge:${forgeVersion}")
}

tasks {
    processResources {
        // define properties that can be used during resource processing
        inputs.property("version", project.version)

        // this will replace the property "${version}" in your mods.toml
        // with the version you've defined in your gradle.properties
        filesMatching("META-INF/mods.toml") {
            expand("version" to project.version)
        }
    }

    withType<JavaCompile>() {
        options.encoding = "UTF-8"
    }

    jar {
        // add some additional metadata to the jar manifest
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
        manifest {
            attributes(
                "Specification-Title" to project.properties["modId"],
                "Specification-Vendor" to project.properties["modAuthor"],
                "Specification-Version" to "1",
                "Implementation-Title"    to project.properties["modName"],
                "Implementation-Version"  to project.version,
                "Implementation-Vendor"   to project.properties["modAuthor"],
                "Implementation-Timestamp" to now
            )
        }
    }
}
