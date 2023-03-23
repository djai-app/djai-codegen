package pro.bilous.codegen.process

import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema
import org.openapitools.codegen.CodeCodegen
import org.openapitools.codegen.CodegenModel
import org.openapitools.codegen.CodegenProperty
import org.slf4j.LoggerFactory
import pro.bilous.codegen.configurator.Database
import pro.bilous.codegen.process.strategy.MySqlTypeResolvingStrategy
import pro.bilous.codegen.process.strategy.PostgreSqlTypeResolvingStrategy
import pro.bilous.codegen.process.strategy.DefaultTypeResolvingStrategy
import pro.bilous.codegen.utils.CamelCaseConverter
import pro.bilous.codegen.utils.SqlNamingUtils

open class ModelPropertyProcessor(val codegen: CodeCodegen) {
	companion object {
		private val log = LoggerFactory.getLogger(ModelPropertyProcessor::class.java)
	}

	private val additionalProperties = codegen.additionalProperties()
	private val entityMode = codegen.entityMode
	private val importMappings = codegen.importMapping()
	private val joinProperties: MutableList<CodegenProperty>
	private val defaultTypeResolvingStrategy = DefaultTypeResolvingStrategy()

	var openApiWrapper: IOpenApiWrapper = OpenApiWrapper(codegen)

	init {
		if (additionalProperties["joinTables"] == null) {
			additionalProperties["joinTables"] = mutableListOf<CodegenProperty>()
		}
		joinProperties = additionalProperties["joinTables"] as MutableList<CodegenProperty>
	}

	fun postProcessModelProperty(model: CodegenModel, property: CodegenProperty) {
		property.name = property.name.trimEnd()
		if ("null" == property.example) {
			property.example = null
		}
		processIfGuidOrObjectWithXDataTypesOrInteger(property)
		processIfOptional(property)
		populateTableExtension(model, property)
		resolvePropertyType(property)
		// TODO support all possible types
		property.vendorExtensions["isNeedSkip"] = "id" == property.name.toLowerCase()
		if (processIfXCodegenType(model, property)) return
		processIfListOrIdentityComplexModelEndsWithId(model, property)
		if (isEnum(property)) {
			convertToMetadataProperty(property, model)
		}
		addGuidAnnotation(property, model)
		addImportOfDateIfPropertyHasTypeDate(property, model)
	}

	private fun processIfGuidOrObjectWithXDataTypesOrInteger(property: CodegenProperty) {
		if (property.vendorExtensions["x-data-type"] == "Guid") {
			property.datatypeWithEnum = "String"
			property.dataType = "String"
			property.vendorExtensions["columnType"] = "\${UUID}"
			property.vendorExtensions["hibernateType"] = "java.lang.String"
		} else if (property.datatypeWithEnum == "Object" && property.vendorExtensions.containsKey("x-data-type")) {
			val ktType = when (property.vendorExtensions["x-data-type"]) {
				"Unsigned Integer" -> "Int"
				"Date" -> "Date"
				else -> "String"
			}
			property.dataType = ktType
			property.datatypeWithEnum = ktType
		} else if (property.datatypeWithEnum == "Integer") {
			property.dataType = "Int"
			property.datatypeWithEnum = "Int"
		}
	}

	private fun processIfOptional(property: CodegenProperty) {
		if (!property.required) {
			val realType = "${property.datatypeWithEnum}?"
			property.datatypeWithEnum = realType
		}
	}

	private fun processIfXCodegenType(model: CodegenModel, property: CodegenProperty): Boolean {
		if (property.vendorExtensions.containsKey("x-codegen-type")) {
			val codegenType = property.vendorExtensions["x-codegen-type"].toString()
			if (importMappings.containsKey(codegenType)) {
				property.datatypeWithEnum = codegenType
				model.imports.add(codegenType)
				if (codegenType == "JSONObject" && entityMode) {
					property.vendorExtensions["hibernateType"] = "com.bhn.datamanagement.usertype.JSONObjectUserType"
					property.vendorExtensions["columnType"] = "\${JSON_OBJECT}"
					model.imports.add("Type")
				}
				return true
			}
		}
		return false
	}

