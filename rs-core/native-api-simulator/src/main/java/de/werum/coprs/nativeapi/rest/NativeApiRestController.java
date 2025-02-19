/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.werum.coprs.nativeapi.rest;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.werum.coprs.nativeapi.rest.model.GetAttributesResponse;
import de.werum.coprs.nativeapi.rest.model.GetMissionsResponse;
import de.werum.coprs.nativeapi.rest.model.GetProductTypesResponse;
import de.werum.coprs.nativeapi.rest.model.PingResponse;
import de.werum.coprs.nativeapi.rest.model.PripMetadataResponse;
import de.werum.coprs.nativeapi.service.NativeApiService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin
@RestController
@RequestMapping("api/${native-api.major-version}")
@ApiResponses
@OpenAPIDefinition(
	info = @Info(
		title = "RS Native API",
		description = "The Native API of the Copernicus Reference System (COPRS) that can be used to query and download product data.",
		version = "${native-api.version}"
	),
	/*servers = {
		@Server(
			url = "http://localhost:8080/",
			description = "local URL"
		),
		@Server(
			url = "http://172.28.149.5:8888",
			description = "dev machine"
		)
	},*/
	tags = @Tag(
		name = "Metadata"
	)
)
public class NativeApiRestController {

	public static final Logger LOGGER = LogManager.getLogger(NativeApiRestController.class);

	public final NativeApiService nativeApiService;

	@Autowired
	public NativeApiRestController(final NativeApiService nativeApiService) {
		this.nativeApiService = nativeApiService;
	}

