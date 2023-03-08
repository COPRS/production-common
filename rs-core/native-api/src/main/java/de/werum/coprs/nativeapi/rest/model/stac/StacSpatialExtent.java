package de.werum.coprs.nativeapi.rest.model.stac;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * https://github.com/radiantearth/stac-spec/blob/master/collection-spec/collection-spec.md#spatial-extent-object
 */
@JsonTypeName("SpatialExtent")
@JsonPropertyOrder({"bbox"})
public class StacSpatialExtent implements Serializable {

	private static final long serialVersionUID = 1673945119710022419L;
	
	private double[] bbox;

	public double[] getBbox() {
		return bbox;
	}

	public void setBbox(double[] bbox) {
		this.bbox = bbox;
	}
}
