plugins {
	kotlin("jvm")
	id("djai.publish-conventions")
	id("djai.java-conventions")
	jacoco
}

group = "app.djai.codegen"
version = "1.2.3"

sourceSets.main {
    java.srcDirs("src/main/java", "src/main/kotlin")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("com.google.guava:guava:30.1.1-jre")
    api("io.airlift:airline:0.9")
    implementation("com.googlecode.lambdaj:lambdaj:2.3.3")
    implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("org.openapitools:openapi-generator:5.1.1")
    implementation("junit:junit:4.13.2")

	testImplementation(kotlin("test-junit"))
	testImplementation("org.mockito:mockito-core:5.4.0")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<Jar>() {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

jacoco {
	toolVersion = "0.8.7"
}

tasks.jacocoTestReport {
	reports {
		xml.required.set(true)
	}
}

tasks.test {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}
tasks.jacocoTestReport {
	dependsOn(tasks.test)
}