	@Operation(
		operationId = "PingApi", tags = "Metadata",
		summary = "check whether the API service is reachable and running and retrieve the API version number",
		description = "A HTTP 200 answer is to be expected when the API service is running and reachable. Additionally the current API version number will be retrieved."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "OK - the API service is running and reachable",
			content = {@Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				//schema = @Schema(implementation = Book.class)
				schema = @Schema(implementation = PingResponse.class)
			)}
		),
		@ApiResponse(
			responseCode = "400",
			description = "Bad Request - the API service rejects to process the request because of client side errors, for example a malformed request syntax",
			content = @Content
		),
		@ApiResponse(
			responseCode = "500",
			description = "Internal Server Error - the API service encountered an unexpected condition that prevented it from fulfilling the request",
			content = @Content
		)
	})
	@RequestMapping(method = RequestMethod.GET, path = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
	public PingResponse ping() {
		LOGGER.debug("Received ping request");
		final String version = this.nativeApiService.getNativeApiVersion();
		return new PingResponse(null != version && !version.isEmpty() ? version : "UNKNOWN");
	}

	@Operation(
		operationId = "GetMissions", tags = "Metadata",
		summary = "retrieve the names of the missions supported by the API",
		description = "To search for satellite product data the data can be filtered by attributes which depend on the product type which in turn depend on the satellite mission. This endpoint returns all names of the missions supported by the API."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "OK - the supperted mission names were returned with this response",
			content = {@Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = GetMissionsResponse.class)
			)}
		),
		@ApiResponse(
			responseCode = "400",
			description = "Bad Request - the API service rejects to process the request because of client side errors, for example a malformed request syntax",
			content = @Content
		),
		@ApiResponse(
			responseCode = "500",
			description = "Internal Server Error - the API service encountered an unexpected condition that prevented it from fulfilling the request",
			content = @Content
		)
	})
	@RequestMapping(method = RequestMethod.GET, path = "/missions", produces = MediaType.APPLICATION_JSON_VALUE)
	public GetMissionsResponse getMissions() {
		LOGGER.debug("request received: /missions");
		return new GetMissionsResponse(this.nativeApiService.getMissions());
	}
	
	@Operation(
		operationId = "GetAttributes", tags = "Metadata",
		summary = "retrieve the names of the base attributes supported for a particular mission",
		description = "To search for satellite product data the data can be filtered by base attributes which depend on the satellite mission. This endpoint returns all base attribute names supported for the given mission."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "OK - the base attribute names for the given mission were returned with this response",
			content = {@Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = GetAttributesResponse.class)
			)}
		),
		@ApiResponse(
			responseCode = "400",
			description = "Bad Request - the API service rejects to process the request because of client side errors, for example a malformed request syntax",
			content = @Content
		),
		@ApiResponse(
			responseCode = "500",
			description = "Internal Server Error - the API service encountered an unexpected condition that prevented it from fulfilling the request",
			content = @Content
		)
	})
	@RequestMapping(method = RequestMethod.GET, path = "/missions/{missionName}/attributes", produces = MediaType.APPLICATION_JSON_VALUE)
	public GetAttributesResponse getAttributes(@PathVariable final String missionName) {
		LOGGER.debug("request received: /missions/{}/attributes", missionName);
		return new GetAttributesResponse(this.nativeApiService.getAttributes(missionName));
	}

	@Operation(
		operationId = "GetProductTypes", tags = "Metadata",
		summary = "retrieve the names of the product types supported for a particular mission",
		description = "To search for satellite product data the data can be filtered by attributes which depend on the product type which in turn depend on the satellite mission. This endpoint returns all names of the product types supported for the given mission."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "OK - the product type names for the given mission were returned with this response",
			content = {@Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = GetProductTypesResponse.class)
			)}
		),
		@ApiResponse(
			responseCode = "400",
			description = "Bad Request - the API service rejects to process the request because of client side errors, for example a malformed request syntax",
			content = @Content
		),
		@ApiResponse(
			responseCode = "500",
			description = "Internal Server Error - the API service encountered an unexpected condition that prevented it from fulfilling the request",
			content = @Content
		)
	})
	@RequestMapping(method = RequestMethod.GET, path = "/missions/{missionName}/productTypes", produces = MediaType.APPLICATION_JSON_VALUE)
	public GetProductTypesResponse getProductTypes(@PathVariable final String missionName) {
		LOGGER.debug("request received: /missions/{}/productTypes", missionName);
		return new GetProductTypesResponse(this.nativeApiService.getProductTypes(missionName));
	}

	@Operation(
		operationId = "GetAttributes", tags = "Metadata",
		summary = "retrieve the names of the attributes supported for a particular mission and product type",
		description = "To search for satellite product data the data can be filtered by attributes which depend on the product type which in turn depend on the satellite mission. This endpoint returns all attribute names supported for the given mission and product type."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "OK - the attribute names for the given mission and product type were returned with this response",
			content = {@Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = GetAttributesResponse.class)
			)}
		),
		@ApiResponse(
			responseCode = "400",
			description = "Bad Request - the API service rejects to process the request because of client side errors, for example a malformed request syntax",
			content = @Content
		),
		@ApiResponse(
			responseCode = "500",
			description = "Internal Server Error - the API service encountered an unexpected condition that prevented it from fulfilling the request",
			content = @Content
		)
	})
	@RequestMapping(method = RequestMethod.GET, path = "/missions/{missionName}/productTypes/{productType}/attributes",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public GetAttributesResponse getAttributes(
			@PathVariable final String missionName,
			@PathVariable final String productType) {
		LOGGER.debug("request received: /missions/{}/productTypes/{}/attributes", missionName, productType);
		return new GetAttributesResponse(this.nativeApiService.getAttributes(missionName, productType));
	}
	
	@Operation(
		operationId = "FindProduct", tags = "Products",
		summary = "find product metadata of a single product for a given product ID",
		description = "This endpoint allows to retrieve the metadata of a single product by its mission and ID."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "OK - the product metadata for the given mission and product ID was returned with this response",
			content = {@Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = PripMetadataResponse.class)
				)}
			),
			@ApiResponse(
				responseCode = "400",
				description = "Bad Request - the API service rejects to process the request because of client side errors, for example a malformed request syntax",
				content = @Content
			),
			@ApiResponse(
				responseCode = "500",
				description = "Internal Server Error - the API service encountered an unexpected condition that prevented it from fulfilling the request",
				content = @Content
			)
	})
	@RequestMapping(method = RequestMethod.GET, path = "/missions/{missionName}/products/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public PripMetadataResponse findProduct(
			@PathVariable final String missionName,
			@PathVariable final String productId) {
		LOGGER.debug("Received single product metadata request: /missions/{}/products/{}", missionName, productId);
		try {
			return this.nativeApiService.findProduct(missionName, productId);
		} catch (final Exception e) {
			throw new NativeApiRestControllerException(String.format("Internal server error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Operation(
		operationId = "FindProducts", tags = "Products",
		summary = "find product metadata for a given mission using a filter",
		description = "To search for satellite product metadata the data can be filtered by attributes which depend on the satellite mission. This endpoint allows to retrieve filtered product metadata for the given mission."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "OK - the product metadata for the given mission and filter was returned with this response",
			content = {@Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = PripMetadataResponse.class)
				)}
			),
			@ApiResponse(
				responseCode = "400",
				description = "Bad Request - the API service rejects to process the request because of client side errors, for example a malformed request syntax",
				content = @Content
			),
			@ApiResponse(
				responseCode = "500",
				description = "Internal Server Error - the API service encountered an unexpected condition that prevented it from fulfilling the request",
				content = @Content
			)
	})
	@RequestMapping(method = RequestMethod.GET, path = "/missions/{missionName}/products", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<PripMetadataResponse> findProducts(
			@PathVariable final String missionName,
			@RequestParam(value = "filter", required = false) final String filter) {
		LOGGER.debug("Received product search request: /missions/{}/products?filter={}", missionName, filter);
		final List<PripMetadataResponse> result;

		try {
			result = this.nativeApiService.findWithFilters(missionName, filter);
		} catch (final Exception e) {
			throw new NativeApiRestControllerException(String.format("Internal server error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return result;
	}
	
	@Operation(
		operationId = "FindProducts", tags = "Products",
		summary = "find product metadata for a given mission and product type using a filter",
		description = "To search for satellite product metadata the data can be filtered by attributes which depend on product type which in turn depend on the satellite mission. This endpoint allows to retrieve filtered product metadata for the given mission and product type."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "OK - the product metadata for the given mission, product type and filter was returned with this response",
			content = {@Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = PripMetadataResponse.class)
				)}
			),
			@ApiResponse(
				responseCode = "400",
				description = "Bad Request - the API service rejects to process the request because of client side errors, for example a malformed request syntax",
				content = @Content
			),
			@ApiResponse(
				responseCode = "500",
				description = "Internal Server Error - the API service encountered an unexpected condition that prevented it from fulfilling the request",
				content = @Content
			)
	})
	@RequestMapping(method = RequestMethod.GET, path = "/missions/{missionName}/productTypes/{productType}/products", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<PripMetadataResponse> findProducts(
			@PathVariable final String missionName,
			@PathVariable final String productType,
			@RequestParam(value = "filter", required = false) final String filter) {
		LOGGER.debug("Received product search request: /missions/{}/productTypes/{}/products?filter={}", missionName, productType, filter);
		final List<PripMetadataResponse> result;

		try {
			result = this.nativeApiService.findWithFilters(missionName, productType, filter);
		} catch (final Exception e) {
			throw new NativeApiRestControllerException(String.format("Internal server error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return result;
	}
	
	@Operation(
		operationId = "DownloadProduct", tags = "Products",
		summary = "download the zipped product file",
		description = "This endpoint enables the download of the zipped product file which is denoted by the given ID."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "OK - the actual link to download the zipped product file was returned with this response",
			content = {@Content(
				mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
				schema = @Schema(type = "string", format = "binary")
			)}
		),
		@ApiResponse(
			responseCode = "400",
			description = "Bad Request - the API service rejects to process the request because of client side errors, for example a malformed request syntax",
			content = @Content
		),
		@ApiResponse(
			responseCode = "404",
			description = "Not Found - the product with the given ID wasn't found",
			content = @Content
		),
		@ApiResponse(
			responseCode = "500",
			description = "Internal Server Error - the API service encountered an unexpected condition that prevented it from fulfilling the request",
			content = @Content
		)
	})
	@RequestMapping(method = RequestMethod.GET, path = "/missions/{missionName}/products/{productId}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public byte[] downloadProduct(
			@PathVariable final String missionName,
			@PathVariable final String productId,
			final HttpServletResponse response) {
		LOGGER.debug("download request received: /missions/{}/products/{}/download", missionName, productId);

		try {
			response.setHeader("Content-Disposition", "attachment; filename=\"dummy-file.zip\"");
			return this.nativeApiService.downloadProduct(missionName, productId);
		} catch (final Exception e) {
			throw new NativeApiRestControllerException(String.format("error downloading product file with ID %s: %s", productId, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
