package djet.tests

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SearchQueryTest {

	@Test
	fun `should return null query`() {
		val result = getSearchQuery(null, null, null)
		assertNull(result)
	}

	@Test
	fun `should return non null query`() {
		val query = "name==john;state==active"
		val result = getSearchQuery(query, null, null)
		assertEquals(query, result)
	}

	@Test
	fun `should return parent query`() {
		val parentId = "guid-guid-guid"
		val result = getSearchQuery(null, parentId, null)
		assertEquals("entity.parent.id==$parentId", result)
	}

	@Test
	fun `should return query + parent`() {
		val query = "name==john;state==active"
		val parentId = "guid-guid-guid"
		val result = getSearchQuery(query, parentId, null)
		assertEquals("entity.parent.id==$parentId;$query", result)
	}

	@Test
	fun `should return paramsQuery`() {
		val result = getSearchQuery(null, null, mapOf(
			"name" to "john",
			"state" to "active"
		))
		assertEquals("name==john;state==active", result)
	}

	@Test
	fun `should return parent + params`() {
		val parentId = "guid-guid-guid"
		val result = getSearchQuery(null, parentId, mapOf(
			"name" to "john",
			"state" to "active"
		))
		assertEquals("entity.parent.id==$parentId;name==john;state==active", result)
	}

	@Test
	fun `should return query + params`() {
		val query = "name==john;state==active"
		val result = getSearchQuery(query, null, mapOf(
			"name" to "john",
			"state" to "active"
		))
		assertEquals("$query;name==john;state==active", result)
	}

	@Test
	fun `should return query + parent + params`() {
		val parentId = "guid-guid-guid"
		val query = "name==john;state==active"
		val result = getSearchQuery(query, parentId, mapOf(
			"name" to "john",
			"state" to "active"
		))
		assertEquals("entity.parent.id==$parentId;$query;name==john;state==active", result)
	}

	private fun getSearchQuery(query: String?, parentId: String?, params: Map<String, String?>?): String? {
		val queryParentPart = if (parentId != null) "entity.parent.id==$parentId" else null
		val queryParams = params?.filter { !it.value.isNullOrEmpty() }?.map { "${it.key}==${it.value}" }?.joinToString(";")
		val allQueries = listOfNotNull(queryParentPart, query, queryParams)
		return if (allQueries.isEmpty()) null else allQueries.joinToString(";")
	}
}
