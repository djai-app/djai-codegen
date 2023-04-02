package pro.bilous.codegen.writer

import org.slf4j.LoggerFactory
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipFileWriter(
	private val zipOutput: ZipOutputStream,
	private val zipLog: StringBuilder
) : CodegenFileWriter {
	companion object {
		private val log = LoggerFactory.getLogger(ZipFileWriter::class.java)
	}
	override fun write(filename: String, contents: ByteArray): File {
		if (filename.contains("/./")) {
			log.debug("File directory should be root, fixing it for $filename")
			val fixedFilename = "./" + filename.split("/./").last()
			return writeToZip(fixedFilename, contents)
		}
		return writeToZip(filename, contents)
	}

	private fun writeToZip(filename: String, contents: ByteArray): File {
		zipOutput.putNextEntry(ZipEntry(filename))
		zipOutput.write(contents)
		zipOutput.closeEntry()
		zipLog.append("Successful write of file $filename\n")
		return File(filename)
	}
}
