package pro.bilous.codegen.core

import org.openapitools.codegen.ClientOptInput
import org.openapitools.codegen.CodeCodegen
import org.openapitools.codegen.CodegenConstants
import org.openapitools.codegen.Generator

open class DataCodeGenerator(private val specIndex: Int = 0) : InCodeGenerator() {

	init {
		setGeneratorPropertyDefault(CodegenConstants.APIS, java.lang.Boolean.TRUE.toString())
		setGeneratorPropertyDefault(CodegenConstants.MODELS, java.lang.Boolean.TRUE.toString())
		setGeneratorPropertyDefault(CodegenConstants.SUPPORTING_FILES, java.lang.Boolean.TRUE.toString())
		setGeneratorPropertyDefault(CodegenConstants.MODEL_TESTS, java.lang.Boolean.FALSE.toString())
		setGeneratorPropertyDefault(CodegenConstants.MODEL_DOCS, java.lang.Boolean.FALSE.toString())
		setGeneratorPropertyDefault(CodegenConstants.API_TESTS, java.lang.Boolean.TRUE.toString())
		setGeneratorPropertyDefault(CodegenConstants.API_DOCS, java.lang.Boolean.FALSE.toString())
	}

	override fun opts(opts: ClientOptInput): Generator {
		val appRoot = "app-module/src/main/kotlin"
		val commonRoot = "common/src/main/kotlin"
		val testRoot = "app-module/src/test/kotlin"

		val config = opts.config as CodeCodegen
		config.generator = this
		config.entityMode = true
		config.modelNameSuffix = ""

		val modelFiles = config.modelTemplateFiles()
		modelFiles.remove("model.mustache")
		modelFiles["$appRoot/domain/entity.mustache"] = ".kt"
		modelFiles["$commonRoot/domain/commonEntity.mustache"] = ".kt"

		val apiFiles = config.apiTemplateFiles()
		apiFiles.remove("api.mustache")
		apiFiles.remove("apiController.mustache")

		apiFiles["$appRoot/controller/api.mustache"] = "Api.kt"

		val isControllerDelegate = config.additionalProperties().getOrDefault("controllerDelegate", false) as Boolean

		apiFiles[if (isControllerDelegate) {
			"$appRoot/controller/apiControllerDelegate.mustache"
		} else {
			"$appRoot/controller/apiController.mustache"
		}] = "Controller.kt"

		apiFiles["$appRoot/repository/repository.mustache"] = "Repository.kt"
		apiFiles["$appRoot/service/service.mustache"] = "Service.kt"

		val apiTestFiles = config.apiTestTemplateFiles()
		apiTestFiles.clear()
		apiTestFiles[if (isControllerDelegate) {
			"$testRoot/controller/apiTestDelegate.kt.mustache"
		} else {
			"$testRoot/controller/apiTest.kt.mustache"
		}] = ".kt"

		config.specIndex = specIndex

		return super.opts(opts)
	}

}
