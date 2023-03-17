rootProject.name = "djet-codegen"

pluginManagement {
	plugins {
		id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
		id("nebula.release") version "15.3.1"
	}
}

dependencyResolutionManagement {
	repositories {
		mavenCentral()
		mavenLocal()
	}
}

include("openapi-load")
include("codegen-cli")
//include("intellij-plugin")
