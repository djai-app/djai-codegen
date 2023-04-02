package pro.bilous.codegen.process

import io.swagger.v3.oas.models.media.Schema
import org.apache.commons.lang3.StringUtils
import org.openapitools.codegen.*
import org.slf4j.LoggerFactory

class OperationAddon(val codegen: CodeCodegen) {

	companion object {
		private val log = LoggerFactory.getLogger(OperationAddon::class.java)
	}

	@Suppress("UNCHECKED_CAST")
	fun populate(objs: MutableMap<String, Any>) {
		val operations = objs["operations"] as MutableMap<String, Any>
		val ops = operations["operation"] as MutableList<CodegenOperation>
		populateClassnames(objs, operations, ops)

		val returnType = objs["returnEntityType"] as String

		for (operation in ops) {
			// fix operation responses
			fixOperationResponses(operation)
			fixOperationParams(operation)
			// set summary as operationId
			operation.vendorExtensions["operationMethodName"] = operation.operationId
			operation.httpMethod = StringUtils.capitalize(operation.httpMethod.toLowerCase())
			if (operation.hasConsumes) {
				operation.vendorExtensions["x-operationConsumes"] = "APPLICATION_JSON_VALUE"
			}
			operation.hasProduces = true
			operation.vendorExtensions["x-operationProduces"] = "APPLICATION_JSON_VALUE"
			populateBooleanHttpMethods(operation)
			populatePrimaryQueryParam(operation)
			populatePrimaryPathParam(operation, objs)
			populateEventMapping(operation)
			applyRequestParams(operation, returnType)
		}
		objs["hasEventMappingImport"] = ops.any {
			it.vendorExtensions[OptsPreProcessor.EVENT_MAPPING].toString().toBoolean()
		}

		applyTestModelHeaderParams(objs, ops, returnType)
	}

	private fun applyTestModelHeaderParams(objs: MutableMap<String, Any>, ops: MutableList<CodegenOperation>, returnType: String) {
		val testModel = objs["testModel"] as CodegenModel? ?: return
		val headerParamSet = mutableSetOf<HeaderParamData>()
		ops.forEach {
			headerParamSet.addAll(collectModelHeaderParams(it, returnType).toSet() )
		}

		testModel.vendorExtensions["testHeaderParams"] = headerParamSet.map {
			mapOf( "left" to it.left, "right" to "test_http_header_value", "baseName" to it.baseName)
		}
	}

	fun applyRequestParams(operation: CodegenOperation, returnType: String) {
		// we support only header params for now, query params may need support as well
		if (!operation.hasHeaderParams) {
			return
		}

		val headerParamList = collectModelHeaderParams(operation, returnType)

		if (headerParamList.isEmpty()) {
			return
		}
		val funcName = when(operation.httpMethod.lowercase()) {
			"get" -> if (operation.returnContainer == "List") {
				operation.vendorExtensions["hasSearchParams"] = true
				"searchParams"
			} else "preGet"
			"post" -> "preCreate"
			"put" -> "preUpdate"
			"patch" -> "preModify"
			"delete" -> "preDelete"
			else -> null
		}
		operation.vendorExtensions["hasPreFunc"] = true
		operation.vendorExtensions["preFuncName"] = funcName
		operation.vendorExtensions["preFuncParams"] = headerParamList
	}

	data class HeaderParamData(
		val left: String,
		val right: String,
		val baseName: String
	)

	private fun collectModelHeaderParams(operation: CodegenOperation, returnType: String): List<HeaderParamData> {
		val codegenModel = readModelByType(returnType)
		val hasEntityType = codegenModel.allVars.any { it.complexType == "ResourceEntity" }
		val optEntityModel = if (hasEntityType) {
			readModelByType("Entity")
		} else null
		val headerParamList = mutableListOf<HeaderParamData>()

		operation.headerParams.forEach {
			when {
				!it.baseName.startsWith("X-access-") && it.baseName.startsWith("X-") -> {
					val paramNameParts = it.baseName.removePrefix("X-").split("-")
					headerParamList.add(HeaderParamData(
						right = it.paramName,
						left = paramNameParts.joinToString("."),
						baseName = it.baseName
					))
				}
				codegenModel.vars.any { param -> param.name == it.paramName  } -> {
					headerParamList.add(HeaderParamData(
						right = it.paramName,
						left = it.paramName,
						baseName = it.baseName
					))
				}
				optEntityModel != null && optEntityModel.vars.any { param -> param.name == it.paramName } -> {
					headerParamList.add(HeaderParamData(
						right = it.paramName,
						left = "entity.${it.paramName}",
						baseName = it.baseName
					))
				}
			}
		}
		return headerParamList
	}

