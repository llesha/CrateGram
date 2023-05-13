plugins {
    kotlin("multiplatform") version "1.8.10"
}

group = "llesha.parse"
version = "0.5"

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
    }
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
    }
}
