package pro.bilous.codegen.process.strateges

import org.openapitools.codegen.CodegenProperty

open class DefaultTypeResolvingStrategy {

	companion object {
		const val DEFAULT_STRING_SIZE = 255
		const val USAGE_DESCRIPTION_NAME = "Description"
		const val DEFAULT_SIZE_FOR_DESCRIPTION = 4096
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

	private fun resolveUndefinedType(property: CodegenProperty, defaultStringSize: Int) {
		property.vendorExtensions["columnType"] =
			if (property.maxLength != null && property.maxLength > 0) {
				resolveStringTypeWithSize(property.maxLength, defaultStringSize)
			} else {
				val format = property.vendorExtensions["x-format"]?.let { it as String }
				format?.let { resolveStringTypeWithFormat(it) } ?: run {
					val usage = property.vendorExtensions["x-usage"]?.let { it as String }
					if (usage != null && usage == USAGE_DESCRIPTION_NAME) {
						resolveStringTypeWithSize(DEFAULT_SIZE_FOR_DESCRIPTION, defaultStringSize)
					} else {
						resolveNoSizeStringType(property, defaultStringSize)
					}
				}
			}
		property.vendorExtensions["hibernateType"] = "java.lang.String"
	}

	protected open fun resolveNoSizeStringType(property: CodegenProperty, defaultStringSize: Int): String {
		return "VARCHAR(${defaultStringSize})"
	}

	protected open fun resolveStringTypeWithSize(size: Int, defaultStringSize: Int): String {
		return if (size <= 0) {
			"VARCHAR(${defaultStringSize})"
		} else {
			"VARCHAR(${size})"
		}
	}

	protected open fun resolveStringTypeWithFormat(format: String): String? = null
}
