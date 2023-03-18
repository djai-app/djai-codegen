package pro.bilous.codegen.exec

data class ExecSettings(
	val projectPath: String,
	val specFilePath: String,
	val configFile: String,
	val additionalSettings: Map<String, String> = mutableMapOf()
)
