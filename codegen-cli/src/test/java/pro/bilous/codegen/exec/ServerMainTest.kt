package pro.bilous.codegen.exec

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipOutputStream

class ServerMainTest {

	@Test
	fun testStart() {
		val zipOut = ZipOutputStream(ByteArrayOutputStream())
		ServerMain().generate(zipOut, ExecSettings(
			projectPath = "",
			specFilePath = "",
			configFile = ""
		))
	}
}

