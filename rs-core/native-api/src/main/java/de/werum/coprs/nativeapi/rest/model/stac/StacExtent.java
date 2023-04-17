package de.werum.coprs.nativeapi.rest.model.stac;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * https://github.com/radiantearth/stac-spec/blob/master/collection-spec/collection-spec.md#extent-object
 */
@JsonTypeName("Extent")
@JsonPropertyOrder({"spatial", "temporal"})
public class StacExtent implements Serializable {
	
	private static final long serialVersionUID = -1435134305439608842L;

	private StacSpatialExtent spatial;
	private StacTemporalExtent temporal;

	public StacSpatialExtent getSpatial() {
		return spatial;
	}

	public void setSpatial(StacSpatialExtent spatial) {
		this.spatial = spatial;
	}

	public StacTemporalExtent getTemporal() {
		return temporal;
	}

	public void setTemporal(StacTemporalExtent temporal) {
		this.temporal = temporal;
	}
}
