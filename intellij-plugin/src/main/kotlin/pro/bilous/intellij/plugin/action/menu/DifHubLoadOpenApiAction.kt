package pro.bilous.intellij.plugin.action.menu

import pro.bilous.intellij.plugin.PathTools
import pro.bilous.intellij.plugin.project.ProjectFileManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import io.swagger.util.Yaml
import org.slf4j.LoggerFactory
import pro.bilous.difhub.config.Config
import pro.bilous.difhub.config.ConfigReader
import pro.bilous.difhub.config.DatasetStatus
import pro.bilous.difhub.convert.DifHubToSwaggerConverter
import pro.bilous.difhub.load.DifHubLoader
import pro.bilous.difhub.load.IModelLoader
import pro.bilous.difhub.load.ModelLoader
import pro.bilous.difhub.write.YamlWriter
import java.lang.IllegalArgumentException

class DifHubLoadOpenApiAction : AnAction() {
	private val log = LoggerFactory.getLogger(DifHubToSwaggerConverter::class.java)

	private val fileManager = ProjectFileManager()

	override fun actionPerformed(e: AnActionEvent) {
		val project = e.project
		val basePath = project!!.basePath ?: throw IllegalArgumentException("Base path not found")

		val configFolder = PathTools.getHomePath(basePath)

		val (username, password, organization) = loadCredentialsAndOrganization(configFolder, project) ?: return
		val (system, datasetStatus) = loadSystemSettings(configFolder, project) ?: return

		val config = ConfigReader.loadConfig(organization).apply {
			this.system = system
			this.datasetStatus = datasetStatus
		}

		val modelLoader = ModelLoader(DifHubLoader(username, password, config))

		createOpenApiFiles(modelLoader, config, configFolder)
		VirtualFileManager.getInstance().syncRefresh()
	}

	private fun loadSystemSettings(configFolder: String, project: Project): Pair<String, DatasetStatus>? {
		val configFilePath = "file://$configFolder/settings.yaml"

		val configFile = VirtualFileManager.getInstance().findFileByUrl(configFilePath)
		if (configFile == null) {
			fileManager.createAndOpenProjectSettings(configFolder, project)
			return null
		}
		val configTree = Yaml.mapper().readTree(configFile.inputStream)
		val system = configTree.get("system").asText()
		// we use DRAFT datasets if status wasn't specified
		val statusName = configTree.get("datasetStatus")?.asText() ?: "DRAFT"
		val datasetStatus: DatasetStatus = try {
			DatasetStatus.valueOf(statusName.toUpperCase())
		} catch (e: IllegalArgumentException) {
			log.warn("Illegal status of system: `$statusName`. Status is set to `Draft`")
			DatasetStatus.DRAFT
		}
		return Pair(system, datasetStatus)
	}

	private fun loadCredentialsAndOrganization(
		configFolder: String,
		project: Project
	): Triple<String, String, String>? {
		val configFilePath = "file://$configFolder/.credentials.yaml"
		val configFile = VirtualFileManager.getInstance().findFileByUrl(configFilePath)
		if (configFile == null) {
			fileManager.createAndOpenProjectCredentials(configFolder, project)
			return null
		}
		val fileTree = Yaml.mapper().readTree(configFile.inputStream)

		val username = fileTree.get("username").asText()
		val password = fileTree.get("password").asText()
		val organization = fileTree.get("organization").asText()
		return Triple(username, password, organization)
	}

	private fun createOpenApiFiles(modelLoader: IModelLoader, config: Config, configFolder: String) {
		DifHubToSwaggerConverter(modelLoader, config).convertAll().forEach {
			YamlWriter(config.system).writeFile(it.openApi, configFolder, "${it.appName.toLowerCase()}-api")
		}
	}
}
