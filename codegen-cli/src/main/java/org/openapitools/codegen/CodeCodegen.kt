package org.openapitools.codegen

import com.google.common.collect.Maps
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.servers.Server
import org.openapitools.codegen.languages.AbstractJavaCodegen
import org.openapitools.codegen.utils.ModelUtils
import org.openapitools.codegen.utils.StringUtils.camelize
import org.slf4j.LoggerFactory
import pro.bilous.codegen.process.*
import pro.bilous.codegen.process.filename.ModelFileNameArgs
import pro.bilous.codegen.process.filename.ModelFileNameResolver
import pro.bilous.codegen.process.models.AllModelsProcessor
import pro.bilous.codegen.process.models.CommonModelsProcessor
import pro.bilous.codegen.writer.CodegenFileWriter
import java.io.File

open class CodeCodegen : AbstractJavaCodegen() {
	companion object {
		private val log = LoggerFactory.getLogger(CodeCodegen::class.java)

		// Version to control backward compatibility, when project is already generated using the latest version
		// there is possible to set lock to disallow generation by lower version of codegen
		const val DJET_VERSION_LOCK = 1
		const val TITLE = "title"
		const val SERVER_PORT = "serverPort"
		const val BASE_PACKAGE = "basePackage"
		const val RESPONSE_WRAPPER = "responseWrapper"
		const val USE_TAGS = "useTags"
		const val IMPLICIT_HEADERS = "implicitHeaders"
		const val OPENAPI_DOCKET_CONFIG = "swaggerDocketConfig"
		const val DB_NAME = "dbName"
		const val BINDING_KEY = "addBindingEntity"
		const val DEFAULT_MODULE_PREFIX_NAME = "app-"

		fun resolveModulePrefixName(properties: Map<String, Any>): String {
			val generationProperty = properties["generation"] as? Map<String, Any>
				?: return DEFAULT_MODULE_PREFIX_NAME
			return generationProperty["modulePrefixName"] as? String ?: DEFAULT_MODULE_PREFIX_NAME
		}
	}

	var fileWriter: CodegenFileWriter? = null

	fun isEnableMerge(): Boolean {
		return additionalProperties.containsKey("enableMerge") && additionalProperties["enableMerge"] as Boolean
	}

	val modulePrefixName: String by lazy { resolveModulePrefixName(additionalProperties) }

	fun getOpenApi() = openAPI

	open fun findOpenApi() = openAPI

	private var modelPropertyProcessor: ModelPropertyProcessor
	private var title = "RESTful Application"

	fun getTitle() = title

//	override fun embeddedTemplateDir(): String {
//		return "$templateDir-embed"
//	}

	var basePackage = "code"
	private var configPackage: String? = null
	fun getConfigPackage() = configPackage
	fun setConfigPackage(it: String) {
		configPackage = it
	}

	var repositoryPackage: String? = null
	var entityPackage: String? = null
	var converterPackage: String? = null
	var servicePackage: String? = null
	var mapperPackage: String? = null
	var validationPackage: String? = null

	private var responseWrapper = ""

	fun getResponseWrapper() = responseWrapper

	private var useTags = true
	private var implicitHeaders = false
	fun isImplicitHeader() = implicitHeaders

	private var openapiDocketConfig = false
	var enableSubModules = true

	var hasEventMapping = true

	private var apiSuffix = "Api"
	var entityMode = false
	var dbName = "servicedb"

	var specIndex: Int = 0

	val metadataEnums = Maps.newHashMap<String, CodegenModel>()

	val modelFolder: String
		get() = if (entityMode) entityFolder else getFolder(modelPackage, "-domain-model")

	val apiFolder: String
		get() = if (entityMode) repositoryFolder else getFolder(apiPackage, "-resources")

	val apiTestFolder: String
		get() = getTestFolder(apiPackage, "-resources")

	val apiIntegrationTestFolded: String
		get() = getIntegrationTestFolder(apiPackage, "-resources")

	val repositoryFolder: String
		get() = getFolder(repositoryPackage, "-data-manager")

	val entityFolder: String
		get() = getFolder(entityPackage, "-data-manager")

	val apiConfigurationFolder: String
		get() = getFolder(configPackage, "-resources")

	val dataManagerConfigurationFolder: String
		get() = getFolder(configPackage, "-data-manager")

	val servicesFolder: String
		get() = getFolder("$basePackage.services", "-services")

	val databaseFolder: String
		get() {
			val subFolder = if (enableSubModules) "$artifactId-database" else ""
			return (subFolder + File.separator + "src/main/resources").fixDot()
		}

