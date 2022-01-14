package de.werum.coprs.nativeapi.rest.model.stac;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("FeatureCollection")
public class StacItemCollection extends GeoJsonBase {

	private static final long serialVersionUID = -6533225776071929373L;

	private List<StacItem> features = new ArrayList<StacItem>();

	// private List<Object> links = new ArrayList<>(); // implement as needed, see StacItem.class

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

}