	private fun populateEventMapping(operation: CodegenOperation) {
		if (!codegen.hasEventMapping) {
			operation.vendorExtensions[OptsPreProcessor.EVENT_MAPPING] = false
			return
		}
		when (operation.httpMethod.toLowerCase()) {
			"post", "put", "patch", "delete" -> operation.vendorExtensions[OptsPreProcessor.EVENT_MAPPING] = true
			else -> operation.vendorExtensions[OptsPreProcessor.EVENT_MAPPING] = false
		}
	}

	private fun populatePrimaryQueryParam(operation: CodegenOperation) {
		if (operation.queryParams.isNotEmpty()) {
			val firstQueryParam = operation.queryParams.first()
			operation.vendorExtensions["primaryQueryParamName"] = firstQueryParam.paramName
		}
	}

	private fun populatePrimaryPathParam(operation: CodegenOperation, objs: MutableMap<String, Any>) {
		val pathParam = operation.pathParams.findLast {
			operation.path.endsWith("{${it.paramName}}")
		}
		var pathParts = operation.path.split("/")
		if (pathParts.last() == "") {
			pathParts = pathParts.dropLast(1)
		}

		if (pathParam != null) {
			operation.vendorExtensions["primaryPathParamName"] = pathParam.paramName
			pathParts = pathParts.dropLast(1)
//			if (pathParam.dataType == "String") {
//				// force system to use UUID as id
//				pathParam.dataType = "UUID"
//			}
		}
		for (pathPart in pathParts.reversed()) {
			val parentParam = operation.pathParams.findLast { pathPart == "{${it.paramName}}" }
			if (parentParam != null) {
				operation.vendorExtensions["parentPathParamName"] = parentParam.paramName
				operation.vendorExtensions["hasPathParent"] = true
				objs["hasPathParent"] = true
				objs["parentPathParamName"] = parentParam.paramName
				// add also test version of controller path
				val controllerPath = objs["controllerPath"].toString()
				val testPath = controllerPath.replace("{${parentParam.paramName}}", "parent-id")
				// also remove all curly braces
				objs["testParentControllerPath"] = testPath.replace("{", "").replace("}", "")
				break
			}
		}

	}

	private fun populateBooleanHttpMethods(operation: CodegenOperation) {
		val extensionMethod = when (operation.httpMethod.toLowerCase()) {
			"get" -> if (operation.returnContainer == "List") "isGetListMethod" else "isGetMethod"
			"post" -> "isPostMethod"
			"put" -> "isPutMethod"
			"patch" -> "isPatchMethod"
			"delete" -> "isDeleteMethod"
			else -> "isNoHttpMethod"
		}
		operation.vendorExtensions[extensionMethod] = true
	}

	private fun fixOperationResponses(operation: CodegenOperation) {
		for (response in operation.responses) {
			if (response.message.isNullOrEmpty()) {
				if ("200" == response.code) {
					response.message = "Success response"
					if (response.baseType == null) {
						response.baseType = operation.returnBaseType
						//response.containerType = operation.returnContainer;
					}
				} else if ("400" == response.code) {
					response.message = "Something goes wrong"
					response.baseType = "ErrorResponse"
				} else if ("404" == response.code) {
					response.message = "Resource not found"
					response.baseType = "ErrorResponse"
				}
			} else {
				response.message = response.message.trimEnd()
				if (response.baseType == "Error") {
					response.baseType = "ErrorResponse"
				}
			}
		}
	}

	fun fixOperationParams(operation: CodegenOperation) {
		removeUnnecessaryParams(operation)
		removeSearchParamFromNoReturnListOperation(operation)
		resolveParamsDataTypes(operation)
		addPageParamForGetListOperation(operation)
	}

	private fun removeUnnecessaryParams(operation: CodegenOperation) {
		operation.allParams.removeIf { it.paramName == "bearer" }
		operation.allParams.removeIf {
			it.isQueryParam &&
					arrayOf("options", "startTime", "endTime").contains(it.paramName)
		}
	}

