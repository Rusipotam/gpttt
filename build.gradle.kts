plugins {
    java
}

group = "com.example"
version = "1.1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    implementation("com.zaxxer:HikariCP:5.1.0")
    runtimeOnly("com.mysql:mysql-connector-j:9.0.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    processResources {
        filteringCharset = "UTF-8"
    }
}
