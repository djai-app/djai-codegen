package pro.bilous.difhub.load

import pro.bilous.difhub.config.Config
import pro.bilous.difhub.config.SystemSettings
import pro.bilous.difhub.model.Model

class ApplicationsLoader(val modelLoader: IModelLoader, config: Config) {

	private var difhub = config.difhub

	fun loadAppBySystem(system: String): List<String> {
		val models = modelLoader.loadModels(difhub.getApplicationsUrl(system))

		val apps = mutableListOf<String>()
		models?.filter {
			it.`object`!!.usage == "Service"
		}?.forEach {
			val name = it.identity.name
			if (name.isNotEmpty()) {
				apps.add(name)
			}
		}
		return apps
	}

	fun loadAll(system: String): List<Model>? {
		return modelLoader.loadModels(difhub.getApplicationsUrl(system))
	}

	fun loadOne(systemSettings: SystemSettings, app: String): Model? {
		return modelLoader.loadModel(difhub.getApplicationUrl(systemSettings.name, app), systemSettings)
	}

	fun loadAppSettings(systemSettings: SystemSettings, app: String): Model? {
		return modelLoader.loadModel(difhub.getApplicationSettingsUrl(systemSettings.name, app), systemSettings)
	}

}
