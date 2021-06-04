package pro.bilous.difhub.load

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import pro.bilous.difhub.config.Config
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DefLoader(val username: String, val password: String, val config: Config) {
	companion object {
		val authHeaders = ConcurrentHashMap<String, String>()

		private val client = OkHttpClient.Builder()
				.retryOnConnectionFailure(true)
				.readTimeout(7, TimeUnit.SECONDS)
				.build()

		private const val MAX_LOAD_ATTEMPTS = 5
	}

	fun load(path: String): String? {
		val url = getUrl(path)

		val request = Request.Builder()
			.url(url)
			.addHeader("Authorization", authHeaders.getOrPut(username){ getAuthHeader() })
			.build()

		val response = call(request)
		if (response == null) {
			println("Max retry attempt to load is reached. Url is not loaded: $url")
			return null
		}

		val result = response.body?.string()
		if(result != null) {
			if (isUnauthorized(response, result)) {
				println("User is unauthorized. Trying to authorize and load again url: $url")
				authHeaders[username] = getAuthHeader()
				//File("/difhub.auth").writeText(authHeader) //TODO  Exception in thread "main" java.lang.IllegalArgumentException: URI is not hierarchical
				return load(path)
			} else if (isNotFound(response, result)) {
				println("Url is not found: $url")
				return null
			}
			println("Url is loaded: $url")
		} else {
			println("Url is not loaded: $url")
		}
		return result
	}

	private fun getUrl(path: String) : String {
		val api = config.difhub.api
		return "$api/$path"
	}

	private fun call(request: Request) : Response? {
		var loadAttempts = 0
		while(loadAttempts++ < MAX_LOAD_ATTEMPTS) {
			try {
				return client.newCall(request).execute()
			} catch (e: SocketTimeoutException) {
				Thread.sleep(3000)
				println("Failed $loadAttempts attempt to load: retry in 3 second.")
			}
		}
		return null
	}

	private fun isUnauthorized(response: Response, body: String): Boolean {
		return response.code == 401 || body.contains("\"status\": 401")
	}

	private fun isNotFound(response: Response, body: String): Boolean {
		return response.code == 404 || body.contains("\"status\": 404")
	}

	private fun getAuthHeader() : String {
		return TokenReader.readAuth(username, password)
	}
 }
