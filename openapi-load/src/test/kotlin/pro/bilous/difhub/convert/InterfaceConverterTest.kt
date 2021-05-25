package pro.bilous.difhub.convert

import org.junit.Assert.*
import pro.bilous.difhub.model.*
import pro.bilous.difhub.model.Model

class InterfaceConverterTest {
	private val model = Model(
		external = false,
		data = null,
		identity = Identity(
			name = "Products",
			id = "f652eb70-44f6-47a8-8937-e17d27435cde",
			description = "Interface to access product information"),
		subscription = null,
		_path = "",
		path = "/\u200Bproductlines/\u200B{productLineId}/products/{productId}",
		version = Version(
			major = 1,
			minor = 0,
			revision = 0),
		structure = null,
		oprerations = listOf(
			OperationsItem(
				identity = Identity(
					name = "CreateProduct",
					id = "a1d841b2-63c1-42a0-86b6-cdf49ba43772",
					description = "Create a new Product. Validate Product not yet exist."),
				deprecated = false,
				action = "POST",
				responses = listOf(
					ResponsesItem(
						code = "",

					)
				)

			)
		)
	)
}

data class Model(val external: Boolean = false,
				 val data: Data? = null,
				 val identity: Identity = Identity(name = "default"),
				 val subscription: Subscription? = null,
				 val _path: String = "",
				 val path: String = "",
				 val version: Version? = null,
				 val structure: Structure? = null,
				 val operations: List<OperationsItem>? = null,
				 val responses: List<ResponsesItem>? = null,
				 val parameters: List<ParametersItem>? = null,
				 val layouts: List<LayoutsItem>? = null,
				 val `object`: Object? = null)
