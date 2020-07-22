package esa.s1pdgs.cpoc.prip.frontend.service.mapping;

import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Algorithm;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Checksums;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.ContentDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.ContentLength;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.ContentType;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.End;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.EvictionDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Footprint;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Id;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Name;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.ProductionType;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.PublicationDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Start;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Value;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.geo.Geospatial;
import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;
import org.apache.olingo.commons.api.edm.geo.LineString;
import org.apache.olingo.commons.api.edm.geo.Point;
import org.apache.olingo.commons.api.edm.geo.Polygon;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;

import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.GeoShapePolygon;
import esa.s1pdgs.cpoc.prip.model.PripGeoCoordinate;
import esa.s1pdgs.cpoc.prip.model.PripGeoShape;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;

public class MappingUtil {
	
	private static final int MILLIS_PER_SECOND = 1000;
	
	public static Entity pripMetadataToEntity(PripMetadata pripMetadata, String rawBaseUri) {
		URI uri = MappingUtil.createId(rawBaseUri, EdmProvider.ES_PRODUCTS_NAME, pripMetadata.getId());
		Entity entity = new Entity()
				.addProperty(new Property(null, Id.name(), ValueType.PRIMITIVE, pripMetadata.getId()))
				.addProperty(new Property(null, Name.name(), ValueType.PRIMITIVE, pripMetadata.getName()))
				.addProperty(new Property(null, ContentType.name(), ValueType.PRIMITIVE, pripMetadata.getContentType()))
				.addProperty(new Property(null, ContentLength.name(), ValueType.PRIMITIVE, pripMetadata.getContentLength()))
				.addProperty(new Property(null, ContentDate.name(), ValueType.COMPLEX, convertToContentDate(pripMetadata.getContentDateStart(), pripMetadata.getContentDateEnd())))
				.addProperty(new Property(null, PublicationDate.name(), ValueType.PRIMITIVE, convertLocalDateTimeToTimestamp(pripMetadata.getCreationDate())))
				.addProperty(new Property(null, EvictionDate.name(), ValueType.PRIMITIVE, convertLocalDateTimeToTimestamp(pripMetadata.getEvictionDate())))
				.addProperty(new Property(null, ProductionType.name(), ValueType.ENUM, mapToProductionType(esa.s1pdgs.cpoc.prip.model.ProductionType.SYSTEMATIC_PRODUCTION)))
				.addProperty(new Property(null, Checksums.name(), ValueType.COLLECTION_COMPLEX, mapToChecksumList(pripMetadata.getChecksums())))
				.addProperty(new Property(null, Footprint.name(), ValueType.GEOSPATIAL, mapToGeospatial(pripMetadata.getFootprint())));
		entity.setMediaContentType(pripMetadata.getContentType());
		entity.setId(uri);
		return entity;
	}

	public static URI createId(String rawBaseUri, String entitySetName, UUID id) {
		try {
			return new URI(rawBaseUri + "/" + entitySetName + "(" + id.toString() + ")");
		} catch (URISyntaxException e) {
			throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
		}
	}
	
	public static Timestamp convertLocalDateTimeToTimestamp(LocalDateTime localDateTime) {
		if (null != localDateTime) {
			try {
	            Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
				Timestamp stamp = new Timestamp(instant.getEpochSecond() * MILLIS_PER_SECOND);
				stamp.setNanos(instant.getNano() / 1000000 * 1000000); // results in cutting off places
	            return stamp;
	        } catch (ArithmeticException ex) {
	            throw new IllegalArgumentException(ex);
	        }
		} else {
			return null;
		}
	}
	
	public static ComplexValue convertToContentDate(LocalDateTime contentDateStart, LocalDateTime contentDateEnd) {
		ComplexValue complexValue = new ComplexValue();
		complexValue.getValue().add(new Property(null, Start.name(), ValueType.PRIMITIVE, convertLocalDateTimeToTimestamp(contentDateStart)));
		complexValue.getValue().add(new Property(null, End.name(), ValueType.PRIMITIVE, convertLocalDateTimeToTimestamp(contentDateEnd)));
		return complexValue;
	}
	
	public static List<ComplexValue> mapToChecksumList(List<Checksum> checksums) {
		List<ComplexValue> listOfComplexValues = new ArrayList<>();
		if (null != checksums) {
			for (Checksum checksum : checksums) {
				ComplexValue complexValue = new ComplexValue();
				complexValue.getValue().add(new Property(null, Algorithm.name(), ValueType.PRIMITIVE, checksum.getAlgorithm()));
				complexValue.getValue().add(new Property(null, Value.name(), ValueType.PRIMITIVE, checksum.getValue()));
				listOfComplexValues.add(complexValue);
			}
		}
		return listOfComplexValues;
	}
	
	public static Geospatial mapToGeospatial(PripGeoShape footprint) {
		Geospatial result = null;
		if(null != footprint && footprint instanceof GeoShapePolygon) {
			SRID srid = SRID.valueOf(String.valueOf(footprint.getSRID()));
			List<Point> points = new ArrayList<>();
			for (PripGeoCoordinate coordinates : footprint.getCoordinates()) {
				Point p = new Point(Dimension.GEOGRAPHY, srid);
				p.setX(coordinates.getLongitude());
				p.setY(coordinates.getLatitude());
				points.add(p);
			}			
			LineString lineString = new LineString(Dimension.GEOGRAPHY, srid, points);
			result = new Polygon(Dimension.GEOGRAPHY, srid, null, lineString);
		}
		return result;
	}

	public static Integer mapToProductionType(esa.s1pdgs.cpoc.prip.model.ProductionType productionType) {
		return productionType.getValue();
	}

}
