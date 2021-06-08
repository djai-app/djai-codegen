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
}

