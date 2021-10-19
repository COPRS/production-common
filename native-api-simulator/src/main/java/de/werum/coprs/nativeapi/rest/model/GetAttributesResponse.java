package de.werum.coprs.nativeapi.rest.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Attributes")
public class GetAttributesResponse {

	@Schema(example = "[\"name\", \"publicationDate\", \"attr_beginningDateTime_date\", \"attr_orbitDirection_string\"]", description = "the attribute names for a particular mission or product type")
	private List<String> attributes = new ArrayList<>();

	public GetAttributesResponse(final List<String> attributes) {
		this.attributes = attributes;
	}

	public List<String> getAttributes() {
		return this.attributes;
	}

	public void setAttributes(final List<String> attributes) {
		this.attributes = attributes;
	}

}
