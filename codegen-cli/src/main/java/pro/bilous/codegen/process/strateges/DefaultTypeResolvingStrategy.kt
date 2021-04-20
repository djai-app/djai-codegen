package pro.bilous.codegen.process.strateges

import org.openapitools.codegen.CodegenProperty

open class DefaultTypeResolvingStrategy {

	companion object {
		const val DEFAULT_STRING_SIZE = 255
		const val USAGE_DESCRIPTION_NAME = "Description"
		const val SIZE_FOR_DESCRIPTION = 4096
	}

	fun resolvePropertyType(property: CodegenProperty, defaultStringSize: Int?) {
		val resolvedDefaultStringSize = defaultStringSize ?: DEFAULT_STRING_SIZE
		when (property.datatypeWithEnum) {
			"Boolean", "Boolean?" -> {
				property.vendorExtensions["columnType"] = "\${BOOLEAN_VALUE}"
				property.vendorExtensions["hibernateType"] = "java.lang.Boolean"
				property.isBoolean = true
			}
			"Date", "Date?" -> {
				property.vendorExtensions["columnType"] = "datetime"
				property.vendorExtensions["hibernateType"] = "java.util.Date"
				property.isDate = true
			}
			"Int", "Int?" -> {
				property.vendorExtensions["columnType"] = "int"
				property.isInteger = true
			}
			"BigDecimal", "BigDecimal?" -> {
				property.vendorExtensions["columnType"] = "decimal(10,2)"
				property.isNumber = true
			}
			"Long", "Long?" -> {
				property.vendorExtensions["columnType"] = "bigint"
				property.isNumber = true
			}
			else -> {
				resolveUndefinedType(property, resolvedDefaultStringSize)
			}
		}
	}

	private fun resolveUndefinedType(property: CodegenProperty, resolvedStringSize: Int) {
		property.vendorExtensions["columnType"] =
			if (property.maxLength != null && property.maxLength > 0) {
				"VARCHAR(${property.maxLength})"
//			} else if (property.vendorExtensions["x-format"] != null) {
			} else
				if (property.vendorExtensions["x-usage"] == USAGE_DESCRIPTION_NAME) {
				"VARCHAR(${SIZE_FOR_DESCRIPTION})"
			} else resolveToStringWhenSizeIsUndefined(property, resolvedStringSize)

		property.vendorExtensions["hibernateType"] = "java.lang.String"
	}

	protected open fun resolveToStringWhenSizeIsUndefined(property: CodegenProperty, defaultStingSize: Int): String {
		return "VARCHAR($defaultStingSize)"
	}

}
