plugins {
    id("co.uzzu.dotenv.gradle") version "4.0.0"
}

allprojects {
    group = project.property("group") as String
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

project(":calinea").version = project.property("version") as String
project(":calinea-playground").version = project.property("version") as String
project(":calinea-generator").version = project.property("generatorVersion") as String
