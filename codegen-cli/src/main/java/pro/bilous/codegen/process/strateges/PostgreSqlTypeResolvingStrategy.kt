package pro.bilous.codegen.process.strateges

import org.openapitools.codegen.CodegenProperty

object PostgreSqlTypeResolvingStrategy : DefaultTypeResolvingStrategy() {
	override fun resolveToStringWhenSizeIsUndefined(property: CodegenProperty, defaultStingSize: Int) : String {
		return "VARCHAR"
	}
}
