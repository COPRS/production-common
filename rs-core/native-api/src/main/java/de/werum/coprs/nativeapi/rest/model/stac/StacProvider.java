package de.werum.coprs.nativeapi.rest.model.stac;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * https://github.com/radiantearth/stac-spec/blob/master/collection-spec/collection-spec.md#provider-object
 */
@JsonTypeName("Provider")
@JsonPropertyOrder({"name", "description", "roles", "url"})
public class StacProvider implements Serializable {
	
	private static final long serialVersionUID = -3970245485445706769L;
	
	private String name;
	private String description;
	private List<String> roles = new ArrayList<>();
	private String url;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
