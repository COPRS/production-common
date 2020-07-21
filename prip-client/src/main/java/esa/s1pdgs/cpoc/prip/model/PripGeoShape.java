package esa.s1pdgs.cpoc.prip.model;

import java.util.List;
import java.util.Objects;

import org.elasticsearch.common.geo.GeoShapeType;
import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;

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
	protected List<Coordinate> coordinates;

	// --------------------------------------------------------------------------

	public PripGeoShape(String type, List<Object> coordinates) {
		this(GeoShapeType.valueOf(type), coordinates);
	}

	public PripGeoShape(GeoShapeType type, List<Object> coordinates) {
		this.type = Objects.requireNonNull(type);

		final CoordinatesBuilder coordBuilder = new CoordinatesBuilder();
		for (final Object coordPair : Objects.requireNonNull(coordinates)) {
			final List<Number> coords = (List<Number>) coordPair;
			final double lon = coords.get(0).doubleValue();
			final double lat = coords.get(1).doubleValue();
			coordBuilder.coordinate(lon, lat);
		}
		// coordBuilder.close();
		this.coordinates = coordBuilder.build();
	}

	// --------------------------------------------------------------------------

	@Override
	public String toString() {
		final JSONObject json = new JSONObject();

		json.put(FIELD_NAMES.TYPE.fieldName, this.type.shapeName());
		json.put(FIELD_NAMES.COORDINATES.fieldName, this.coordinates);

		return json.toString();
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

	public GeoShapeType getType() {
		return type;
	}

	public void setType(GeoShapeType type) {
		this.type = type;
	}

	public List<Coordinate> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(List<Coordinate> coordinates) {
		this.coordinates = coordinates;
	}

}
