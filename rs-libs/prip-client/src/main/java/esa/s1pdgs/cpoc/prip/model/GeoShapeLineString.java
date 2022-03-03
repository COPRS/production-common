package esa.s1pdgs.cpoc.prip.model;

import java.util.List;

import org.elasticsearch.common.geo.GeoShapeType;

/**
 * Java representation of the elasticsearch geo_shape line string type.
 */
public class GeoShapeLineString extends PripGeoShape {
	
	public GeoShapeLineString(List<PripGeoCoordinate> coordinates) {
		super(GeoShapeType.LINESTRING,coordinates);
	}

}
