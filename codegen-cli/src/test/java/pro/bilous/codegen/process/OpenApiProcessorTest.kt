package pro.bilous.codegen.process

import io.swagger.models.parameters.HeaderParameter
import org.junit.jupiter.api.Test

import org.junit.Assert.*
import org.openapitools.codegen.CodeCodegen
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.parameters.Parameter

class OpenApiProcessorTest {

    @Test
    fun `createAuthRules for Secured Path with ReferenceGuardV2`() {
		val codegen = CodeCodegen().apply {
			val additionalProperties = additionalProperties()
			additionalProperties["security"] = mapOf(
				"guards" to mapOf(
					"referenceGuard" to mapOf(
						"v2" to true
					)
				)
			)
		}
		val processor = OpenApiProcessor(codegen)
		val openApi = OpenAPI().apply {
			paths = Paths().apply {
				set("/root", PathItem().apply {
					get = Operation().apply {
						parameters = listOf<Parameter>(
							Parameter().apply {
								`in` = "header"
								name = "X-access-reference"
								extensions = mapOf(
									"x-auth-format" to "test-auth-format",
									"x-auth-access" to "test-auth-access"
								)
							},
							Parameter().apply {
								`in` = "header"
								name = "bearer"
							}
						)
					}
				})
			}
		}
		val guardsSet = mutableSetOf<Map<String, Any?>>()
		val result = processor.createAuthRules(openApi, guardsSet)
		val expected = setOf(mapOf(
			"antMatcher" to "/root",
			"secured" to true,
			"guards" to listOf(mapOf(
				"headerName" to "X-access-reference",
				"guardName" to "referenceGuard",
				"guardClassName" to "ReferenceGuard",
				"format" to "test-auth-format",
				"args" to listOf("authentication", "request", "'X-access-reference'", "'test-auth-format'")
			)),
			"multiGuards" to false,
			"hasGuards" to true
		))
		assertEquals(expected, result)
    }
}