	init {

		outputFolder = "generated-code/javaSpring"
		templateDir = "kotlin-codegen"
		embeddedTemplateDir = templateDir
		apiPackage = "$basePackage.controller"
		modelPackage = "$basePackage.restmodel"
		invokerPackage = "$basePackage.controllers"
		artifactId = "service-management"
		modelNameSuffix = if (entityMode) "" else "Model"

		// clioOptions default redifinition need to be updated
		updateOption(CodegenConstants.INVOKER_PACKAGE, this.getInvokerPackage())
		updateOption(CodegenConstants.ARTIFACT_ID, this.getArtifactId())
		updateOption(CodegenConstants.API_PACKAGE, apiPackage)
		updateOption(CodegenConstants.MODEL_PACKAGE, modelPackage)
		updateOption(CodegenConstants.MODEL_NAME_SUFFIX, modelNameSuffix)

		// spring uses the jackson lib
		additionalProperties["jackson"] = "true"

		cliOptions.add(CliOption(TITLE, "server title name or client service name").defaultValue(title))
		cliOptions.add(CliOption(BASE_PACKAGE, "base package (invokerPackage) for generated code").defaultValue(this.basePackage))
		cliOptions.add(CliOption(RESPONSE_WRAPPER, "wrap the responses in given type (Future,Callable,CompletableFuture,ListenableFuture,DeferredResult,HystrixCommand,RxObservable,RxSingle or fully qualified type)"))
		cliOptions.add(CliOption.newBoolean(USE_TAGS, "use tags for creating interface and controller classnames", useTags))
		cliOptions.add(CliOption.newBoolean(IMPLICIT_HEADERS, "Skip header parameters in the generated API methods using @ApiImplicitParams annotation.", implicitHeaders))
		cliOptions.add(CliOption.newBoolean(OPENAPI_DOCKET_CONFIG, "Generate Spring OpenAPI Docket configuration class.", openapiDocketConfig))
		//cliOptions.add(CliOption.newBoolean(SUB_MODULES, "Generate sub modules", enableSubModules));
		cliOptions.add(CliOption(DB_NAME, "database name for generated code").defaultValue(dbName))
		cliOptions.add(CliOption.newBoolean(BINDING_KEY, "add Binding entity support to the service", false))
		modelPropertyProcessor = ModelPropertyProcessor(this)
	}

	public override fun findMethodResponse(responses: ApiResponses): ApiResponse? {
		return super.findMethodResponse(responses)
	}

	override fun getTag() = CodegenType.SERVER
	override fun getName() = "bhn-codegen"
	override fun getHelp() = "Generates a Java SpringBoot Server application using the SpringFox integration."

	override fun processOpts() {
		checkVersionLock()
		OptsPreProcessor(this).process()
		super.processOpts()
		OptsPostProcessor(this).processOpts()
	}

	override fun addOperationToGroup(tag: String, resourcePath: String?, operation: Operation?, co: CodegenOperation, operations: MutableMap<String, List<CodegenOperation>>) {
		if (!useTags) {
			var basePath = resourcePath
			if (basePath!!.startsWith("/")) {
				basePath = basePath.substring(1)
			}
			val pos = basePath.indexOf("/")
			if (pos > 0) {
				basePath = basePath.substring(0, pos)
			}

			if (basePath == "") {
				basePath = "default"
			} else {
				co.subresourceOperation = co.path.isNotEmpty()
			}
			val opList = operations.computeIfAbsent(basePath) { listOf() }.toMutableList()
			opList.add(co)
			co.baseName = basePath
		} else {
			super.addOperationToGroup(tag, resourcePath, operation, co, operations)
		}
	}

	override fun preprocessOpenAPI(openAPI: OpenAPI?) {
		super.preprocessOpenAPI(openAPI)
		OpenApiProcessor(this).preprocessOpenAPI(openAPI!!)
	}

	override fun postProcessOperationsWithModels(objs: MutableMap<String, Any>, allModels: List<Any>?): Map<String, Any> {
		return OperationsWithModelsProcessor(this)
			.postProcessOperationsWithModels(objs, allModels!!)
	}

	override fun postProcessSupportingFileData(objs: MutableMap<String, Any>): Map<String, Any> {
		generateYAMLSpecFile(objs)
		objs["metadataEnums"] = metadataEnums.values
		return objs
	}

