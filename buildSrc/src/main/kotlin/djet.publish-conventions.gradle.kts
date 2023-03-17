plugins {
    `maven-publish`
    signing
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])

            versionMapping {
                allVariants {
                    fromResolutionResult()
                }
            }
            val isStable = findProperty("djet.stable") == "true"
            if (!isStable) {
                val versionParts = version.split('-').toMutableList()
                versionParts[0] += "-alpha"
                version = versionParts.joinToString("-")
            }

            afterEvaluate {
                val mavenGroupId: String? by project
                if (mavenGroupId != null) {
                    groupId = mavenGroupId
                }
                artifactId = artifactPrefix(project, base.archivesName.get()) + base.archivesName.get()

                if (!groupId.startsWith("cloud.djet.")) {
                    throw GradleException("groupId is not set for this project or its parent ${project.parent}")
                }
                pom.description.set((project.description ?: "Instrumentation libraries for DJet Kotlin/Spring template"))
            }
            pom {
                name.set("DJet instrumentation for Kotlin/Spring")
                url.set("https://github.com/DJetCloud/djet-spring-kotlin")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("djetcloud")
                        name.set("DJetCloud")
                        url.set("https://github.com/DJetCloud/djet-spring-kotlin/discussions")
                    }
                }

                scm {
                    connection.set("scm:git:git@github.com:DJetCloud/djet-spring-kotlin.git")
                    developerConnection.set("scm:git:git@github.com:DJetCloud/djet-spring-kotlin.git")
                    url.set("git@github.com:DJetCloud/djet-spring-kotlin.git")
                }
            }
        }
    }
}

fun artifactPrefix(p: Project, archivesBaseName: String): String {
    if (archivesBaseName.startsWith("djet")) {
        return ""
    }
    if (p.name.startsWith("djet")) {
        return ""
    }
    return "djet-"
}

rootProject.tasks.named("release").configure {
    finalizedBy(tasks["publishToSonatype"])
}

// Stub out entire signing block off of CI since Gradle provides no way of lazy configuration of
// signing tasks.
if (System.getenv("CI") != null) {
    signing {
        useInMemoryPgpKeys(System.getenv("GPG_PRIVATE_KEY"), System.getenv("GPG_PASSWORD"))
        sign(publishing.publications["maven"])
    }
}