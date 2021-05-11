package pro.bilous.difhub

import pro.bilous.difhub.config.DatasetStatus
import pro.bilous.difhub.config.SystemSettings
import pro.bilous.difhub.console.Console
import pro.bilous.difhub.convert.DifHubToSwaggerConverter
import pro.bilous.difhub.write.YamlWriter

fun main() {
	Console.select()
	val systemSettings = SystemSettings(Console.system!!, DatasetStatus.APPROVED)
	DifHubToSwaggerConverter(systemSettings).convertAll().forEach {
		YamlWriter(it.appName).write(it.openApi)
	}
}


