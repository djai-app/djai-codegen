package pro.bilous.codegen.exec

import io.airlift.airline.*
import org.openapitools.codegen.cmd.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pro.bilous.codegen.core.ZipGenerateInvoker
import java.io.PrintWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ServerMain {
	companion object {
		val log: Logger = LoggerFactory.getLogger(ServerMain::class.java)
	}

	fun generate(zipOutput: ZipOutputStream, execSettings: ExecSettings) {
		val generateLog = StringBuilder()

		ZipOutputStream(zipOutput).use { zos ->
			try {
				start(zos, execSettings, generateLog)
			} catch (e: java.lang.Exception) {
				// If we get an error, put an PROJECT_GENERATION_ERROR file into the ZIP.
				zos.putNextEntry(ZipEntry("PROJECT_GENERATION_FAILED.txt"))
				zos.write("An unexpected server error was encountered while generating the project.  See\r\n".toByteArray())
				zos.write("the details of the error below.\r\n\r\n".toByteArray())
				zos.write("Generation Log:\r\n\r\n".toByteArray())
				zos.write(generateLog.toString().toByteArray())
				zos.write("\r\n\r\nServer Stack Trace:\r\n".toByteArray())
				val writer = PrintWriter(zos)
				e.printStackTrace(writer)
				writer.flush()
				zos.closeEntry()
			}
		}
	}

	private fun start(zipOutput: ZipOutputStream, execSettings: ExecSettings, zipLog: StringBuilder) {
		zipLog.append("Start Generating ${execSettings.projectPath}\r\n")

		val projectPath = execSettings.projectPath
		val specFilePath = execSettings.specFilePath
		val configFile = execSettings.configFile

		val args = arrayOf(
			"generate",
			"-g",
			"bhn-codegen",
			"-o",
			projectPath,
			"-i",
			specFilePath,
			"-c",
			configFile
		)

		val version = Version.readVersionFromResources()
		val builder = Cli.builder<Runnable>("codegen-cli")
			.withDescription("Code Generation CLI (version $version).")
			.withDefaultCommand(ListGenerators::class.java)
			.withCommands(
				ListGenerators::class.java,
				Generate::class.java,
				Meta::class.java,
				Help::class.java,
				ConfigHelp::class.java,
				Validate::class.java,
				Version::class.java,
				CompletionCommand::class.java
			)

		try {
			val command = builder.build().parse(*args)
			(command as Generate).setGenerateInvoker(ZipGenerateInvoker(zipOutput, zipLog))
			command.run()
			zipLog.append("Stop Generating ${execSettings.projectPath}\r\n")
			log.debug("Successfully generate the project $projectPath")
		} catch (e: Exception) {
			zipLog.append("Failed with exception ${e.message}\r\n")
			log.error("Failed Code Generator runner ${execSettings.projectPath}", e)
			throw IllegalArgumentException(e)
		}
	}
}
