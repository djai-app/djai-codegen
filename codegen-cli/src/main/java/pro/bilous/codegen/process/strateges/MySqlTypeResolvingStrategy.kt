package pro.bilous.codegen.process.strateges

object MySqlTypeResolvingStrategy : DefaultTypeResolvingStrategy() {

	private val DATA_TYPES = setOf(
		"TINYBLOB", "TEXT", "TINYTEXT", "BLOB", "MEDIUMBLOB", "MEDIUMTEXT", "LONGBLOB", "LONGTEXT"
	)

	private const val MAX_SIZE_FOR_VARCHAR = 21844

	override fun resolveNoSizeStringType(defaultStringSize: Int): String {
		return resolveStringTypeWithSize(defaultStringSize)
	}

	override fun resolveStringTypeWithSize(size: Int): String {
		return if (size <= MAX_SIZE_FOR_VARCHAR) "VARCHAR(${size})" else "TEXT"
	}

	override fun resolveStringTypeWithFormat(format: String): String? {
		val type = format.trim().toUpperCase()
		return if (DATA_TYPES.contains(type)) type else null
	}
}
