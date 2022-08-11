package esa.s1pdgs.cpoc.prip.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.elasticsearch.common.geo.GeoShapeType;

/**
 * Java representation of the elasticsearch geo_shape type.
 */
public class PripGeoShape {

	public enum FIELD_NAMES {
		TYPE("type"), //
		COORDINATES("coordinates");

		private String fieldName;

		FIELD_NAMES(String fieldName) {
			this.fieldName = fieldName;
		}

		public String fieldName() {
			return fieldName;
		}
	}

	// --------------------------------------------------------------------------

	protected GeoShapeType type;
	protected List<PripGeoCoordinate> coordinates;

	// --------------------------------------------------------------------------

	public PripGeoShape(String type, List<PripGeoCoordinate> coordinates) {
		this(GeoShapeType.valueOf(type), coordinates);
	}

	protected PripGeoShape(GeoShapeType type, List<PripGeoCoordinate> coordinates) {
		this.type = Objects.requireNonNull(type);
		this.coordinates = coordinates;
	}

	// --------------------------------------------------------------------------

	public Map<String, Object> asMap() {
		final Map<String, Object> map = new HashMap<>();

		List<List<List<Double>>> coordExportOuterList = new ArrayList<>();
		List<List<Double>> coordExportInnerList = new ArrayList<>();
		coordExportOuterList.add(coordExportInnerList);
		for (PripGeoCoordinate coords : coordinates) {
			ArrayList<Double> p = new ArrayList<>();
			p.add(coords.getLongitude());
			p.add(coords.getLatitude());
			coordExportInnerList.add(p);
		}

		map.put(FIELD_NAMES.TYPE.fieldName, this.type.shapeName());

		if (GeoShapeType.LINESTRING.equals(this.type)) {
			map.put(FIELD_NAMES.COORDINATES.fieldName, coordExportInnerList);
		} else if (GeoShapeType.POLYGON.equals(this.type)) {
			map.put(FIELD_NAMES.COORDINATES.fieldName, coordExportOuterList);
		}
		return map;
	}

	@Override
	public int hashCode() {
		final int prime = 37;
		int result = 1;

		result = prime * result + ((this.getType() == null) ? 0 : this.getType().hashCode());
		result = prime * result + ((this.getCoordinates() == null) ? 0 : this.getCoordinates().hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}

		final PripGeoShape other = (PripGeoShape) obj;

		if (null == this.getType()) {
			if (null != other.getType()) {
				return false;
			}
		} else if (!this.getType().equals(other.getType())) {
			return false;
		}

		if (null == this.getCoordinates() || this.getCoordinates().isEmpty()) {
			if (null != other.getCoordinates() && !other.getCoordinates().isEmpty()) {
				return false;
			}
		} else if (!this.getCoordinates().equals(other.getCoordinates())) {
			return false;
		}

		return true;
	}

	// --------------------------------------------------------------------------

	protected GeoShapeType getType() {
		return type;
	}

	protected void setType(GeoShapeType type) {
		this.type = type;
	}

	public List<PripGeoCoordinate> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(List<PripGeoCoordinate> coordinates) {
		this.coordinates = coordinates;
	}

	public int getSRID() {
		return 4326; // EPSG-Code for WGS84 (Elasticsearch uses WGS-84 coordinates only)
	}

}
