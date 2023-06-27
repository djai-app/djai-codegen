package pro.bilous.codegen.process

import org.openapitools.codegen.CodeCodegen
import org.openapitools.codegen.CodegenModel
import org.slf4j.LoggerFactory
import pro.bilous.codegen.process.models.CommonModelsProcessor
import pro.bilous.codegen.process.models.ModelStrategyResolver

class FromModelProcessor(val codegen: CodeCodegen) {
	companion object {
		private val LOGGER = LoggerFactory.getLogger(FromModelProcessor::class.java)
	}

	fun process(codegenModel: CodegenModel): CodegenModel {
		removeIgnoredFields(codegenModel)
		applyModelStrategies(codegenModel)

		fixEnumName(codegenModel)

		val properties = codegen.additionalProperties()
		CommonModelsProcessor(properties).process(model = codegenModel)

		fixRequiredFieldsDefaultValue(codegenModel)

		resolveDataClass(codegenModel)

		return codegenModel
	}

	private fun applyModelStrategies(codegenModel: CodegenModel) {
		val resolver = ModelStrategyResolver(codegenModel)
		val args = resolver.buildArgs()
		resolver.resolveParent(args)
		resolver.cleanupImports()
		resolver.addExtensions(args)
	}

	fun fixEnumName(model: CodegenModel) {
		// do not add suffix to the Enum!
		if (model.isEnum) {
			LOGGER.debug("enum detected")
			model.classname = model.name
			//codegenModel.classFilename = codegenModel.name
		}
	}

	fun removeIgnoredFields(model: CodegenModel) {
		model.vars = model.vars.filter { !it.name.startsWith("_") && !it.name.startsWith("#")}
	}

	fun fixRequiredFieldsDefaultValue(model: CodegenModel) {
		// should introduce processor for the default values
		model.vars.forEach {
			if (it.required && it.defaultValue == "null") {
				it.defaultValue = null
			}
		}
	}

	fun resolveDataClass(model: CodegenModel) {
		model.vendorExtensions["isDataClass"] = model.vars.isNotEmpty()
	}

}
