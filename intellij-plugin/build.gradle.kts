plugins {
	kotlin("jvm")
    id("org.jetbrains.intellij")
	jacoco
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(project(":openapi-load"))
    implementation(project(":codegen-cli"))

	testImplementation(kotlin("test-junit"))
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
	testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

group = "cloud.djet"
version = "1.0.0-alpha"

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
	version.value("2021.2")
}

//tasks.patchPluginXml {
//    changeNotes("""
//      Add change notes here.<br>
//      <em>most HTML tags may be used</em>""")
//    sinceBuild("192")
//}

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
