import nebula.plugin.release.git.opinion.Strategies
import java.time.Duration

plugins {
	kotlin("jvm") version "1.5.10"
	id("org.jetbrains.intellij") version "1.0" apply false
	id("org.jetbrains.gradle.plugin.idea-ext") version "1.0" apply false
	id("io.github.gradle-nexus.publish-plugin")
	id("nebula.release")
	jacoco
}

group = "cloud.djet"
version = "1.0.1"

release {
	defaultVersionStrategy = Strategies.getSNAPSHOT()
}

nebulaRelease {
	addReleaseBranchPattern("""v\d+\.\d+\.x""")
}

allprojects {
	repositories {
		mavenLocal()
		mavenCentral()
	}
	tasks.withType<JavaCompile> {
		sourceCompatibility = "1.8"
		targetCompatibility = "1.8"
	}
	tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
		kotlinOptions {
			jvmTarget = "1.8"
		}
	}
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
}

tasks {
	compileKotlin {
		kotlinOptions.jvmTarget = "1.8"
	}
	compileTestKotlin {
		kotlinOptions.jvmTarget = "1.8"
	}
}

subprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "jacoco")

	jacoco {
		toolVersion = "0.8.7"
	}

	tasks.jacocoTestReport {
		reports {
			xml.isEnabled = true
		}
	}

	tasks.test {
		useJUnitPlatform()
		finalizedBy(tasks.jacocoTestReport)
	}
	tasks.jacocoTestReport {
		dependsOn(tasks.test)
	}
}

nexusPublishing {
	packageGroup.set("cloud.djet")

	repositories {
		sonatype {
			nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
			snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
			username.set(System.getenv("SONATYPE_USER"))
			password.set(System.getenv("SONATYPE_KEY"))
		}
	}

	connectTimeout.set(Duration.ofMinutes(5))
	clientTimeout.set(Duration.ofMinutes(5))

	transitionCheckOptions {
		// We have many artifacts so Maven Central takes a long time on its compliance checks. This sets
		// the timeout for waiting for the repository to close to a comfortable 50 minutes.
		maxRetries.set(300)
		delayBetween.set(Duration.ofSeconds(10))
	}
}

description = "Libraries for the DJet template with SpringBoot / Kotlin "
