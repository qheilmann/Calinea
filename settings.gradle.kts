plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Calinea"

include("calinea")
include("calinea-generator")
include("calinea-playground")
