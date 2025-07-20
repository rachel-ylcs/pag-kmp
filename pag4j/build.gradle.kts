plugins {
    `java-library`
    `maven-publish`
}

group = "love.yinlin"
version = "4.4.31"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
    }
}

tasks.jar {
    archiveBaseName.set("pag4j")
    from(sourceSets.main.get().output)
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
    dependsOn(tasks.javadoc)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            
            pom {
                name = "pag4j"
                description = "Java binding for libpag"
                url = "https://github.com/rachel-ylcs/pag-kmp/"
                
                licenses {
                    license {
                        name = "Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                    }
                }
                
                developers {
                    developer {
                        id = "libpag"
                        name = "libpag"
                        email = "libpag@tencent.com"
                    }
                    developer {
                        id = "ylcs"
                        name = "银临茶舍"
                    }
                }
            }
        }
    }
}
