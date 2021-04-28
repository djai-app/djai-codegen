package pro.bilous.codegen.process.strateges

object MySqlTypeResolvingStrategy : DefaultTypeResolvingStrategy() {

	private val DATA_TYPES = mapOf(
		"TINYBLOB" to "\${TINYBLOB_OBJECT}",
		"TEXT" to "\${TEXT_OBJECT}",
		"TINYTEXT" to "\${TINYTEXT_OBJECT}",
		"BLOB" to "\${BLOB}",
		"MEDIUMBLOB" to "\${MEDIUMBLOB_OBJECT}",
		"MEDIUMTEXT" to "\${MEDIUMTEXT_OBJECT}",
		"LONGBLOB" to "\${LONGBLOB_OBJECT}",
		"LONGTEXT" to "\${LONGTEXT_OBJECT}"
	)

	private const val MAX_SIZE_FOR_VARCHAR = 21844

	override fun resolveStringTypeWithSize(size: Int): ColumnTypePare {
		return if (size <= MAX_SIZE_FOR_VARCHAR) {
			ColumnTypePare("VARCHAR(${size})", null)
		} else {
			ColumnTypePare("TEXT", DATA_TYPES["TEXT"])
		}
	}

	override fun resolveStringTypeWithFormat(format: String): ColumnTypePare? {
		val columnType = format.trim().toUpperCase()
		val columnDefinition = DATA_TYPES[columnType]
		return if (columnDefinition != null) {
			ColumnTypePare(columnType, columnDefinition)
		} else {
			null
		}
	}
}
