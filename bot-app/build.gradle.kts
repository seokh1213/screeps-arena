plugins {
    kotlin("multiplatform")
}

kotlin {
    sourceSets {
        jsMain {
            dependencies {
                implementation(project(":screeps-types"))
            }
        }
        jsTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
    js {
        useEsModules()
        binaries.executable()
        nodejs()
    }
}

tasks.register<Sync>("deploy") {
    description = "Copies compiled JS files to the Screeps Arena upload directory."
    group = "screeps"
    dependsOn("jsProductionExecutableCompileSync")

    val arenaUploadPath = providers.gradleProperty("arenaUploadDir").orElse("arena/upload")
    val uploadDir = rootProject.layout.projectDirectory.dir(arenaUploadPath.get())

    from(layout.buildDirectory.dir("compileSync/js/main/productionExecutable/kotlin"))
    include("**/*.mjs")
    include("**/*.map")
    rename("screeps-arena-starter-bot-app.mjs", "main.mjs")
    rename("screeps-arena-starter-bot-app.mjs.map", "main.mjs.map")
    into(uploadDir)
}

tasks.register<Delete>("cleanArenaUpload") {
    description = "Deletes arena upload output."
    group = "screeps"
    val arenaUploadPath = providers.gradleProperty("arenaUploadDir").orElse("arena/upload")
    delete(rootProject.layout.projectDirectory.dir(arenaUploadPath.get()))
}

tasks.named("clean") {
    dependsOn("cleanArenaUpload")
}
