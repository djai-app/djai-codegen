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

		val result = OpenApiProcessor(CodeCodegen()).getOrCreatePathAntMatchers(openApi)

		assertEquals(4, result.size)
		assertEquals("/parties/*/libraries/*", result.elementAt(0))
		assertEquals("/parties/*/libraries", result.elementAt(1))
		assertEquals("/clubs/*", result.elementAt(2))
		assertEquals("/clubs", result.elementAt(3))
	}
}
