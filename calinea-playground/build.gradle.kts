plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation(project(":calinea"))
    compileOnly(libs.paper.api)
}

tasks.shadowJar {
    dependsOn(tasks.processResources)
    archiveBaseName.set("calinea-playground")
    archiveClassifier.set("")
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

tasks.register<Copy>("copyJarToServer") {
    from(tasks.shadowJar.flatMap { it.archiveFile })
    into("${env.fetch("SERVER_PATH")}/plugins") 
    // This uses environment variable from .env file via dotenv plugin applied to root project
    
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
