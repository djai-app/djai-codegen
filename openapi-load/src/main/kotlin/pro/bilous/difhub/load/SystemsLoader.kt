package pro.bilous.difhub.load

import pro.bilous.difhub.config.ConfigReader

class SystemsLoader(private val modelLoader: IModelLoader) {

	var config = ConfigReader.loadConfig()

	fun loadSystems(): List<String> {
		val difhub = config.difhub

		val models = modelLoader.loadModels(difhub.getSystemsUrl())
		val systems = mutableListOf<String>()

		models?.forEach {
			val name = it.identity.name
			if (name.isNotEmpty()) {
				systems.add(name)
			}
		}

		return systems
	}
}