	private fun removeSearchParamFromNoReturnListOperation(operation: CodegenOperation) {
		if (operation.returnContainer != "List") {
			operation.queryParams.removeIf { it.paramName == "search" }
			operation.allParams.removeIf { it.paramName == "search" }
		}
	}

	private fun resolveParamsDataTypes(operation: CodegenOperation) {
		operation.allParams.forEach {
			resolveParamDataType(it)
			setDataTypeNullableForNotRequiredParam(it)
		}
	}

	private fun addPageParamForGetListOperation(operation: CodegenOperation) {
		if (operation.httpMethod.toLowerCase() == "get" && operation.isArray) {
			val pageParameter = CodegenParameter()
				.apply {
					dataType = "Pageable"
					baseType = "Pageable"
					paramName = "page"
					baseName = "page"
					isPrimitiveType = false
					isInteger = false
					vendorExtensions["isPageParam"] = true
				}
			operation.allParams.add(pageParameter)
		}
	}

	private fun resolveParamDataType(param: CodegenParameter) {
		param.dataType = when {
			param.dataType == "Integer" -> "Int"
			param.paramName == "ids" && param.dataType == "UUID" -> "List<UUID>"
			param.paramName == "ids" && param.dataType == "String" -> "List<UUID>"
			else -> param.dataType
		}
	}

	private fun setDataTypeNullableForNotRequiredParam(param: CodegenParameter) {
		if (!param.required && param.isPrimitiveType) {
			param.dataType = "${param.dataType}?"
		}
	}

	private fun populateClassnames(
		objs: MutableMap<String, Any>,
		operations: MutableMap<String, Any>,
		ops: MutableList<CodegenOperation>
	) {
		val returnType = resolveClassname(objs, ops).removeSuffix("Model")
		objs["returnEntityType"] = returnType
		val classPrefix = operations["classname"]
			.toString().removeSuffix("Api").removeSuffix("Repository")

		objs["controllerClassname"] = classPrefix + "Controller"
		objs["serviceClassname"] = classPrefix + "Service"
		objs["repositoryClassname"] = classPrefix + "Repository"
		objs["mapperClassname"] = classPrefix + "Mapper"
		objs["converterClassname"] = classPrefix + "Converter"
		objs["controllerPath"] = findControllerPath(ops)

		addTestModel(objs, returnType)

		objs["converterLinkMethodname"] = ops.find { operation ->
			val idPathParam = operation.path.split("/").last().removePrefix("{").removeSuffix("}")
			operation.httpMethod == "GET" && operation.pathParams.any { it.paramName == idPathParam }
		}?.operationId ?: "getMethodNotFound"

		objs["validationRuleClassname"] = classPrefix + "ValidationRule"
		objs["returnModelType"] = returnType
	}

	fun addTestModel(objs: MutableMap<String, Any>, returnType: String) {
		if (returnType == "UUID") {
			return;
		}
		val testModel = findTestCodegenModel(returnType)
		if (testModel.classname == null) {
			log.error("classname for $returnType was not assigned")
			return
		}
		objs["testModel"] = testModel
		objs["hasTestModel"] = true
		applyImportsForTest(objs, testModel)
	}

	fun applyImportsForTest(objs: MutableMap<String, Any>, testModel: CodegenModel) {
		val importList = mutableListOf<Map<String, String>>()
		val mappingSet = mutableSetOf<String>()
		addImportElements(testModel, mappingSet, importList)
		objs["testImports"] = importList
	}

	private fun addImportElements(
		testModel: CodegenModel,
		mappingSet: MutableSet<String>,
		importList: MutableList<Map<String, String>>,
	) {
		for (it in testModel.vars) {
			if (!it.vendorExtensions.containsKey("testModel")) {
				continue
			}
			val inner = it.vendorExtensions["testModel"] as CodegenModel
			if (inner.classname == null) {
				log.error("addImportElements: classname for the ${inner.name} was not assigned")
				continue
			}
			if (mappingSet.contains(inner.classname)) {
				continue
			}
			mappingSet.add(inner.classname)
			val importLine = codegen.toModelImport(inner.classname)
			importList.add(mapOf("import" to importLine, "classname" to inner.classname))
			addImportElements(inner, mappingSet, importList)
		}
	}

