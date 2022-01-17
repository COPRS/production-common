package de.werum.coprs.nativeapi.rest.model.stac;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * https://github.com/radiantearth/stac-api-spec/blob/master/stac-spec/item-spec/item-spec.md#asset-object
 */
@JsonTypeName("Asset")
@JsonPropertyOrder({ "href", "type", "title", "description", "roles" })
public class StacAsset implements Serializable {

	private static final long serialVersionUID = -6421597803514476945L;

	private String href;
	private String title;
	private String description;
	private String type;
	private List<String> roles;

	public String getHref() {
		return this.href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getRoles() {
		return this.roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

}
