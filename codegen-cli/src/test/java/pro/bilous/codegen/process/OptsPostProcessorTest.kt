package pro.bilous.codegen.process

import org.junit.Assert.*
import org.junit.jupiter.api.Test
import org.openapitools.codegen.CodeCodegen
import org.openapitools.codegen.SupportingFile

internal class OptsPostProcessorTest {
	@Test
	fun `check if _gitignore is added to the files to be generated`() {
		val shouldBeGenerated = SupportingFile(
			"raw/_gitignore",
			".gitignore"
		)
		val codegen = CodeCodegen()
		val processor = OptsPostProcessor(codegen)
		processor.processOpts()
		val codegenSupportingFiles = codegen.supportingFiles()
		assertNotNull(codegenSupportingFiles?.find { it.equals(shouldBeGenerated) })
	}

	@Test
	fun `check if _editorconfig is added to the files to be generated`() {
		val shouldBeGenerated = SupportingFile(
			"raw/_editorconfig",
			".editorconfig"
		)
		val codegen = CodeCodegen()
		val processor = OptsPostProcessor(codegen)
		processor.processOpts()
		val codegenSupportingFiles = codegen.supportingFiles()
		assertNotNull(codegenSupportingFiles?.find { it.equals(shouldBeGenerated) })
	}

	@Test
	fun `check if _gradle_properties is added to the files to be generated`() {
		val shouldBeGenerated = SupportingFile(
			"raw/_gradle_properties",
			"gradle.properties"
		)
		val codegen = CodeCodegen()
		val processor = OptsPostProcessor(codegen)
		processor.processOpts()
		val codegenSupportingFiles = codegen.supportingFiles()
		assertNotNull(codegenSupportingFiles?.find { it.equals(shouldBeGenerated) })
	}

	@Test
	fun `check if idea files is added to the files to be generated`() {
		val shouldBeGenerated = SupportingFile(
			"idea/runConfiguration.mustache",
			".idea/runConfigurations/Application.xml"
		)
		val codegen = CodeCodegen()
		val processor = OptsPostProcessor(codegen)
		processor.processOpts()
		val codegenSupportingFiles = codegen.supportingFiles()
		assertNotNull(codegenSupportingFiles?.find { it.equals(shouldBeGenerated) })
	}

	@Test
	fun `check if common test files are added to the files to be generated`() {
		val artifactId = "artifactId"
		val appRoot = "app-${artifactId.toLowerCase()}"
		val appPackage = "appPackage"
		val appName = "appRealName"
		val codegen = CodeCodegen().apply {
			this.artifactId = artifactId
			val additionalProperties = additionalProperties()
			additionalProperties["authorizationEnabled"] = true
			additionalProperties["appPackage"] = appPackage
			additionalProperties["appRealName"]	= appName
		}

		val inputTest = "common/src/test/kotlin"
		val destTest = "$appRoot/src/test/kotlin/$appPackage"
		val shouldBeGenerated = listOf(
			SupportingFile(
			"$inputTest/controller/AbstractIntegrationTest.kt.mustache",
			"$destTest/controller",
			"AbstractIntegrationTest.kt"
			),
			SupportingFile(
			"$inputTest/controller/CommonIntegrationTest.kt.mustache",
			"$destTest/controller",
			"CommonIntegrationTest.kt"
			)
		)

		val processor = OptsPostProcessor(codegen)
		processor.processOpts()
		val codegenSupportingFiles = codegen.supportingFiles()
		shouldBeGenerated.forEach {
			shouldBe -> assertNotNull(codegenSupportingFiles?.find { it.equals(shouldBe) })
		}
	}
}

