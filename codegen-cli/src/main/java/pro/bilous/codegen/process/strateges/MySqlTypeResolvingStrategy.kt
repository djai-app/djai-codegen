package pro.bilous.codegen.process.strateges

import org.openapitools.codegen.CodegenProperty

object MySqlTypeResolvingStrategy : DefaultTypeResolvingStrategy() {

	private val DATA_TYPES = setOf(
		"TINYBLOB", "TEXT", "TINYTEXT", "BLOB", "MEDIUMBLOB", "MEDIUMTEXT", "LONGBLOB", "LONGTEXT"
	)

	private const val MAX_SIZE_FOR_VARCHAR = 21844

	override fun resolveNoSizeStringType(property: CodegenProperty, defaultStringSize: Int): String {
		return "VARCHAR(${defaultStringSize})"
	}

	override fun resolveStringTypeWithSize(size: Int, defaultStringSize: Int): String {
		return if (size <= 0) {
			"VARCHAR(${defaultStringSize})"
		} else if (size <= MAX_SIZE_FOR_VARCHAR) {
			"VARCHAR(${size})"
		} else {
			"TEXT"
		}
	}

	override fun resolveStringTypeWithFormat(format: String): String? {
		val type = format.trim().toUpperCase()
		return if (DATA_TYPES.contains(type)) type else null
	}
}
