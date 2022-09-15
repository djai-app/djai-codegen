package pro.bilous.difhub.convert

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.slf4j.LoggerFactory
import pro.bilous.difhub.config.Config
import pro.bilous.difhub.load.*
import pro.bilous.difhub.model.Model
import java.lang.IllegalStateException

class DifHubToSwaggerConverter(val modelLoader: IModelLoader, val config: Config) {
	private val log = LoggerFactory.getLogger(DifHubToSwaggerConverter::class.java)

	var appLoader = ApplicationsLoader(modelLoader, config)
	var datasetsLoader: IDatasetsLoader = DatasetsLoader(modelLoader, config)
	var interfacesLoader: IInterfacesLoader = InterfacesLoader(modelLoader, config)

	fun convertAll(): List<OpenApiData> {
		ModelLoader.clearCache()

		val appModels = appLoader.loadAll()

		val result = mutableListOf<OpenApiData>()
		appModels?.forEach {
			if (it.`object`!!.usage == "Service") {
				val appName = it.identity.name
				result.add(OpenApiData(convert(appName), appName, config.system))
			} else {
				log.warn("Ignoring application with name ${it.identity.name}. Usage =`Service` required to enable code generation.")
			}
		}
		return result
	}

	fun convert(application: String): OpenAPI {
		val openApi = OpenAPI()

		val appModel = appLoader.loadOne(application)
		if (appModel!!.`object`!!.usage != "Service") {
			throw IllegalStateException("Only Service application open for Generation. Please change Usage on Difhub!")
		}
		val appSettings = appLoader.loadAppSettings(application)
		openApi.info = readInfo(appModel)
		openApi.servers = buildServers(appSettings)

		//addPathAndDefRecursively()
		convertModelsToDefinitions(application, openApi)

		convertInterfaces(application, openApi)

		return openApi
	}

	private fun convertInterfaces(application: String, openApi: OpenAPI) {
		val interfaces = interfacesLoader.load(application)

		val paths = mutableMapOf<String, PathItem>()
		val modelsToLoad = mutableMapOf<String, String>()
		val parameters = mutableMapOf<String, Parameter>()
		val requestBodies = mutableMapOf<String, RequestBody>()
		val tags = mutableListOf<Tag>()

		interfaces
			.sortedBy { it.identity.name }
			.filter {
				it.identity.name != "Entities"
			}
			.forEach {
			val converter = InterfaceConverter(it).apply {
				convert()
			}
			converter.paths.forEach { (key, path) ->
				if (path.readOperations().isEmpty()) {
					System.err.println("Missing operations for the PATH: $key, ignoring path...")
				} else {
					paths[key] = path
				}
			}
			val tag = if (it.`object`?.tags.isNullOrEmpty()) {
				it.identity
			} else {
				it.`object`!!.tags!!.first()
			}
			tags.add(Tag().name(tag.name).description(
				tag.description ?: it.identity.description
			))
			modelsToLoad.putAll(converter.pathModels)
			parameters.putAll(converter.parameters)
			requestBodies.putAll(converter.requestBodies)
		}

		paths.forEach {
			openApi.path(it.key, it.value)
		}
		tags.forEach {
			if (openApi.tags == null) {
				openApi.tags = mutableListOf()
			}
			if (!openApi.tags.any { tag -> tag.name == it.name }) {
				openApi.addTagsItem(it)
			}
		}
		modelsToLoad.forEach {
			val model = modelLoader.loadModel(it.value, config.datasetStatus)
			if (model != null) {
				addDefRecursively(model, openApi)
			} else {
				System.err.println("Model failed when load: ${it.value}")
			}
		}
		parameters.forEach{
			openApi.components.addParameters(it.key, it.value)
		}
		requestBodies.forEach{
			openApi.components.addRequestBodies(it.key, it.value)
		}
	}

	private fun convertModelsToDefinitions(application: String, openApi: OpenAPI) {
		val datasets = datasetsLoader.load(application, type = "Resource")

		datasets?.forEach {
			addDefRecursively(it, openApi)
		}
	}

	private val processedDefinitions = mutableMapOf<String, MutableList<String>>()
	//private val innerPaths = mutableListOf<Model>()

	private fun addDefRecursively(source: Model, openApi: OpenAPI) {
		val definition = processedDefinitions.getOrPut(openApi.info.title) { mutableListOf() }

		val targetName = normalizeTypeName(source.identity.name)
		if (definition.contains(targetName)) {
			return
		}

		definition.add(targetName)
		val defConverter = DefinitionConverter(modelLoader, source, config.datasetStatus)
		defConverter.convert()
				.forEach {
					openApi.schema(it.key, it.value)
				}
	}

	private fun buildServers(source: Model?): List<Server> {
		val defaultPort = "8080"
		val serverPort = if (source != null) {
			findServerPort(source) ?: defaultPort
		} else defaultPort

		return listOf(
			Server().apply {
				url = "http://localhost:$serverPort"
				description = "Development server"
			}
		)
	}

	private fun findServerPort(source: Model): String? {
		return source.`object`?.properties?.find {
			it.identity?.name == "serverPort"
		}?.value
	}

	private fun readInfo(source: Model): Info {
		val info = Info()
//		source.version!!
		info.version = "1.0.0" // "${source.version.major}.${source.version.minor}.${source.version.revision}"
		info.title = "${source.identity.name} API"
		info.description = "${source.identity.description}"
			.trimEnd()
			.removeSuffix("\n").removeSuffix("\n").removeSuffix("\t")

		if (!source.`object`?.alias.isNullOrEmpty()) {
			info.extensions = mapOf("x-app-alias" to source.`object`?.alias)
		}

		return info
	}
}