	private fun processIfListOrIdentityComplexModelEndsWithId(model: CodegenModel, property: CodegenProperty) {
		if (property.isArray && property.datatypeWithEnum.startsWith("List")) {
//			property.datatypeWithEnum = "Set" + property.datatypeWithEnum.removePrefix("List")
			property.defaultValue = if (property.required) "listOf()" else "null"
			model.imports.remove("List")
			model.imports.remove("ArrayList")
		} else if (property.isModel && !property.complexType.isNullOrEmpty() &&
			property.complexType.endsWith("IdentityModel") &&
			property.name.endsWith("Id")
		) {
			// convert Reference Type to the String
			property.dataType = "String"
			property.datatypeWithEnum = property.dataType
			property.isModel = false
			property.isString = true
		}
	}

	fun resolvePropertyType(property: CodegenProperty) {
		if (property.vendorExtensions["columnType"] != null) {
			return
		}
		val defaultStringSize = additionalProperties["defaultStringSize"]?.let { it as? Int }
		when (additionalProperties["database"]?.let { it as? Database }?.name) {
			"mysql" -> {
				MySqlTypeResolvingStrategy.resolvePropertyType(property, defaultStringSize)
			}
			"postgresql" -> {
				PostgreSqlTypeResolvingStrategy.resolvePropertyType(property, defaultStringSize)
			}
			else -> {
				defaultTypeResolvingStrategy.resolvePropertyType(property, defaultStringSize)
			}
		}
	}

	private fun addGuidAnnotation(property: CodegenProperty, model: CodegenModel) {
		if (property.datatypeWithEnum != "String") {
			return
		}
		val xDataType = property.vendorExtensions.getOrDefault("x-data-type", null)
		if (xDataType != null && xDataType == "Reference") {
			property.vendorExtensions["isGuid"] = true
			if (!codegen.entityMode) {
				model.imports.add("Guid")
			}
		}
	}

	fun convertToMetadataProperty(property: CodegenProperty, model: CodegenModel) {
		property.vendorExtensions["isMetadataAnnotation"] = true
		if (property.complexType != null) {
			property.vendorExtensions["metaGroupName"] =
				CamelCaseConverter.convert(property.complexType.removeSuffix("Model"))
		}
		if (property.isArray) {
			property.datatypeWithEnum = if (property.required) "List<String>" else "List<String>?"
		} else {
			property.datatypeWithEnum = if (property.required) "String" else "String?"
		}
		model.imports.removeIf { it == property.complexType }

		if (!codegen.entityMode) {
			model.imports.add("MetaDataAnnotation")
		}
	}

	fun isEnum(property: CodegenProperty): Boolean {
		val prop = if (property.isArray && property.items != null) property.items else property
		return hasEnumValues(prop)
	}

	private fun hasEnumValues(property: CodegenProperty): Boolean {
		if (property.allowableValues == null || !property.allowableValues.containsKey("values")) {
			return false
		}
		val enumValues = property.allowableValues["values"] as List<String>
		return enumValues.isNotEmpty()
	}

	private fun populateTableExtension(model: CodegenModel, property: CodegenProperty) {
		applyColumnNames(property)
		applyEmbeddedComponentOrOneToOne(model, property)

		if (entityMode && property.isArray) {
			val modelTableName = CamelCaseConverter.convert(model.name).toLowerCase()
			val complexType = readComplexTypeFromProperty(property)

			// The field 'tags' with type 'Identity' was forbidden, but now it is allowed
			// if (complexType == "Identity" && property.name == "tags") {
			//     return
			// }

			// if we do not have information for the join table. set it to JSON field
			if (complexType == null) {
				property.vendorExtensions["hasJsonType"] = true
				property.vendorExtensions["columnType"] = "\${JSON_OBJECT}"
				model.imports.add("JsonType")
				return
			}
			if (setForJsonField(model, property)) return

			val hasOtherPropertyWithSameType =
			// consider to build reference table with a custom name instead of adding _identity suffix
				// hardcode for the CodeableConcept as well, consider to implement a feature for this use case
				arrayOf("Identity", "CodeableConcept").contains(complexType)
						|| (complexType.isNotEmpty() && model.vars.any {
					it.isArray && it.name != property.name && readComplexTypeFromProperty(it) == complexType
				})

			val (propertyTableName, propertyTableColumnName, realPropertyTableName) = if (hasOtherPropertyWithSameType) {
				readManyPropertyTableData(complexType, property)
			} else {
				readSinglePropertyTableData(complexType, property)
			}

			val joinTableName = joinTableName(modelTableName, propertyTableName, !hasOtherPropertyWithSameType)

			val inverseColName =
				if (propertyTableColumnName == modelTableName) "ref_$propertyTableColumnName" else propertyTableColumnName

			property.getVendorExtensions()["modelTableName"] = SqlNamingUtils.escapeTableNameIfNeeded(modelTableName)
			property.getVendorExtensions()["propertyTableName"] = SqlNamingUtils.escapeColumnNameIfNeeded(realPropertyTableName)
			property.vendorExtensions["hasPropertyTable"] = openApiWrapper.isOpenApiContainsType(complexType)
			property.getVendorExtensions()["joinTableName"] = joinTableName
			property.getVendorExtensions()["joinColumnName"] = "${modelTableName}_id"
			property.getVendorExtensions()["inverseJoinColumnName"] = "${inverseColName}_id"
			property.vendorExtensions["isReferenceElement"] = property.complexType.isNullOrEmpty()
			property.vendorExtensions["joinReferencedColumnName"] =
				if (modelTableName == "entity") "entity_id" else "id"

			if (!joinProperties.any { it.vendorExtensions["joinTableName"] == joinTableName }) {
				joinProperties.add(property)
			}
		}
	}

