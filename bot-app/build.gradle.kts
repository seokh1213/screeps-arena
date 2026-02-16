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

    val uploadDir = layout.projectDirectory.dir("../arena/upload")

    from(layout.buildDirectory.dir("compileSync/js/main/productionExecutable/kotlin"))
    include("**/*.mjs")
    include("**/*.map")
    rename("screeps-arena-starter-bot-app.mjs", "main.mjs")
    rename("screeps-arena-starter-bot-app.mjs.map", "main.mjs.map")
    into(uploadDir)
}