	override fun toApiName(name: String): String {
		if (name.isEmpty()) {
			return "Default$apiSuffix"
		}
		return camelize(sanitizeName(name)) + apiSuffix
	}

	override fun setParameterExampleValue(p: CodegenParameter) {
		var type: String? = p.baseType
		if (type == null) {
			type = p.dataType
		}
		if ("File" == type) {
			var example = if (p.defaultValue == null) p.example else p.defaultValue

			if (example == null) {
				example = "/path/to/file"
			}
			example = "new org.springframework.core.io.FileSystemResource(new java.io.File(\"" + escapeText(example) + "\"))"
			p.example = example
		} else {
			super.setParameterExampleValue(p)
		}
	}

	fun setTitle(title: String) {
		this.title = title
	}

	fun setResponseWrapper(responseWrapper: String) {
		this.responseWrapper = responseWrapper
	}

	fun setUseTags(useTags: Boolean) {
		this.useTags = useTags
	}

	fun setImplicitHeaders(implicitHeaders: Boolean) {
		this.implicitHeaders = implicitHeaders
	}

	fun setOpenapiDocketConfig(openapiDocketConfig: Boolean) {
		this.openapiDocketConfig = openapiDocketConfig
	}

	fun setApiSuffix(apiSuffix: String) {
		this.apiSuffix = apiSuffix
	}

	override fun fromModel(name: String, model: Schema<*>): CodegenModel {
		return FromModelProcessor(this)
			.process(super.fromModel(name, model))
	}

	override fun fromProperty(name: String?, p: Schema<*>?): CodegenProperty {
		return FromPropertyProcessor(this)
			.process(name, p, super.fromProperty(name, p))
	}

	override fun fromOperation(path: String, httpMethod: String, operation: Operation, servers: List<Server>?): CodegenOperation {
		val codegenOperation = super.fromOperation(path, httpMethod, operation, servers)

		// try to fetch baseType with DifHub issue (wrong model)
		if (codegenOperation.returnBaseType == null) {
			OperationResponseResolver(this)
				.resolve(operation, codegenOperation)
		}
		codegenOperation.imports.remove("Error")
		codegenOperation.imports.remove("List")

		return codegenOperation
	}

	override fun postProcessModelProperty(model: CodegenModel, property: CodegenProperty) {
		super.postProcessModelProperty(model, property)

		ModelPropertyProcessor(this)
			.postProcessModelProperty(model, property)
	}

	fun getImportMappings() = importMapping

	override fun postProcessModelsEnum(objs: Map<String, Any>): Map<String, Any> {
		return ModelsEnumPostProcessor(this).process(super.postProcessModelsEnum(objs))
	}

	override fun getSchemaType(p: Schema<*>?): String {
		val schema = ModelUtils.getReferencedSchema(openAPI, p)
		if (schema.type == "string" && schema.enum != null && schema.enum.isNotEmpty()) {
			return super.getSchemaType(p).removeSuffix(modelNameSuffix)
		}
		return super.getSchemaType(p)
	}

	override fun apiFilename(templateName: String, tag: String): String {
		val suffix = apiTemplateFiles()[templateName]

		val outSrc = "$outputFolder/$modulePrefixName$artifactId/src/main/kotlin"

		val appPackageName = additionalProperties["appPackage"] as? String
			?: throw IllegalArgumentException("invalid package name - it should be a string")

		val appPackage = appPackageName.fixDot()

		val result = if (templateName.endsWith("/service.mustache")) {
			"$outSrc/$appPackage/service/${tag}"
		} else if (templateName.endsWith("/repository.mustache")) {
			"$outSrc/$appPackage/repository/${tag}"
		} else if (templateName.endsWith("/api.mustache")) {
			"$outSrc/$appPackage/controller/api/${tag}"
		} else if (templateName.endsWith("/apiController.mustache")) {
			"$outSrc/$appPackage/controller/${tag}"
		} else if(templateName.endsWith("/apiControllerDelegate.mustache")) {
			"$outSrc/$appPackage/controller/${tag}"
		} else {
			apiFileFolder() + File.separator + toApiFilename(tag)
		}
//		if (templateName == "resources/mapper.mustache") {
//			return this.outputFolder + File.separator + getFolder("$basePackage.mapper", "-resources") + File.separator + tag + "Mapper" + suffix
//		}
//		if (templateName == "resources/converter.mustache") {
//			return this.outputFolder + File.separator + getFolder("$basePackage.converters", "-resources") + File.separator + tag + "Converter" + suffix
//		}
//		return if (templateName == "resources/validationRules.mustache") {
//			this.outputFolder + File.separator + getFolder("$basePackage.validation.rules", "-resources") + File.separator + tag + "ValidationRule" + suffix
//		} else
		return result.fixSeparator() + suffix
	}