	private fun setForJsonField(model: CodegenModel, property: CodegenProperty): Boolean {
		if (property.complexType != null && property.complexType != "UUID") {
			val realType = property.complexType.removeSuffix("Model")
			val innerModelSchema = codegen.getOpenApi().components.schemas[realType] as Schema<*>
			val innerModel = codegen.fromModel(realType, innerModelSchema)

			val forceToJson = innerModel.parent == null
					&& innerModel.allVars.find { it.name == "id" || it.name == "identity" } == null
			if (forceToJson) {
				property.vendorExtensions["hasJsonType"] = true
				property.vendorExtensions["columnType"] = "\${JSON_OBJECT}"
				model.imports.add("JsonType")
				return true
			}
		}
		return false
	}

	private fun readComplexTypeFromProperty(property: CodegenProperty): String? {
		return if (property.complexType.isNullOrEmpty()) {
			readTypeFromFormat(property)
		} else property.complexType
	}

	fun readSinglePropertyTableData(complexType: String, property: CodegenProperty): Triple<String, String, String> {
		return when {
			property.isArray && openApiWrapper.isOpenApiContainsType(complexType) -> {
				createTriple(complexType, complexType, complexType)
			}
			openApiWrapper.isOpenApiContainsType(complexType) -> {
				createTriple(complexType, complexType, complexType)
			}
			complexType.isNotEmpty() && property.isArray -> {
				createTriple(complexType, complexType, complexType)
			}
			else -> createTriple(property.name, property.name, property.name)
		}
	}

	fun readManyPropertyTableData(complexType: String, property: CodegenProperty): Triple<String, String, String> {
		return when {
			property.isArray && openApiWrapper.isOpenApiContainsType(complexType) -> {
				createTriple(property.name, complexType, complexType)
			}
			openApiWrapper.isOpenApiContainsType(complexType) -> {
				createTriple(complexType, complexType, complexType)
			}
			complexType.isNotEmpty() && property.isArray -> {
				createTriple(property.name, complexType, complexType)
			}
			else -> createTriple(property.name, property.name, property.name)
		}
	}

	private fun createTriple(first: String, second: String, third: String): Triple<String, String, String> {
		return Triple(
			CamelCaseConverter.convert(first).toLowerCase(),
			CamelCaseConverter.convert(second).toLowerCase(),
			CamelCaseConverter.convert(third).toLowerCase()
		)
	}

	private fun applyColumnNames(property: CodegenProperty) {
		val columnName = CamelCaseConverter.convert(property.name).toLowerCase()
		property.getVendorExtensions()["columnName"] = columnName
		property.getVendorExtensions()["escapedColumnName"] = SqlNamingUtils.escapeColumnNameIfNeeded(columnName)
		property.getVendorExtensions()["columnName"] = property.getVendorExtensions()["escapedColumnName"]
	}

