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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geojson.Feature;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * https://github.com/radiantearth/stac-api-spec/blob/master/stac-spec/item-spec/item-spec.md
 */
@JsonTypeName("Feature")
@JsonPropertyOrder({ "type", "stac_version", "stac_extensions", "id", "geometry", "bbox", "properties", "links", "assets", "collection" })
public class StacItem extends Feature {

	private static final long serialVersionUID = -82559048366932283L;

	@JsonProperty("stac_version")
	private String stacVersion = "1.0.0";

	@JsonProperty("stac_extensions")
	private Set<String> stacExtensions = new HashSet<>();

	private List<StacLink> links = new ArrayList<>();

	private Map<String, StacAsset> assets = new HashMap<>();

	private String collection;

	public String getStacVersion() {
		return this.stacVersion;
	}

	public void setStacVersion(final String stacVersion) {
		this.stacVersion = stacVersion;
	}

	public Set<String> getStacExtensions() {
		return this.stacExtensions;
	}

	public void setStacExtensions(final Set<String> stacExtensions) {
		this.stacExtensions = stacExtensions;
	}

	public List<StacLink> getLinks() {
		return this.links;
	}

	public void setLinks(final List<StacLink> links) {
		this.links = links;
	}

	public Map<String, StacAsset> getAssets() {
		return this.assets;
	}

	public void setAssets(final Map<String, StacAsset> assets) {
		this.assets = assets;
	}

	public String getCollection() {
		return this.collection;
	}

	public void setCollection(final String collection) {
		this.collection = collection;
	}

}
