package pro.bilous.intellij.plugin

import com.fasterxml.jackson.databind.node.ObjectNode
import com.intellij.openapi.vfs.VirtualFileManager
import io.swagger.util.Yaml
import pro.bilous.difhub.write.YamlWriter
import java.io.IOException

object Migrations {
	const val ANSI_GREEN = "\u001B[32m"

	fun movePropertyOrganizationFromCredentialsToSettings(projectPath: String) {
		val homePath = "file://$projectPath/.difhub-codegen"
		val credentialsPath = "$homePath/.credentials.yaml"
		val credentials = getPropertiesFromYamlFile(credentialsPath) ?: return
		val organization = credentials["organization"]?.asText() ?: return
		val settingsPath = "$homePath/settings.yaml"
		val settings = getPropertiesFromYamlFile(settingsPath) ?: return
		settings.put("organization", organization)
		credentials.remove("organization")
		val writer = YamlWriter("")
		val folder = "$projectPath/.difhub-codegen"
		writer.writeFile(credentials, folder, ".credentials")
		writer.writeFile(settings, folder, "settings")
		info("Property `organization` is successfully moved from .credentials.yaml to settings.yaml")
	}

	private fun getPropertiesFromYamlFile(filePath: String): ObjectNode? {
		try {
			val file = VirtualFileManager.getInstance().findFileByUrl(filePath) ?: return null
			return Yaml.mapper().readTree(file.inputStream) as? ObjectNode
		} catch (e: IOException) {
			return null
		}
	}

	private fun info(message: String) {
		println("${ANSI_GREEN}Migration: $message$ANSI_GREEN")
	}
}
