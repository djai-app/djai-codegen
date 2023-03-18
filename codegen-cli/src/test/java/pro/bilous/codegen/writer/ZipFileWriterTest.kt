package pro.bilous.codegen.writer

import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.test.assertEquals

class ZipFileWriterTest {

	@Test
	fun `should write file to zip`() {
		val byteOutput = ByteArrayOutputStream()
		val zipOutput = ZipOutputStream(byteOutput)
		val writer = ZipFileWriter(zipOutput, zipLog)

		val inputFileName = "/this/filepath/Test.txt"
		val inputFileContent = "content".encodeToByteArray()
		writer.write(inputFileName, inputFileContent)

		val zipInput = ZipInputStream(ByteArrayInputStream(byteOutput.toByteArray()))
		val zipEntry = zipInput.nextEntry ?: throw IllegalStateException("Zip entry is null")
		assertEquals(inputFileName, zipEntry.name)
	}
}
