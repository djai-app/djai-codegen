package pro.bilous.codegen.process

import org.mockito.kotlin.*
import org.junit.jupiter.api.Test
import org.openapitools.codegen.CodegenProperty
import kotlin.test.assertEquals

class PropertyTableNameTest {

	@Test
	fun `should return name from complex type many`() {
		val processor = ModelPropertyProcessor(mock())
		processor.openApiWrapper = mock()

		val complexType = "UserAddress"
		whenever(processor.openApiWrapper.isOpenApiContainsType(complexType)).thenReturn(true)

		val property = CodegenProperty().apply {
			name = "address"
			isArray = false
		}

		val (tableName, columnName, realTableName) = processor.readManyPropertyTableData(complexType, property)
		assertEquals("user_address", tableName)
		assertEquals("user_address", columnName)
		assertEquals("user_address", realTableName)

	}

	@Test
	fun `should return name from property name many`() {
		val processor = ModelPropertyProcessor(mock())
		processor.openApiWrapper = mock()

		whenever(processor.openApiWrapper.isOpenApiContainsType("UserAddress")).thenReturn(true)

		val property = CodegenProperty().apply {
			name = "address"
			isArray = true
		}

		val (tableName, columnName, realTableName) = processor.readManyPropertyTableData("UserAddress", property)
		assertEquals("address", tableName)
		assertEquals("user_address", columnName)
		assertEquals("user_address", realTableName)
	}

	@Test
	fun `should return name from property name and real name as table many`() {
		val processor = ModelPropertyProcessor(mock())
		processor.openApiWrapper = mock()

		val property = CodegenProperty().apply {
			name = "address"
			isArray = false
		}

		val (tableName, columnName, realTableName) = processor.readManyPropertyTableData("UserAddress", property)
		assertEquals("address", tableName)
		assertEquals("address", columnName)
		assertEquals("address", realTableName)
	}

	@Test
	fun `should return name from data type reference many`() {
		val processor = ModelPropertyProcessor(mock())
		processor.openApiWrapper = mock()

		val property = CodegenProperty().apply {
			name = "parties"
			isArray = true
		}

		val (tableName, columnName, realTableName) = processor.readManyPropertyTableData("Party", property)
		assertEquals("parties", tableName)
		assertEquals("party", columnName)
		assertEquals("party", realTableName)
	}

	@Test
	fun `should return name from complex type single`() {
		val processor = ModelPropertyProcessor(mock())
		processor.openApiWrapper = mock()

		val complexType = "UserAddress"
		whenever(processor.openApiWrapper.isOpenApiContainsType(complexType)).thenReturn(true)

		val property = CodegenProperty().apply {
			name = "address"
			isArray = false
		}

		val (tableName, columnName, realTableName) = processor.readSinglePropertyTableData(complexType, property)
		assertEquals("user_address", tableName)
		assertEquals("user_address", columnName)
		assertEquals("user_address", realTableName)

	}

	@Test
	fun `should return name from property name single`() {
		val processor = ModelPropertyProcessor(mock())
		processor.openApiWrapper = mock()

		whenever(processor.openApiWrapper.isOpenApiContainsType("UserAddress")).thenReturn(true)

		val property = CodegenProperty().apply {
			name = "address"
			isArray = true
		}

		val (tableName, columnName, realTableName) = processor.readSinglePropertyTableData("UserAddress", property)
		assertEquals("user_address", tableName)
		assertEquals("user_address", columnName)
		assertEquals("user_address", realTableName)
	}

	@Test
	fun `should return name from property name and real name as table single`() {
		val processor = ModelPropertyProcessor(mock())
		processor.openApiWrapper = mock()

		val property = CodegenProperty().apply {
			name = "address"
			isArray = false
		}

		val (tableName, columnName, realTableName) = processor.readSinglePropertyTableData("UserAddress", property)
		assertEquals("address", tableName)
		assertEquals("address", columnName)
		assertEquals("address", realTableName)
	}

	@Test
	fun `should return name from data type reference single`() {
		val processor = ModelPropertyProcessor(mock())
		processor.openApiWrapper = mock()

		val property = CodegenProperty().apply {
			name = "parties"
			isArray = true
		}

		val (tableName, columnName, realTableName) = processor.readSinglePropertyTableData("Party", property)
		assertEquals("party", tableName)
		assertEquals("party", columnName)
		assertEquals("party", realTableName)
	}
}
