package pro.bilous.difhub.load

import pro.bilous.difhub.model.Model
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.swagger.util.Json
import pro.bilous.difhub.config.DatasetStatus

class ModelLoader(private val defLoader: DefLoader) : IModelLoader {

	companion object {
		val globalModelCache = mutableMapOf<String, String>()
	}

	init {
		Json.mapper().registerKotlinModule()
	}

	override fun loadModel(reference: String, datasetStatus: DatasetStatus): Model? {
		// remove the version suffix to always get the latest one.
		var fixedRef = reference
		if (fixedRef.contains("/versions/")) {
			fixedRef = reference.split("/").dropLast(2).joinToString("/")
		}
		val text = loadString(fixedRef, datasetStatus)
		return if (text.isNullOrEmpty()) null else try {
			Json.mapper().readValue<Model>(text)
		} catch (e: MismatchedInputException) {
			System.err.println("Failed when model loading: $reference")
			println(e)
			null
		}
	}

	override fun loadModels(reference: String): List<Model>? {
		val text = loadString(reference, null)
		return if (text.isNullOrEmpty()) null else Json.mapper().readValue<List<Model>>(text)
	}

	private inline fun <reified T> ObjectMapper.readValue(json: String): T =
		readValue(json, object : TypeReference<T>() {})

	private fun loadString(reference: String, datasetStatus: DatasetStatus?): String? {
		var fixedReference = reference.trim()
		while (fixedReference.contains("//")) {
			fixedReference = fixedReference.replace("//", "/")
		}
		fixedReference = fixedReference.removePrefix("/")

		if (globalModelCache.containsKey(fixedReference)) {
			return globalModelCache[fixedReference]
		}

		val url = resolveReference(fixedReference, datasetStatus)

		val sourceText = defLoader.load(url)
		if (sourceText == null || sourceText.contains("EntityForbiddenException")) {
			return null
		}
		globalModelCache[fixedReference] = sourceText
		return sourceText
	}

	private fun resolveReference(reference: String, datasetStatus: DatasetStatus?): String {

		var resolvedReference = reference

		val datasetStatusParam = datasetStatus?.pathParam
		if (!datasetStatusParam.isNullOrEmpty()) {
			resolvedReference = if (reference.contains("?")) {
				"$reference&status=${datasetStatusParam}"
			} else {
				"$reference?status=${datasetStatusParam}"
			}
		}

		return resolvedReference
	}
}