	fun applyEmbeddedComponentOrOneToOne(model: CodegenModel, property: CodegenProperty) {
		if (!isInnerModel(property)) {
			return
		}
		val realType = property.complexType.removeSuffix("Model")
		val innerModelSchema = openApiWrapper.findSchema(realType)
		if (innerModelSchema == null) {
			log.error("type '$realType' is not found")
			return
		} else {
			for (prop in innerModelSchema.properties) {
				val value = prop.value
				if (value == null || value !is ArraySchema) {
					continue
				}
				if (value.items?.`$ref` == null ) {
					continue
				}
				val realPropType = value.items.`$ref`.split("/").last()
				if (realPropType == model.name) {
					log.warn("FIX THIS! This is recursive dependency! $realType vs $realPropType")
					return
				}
			}
		}
		val innerModel = codegen.fromModel(realType, innerModelSchema)
		if (innerModel.vendorExtensions["isEmbeddable"] == true) {
			assignEmbeddedModel(property, innerModel, true)
		} else if (property.name == "_extends") {
			assignExtendsModel(property, innerModel)
		} else { // assign one-to-one relationship if not isEmbeddable model (has id)
			property.vendorExtensions["isOneToOne"] = true
		}
	}

	private fun assignExtendsModel(property: CodegenProperty, innerModel: CodegenModel) {
		property.vendorExtensions["extendsComponent"] = innerModel
		property.vendorExtensions["isExtends"] = true
	}

	private fun isInnerModel(property: CodegenProperty): Boolean {
		return property.isModel && !property.complexType.isNullOrEmpty()
//				&& !importMappings.containsKey(property.nameInCamelCase)
				&& !importMappings.containsKey(property.complexType)
				&& openApiWrapper.isOpenApiContainsType(property.complexType)
				&& !isEnum(property)
	}

	fun assignEmbeddedModel(
		property: CodegenProperty,
		innerModel: CodegenModel,
		isRoot: Boolean
	) {
		property.vendorExtensions["embeddedComponent"] = innerModel
		property.vendorExtensions["isEmbedded"] = true
		var isAttributeOverrides = false
		var isAssociationOverrides = false
		// add embedded column names since we are in embedded mode.
		innerModel.vars.forEach { prop ->
			if (prop.vendorExtensions["isOneToOne"] != null) {
				isAssociationOverrides = true
			} else {
				isAttributeOverrides = true
			}
			val originalColumnName = prop.vendorExtensions["columnName"]
			val originalVarName = prop.name
			val parentColumnName = if (property.vendorExtensions.containsKey("embeddedColumnName")) {
				property.vendorExtensions["embeddedColumnName"]
			} else {
				property.vendorExtensions["columnName"]
			}
			prop.vendorExtensions["embeddedColumnName"] = if (property.name == "history") {
				originalColumnName
			} else "${parentColumnName}_${originalColumnName}"

			if (!isRoot) {
				val parentVarName = if (property.vendorExtensions.containsKey("embeddedVarName")) {
					property.vendorExtensions["embeddedVarName"]
				} else {
					property.name
				}
				prop.vendorExtensions["embeddedVarName"] = "${parentVarName}.${originalVarName}"
			}

			if (isInnerModel(prop) && prop.vendorExtensions.containsKey("embeddedComponent")) {
				assignEmbeddedModel(prop, prop.vendorExtensions["embeddedComponent"] as CodegenModel, false)
			}
		}
		property.vendorExtensions["isAttributeOverrides"] = isAttributeOverrides
		property.vendorExtensions["isAssociationOverrides"] = isAssociationOverrides
	}


	fun readTypeFromFormat(property: CodegenProperty): String? {
		val dataKey = "dataType:"
		if (property.dataFormat.isNullOrEmpty()) {
			return null
		}
		val dataFormat = property.dataFormat.replace(" ", "")
		return dataFormat.split("|")
			.first { it.startsWith(dataKey) }
			.removePrefix(dataKey)
	}

	fun joinTableName(first: String, second: String, sort: Boolean = true): String {
		return (if (sort) arrayOf(first, second).sortedArray() else arrayOf(first, second))
			.joinToString(separator = "_to_")
	}

	private fun addImportOfDateIfPropertyHasTypeDate(property: CodegenProperty, model: CodegenModel) {
		val imports = model.imports
		if(!imports.contains("Date") && property.datatypeWithEnum.removeSuffix("?") == "Date") {
			imports.add("Date")
		}
	}
}
