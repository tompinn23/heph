plugins {
    id("heph.architectury-convention")
}

val enabledPlatforms: String by project
val fabricLoaderVersion: String by project

architectury {
    common(enabledPlatforms.split(","))
}

loom {
    accessWidenerPath.set(file("src/main/resources/hephaestus.accesswidener"))
    //accessWidenerPath("src/main/resources/examplemod.accesswidener")
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    // Remove the next line if you don't want to depend on the API
}

//publishing {
//    publications {
//        mavenCommon(MavenPublication) {
//            artifactId = rootProject.archives_base_name
//            from components.java
//        }
//    }
//
//    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
//    repositories {
//        // Add repositories to publish to here.
//    }
//}