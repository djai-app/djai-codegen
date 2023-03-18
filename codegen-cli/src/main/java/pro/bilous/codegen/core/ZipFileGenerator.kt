package pro.bilous.codegen.core

import org.openapitools.codegen.ClientOptInput
import org.openapitools.codegen.CodeCodegen
import org.openapitools.codegen.Generator
import pro.bilous.codegen.writer.ZipFileWriter
import java.util.zip.ZipOutputStream

class ZipFileGenerator(
	specIndex: Int = 0,
	private val zipOutput: ZipOutputStream,
	private val zipLog: StringBuilder
) : DataCodeGenerator(specIndex) {

	override fun opts(opts: ClientOptInput): Generator {
		val config = opts.config as CodeCodegen
		config.fileWriter = ZipFileWriter(zipOutput, zipLog)
		return super.opts(opts)
	}
}
