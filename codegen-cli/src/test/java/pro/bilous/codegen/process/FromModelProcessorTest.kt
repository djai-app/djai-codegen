package pro.bilous.codegen.process

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.openapitools.codegen.CodegenModel
import org.openapitools.codegen.CodegenProperty
import pro.bilous.codegen.process.models.IModelStrategyResolver
import pro.bilous.codegen.process.models.ModelStrategyResolver
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class FromModelProcessorTest {

	@Test
	fun `should change enum classname`() {
		val processor = FromModelProcessor(mock())
		val model = CodegenModel().apply {
			isEnum = true
			classname = "AccountTypeEnum"
			name = "AccountType"
		}
		processor.fixEnumName(model)
		assertEquals("AccountType", model.classname)
	}

	@Test
	fun `should not change not enum classname`() {
		val processor = FromModelProcessor(mock())
		val model = CodegenModel().apply {
			isEnum = false
			classname = "AccountTypeBody"
			name = "AccountType"
		}
		processor.fixEnumName(model)
		assertEquals("AccountTypeBody", model.classname)
	}

	@Test
	fun `should remove vars started with _ or #`() {
		val processor = FromModelProcessor(mock())
		val model = CodegenModel().apply {
			name = "Account"
			vars = listOf(
				CodegenProperty().apply { name = "_required" },
				CodegenProperty().apply { name = "#firstName" },
				CodegenProperty().apply { name = "fullName" }
			)
		}
		processor.removeIgnoredFields(model)
		assertEquals(1, model.vars.size)
		assertEquals("fullName", model.vars.first().name)
	}

	@Test
	fun `should fix required fields with "null" default value`() {
		val processor = FromModelProcessor(mock())
		val model = CodegenModel().apply {
			name = "Account"
			vars = listOf(
				CodegenProperty().apply {
					required = true
					name = "required"
					defaultValue = "null"
				}
			)
		}
		processor.fixRequiredFieldsDefaultValue(model)

		assertNull(model.vars.first().defaultValue)
	}

	@Test
	fun `should not fix non-required fields with "null" default value`() {
		val processor = FromModelProcessor(mock())
		val model = CodegenModel().apply {
			name = "Account"
			vars = listOf(CodegenProperty().apply { defaultValue = "null" })
		}
		processor.fixRequiredFieldsDefaultValue(model)
		assertEquals("null", model.vars.first().defaultValue)
	}

	@Test
	fun `should set class of model as data if any var presents`() {
		val processor = FromModelProcessor(mock())
		val model = CodegenModel().apply {
			name = "Account"
			vars = listOf(CodegenProperty())
		}
		processor.resolveDataClass(model)
		assertTrue(model.vendorExtensions["isDataClass"] as Boolean)
	}

	@Test
	fun `should not set class of model as data if no var presents`() {
		val processor = FromModelProcessor(mock())
		val model = CodegenModel().apply {
			name = "Account"
			vars = listOf()
		}
		processor.resolveDataClass(model)
		assertFalse(model.vendorExtensions["isDataClass"] as Boolean)
	}
}
