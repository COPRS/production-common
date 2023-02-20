package de.werum.coprs.nativeapi.rest.model.stac;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * https://github.com/radiantearth/stac-spec/blob/master/collection-spec/README.md
 */
@JsonTypeName("Collection")
@JsonPropertyOrder({ "type", "stac_version", "stac_extensions", "id", "title", "description", "keywords", "license",
		"providers", "extent", "summaries", "links", "assets" })
public class StacCollection implements Serializable {

	private static final long serialVersionUID = -632503474493792210L;

	private String type = "Collection";
	
	@JsonProperty("stac_version")
	private String stacVersion = "1.0.0";
	
	@JsonProperty("stac_extensions")
	private List<String> stacExtensions = new ArrayList<>();
	
	private String id;
	
	private String title;
	
	private String description;
	
	private List<String> keywords = new ArrayList<>();
	
	private String license;
	
	private List<StacProvider> providers = new ArrayList<>();
	
	private StacExtent extent;
	
	private Map<String, Object> summaries = new HashMap<>();
	
	private List<StacLink> links = new ArrayList<>();
	
	private Map<String, StacAsset> assets = new HashMap<>();

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStacVersion() {
		return stacVersion;
	}

	public void setStacVersion(String stacVersion) {
		this.stacVersion = stacVersion;
	}

	public List<String> getStacExtensions() {
		return stacExtensions;
	}

	public void setStacExtensions(List<String> stacExtensions) {
		this.stacExtensions = stacExtensions;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public List<StacProvider> getProviders() {
		return providers;
	}

	public void setProviders(List<StacProvider> providers) {
		this.providers = providers;
	}

	public StacExtent getExtent() {
		return extent;
	}

	public void setExtent(StacExtent extent) {
		this.extent = extent;
	}

	public Map<String, Object> getSummaries() {
		return summaries;
	}

	public void setSummaries(Map<String, Object> summaries) {
		this.summaries = summaries;
	}

	public List<StacLink> getLinks() {
		return links;
	}

	public void setLinks(List<StacLink> links) {
		this.links = links;
	}

	public Map<String, StacAsset> getAssets() {
		return assets;
	}

	public void setAssets(Map<String, StacAsset> assets) {
		this.assets = assets;
	}
}
