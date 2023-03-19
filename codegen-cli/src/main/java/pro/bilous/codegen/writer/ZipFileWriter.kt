package pro.bilous.codegen.writer

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipFileWriter(
	private val zipOutput: ZipOutputStream,
	private val zipLog: StringBuilder
) : CodegenFileWriter {
	override fun write(filename: String, contents: ByteArray): File {
		zipLog.append("Start file writing $filename\n")
		zipOutput.putNextEntry(ZipEntry(filename))
		zipOutput.write(contents)
		zipOutput.closeEntry()
		zipLog.append("End file writing $filename\n")
		return File(filename);
	}
}
