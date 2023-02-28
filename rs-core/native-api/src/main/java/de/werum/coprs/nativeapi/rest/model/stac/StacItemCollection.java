package de.werum.coprs.nativeapi.rest.model.stac;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * https://github.com/radiantearth/stac-api-spec/blob/master/fragments/itemcollection/README.md
 */
@JsonTypeName("FeatureCollection")
@JsonPropertyOrder({ "rel", "href", "type", "title" })
public class StacItemCollection extends GeoJsonBase {

	private static final long serialVersionUID = -6533225776071929373L;

	private List<StacItem> features = new ArrayList<StacItem>();

	private List<StacLink> links = new ArrayList<>();

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof StacItemCollection)) {
			return false;
		}
		return this.features.equals(((StacItemCollection) o).features);
	}

	@Override
	public int hashCode() {
		return this.features.hashCode();
	}

	@Override
	public String toString() {
		return "ItemCollection{" + "features=" + this.features + '}';
	}

	public List<StacItem> getFeatures() {
		return this.features;
	}

	public void setFeatures(List<StacItem> features) {
		this.features = features;
	}

	public List<StacLink> getLinks() {
		return this.links;
	}

	public void setLinks(List<StacLink> links) {
		this.links = links;
	}

}
