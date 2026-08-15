plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation(project(":calinea"))
    compileOnly(libs.paper.api)
    implementation(libs.commandapi)
    
    // Null safety annotations
    implementation(libs.jspecify)
}

tasks.shadowJar {
    dependsOn(tasks.processResources)
    archiveBaseName.set("calinea-playground")
    archiveClassifier.set("")

    relocate("dev.jorel.commandapi", "io.calinea.playground.libs.commandapi")
}


tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "name" to project.name,
        "description" to project.property("description") as String,
        "apiVersion" to project.property("apiVersion") as String,
        "author" to project.property("author") as String,
        "website" to project.property("website") as String,
        "mainClass" to project.property("mainClass") as String
    )
    inputs.properties(props)
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

// Minimal ".env" reader for deployment tasks (replaces the unmaintained co.uzzu.dotenv.gradle plugin).
// Values from the environment take precedence over the ".env" file.
val dotEnv: Map<String, String> = rootProject.file(".env").let { file ->
    if (!file.exists()) {
        emptyMap()
    } else {
        file.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
            .associate { line ->
                val (key, value) = line.split("=", limit = 2)
                key.trim() to value.trim().trim('"', '\'')
            }
    }
}

fun envFetchOrNull(key: String): String? = System.getenv(key) ?: dotEnv[key]

fun envFetch(key: String): String = envFetchOrNull(key)
    ?: throw GradleException("Missing required environment variable \"$key\" (set it in the environment or in .env)")

tasks.register<Copy>("copyJarToServer") {
    from(tasks.shadowJar.flatMap { it.archiveFile })
    into("${envFetch("SERVER_PATH")}/plugins")
    // This uses environment variable from the environment or .env file

    val archiveBaseName = tasks.shadowJar.flatMap { it.archiveBaseName }
    
    doFirst {
        val baseName = archiveBaseName.get()
        val targetDir = destinationDir
        
        // Clean old plugin files with same base name
        targetDir.listFiles()?.filter { file ->
            file.name.startsWith("$baseName") && file.name.endsWith(".jar")
        }?.forEach { file ->
            logger.info("Deleting old plugin file: ${file.name}")
            file.delete()
        }
        
        logger.info("Copying new jar to server...")
    }
}

tasks.register("resolveBuildCopyPipeline") {
    dependsOn(tasks.shadowJar)
    finalizedBy(tasks.named("copyJarToServer"))
    
    group = "deployment"
    description = "Build shadow jar and copy to development server"
}
