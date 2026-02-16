plugins {
    base
    kotlin("multiplatform") version "2.3.0" apply false
    kotlin("plugin.js-plain-objects") version "2.3.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

tasks.register("deploy") {
    description = "Build and copy bot JS output into arena/upload."
    group = "screeps"
    dependsOn(":bot-app:deploy")
}

tasks.register<Delete>("cleanArenaUpload") {
    description = "Deletes arena upload output."
    group = "screeps"
    val arenaUploadPath = providers.gradleProperty("arenaUploadDir").orElse("arena/upload")
    delete(layout.projectDirectory.dir(arenaUploadPath.get()))
}

tasks.named("clean") {
    dependsOn("cleanArenaUpload")
}
