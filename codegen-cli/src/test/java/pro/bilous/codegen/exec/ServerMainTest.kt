package pro.bilous.codegen.exec

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayOutputStream
import java.util.zip.ZipOutputStream

class ServerMainTest {

	@Test
	fun testStart() {
		val zipOut = ZipOutputStream(ByteArrayOutputStream())
		val execSettings = ExecSettings(
			projectPath = "/test/",
			specFilePath = "test-spec.yaml",
			configFile = "test-conf.yaml"
		)
		assertThrows<Exception> {
			ServerMain().generate(zipOut, execSettings)
		}
	}
}

