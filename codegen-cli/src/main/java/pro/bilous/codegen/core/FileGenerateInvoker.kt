package pro.bilous.codegen.core

import org.openapitools.codegen.ClientOptInput

class FileGenerateInvoker : IGenerateInvoker {
	override fun invoke(index: Int, optInput: ClientOptInput) {
		DataCodeGenerator(index).opts(optInput).generate()
	}
}
