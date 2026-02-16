plugins {
    kotlin("multiplatform")
    kotlin("plugin.js-plain-objects")
}

kotlin {
    sourceSets {
        jsTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
    js {
        useEsModules()
        binaries.library()
        nodejs()
    }
}
