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

package de.werum.coprs.nativeapi.rest.model.stac;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * https://github.com/radiantearth/stac-spec/blob/master/catalog-spec/README.md
 */
@JsonTypeName("Catalog")
@JsonPropertyOrder({ "type", "stac_version", "stac_extensions", "id", "title", "description", "links", "collections" })
public class StacCatalog implements Serializable {

	private static final long serialVersionUID = -7836423582307186362L;

	private String type = "Catalog";

	@JsonProperty("stac_version")
	private String stacVersion = "1.0.0";

	@JsonProperty("stac_extensions")
	private List<String> stacExtensions = new ArrayList<>();

	private String id;

	private String title;

	private String description;

	private List<StacLink> links = new ArrayList<>();

	private List<StacCollection> collections = new ArrayList<>();

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

	public List<StacLink> getLinks() {
		return links;
	}

	public void setLinks(List<StacLink> links) {
		this.links = links;
	}

	public List<StacCollection> getCollections() {
		return collections;
	}

	public void setCollections(List<StacCollection> collections) {
		this.collections = collections;
	}
}