	private fun findTestCodegenModel(returnType: String): CodegenModel {
		val model = readModelByType(returnType)
		applyTestVars(model)
		return model
	}

	private fun readModelByType(type: String): CodegenModel {
		val schemas = codegen.findOpenApi().components.schemas
		return if (schemas.containsKey(type)) {
			codegen.fromModel(type, schemas[type] as Schema<*>)
		} else {
			log.error("Cant find schema for type $type, list of schema available: ${schemas.keys}")
			return CodegenModel()
		}
	}

	fun applyTestVars(model: CodegenModel) {
		model.vars.forEach {
			if (it.vendorExtensions.containsKey("isOneToOne") && it.vendorExtensions["isOneToOne"] as Boolean) {
				val embeddedModel = readModelByType(it.complexType)
				applyTestVars(embeddedModel)
				it.vendorExtensions["testModel"] = embeddedModel
				it.vendorExtensions["hasTestModel"] = true
			} else if (it.vendorExtensions.containsKey("embeddedComponent")) {
				val embeddedModel = it.vendorExtensions["embeddedComponent"] as CodegenModel
				applyTestVars(embeddedModel)
				it.vendorExtensions["testModel"] = embeddedModel
				it.vendorExtensions["hasTestModel"] = true
			} else if (it.isArray && !it.complexType.isNullOrEmpty() && !arrayOf(
					"List<String>",
					"List<String>?"
				).contains(it.datatypeWithEnum)
			) {
				val embeddedInListModel = readModelByType(it.complexType)
				applyTestVars(embeddedInListModel)
				it.vendorExtensions["testModel"] = embeddedInListModel
				it.vendorExtensions["hasTestModel"] = true
			} else {
				it.defaultValue = when {
					!it.defaultValue.isNullOrEmpty() && it.defaultValue != "null" && it.defaultValue != "listOf()" -> {
						it.defaultValue
					}
					it.vendorExtensions["x-data-type"] == "Guid" -> "aaaaaaaa-bbbb-cccc-dddd-eeeeeeffffff"
					it.isString -> {
						val maxLength = it.maxLength
						if (it.maxLength != null && it.maxLength > 0 && it.maxLength <= 16) {
							"test string value".substring(0, maxLength)
						} else "test string value"
					}
					it.isInteger -> "8"
					it.dataType == "Date" -> {
						"Date()"
					}
					it.isBoolean -> "false"
					it.isLong -> "9223372036854775807L"
					it.dataType == "BigDecimal" -> "777.77.toBigDecimal()"
					it.isModel && arrayOf("String", "String?").contains(it.datatypeWithEnum) -> {
						it.isString = true
						"test_enum_value"
					}
					it.isArray && arrayOf("List<String>", "List<String>?").contains(it.datatypeWithEnum) -> {
						"\"test_list_string_value\""
					}
					it.isFreeFormObject && arrayOf("String", "String?").contains(it.datatypeWithEnum) -> {
						it.isString = true
						"test string (was object) value"
					}
					else -> "null"
				}
			}
			postProcessOfTestProperty(it)
		}
	}

	private fun postProcessOfTestProperty(property: CodegenProperty) {
		if (property.dataType == "BigDecimal") {
			property.vendorExtensions["isBigDecimal"] = true
		}
	}

	private fun findControllerPath(ops: List<CodegenOperation>): String {
		val path = ops.find { operation ->
			val idPathParam = operation.path.split("/").last().removePrefix("{").removeSuffix("}")
			operation.httpMethod == "GET" && operation.pathParams.any { it.paramName == idPathParam }
		}?.path ?: "/path-not-found"

		if (path.endsWith("}")) {
			return path.split("/").dropLast(1).joinToString("/")
		}
		return path
	}

	private fun resolveClassname(objs: MutableMap<String, Any>, ops: MutableList<CodegenOperation>): String {
		val imports = objs["imports"] as List<Any>
		if (imports.isNotEmpty()) {
			val importMap = imports.first() as Map<String, Any>
			if (importMap.containsKey("classname")) {
				return importMap["classname"].toString()
			}
		}
		return ops.find { it.returnType != "Void" }?.returnType ?: "Void"
	}
}
