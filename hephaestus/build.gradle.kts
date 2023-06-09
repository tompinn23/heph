import net.minecraftforge.gradle.common.util.ModConfig

plugins {
  id("maven-publish")
  id("java-library")
  id("heph.forgegradle-convention")
}

repositories {
  maven("https://maven.shedaniel.me/")
}

val modId: String by extra
val modGroup: String by extra
val minecraftVersion: String by extra
val forgeVersion: String by extra
val specificationVersion: String by project

//adds the build number to the end of the version string if on a build server
var buildNumber = project.findProperty("BUILD_NUMBER")
if (buildNumber == null) {
  buildNumber = "9999"
}

version = "${specificationVersion}.${buildNumber}"
group = modGroup

val baseArchiveName = "$modId-$minecraftVersion"
base { archivesName.set(baseArchiveName) }

val generatedResources = file("src/generated")

sourceSets {
  main {
    resources.srcDirs.add(generatedResources)
  }
}


//loom {
//  runs {
//    create("data") {
//      data()
//      programArgs("--all", "--mod", "hephaestus", "--output", generatedResources.absolutePath)
//    }
//    getByName("client") {
//      runDir = "run"
//      vmArgs("-XX:+AllowEnhancedClassRedefinition")
//    }
//  }
//}

minecraft {
  accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

  runs {
    create("client") {
      workingDirectory = "run"
      mods {
        create("euthenia") {
          source(project(":euthenia").sourceSets.getByName("main"))
        }
        create("hephaestus") {
          source(sourceSets.getByName("main"))
        }
      }
    }
  }
}

dependencies {
  //forge("net.minecraftforge:forge:${forgeVersion}")
  implementation(project(path = ":euthenia"))
  api(fg.deobf("me.shedaniel.cloth:cloth-config-forge:9.0.94"))
}

//
//// Mojang ships Java 17 to end users in 1.18+, so your mod should target Java 17.
//java {
//  toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
//  withSourcesJar()
//}
//
//println("Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}")
//
//dependencies {
//  "minecraft"(
//      group = "net.minecraftforge",
//      name = "forge",
//      version = "$minecraftVersion-$forgeVersion",
//  )
//}
//
//minecraft {
//  // The mappings can be changed at any time and must be in the following format.
//  // Channel:   Version:
//  // official   MCVersion             Official field/method names from Mojang mapping files
//  // parchment  YYYY.MM.DD-MCVersion  Open community-sourced parameter names and javadocs layered on
//  // top of official
//  //
//  // You must be aware of the Mojang license when using the "official" or "parchment" mappings.
//  // See more information here: https://github.com/MinecraftForge/MCPConfig/blob/master/Mojang.md
//  //
//  // Parchment is an unofficial project maintained by ParchmentMC, separate from MinecraftForge
//  // Additional setup is needed to use their mappings: https://parchmentmc.org/docs/getting-started
//  //
//  // Use non-default mappings at your own risk. They may not always work.
//  // Simply re-run your setup task after changing the mappings to update your workspace.
//  mappings("parchment", "2023.03.12-1.19.3")
//
//  accessTransformer(
//      file(
//          "src/main/resources/META-INF/accesstransformer.cfg")) // Currently, this location cannot
//                                                                // be changed from the default.
//
//  // Default run configurations.
//  // These can be tweaked, removed, or duplicated as needed.
//  runs {
//    create("client") {
//      taskName("runClientDev")
//      property("forge.logging.console.level", "debug")
//      workingDirectory(file("run/client/Dev"))
//      mods { create(modId) { source(sourceSets.main.get()) } }
//    }
//
//    create("server") {
//      taskName("server")
//      property("forge.logging.console.level", "debug")
//      workingDirectory(file("run/server"))
//      mods { create(modId) { source(sourceSets.main.get()) } }
//    }
//
//    //        // This run config launches GameTestServer and runs all registered gametests, then
//    // exits.
//    //        // By default, the server will crash when no gametests are provided.
//    //        // The gametest system is also enabled by default for other run configs under the
//    // /test command.
//    //        create("gameTestServer") {
//    //            workingDirectory(project.file("run"))
//    //
//    //            property("forge.logging.markers", "REGISTRIES")
//    //
//    //            property("forge.logging.console.level", "debug")
//    //
//    //            property("forge.enabledGameTestNamespaces", "hephaestus")
//    //
//    // //            mods {
//    // //                hephaestus {
//    // //                    source(sourceSets.main)
//    // //                }
//    // //            }
//    //        }
//
//    create("data") {
//      taskName("runData")
//      workingDirectory(file("run/data"))
//      property("forge.logging.console.level", "debug")
//      // Specify the modid for data generation, where to output the resulting resource, and where to
//      // look for existing resources.
//      args(
//          "--mod",
//          "examplemod",
//          "--all",
//          "--output",
//          file("src/generated/resources/"),
//          "--existing",
//          file("src/main/resources/"))
//      mods { create(modId) { source(sourceSets.main.get()) } }
//    }
//  }
//}
//
//// Include resources generated by data generators.
//sourceSets { named("main") { resources { srcDir("src/generated/resources") } } }
//
//repositories {
//  // Put repositories for dependencies here
//  // ForgeGradle automatically adds the Forge maven and Maven Central for you
//
//  // If you have mod jar dependencies in ./libs, you can declare them as a repository like so:
//  // flatDir {
//  //     dir "libs"
//  // }
//}
//
//dependencies {
//  // Specify the version of Minecraft to use. If this is any group other than "net.minecraft", it is
//  // assumed
//  // that the dep is a ForgeGradle "patcher" dependency, and its patches will be applied.
//  // The userdev artifact is a special name and will get all sorts of transformations applied to it.
//  // minecraft("net.minecraftforge:forge:1.19.3-44.1.23")
//
//  // Real mod deobf dependency examples - these get remapped to your current mappings
//  // implementation
//  // fg.deobf("com.tterrag.registrate:Registrate:MC${mc_version}-${registrate_version}") // Adds
//  // registrate as a dependency
//
//  // Examples using mod jars from ./libs
//  // implementation fg.deobf("blank:coolmod-${mc_version}:${coolmod_version}")
//
//  // For more info...
//  // http://www.gradle.org/docs/current/userguide/artifact_dependencies_tutorial.html
//  // http://www.gradle.org/docs/current/userguide/dependency_management.html
//  implementation(project(":euthenia"))
//}
//
//tasks.jar {
//  from(sourceSets.main.get().output)
//  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//  finalizedBy("reobfJar")
//}
//
//val sourcesJarTask =
//    tasks.named<Jar>("sourcesJar") {
//      from(sourceSets.main.get().allJava)
//
//      duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//      archiveClassifier.set("sources")
//    }
//
//artifacts {
//  archives(tasks.jar.get())
//  archives(sourcesJarTask.get())
//}
