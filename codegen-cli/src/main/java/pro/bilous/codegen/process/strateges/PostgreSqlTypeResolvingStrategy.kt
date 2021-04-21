package pro.bilous.codegen.process.strateges

object PostgreSqlTypeResolvingStrategy : DefaultTypeResolvingStrategy() {

	private val DATA_TYPES = setOf("VARCHAR", "TEXT")

	private const val MAX_SIZE_FOR_VARCHAR = 10485760

	override fun resolveNoSizeStringType(defaultStringSize: Int): String {
		return "VARCHAR"
	}

	override fun resolveStringTypeWithSize(size: Int): String {
		return if (size <= MAX_SIZE_FOR_VARCHAR) "VARCHAR(${size})" else "TEXT"
	}

	override fun resolveStringTypeWithFormat(format: String): String? {
		val type = format.trim().toUpperCase()
		return if (DATA_TYPES.contains(type)) type else null
	}
}
