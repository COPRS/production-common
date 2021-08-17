package de.werum.csgrs.nativeapi.rest.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ProductTypes")
public class GetProductTypesResponse {

	@Schema(example = "[\"l1\", \"l2\", \"aux-safe\"]", description = "the names of the product types for a particular mission")
	private List<String> productTypes = new ArrayList<>();

	public GetProductTypesResponse(final List<String> productTypes) {
		this.productTypes = productTypes;
	}

	public List<String> getProductTypes() {
		return productTypes;
	}

	public void setProductTypes(final List<String> productTypes) {
		this.productTypes = productTypes;
	}
}
