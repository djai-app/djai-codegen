package pro.bilous.codegen.writer

import org.openapitools.codegen.CodeCodegen
import pro.bilous.codegen.core.DjetTemplateManager
import java.io.File

class FileSystemWriter(
	val codegen: CodeCodegen,
	private val templateManager: DjetTemplateManager
) : CodegenFileWriter {
	override fun write(filename: String, contents: ByteArray): File? {
		if (codegen.isEnableMerge()) {
			return templateManager.writeFileWithMerge(filename, contents)
		}
		return templateManager.writeToFile(filename, contents)
	}
}
