rootProject.name = "djai-codegen"

pluginManagement {
	plugins {
		id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
		id("nebula.release") version "17.2.2"
	}
	repositories {
		gradlePluginPortal()
		google()
		mavenCentral()
	}
}

dependencyResolutionManagement {
	repositories {
		google()
		mavenCentral()
		mavenLocal()
	}
}

include("openapi-load")
include("codegen-cli")
//include("intellij-plugin")
