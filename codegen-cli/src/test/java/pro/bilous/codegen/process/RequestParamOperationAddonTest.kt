package pro.bilous.codegen.process

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.ObjectSchema
import org.junit.jupiter.api.Test
import org.openapitools.codegen.*
import kotlin.test.assertEquals

internal class RequestParamOperationAddonTest {

	@Test
	fun `should apply function for create`() {
		val operation = CodegenOperation().apply {
			headerParams.add(CodegenParameter().apply {
				isHeaderParam = true
				paramName = "partyId"
			})
			httpMethod = "post"
		}
		val codegenMock = mock<CodeCodegen>()

		whenever(codegenMock.findOpenApi()).thenReturn(OpenAPI().apply {
			components = Components()
				.addSchemas("Blog", ObjectSchema())
				.addSchemas("Entity", ObjectSchema())
		})

		whenever(codegenMock.fromModel(eq("Blog"), any())).thenReturn(CodegenModel().apply {
			allVars.add(CodegenProperty().apply {
				complexType ="ResourceEntity"
			})
		})
		whenever(codegenMock.fromModel(eq("Entity"), any())).thenReturn(CodegenModel().apply {
			vars.add(CodegenProperty().apply {
				name ="partyId"
			})
		})

		OperationAddon(codegenMock).applyRequestParams(operation, "Blog")
		assertEquals("preCreate", operation.vendorExtensions["preFuncName"])

		val funcParams = operation.vendorExtensions["preFuncParams"] as ArrayList<Map<String, String>>
		assertEquals("entity.partyId", funcParams[0]["left"])
		assertEquals("partyId", funcParams[0]["right"])
	}

	@Test
	fun `should apply function name for get`() {
		val operation = CodegenOperation().apply {
			headerParams.add(CodegenParameter().apply {
				isHeaderParam = true
				paramName = "partyId"
			})
			httpMethod = "get"
		}
		val codegenMock = mock<CodeCodegen>()

		whenever(codegenMock.findOpenApi()).thenReturn(OpenAPI().apply {
			components = Components()
				.addSchemas("Club", ObjectSchema())
		})

		whenever(codegenMock.fromModel(eq("Club"), any())).thenReturn(CodegenModel().apply {
			vars.add(CodegenProperty().apply {
				name ="partyId"
			})
		})
		OperationAddon(codegenMock).applyRequestParams(operation, "Club")
		assertEquals("preGet", operation.vendorExtensions["preFuncName"])

		val funcParams = operation.vendorExtensions["preFuncParams"] as ArrayList<Map<String, String>>
		assertEquals("partyId", funcParams[0]["left"])
		assertEquals("partyId", funcParams[0]["right"])
	}
}
