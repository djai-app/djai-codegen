package pro.bilous.difhub.convert

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import pro.bilous.difhub.config.DatasetStatus
import pro.bilous.difhub.config.SystemSettings
import pro.bilous.difhub.load.ApplicationsLoader
import pro.bilous.difhub.load.IModelLoader
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
		operations = listOf(
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
						field = null,
						id = "79c59e2e-c8e5-406b-9e40-32d50e16ba64",
						name = "SuccessCreated",
						description = "Object created successfully."
					),
					ResponsesItem(
						code = "",
						field = null,
						id = "00f1bcf5-a204-43bb-afdf-1e7e1e07102b",
						name = "Failure",
						description = "Execution of user request failed."
					),
					ResponsesItem(
						code = "",
						field = null,
						id = "6385e469-fac5-49f4-8eb8-b9e6fe902b9d",
						name = "FailureAlreadyExist",
						description = "Contact is already exist."
					)
				),
				parameters = listOf(
					ParametersItem(
						field = null,
						location = "",
						id = "41fe449e-b8d4-4b7a-8611-c4053fc1a7e3",
						name = "product",
						description = "Product data specification.\n"
					),
					ParametersItem(
						field = null,
						location = "",
						id = "0b0f57ed-161a-4cb2-a036-36ac4d90b4c5",
						name = "productLineId",
						description = "Id of Product Line"
					)
				)
			),
			OperationsItem(
				identity = Identity(
					name = "DeleteProduct",
					id = "9458b5f8-a96f-4eaa-a53c-87b5117f769b",
					description = "Soft delete of the product by id."),
				deprecated = false,
				action = "DELETE",
				responses = listOf(
					ResponsesItem(
						code = "",
						field = null,
						id = "a5dfede5-b294-44cd-9a8a-0a7c80a40e69",
						name = "SuccessCompleted",
						description = "Operation completed successfully."
					),
					ResponsesItem(
						code = "",
						field = null,
						id = "00f1bcf5-a204-43bb-afdf-1e7e1e07102b",
						name = "Failure",
						description = "Execution of user request failed."
					),
					ResponsesItem(
						code = "",
						field = null,
						id = "72af19d9-e3d6-4bb1-b6e8-d719363f597a",
						name = "FailureNotFound",
						description = "Contact is not found"
					)
				),
				parameters = listOf(
					ParametersItem(
						field = null,
						location = "",
						id = "04244816-4e4a-4340-9c56-62233cb796ce",
						name = "productId",
						description = "Product Id to fetch and put records"
					),
					ParametersItem(
						field = null,
						location = "",
						id = "0b0f57ed-161a-4cb2-a036-36ac4d90b4c5",
						name = "productLineId",
						description = "Id of Product Line"
					)
				)
			),
			OperationsItem(
				identity = Identity(
					name = "GetProduct",
					id = "9ff90844-2e3c-418e-9cfa-b6d5b20a423c",
					description = "Get a product or list of products from the service.\n"),
				deprecated = false,
				action = "GET",
				responses = listOf(
					ResponsesItem(
						code = "",
						field = null,
						id = "3c0cb368-40de-4019-ad04-69b9300f95b8",
						name = "Success",
						description = "Contact is found and returned."
					),
					ResponsesItem(
						code = "",
						field = null,
						id = "00f1bcf5-a204-43bb-afdf-1e7e1e07102b",
						name = "Failure",
						description = "Execution of user request failed."
					),
					ResponsesItem(
						code = "",
						field = null,
						id = "72af19d9-e3d6-4bb1-b6e8-d719363f597a",
						name = "FailureNotFound",
						description = "Contact is not found"
					)
				),
				parameters = listOf(
					ParametersItem(
						field = null,
						location = "",
						id = "4a98c672-73dc-40a2-93b0-b70365cfb9b3",
						name = "search",
						description = "Expression to limit number of entities in response."
					),
					ParametersItem(
						field = null,
						location = "",
						id = "04244816-4e4a-4340-9c56-62233cb796ce",
						name = "productId",
						description = "Product Id to fetch and put records"
					),
					ParametersItem(
						field = null,
						location = "",
						id = "0b0f57ed-161a-4cb2-a036-36ac4d90b4c5",
						name = "productLineId",
						description = "Id of Product Line"
					),
					ParametersItem(
						field = null,
						location = "",
						id = "addadecd-9ec0-46f4-820a-f95a7f802387",
						name = "partyId",
						description = "for filtering"
					),
					ParametersItem(
						field = null,
						location = "",
						id = "4d5ddc1b-0245-4f7f-b2e8-c41ae79c13a2",
						name = "type",
						description = "for filtering as a sample of Enum"
					),
					ParametersItem(
						field = null,
						location = "",
						id = "e670f707-3222-4096-9419-c865a2990139",
						name = "critical",
						description = "for filtering as a sample of boolean"
					),
					ParametersItem(
						field = null,
						location = "",
						id = "b7738667-c6b3-44e2-954a-68074837c2c9",
						name = "rank",
						description = "for filtering as a sample of Integer"
					),
					ParametersItem(
						field = null,
						location = "",
						id = "20818191-d951-4c07-8b8c-46c452bc260f",
						name = "estimation",
						description = "for filtering as a sample of String"
					)
				)
			),
			OperationsItem(
				identity = Identity(
					name = "ModifyProduct",
					id = "612c81e5-342c-4d23-8972-ca099c8fd6cd",
					description = "Change Product properties to values specified in the request. Property not specified will remain the same. No element of any collection will be deleted."),
				deprecated = false,
				action = "PATCH",
				responses = listOf(
					ResponsesItem(
						code = "",
						field = null,
						id = "a5dfede5-b294-44cd-9a8a-0a7c80a40e69",
						name = "SuccessCompleted",
						description = "Operation completed successfully."
					),
					ResponsesItem(
						code = "",
						field = null,
						id = "00f1bcf5-a204-43bb-afdf-1e7e1e07102b",
						name = "Failure",
						description = "Execution of user request failed."
					),
					ResponsesItem(
						code = "",
						field = null,
						id = "72af19d9-e3d6-4bb1-b6e8-d719363f597a",
						name = "FailureNotFound",
						description = "Contact is not found"
					)
				),
				parameters = listOf(
					ParametersItem(
						field = null,
						location = "",
						id = "04244816-4e4a-4340-9c56-62233cb796ce",
						name = "productId",
						description = "Product Id to fetch and put records"
					),
					ParametersItem(
						field = null,
						location = "",
						id = "41fe449e-b8d4-4b7a-8611-c4053fc1a7e3",
						name = "product",
						description = "Product data specification.\n"
					),
					ParametersItem(
						field = null,
						location = "",
						id = "0b0f57ed-161a-4cb2-a036-36ac4d90b4c5",
						name = "productLineId",
						description = "Id of Product Line"
					),
					ParametersItem(
						field = null,
						location = "",
						id = "addadecd-9ec0-46f4-820a-f95a7f802387",
						name = "partyId",
						description = "for filtering as a sample of Guid"
					),
					ParametersItem(
						field = null,
						location = "",
						id = "b7738667-c6b3-44e2-954a-68074837c2c9",
						name = "rank",
						description = "for filtering as a sample of Integer"
					)
				)
			),
			OperationsItem(
				identity = Identity(
					name = "UpdateProduct",
					id = "69059a8e-d3d4-402d-81e0-a3750da7687f",
					description = "Change the product specification."),
				deprecated = false,
				action = "PUT",
				responses = listOf(
					ResponsesItem(
						code = "",
						field = null,
						id = "a5dfede5-b294-44cd-9a8a-0a7c80a40e69",
						name = "SuccessCompleted",
						description = "Operation completed successfully."
					),
					ResponsesItem(
						code = "",
						field = null,
						id = "00f1bcf5-a204-43bb-afdf-1e7e1e07102b",
						name = "Failure",
						description = "Execution of user request failed."
					),
					ResponsesItem(
						code = "",
						field = null,
						id = "72af19d9-e3d6-4bb1-b6e8-d719363f597a",
						name = "FailureNotFound",
						description = "Contact is not found"
					)
				),
				parameters = listOf(
					ParametersItem(
						field = null,
						location = "",
						id = "04244816-4e4a-4340-9c56-62233cb796ce",
						name = "productId",
						description = "Product Id to fetch and put records"
					),
					ParametersItem(
						field = null,
						location = "",
						id = "41fe449e-b8d4-4b7a-8611-c4053fc1a7e3",
						name = "product",
						description = "Product data specification.\n"
					),
					ParametersItem(
						field = null,
						location = "",
						id = "0b0f57ed-161a-4cb2-a036-36ac4d90b4c5",
						name = "productLineId",
						description = "Id of Product Line"
					),
					ParametersItem(
						field = null,
						location = "",
						id = "addadecd-9ec0-46f4-820a-f95a7f802387",
						name = "partyId",
						description = "for filtering as a sample of Guid"
					)
				)
			)
		),
		responses = listOf(
			ResponsesItem(
				code = "200",
				field = Field(
					reference = "/organizations/DJet/systems/Test/applications/Product/datasets/Product/versions/1.0.0",
					access = 0,
					identity = Identity(
						name = "Success",
						id = "c3cb99b1-835f-40f3-8033-b31cafd4b26c",
						description = "Contact is found and returned."
					),
					keys = false,
					usage = "Property",
					count = 1,
					format = "",
					privacy = "low",
					optional = true,
					type = "Structure",
					value = "",
					order = 1,
					size = 0
				),
				id = "",
				name = "",
				description = ""
			),
			ResponsesItem(
				code = "201",
				field = Field(
					reference = "",
					access = 255,
					identity = Identity(
						name = "SuccessCreated",
						id = "d129796d-3156-4df1-a39e-5a7012f2deaa",
						description = "Object created successfully."
					),
					keys = false,
					usage = "Property",
					count = 1,
					format = "",
					privacy = "low",
					optional = true,
					type = "String",
					value = "",
					order = 2,
					size = 0
				),
				id = "",
				name = "",
				description = ""
			),
			ResponsesItem(
				code = "204",
				field = Field(
					reference = "",
					access = 255,
					identity = Identity(
						name = "SuccessCompleted",
						id = "e89edf19-b5c8-4e7f-b9e9-3751a277e73c",
						description = "Operation completed successfully."
					),
					keys = false,
					usage = "Property",
					count = 1,
					format = "",
					privacy = "low",
					optional = true,
					type = "String",
					value = "",
					order = 3,
					size = 0
				),
				id = "",
				name = "",
				description = ""
			),
			ResponsesItem(
				code = "400",
				field = Field(
					reference = "/organizations/Infort Technologies/datasets/Error/versions/1.1.0",
					access = 0,
					identity = Identity(
						name = "Failure",
						id = "627275b9-ec93-4a50-b527-f40340f452a0",
						description = "Execution of user request failed."
					),
					keys = false,
					usage = "Property",
					count = 0,
					format = "",
					privacy = "low",
					optional = true,
					type = "Structure",
					value = "",
					order = 4,
					size = 0
				),
				id = "",
				name = "",
				description = ""
			),
			ResponsesItem(
				code = "401",
				field = Field(
					reference = "/organizations/Infort Technologies/datasets/Error/versions/1.1.0",
					access = 0,
					identity = Identity(
						name = "FailureAlreadyExist",
						id = "0530e084-387e-4461-96af-52c7ac6bba1d",
						description = "Contact is already exist."
					),
					keys = false,
					usage = "Property",
					count = 1,
					format = "",
					privacy = "low",
					optional = true,
					type = "Structure",
					value = "",
					order = 5,
					size = 0
				),
				id = "",
				name = "",
				description = ""
			),
			ResponsesItem(
				code = "404",
				field = Field(
					reference = "/organizations/Infort Technologies/datasets/Error/versions/1.1.0",
					access = 0,
					identity = Identity(
						name = "FailureNotFound",
						id = "d13b081f-49e2-489a-9f52-2b42c5a13d79",
						description = "Contact is not found"
					),
					keys = false,
					usage = "Property",
					count = 1,
					format = "",
					privacy = "low",
					optional = true,
					type = "Structure",
					value = "",
					order = 6,
					size = 0
				),
				id = "",
				name = "",
				description = ""
			)
		),
		parameters = listOf(
			ParametersItem(
				field = Field(
					reference = "",
					access = 255,
					identity = Identity(
						name = "critical",
						id = "f2a85025-cc6c-4002-8571-1470eb6f8674",
						description = "for filtering as a sample of boolean"
					),
					keys = false,
					usage = "Property",
					count = 1,
					format = "",
					privacy = "low",
					optional = true,
					type = "Boolean",
					value = "",
					order = 6,
					size = 0
				),
				location = "Header",
				id = "",
				name = "",
				description = ""
			),
			ParametersItem(
				field = Field(
					reference = "",
					access = 255,
					identity = Identity(
						name = "estimation",
						id = "0bcb5cb8-1988-4b02-9c57-ecb7235d4a47",
						description = "for filtering as a sample of String"
					),
					keys = false,
					usage = "Property",
					count = 1,
					format = "",
					privacy = "low",
					optional = true,
					type = "String",
					value = "",
					order = 9,
					size = 0
				),
				location = "Header",
				id = "",
				name = "",
				description = ""
			),
			ParametersItem(
				field = Field(
					reference = "",
					access = 255,
					identity = Identity(
						name = "partyId",
						id = "afccc7ab-1a9c-4ae7-aedc-30a6c7a2d284",
						description = "for filtering as a sample of Guid"
					),
					keys = false,
					usage = "Property",
					count = 1,
					format = "",
					privacy = "low",
					optional = true,
					type = "String",
					value = "",
					order = 5,
					size = 36
				),
				location = "Header",
				id = "",
				name = "",
				description = ""
			),
			ParametersItem(
				field = Field(
					reference = "/organizations/DJet/systems/Test/applications/Product/datasets/Product/versions/1.0.0",
					access = 255,
					identity = Identity(
						name = "product",
						id = "194037ca-a7ed-44b0-a206-c03ae4f33400",
						description = "Product data specification.\n"
					),
					keys = false,
					usage = "Property",
					count = 1,
					format = "",
					privacy = "low",
					optional = true,
					type = "Structure",
					value = "",
					order = 3,
					size = 0
				),
				location = "Body",
				id = "",
				name = "",
				description = ""
			),
			ParametersItem(
				field = Field(
					reference = "",
					access = 255,
					identity = Identity(
						name = "productId",
						id = "365ffc5a-e846-47ab-8e40-9ad71a7e6308",
						description = "Product Id to fetch and put records"
					),
					keys = false,
					usage = "Property",
					count = 1,
					format = "",
					privacy = "low",
					optional = true,
					type = "String",
					value = "",
					order = 2,
					size = 0
				),
				location = "Path",
				id = "",
				name = "",
				description = ""
			),
			ParametersItem(
				field = Field(
					reference = "",
					access = 255,
					identity = Identity(
						name = "productLineId",
						id = "0416c418-af65-4a3b-b290-3d6068fd2239",
						description = "Id of Product Line"
					),
					keys = false,
					usage = "Property",
					count = 1,
					format = "",
					privacy = "low",
					optional = true,
					type = "String",
					value = "",
					order = 4,
					size = 0
				),
				location = "Path",
				id = "",
				name = "",
				description = ""
			),
			ParametersItem(
				field = Field(
					reference = "",
					access = 255,
					identity = Identity(
						name = "rank",
						id = "ebb83ec4-7946-4108-968c-251d077e19fa",
						description = "for filtering as a sample of Integer"
					),
					keys = false,
					usage = "Property",
					count = 1,
					format = "",
					privacy = "low",
					optional = true,
					type = "Integer",
					value = "",
					order = 8,
					size = 0
				),
				location = "Header",
				id = "",
				name = "",
				description = ""
			),
			ParametersItem(
				field = Field(
					reference = "",
					access = 255,
					identity = Identity(
						name = "search",
						id = "785e0028-a4c0-4072-8e81-10e9a2d0a23d",
						description = "Expression to limit number of entities in response."
					),
					keys = false,
					usage = "Property",
					count = 1,
					format = "",
					privacy = "low",
					optional = true,
					type = "String",
					value = "",
					order = 1,
					size = 0
				),
				location = "Query",
				id = "",
				name = "",
				description = ""
			),
			ParametersItem(
				field = Field(
					reference = "",
					access = 255,
					identity = Identity(
						name = "type",
						id = "4f289f21-d276-4c48-97f0-069a3326fa26",
						description = "for filtering as a sample of Enum"
					),
					keys = false,
					usage = "Property",
					count = 1,
					format = "",
					privacy = "low",
					optional = true,
					type = "String",
					value = "",
					order = 7,
					size = 0
				),
				location = "Header",
				id = "",
				name = "",
				description = ""
			),
		),
		layouts = listOf(),
		`object` = Object(
			parent = Parent(
				name = "/organizations/DJet/systems/Test/applications/Product",
				id = "78d2023d-d789-4778-bf3d-96a6aa64cecc"
			),
			access = "External",
			usage = "RESTful API",
			subscriptionCount = 0,
			subscriptioncount = 0,
			history = History(
				updatedby = "",
				createdby = "",
				created = "2021-05-24T12:01:05.873",
				completions = listOf(),
				updated = "2021-05-24T12:01:07.03",
				mirrored = ""
			),
			type = "Interface",
			picture = "",
			publicationCount = 0,
			publicationcount = 0,
			contact = null,
			lastapprovedversion = null,
			elements = listOf(),
			tags = listOf(),
			documents = listOf(),
			properties = listOf(),
			name = null,
			alias = ""
		)
	)


	@Test
	fun `should convert`() {
		val converter = InterfaceConverter(model).apply {
			convert()
		}
		val paths = converter.paths
	}

}

