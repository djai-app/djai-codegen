package pro.bilous.codegen.writer

import java.io.File

interface CodegenFileWriter {

	fun write(filename: String, contents: ByteArray): File?
}
