package pro.bilous.codegen.core

import org.openapitools.codegen.ClientOptInput
import java.util.zip.ZipOutputStream

class ZipGenerateInvoker(
	private val zipOutput: ZipOutputStream,
	private val zipLog: StringBuilder
) : IGenerateInvoker {
	override fun invoke(index: Int, optInput: ClientOptInput) {
		ZipFileGenerator(index, zipOutput, zipLog).opts(optInput).generate()
	}
}
