plugins {
    java
    alias(libs.plugins.shadow)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation(project(":calinea"))
    implementation(libs.bundles.jackson)
    testImplementation(libs.bundles.testing.implementation)
    testRuntimeOnly(libs.bundles.testing.runtime)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set("calinea-generator")
    manifest {
        attributes("Main-Class" to "io.calinea.generator.CalineaGenerator")
    }
}

tasks.shadowJar {
    archiveBaseName.set("calinea-generator")
    archiveClassifier.set("")
    manifest {
        attributes("Main-Class" to "io.calinea.generator.CalineaGenerator")
    }
}
