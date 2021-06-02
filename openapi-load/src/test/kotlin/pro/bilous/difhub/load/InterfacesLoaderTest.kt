package pro.bilous.difhub.load

import com.nhaarman.mockitokotlin2.mock
import pro.bilous.difhub.config.DatasetStatus
import pro.bilous.difhub.config.SystemSettings

class InterfacesLoaderTest {
}

fun main() {
	val mockModelLoader: IModelLoader = mock()
	InterfacesLoader(mockModelLoader).load(SystemSettings("Customer", DatasetStatus.APPROVED), "Product")
}
