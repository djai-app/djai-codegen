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
		val fixedFilename = filename.replace("//", "/")
		if (fixedFilename.contains("/./")) {
			log.debug("File directory should be root, fixing it for $filename")
			return writeToZip("./" + fixedFilename.split("/./").last(), contents)
		}
		return writeToZip(fixedFilename, contents)
	}

	private fun writeToZip(filename: String, contents: ByteArray): File {
		zipOutput.putNextEntry(ZipEntry(filename))
		zipOutput.write(contents)
		zipOutput.closeEntry()
		zipLog.append("Successful write of file $filename\n")
		return File(filename)
	}
}
