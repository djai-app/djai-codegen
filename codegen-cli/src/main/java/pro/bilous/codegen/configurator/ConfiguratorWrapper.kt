package pro.bilous.codegen.configurator

import org.openapitools.codegen.ClientOptInput
import pro.bilous.codegen.core.ICustomConfigurator
import pro.bilous.codegen.core.IGenerateInvoker
import org.slf4j.LoggerFactory

class ConfiguratorWrapper(
	val instance: ICustomConfigurator,
	private val generateInvoker: IGenerateInvoker
) {


	companion object  {
		private val log = LoggerFactory.getLogger(ConfiguratorWrapper::class.java)
		private val supportedFmt = arrayOf("json", "yaml", "yml")
	}

	fun generate() {
		val settings = instance.getCustomSettings() ?: throw IllegalArgumentException("Settings file is required")

		val specPath = instance.getSpecCopyValue()
		val props = settings.dynamicProperties["application"]
		val apps = mutableListOf<String>()
		if (props is List<*> && specPath.endsWith("/")) {
			apps.addAll(props.map { it.toString().lowercase() })
		} else if(props is List<*> && supportedFmt.contains(specPath.split(".").last())) {
			apps.addAll(props.map { it.toString().lowercase() })
		}

		val basePackage =  settings.dynamicProperties["basePackage"].toString()
		val database = settings.dynamicProperties["database"]?.toString() ?: "MySQL"

		val system = settings.dynamicProperties["system"]!!.toString()
		instance.setCustomProperty("systemLower", system.toLowerCase())
		instance.setCustomProperty("appsLower", apps)

		val appList = mutableListOf<Map<String, Any>>()
		apps.forEachIndexed { index, name ->
			appList.add(mapOf(
				"name" to name,
				"port" to (8040 + index).toString()
			))
		}
		instance.setCustomProperty("appsMap", appList)

		instance.setCustomProperty("database",
			DatabaseResolver.getByType(database)
		)
		apps.forEachIndexed { index, appName ->
			try {
				generateOne(GenerateArgs(index, appName, specPath, basePackage))
				log.info("Code generation for spec $appName completed")
			} catch (error: Throwable) {
				log.error("Failed generation for the app $appName", error)
				throw IllegalArgumentException(error)
			}
		}
	}

	private fun generateOne(args: GenerateArgs) {
		val app = args.appName.toLowerCase()
		val inputSpecFile = if (supportedFmt.contains(args.specDir.split(".").last())) {
			args.specDir
		} else {
			"${args.specDir}$app-api.yaml"
		}
		instance.setCustomInputSpec(inputSpecFile)
		instance.setCustomArtifactId(app)
		instance.setCustomProperty("appPackage", "${args.basePackage}.$app")
		instance.setCustomProperty("appRealName", args.appName.capitalize())
		instance.setCustomProperty("appNameLower", app)

		val entityOptInput: ClientOptInput = instance.toCustomClientOptInput()
		generateInvoker.invoke(args.index, entityOptInput)
	}

	data class GenerateArgs(
		val index: Int,
		val appName: String,
		val specDir: String,
		val basePackage: String
	)
}
