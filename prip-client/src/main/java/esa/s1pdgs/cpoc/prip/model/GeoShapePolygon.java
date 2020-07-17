package esa.s1pdgs.cpoc.prip.model;

import java.util.List;

import org.elasticsearch.common.geo.GeoShapeType;

/**
 * Java representation of the elasticsearch geo_shape polygon type.
 */
public class GeoShapePolygon extends PripGeoShape {
	
	public GeoShapePolygon(List<Object> coordinates) {
		super(GeoShapeType.POLYGON,coordinates);
	}

}
