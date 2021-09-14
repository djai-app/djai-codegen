package pro.bilous.codegen.process

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import org.junit.jupiter.api.Test
import org.openapitools.codegen.CodeCodegen
import kotlin.test.assertEquals

class PathAntMatchersTest {

	@Test
	fun `should create ant matchers`() {
		val openApi = OpenAPI().apply {
			paths = Paths().apply {
				addPathItem("/parties/{partyId}/libraries/{libraryId}", PathItem())
				addPathItem("/parties/{partyId}/libraries", PathItem())
				addPathItem("/clubs/{clubId}", PathItem())
				addPathItem("/clubs", PathItem())
			}
		}

		val guardsSet = mutableSetOf<Map<String, Any?>>()

		val result = OpenApiProcessor(CodeCodegen()).createAuthRules(openApi, guardsSet)

		assertEquals(4, result.size)
		assertEquals("/parties/*/libraries/*", result.antMatcherAt(0))
		assertEquals("/parties/*/libraries", result.antMatcherAt(1))
		assertEquals("/clubs/*", result.antMatcherAt(2))
		assertEquals("/clubs", result.antMatcherAt(3))
	}

	private fun MutableSet<Map<String, Any?>>.antMatcherAt(index: Int): String? {
		return this.elementAt(index)["antMatcher"] as String?
	}
}
