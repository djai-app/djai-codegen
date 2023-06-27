package pro.bilous.codegen.process.models

import org.openapitools.codegen.CodeCodegen
import org.openapitools.codegen.CodegenModel

class AllModelsProcessor(val codegen: CodeCodegen) {

	/**
	 * Models processor to fix the all models things
	 * @param objs – map with all items to use for template processor
	 * @return objs
	 */

	val paths = codegen.generator?.processPaths(codegen.findOpenApi().paths)

	fun process(objs: MutableMap<String, Any>): MutableMap<String, Any> {
		val allModels = mutableSetOf<CodegenModel>()
		objs.forEach { (_, modelMap) ->
			val model = (modelMap as HashMap<String, List<HashMap<String, CodegenModel>>>)["models"]?.first()?.get("model")
			if (model != null) {
				allModels.add(model)
				setHasOperation(model, modelMap as HashMap<String, Any>)
			}
		}
		return objs
	}

	private fun setHasOperation(model: CodegenModel, modelMap: HashMap<String, Any>) {
		if (hasOperationWithReturnType(model.name) && model.imports.contains("BaseDomain")) {
			model.vendorExtensions["hasOperation"] = true
			model.parent = "BaseResource()"
			model.vars = model.vars.filter { "id" != it.name && "identity" != it.name }
			model.imports.remove("BaseDomain")
			model.imports.add("BaseResource")
			val imports = modelMap["imports"] as List<HashMap<String, String>>
			val baseDomainImport = imports.find { it["import"]?.endsWith("BaseDomain") ?: false }
			if (baseDomainImport != null) {
				baseDomainImport["import"] = baseDomainImport["import"]!!.replace("BaseDomain", "BaseResource")
			}
		}
	}

	private fun hasOperationWithReturnType(name: String): Boolean {
		paths?.forEach { (_, values) ->
			values.forEach { operation ->
				if (operation.returnBaseType == name) {
					return true
				} else if (operation.bodyParam != null && operation.bodyParam.baseType == name) {
					return true
				}
			}
		}
		return false
	}
}
