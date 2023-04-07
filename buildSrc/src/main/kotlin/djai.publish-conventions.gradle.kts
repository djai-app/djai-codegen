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
            val isStable = findProperty("djai.stable") == "true"
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

                if (!groupId.startsWith("app.djai.")) {
                    throw GradleException("groupId is not set for this project or its parent ${project.parent}")
                }
                pom.description.set((project.description ?: "DJAI code generation libraries"))
            }
            pom {
                name.set("DJAI Codegen instrumentation")
                url.set("https://github.com/djai-app/djai-codegen")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("v-bilous")
                        name.set("Vova Bilous")
                        url.set("https://github.com/djai-app/djai-codegen/discussions")
                    }
                }

                scm {
                    connection.set("scm:git:git@github.com:djai-app/djai-codegen.git")
                    developerConnection.set("scm:git:git@github.com:djai-app/djai-codegen.git")
                    url.set("git@github.com:djai-app/djai-codegen.git")
                }
            }
        }
    }
}

fun artifactPrefix(p: Project, archivesBaseName: String): String {
    if (archivesBaseName.startsWith("djai")) {
        return ""
    }
    if (p.name.startsWith("djai")) {
        return ""
    }
    return "djai-"
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
