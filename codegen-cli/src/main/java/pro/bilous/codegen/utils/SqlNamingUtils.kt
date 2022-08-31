package pro.bilous.codegen.utils

object SqlNamingUtils {

	private val sqlNamesToEscape = setOf(
		"all",					"and",				"any",				"array",			"as",
		"asymmetric",			"authorization",

		"between",				"both",

		"case",					"cast",				"check",			"condition",		"constraint",
		"create",				"cross",			"current_catalog",	"current_date",		"current_path",
		"current_role",			"current_schema",	"current_time",		"current_timestamp","current_user",

		"day",					"default",			"distinct",			"drop",

		"else",					"end",				"except",			"exists",

		"false",				"fetch",			"for",				"foreign",			"from",
		"full",					"function",

		"group",				"groups",

		"having",				"hour",

		"if",					"in",				"inner",			"intersect",
		"interval",				"is",

		"join",

		"key",

		"leading",				"left",				"like",				"limit",			"localtime",
		"localtimestamp",

		"minus",				"minute",			"month",			"main",
		"natural",				"not",				"null",

		"offset",				"on",				"open",				"option",			"or",
		"order",				"over",

		"partition",			"primary",			"procedure",
		"qualify",

		"range",				"rank",				"regexp",			"right",			"row",
		"rownum",				"rows",

		"second",				"select",			"session_user",		"set",				"some",
		"symmetric",			"system",			"system_user",

		"table",				"to",				"top",				"trailing",			"true",

		"uescape",				"union",			"unique",			"unknown",			"use",
		"user",					"using",

		"value",				"values",

		"when",					"where",			"window",			"with",

		"year"
	)

	private val columnNamesToEscape = sqlNamesToEscape
	fun escapeColumnNameIfNeeded(columnName: String): String {
		return if (columnNamesToEscape.contains(columnName)) "${columnName}_" else columnName
	}

	private val tableNamesToEscape = sqlNamesToEscape
	fun escapeTableNameIfNeeded(tableName: String): String {
		return if (tableNamesToEscape.contains(tableName)) "${tableName}_" else tableName
	}

}