	override fun apiFileFolder() = resolveFolder(apiFolder)

	override fun apiTestFileFolder() = resolveFolder(apiTestFolder)

	private fun apiIntegrationTestFolder() = resolveFolder(apiIntegrationTestFolded)

	override fun modelFileFolder() = resolveFolder(modelFolder)

	private fun resolveFolder(folder: String): String {
		return (outputFolder + File.separator + folder).fixSeparator()
	}

	/** Add trailing dashes to allow tests overwrite by codegen
	 * see why **[DefaultGenerator.generateApis]** block code where __generateApiTests__ used
	 **/
	override fun apiTestFilename(templateName: String, tag: String): String {
		val suffix = apiTestTemplateFiles()[templateName] +
			if (additionalProperties.getOrDefault("overwriteTests", false) as Boolean) {
				"__"
			} else ""
		val fileFolder = if (templateName.startsWith("resources/integration-test")) {
			apiIntegrationTestFolder()
		} else apiTestFileFolder()
		return fileFolder + File.separator + toApiTestFilename(tag) + suffix
	}

	override fun toApiTestFilename(name: String): String {
		return "${toApiName(name)}IT"
	}

	override fun toModelName(name: String?): String {
		val modelName =  super.toModelName(name)
		return ModelFileNameResolver(ModelFileNameArgs(
			metadataEnums,
			modelNameSuffix
		)).resolve(modelName)
	}

	private fun getFolder(sourcePackage: String?, subModule: String): String {
		val subFolder = "$modulePrefixName$artifactId"
		return (subFolder + File.separator + "src/main/kotlin" + File.separator + sourcePackage).fixDot().fixSeparator()
	}

	fun getTestFolder(sourcePackage: String?, subModule: String): String {
		val subFolder = "$modulePrefixName$artifactId"
		val rightSourcePkg = sourcePackage?.replace("repository", "controller")
		return (subFolder + File.separator + "src/test/kotlin" + File.separator + rightSourcePkg).fixDot().fixSeparator()
	}

	fun getIntegrationTestFolder(sourcePackage: String, subModule: String): String {
		val subFolder = if (enableSubModules) artifactId + subModule else ""
		return (subFolder + File.separator + "src/integration-test/kotlin" + File.separator + sourcePackage).fixDot().fixSeparator()
	}

	override fun postProcessAllModels(objs: MutableMap<String, Any>): MutableMap<String, Any> {
		return AllModelsProcessor().process(super.postProcessAllModels(objs))
	}

	override fun toModelImport(name: String): String {
		val processor = CommonModelsProcessor(additionalProperties)
		if (processor.canResolveImport(name)) {
			return processor.resolveImport(name)
		}
		return super.toModelImport(name)
	}

	fun addSupportFile(source: String, folder: String = "", target: String, condition: Boolean = true) {
		if (condition) {
			supportingFiles.add(SupportingFile(source, folder.fixDot(), target))
		}
	}

	override fun postProcess() {
		addVersionCode()
		println("################################################################################")
		println("# Thanks for using DJet Codegen.                                               #")
		println("# Please star this project https://github.com/DJetCloud/djet-codegen \uD83D\uDE4F        #")
		println("# Project site https://djet.cloud                                              #")
		println("################################################################################")
	}

	private fun getVersionLockPath(): String {
		return outputFolder() + File.separator + "djet" + File.separator + "djet.lock"
	}

	private fun addVersionCode() {
		File(getVersionLockPath()).apply {
			writeText("versionLock=$DJET_VERSION_LOCK")
		}
	}

	private fun checkVersionLock() {
		val versionFile = File(getVersionLockPath())
		// bypass validation for existing projects.
		if (!versionFile.exists()) {
			return
		}
		val projectVersionLock = versionFile.readText().trim().removePrefix("versionLock=").toInt()
		if (projectVersionLock > DJET_VERSION_LOCK) {
			throw IllegalStateException("""
				Your project was already generated using version: ${projectVersionLock}.
				But your current djet-codegen version is: ${DJET_VERSION_LOCK}.
				Please checkout new version of djet-codegen to continue work on the Project.
			""")
		}
	}

	private fun String.fixSeparator() = this.replace('/', File.separatorChar)

	private fun String.fixDot() = this.replace('.', File.separatorChar)
}
