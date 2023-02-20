package de.werum.coprs.nativeapi.rest.model.stac;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("Catalog")
@JsonPropertyOrder({"type", "stac_version", "id", "title", "description", "conformsTo", "links"})
public class StacRootCatalog extends StacCatalog {

	private static final long serialVersionUID = 8792496061447498814L;
	
	private List<String> conformsTo;

	public List<String> getConformsTo() {
		return conformsTo;
	}

	public void setConformsTo(List<String> conformsTo) {
		this.conformsTo = conformsTo;
	}
}
