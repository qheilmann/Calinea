plugins {
    `java-library`
    `maven-publish`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    api(libs.bundles.adventure)
    testImplementation(libs.bundles.testing.implementation)
    testRuntimeOnly(libs.bundles.testing.runtime)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    
    group = "verification"
    description = "Run unit tests"
}

tasks.jar {
    archiveBaseName.set("calinea")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("Calinea")
                description.set("Calinea - Adventure Component Manipulation Library")
                url.set("https://github.com/qheilmann/Calinea")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("qheilmann")
                        name.set("qheilmann")
                    }
                }
            }
        }
    }
    repositories {
        mavenLocal()
    }
}
