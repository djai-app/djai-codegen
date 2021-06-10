package pro.bilous.intellij.plugin.action.menu

import com.fasterxml.jackson.databind.node.ObjectNode
import pro.bilous.intellij.plugin.PathTools
import pro.bilous.intellij.plugin.project.ProjectFileManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import io.swagger.util.Yaml
import org.slf4j.LoggerFactory
import pro.bilous.difhub.config.SystemSettings
import pro.bilous.difhub.config.DatasetStatus
import pro.bilous.difhub.convert.DifHubToSwaggerConverter
import pro.bilous.difhub.write.YamlWriter
import java.io.File

class DifHubLoadOpenApiAction : AnAction() {
	private val log = LoggerFactory.getLogger(DifHubToSwaggerConverter::class.java)

	private val fileManager = ProjectFileManager()

    override fun actionPerformed(e: AnActionEvent) {
		val ve = VerifiedEvent(e)
        val project = ve.project
        val projectPath = ve.projectPath

        val configFolder = PathTools.getHomePath(projectPath)

		if (!loadCredentials(configFolder, project)) {
			return
		}
		val system = loadSystem(configFolder, project) ?: return

		createOpenApiFiles(system, configFolder)

        VirtualFileManager.getInstance().syncRefresh()
    }

	private fun loadSystem(configFolder: String, project: Project): SystemSettings? {
		val configFilePath = "$configFolder/settings.yaml"

		val file = File(configFilePath)
		if (!file.exists()) {
			fileManager.createAndOpenProjectSettings(configFolder, project)
			return null
		}
		val configTree = file.inputStream().use { Yaml.mapper().readTree(it) as? ObjectNode } ?: return null

		System.setProperty("DIFHUB_ORG_NAME", configTree.get("organization").asText())

		val name = configTree.get("system").asText()
		// we use DRAFT datasets if status wasn't specified
		val statusName = configTree.get("datasetStatus")?.asText() ?: "DRAFT"
		val status : DatasetStatus = try {
			DatasetStatus.valueOf(statusName.toUpperCase())
		} catch(e: IllegalArgumentException) {
			log.warn("Illegal status of system: `$statusName`. Status is set to `Draft`")
			DatasetStatus.DRAFT
		}
		return SystemSettings(name, status)
	}

	private fun loadCredentials(configFolder: String, project: Project): Boolean {
		val configFilePath = "file://$configFolder/.credentials.yaml"
		val configFile = VirtualFileManager.getInstance().findFileByUrl(configFilePath)
		if (configFile == null) {
			fileManager.createAndOpenProjectCredentials(configFolder, project)
			return false
		}
		val fileTree = Yaml.mapper().readTree(configFile.inputStream)

		System.setProperty("DIFHUB_USERNAME", fileTree.get("username").asText())
		System.setProperty("DIFHUB_PASSWORD", fileTree.get("password").asText())
		return true
	}

	private fun createOpenApiFiles(systemSettings: SystemSettings, configFolder: String) {
        DifHubToSwaggerConverter(systemSettings).convertAll().forEach {
            YamlWriter(systemSettings.name).writeFile(it.openApi, configFolder, "${it.appName.toLowerCase()}-api")
        }
    }


}
