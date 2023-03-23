package pro.bilous.codegen.exec

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream
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

	@Test
	fun shouldGenerateValidProject() {
		val execSettings = ExecSettings(
			projectPath = "/Users/vova/Projects/PRO/djet-codegen/codegen-cli/build/test-results/file.zip",
			specFilePath = "/Users/vova/Projects/PRO/djet-codegen/codegen-cli/src/test/resources/catalog-api.yaml",
			configFile = "/Users/vova/Projects/PRO/djet-codegen/codegen-cli/src/test/resources/catalog-settings.yaml"
		)
		val outputStream = FileOutputStream(execSettings.projectPath)
		ServerMain().generate(outputStream, execSettings)
	}
}

