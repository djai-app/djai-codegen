package org.openapitools.codegen

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

internal class CodeCodegenTest {

	@Test
	fun `test package creation`() {
		val gen = CodeCodegen().apply {
			artifactId = "user"
		}
		val testSrc = "app/client/user/repository"
		val result = gen.getTestFolder(testSrc, "")
		assertEquals("app-user/src/test/kotlin/app/client/user/controller".replace("/", File.separator), result)
	}

	@Test
	fun `fromOperation check redundant imports not present`() {
		val gen = CodeCodegen()
		val operation = Operation().apply {
			operationId = "opId"
			responses = ApiResponses().addApiResponse(
				"200", ApiResponse()
			)
		}
		val result = gen.fromOperation(path = "", httpMethod = "", operation = operation, servers = null)

		assertNotNull(result)
		assertFalse(result.imports.contains("List"))
		assertFalse(result.imports.contains("Error"))
	}

	@Disabled
	@Test
	fun `print post process`() {
		val gen = CodeCodegen()
		gen.postProcess()
	}

	@Test
	fun `should return full path of the generated service`() {
		val template = "template/service.mustache"
		val gen = TestCodegen(template)
		val expected = "my.projects/project/app-service/src/main/kotlin/my/package/com/service/file.kt".fixSeparator()
		assertEquals(expected, gen.apiFilename(template, "file"))
	}

	@Test
	fun `should return full path of the generated repository`() {
		val template = "template/repository.mustache"
		val gen = TestCodegen(template)
		val expected = "my.projects/project/app-service/src/main/kotlin/my/package/com/repository/file.kt".fixSeparator()
		assertEquals(expected, gen.apiFilename(template, "file"))
	}

	@Test
	fun `should return full path of the generated api controller`() {
		val template = "template/api.mustache"
		val gen = TestCodegen(template)
		val expected = "my.projects/project/app-service/src/main/kotlin/my/package/com/controller/api/file.kt".fixSeparator()
		assertEquals(expected, gen.apiFilename(template, "file"))
	}

	@Test
	fun `should return full path of the generated controller`() {
		val template = "template/apiController.mustache"
		val gen = TestCodegen(template)
		val expected = "my.projects/project/app-service/src/main/kotlin/my/package/com/controller/file.kt".fixSeparator()
		assertEquals(expected, gen.apiFilename(template, "file"))
	}

	@Test
	fun `should return full path of the common generated file`() {
		val template = "template/any.mustache"
		val gen = TestCodegen(template)
		val expected = "my.projects/project/app-service/src/main/kotlin/repository-package/api-file.kt".fixSeparator()
		assertEquals(expected, gen.apiFilename(template, "file"))
	}

	class TestCodegen(template: String) : CodeCodegen() {
		init {
			additionalProperties["appPackage"] = "my.package.com"
			outputFolder = "my.projects/project"
			artifactId = "service"
			apiTemplateFiles[template] = ".kt"
			entityMode = true
			repositoryPackage = "repository-package"
		}

		override fun toApiFilename(name: String?) = "api-file"
	}

	private fun String.fixSeparator() = this.replace('/', File.separatorChar)
}
