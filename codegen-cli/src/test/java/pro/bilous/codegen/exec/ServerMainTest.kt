package pro.bilous.codegen.exec

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.util.zip.ZipOutputStream

fun main() {
	ServerMainTest().shouldGenerateUsingCustomTemplates()
}

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

	val outputZip = "/Users/vova/Projects/test-results/generated.zip"

	fun shouldGenerateValidProject() {
		val execSettings = ExecSettings(
			projectPath = "",
			specFilePath = "/Users/vova/Projects/djai-codegen/codegen-cli/src/test/resources/pet-api.yml",
			configFile = "/Users/vova/Projects/djai-codegen/codegen-cli/src/test/resources/pet-settings.yml"
		)
		val outputStream = FileOutputStream(outputZip)
		ServerMain().generate(outputStream, execSettings)

		unzipProject();
	}

	fun shouldGenerateUsingCustomTemplates() {
		val execSettings = ExecSettings(
			projectPath = "",
			specFilePath = "/Users/vova/Projects/PRO/djet-codegen/codegen-cli/src/test/resources/pet-api.yml",
			configFile = "/Users/vova/Projects/PRO/djet-codegen/codegen-cli/src/test/resources/pet-settings.yml",
			templateDir = "/Users/vova/Projects/PRO/djet-codegen/codegen-cli/src/test/resources/test-templates"
		)
		val outputStream = FileOutputStream(outputZip)
		ServerMain().generate(outputStream, execSettings)

		unzipProject();
	}

	private fun unzipProject() {
		ProcessBuilder()
			.command("rm", "-rf", "/Users/vova/Projects/test-results/project-flo/")
			.redirectError(ProcessBuilder.Redirect.INHERIT)
			.redirectOutput(ProcessBuilder.Redirect.INHERIT)
			.start()
			.waitFor()

		ProcessBuilder()
			.command("unzip", "-o", outputZip, "-d", "/Users/vova/Projects/test-results/project-flo/")
			.redirectError(ProcessBuilder.Redirect.INHERIT)
			.redirectOutput(ProcessBuilder.Redirect.INHERIT)
			.start()
			.waitFor()
	}
}

