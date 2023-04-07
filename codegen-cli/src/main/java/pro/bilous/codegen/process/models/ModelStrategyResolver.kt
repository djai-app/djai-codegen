package pro.bilous.codegen.process.models

import com.google.common.base.CaseFormat
import org.openapitools.codegen.CodegenModel
import pro.bilous.codegen.utils.SqlNamingUtils
import pro.bilous.codegen.utils.SuperclassRegistry

class ModelStrategyResolver(val model: CodegenModel) : IModelStrategyResolver {

	companion object {
		val importsToIgnore = arrayOf(
			"ApiModel",
			"ApiModelProperty",
			"JsonProperty",
			"Entity",
			"ResourceEntity",
			"Identity",
			"Property",
			"Set",
			"List",
			"LinkedHashSet",
			"Map",
			"HashMap"
		)
	}

	override fun resolveParent(args: Args) {
		val extensions = model.vendorExtensions
		when {
			args.hasEntity -> {
				val parent = "BaseResource()"
				model.parent = parent
				model.imports.add("BaseResource")

				model.vars = model.vars.filter { "identity" != it.name && "entity" != it.name }
				extensions["hasTableEntity"] = true
			}
			args.hasId && args.hasName && args.hasDescription -> {
				model.parent = "BaseResource()"
				model.imports.add("BaseResource")
				model.vars = model.vars.filter { "id" != it.name && "identity" != it.name }
			}
			args.hasId && args.hasCreatedDate && args.hasUpdatedDate -> {
				model.parent = "BaseResource()"
				model.imports.add("BaseResource")
				model.vars = model.vars.filter { "id" != it.name && "identity" != it.name }
			}
			args.hasId && args.hasRealm && args.hasIsDeleted -> {
				model.parent = "BaseResource()"
				model.imports.add("BaseResource")
				model.vars = model.vars.filter { "id" != it.name && "identity" != it.name }
			}
			args.hasId -> {
				model.parent = "BaseDomain()"
				model.imports.add("BaseDomain")
				model.vars = model.vars.filter { "id" != it.name && "identity" != it.name }
			}
			args.hasIdentity -> {
				model.parent = "BaseDomain()"
				model.imports.add("BaseDomain")
				// we are unable to support identity for now. So, just remove the field and add ID instead of it.
				model.vars = model.vars.filter { "id" != it.name && "identity" != it.name }
			}
			args.hasExtends -> {
				val extendProperty = model.requiredVars.find { it.name == "_extends" }!!
				val parentType = extendProperty.complexType
				model.parent = "${parentType}()"
				model.imports.add(parentType)
			}
		}
	}

	override fun cleanupImports() {
		importsToIgnore.forEach {
			model.imports.remove(it)
		}
	}

	override fun buildArgs(): Args {
		return Args(
			hasEntity = model.vars.any { "entity" == it.name.lowercase() },
			hasIdentity = model.vars.any { "identity" == it.name.lowercase() },
			hasId = model.vars.any { "id" == it.name.lowercase() } && model.name != "Identity",
			hasName = model.vars.any { "name" == it.name.lowercase() },
			hasDescription = model.vars.any { "description" == it.name.lowercase() },
			hasExtends = model.requiredVars.any { "_extends" == it.name.lowercase() },
			hasCreatedDate = model.vars.any { "createddate" == it.name.lowercase() },
			hasUpdatedDate = model.vars.any { "updateddate" == it.name.lowercase() },
			hasRealm = model.vars.any { "realm" == it.name.lowercase() },
			hasIsDeleted = model.vars.any { "isdeleted" == it.name.lowercase() }
		)
	}

	override fun addExtensions(args: Args) {
		val extensions = model.vendorExtensions
		if (!args.hasEntity && args.hasIdentity) {
			extensions["hasTableEntity"] = false
		}
		val tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, model.name)
		extensions["tableName"] = SqlNamingUtils.escapeTableNameIfNeeded(tableName)
		extensions["isEmbeddable"] = !args.hasEntity && !args.hasIdentity && !args.hasId && !args.hasExtends
		extensions["addIdVar"] = false // !hasEntity && hasIdentity
		val isSuperclass = SuperclassRegistry.hasName(model.name)
		extensions["isSuperclass"] = isSuperclass
		extensions["hasIdentity"] = args.hasIdentity
		extensions["hasEntity"] = args.hasEntity
	}

	data class Args(
		val hasEntity: Boolean = false,
		val hasIdentity: Boolean = false,
		val hasId: Boolean = false,
		val hasName: Boolean = false,
		val hasDescription: Boolean = false,
		val hasExtends: Boolean = false,
		val hasCreatedDate: Boolean = false,
		val hasUpdatedDate: Boolean = false,
		val hasRealm: Boolean = false,
		val hasIsDeleted: Boolean = false
	)
}
