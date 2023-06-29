plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
}

tasks {
	compileJava {
		sourceCompatibility = "11"
		targetCompatibility = "11"
	}
	compileKotlin {
		kotlinOptions.jvmTarget = "11"
	}
	compileTestKotlin {
		kotlinOptions.jvmTarget = "11"
	}
}
