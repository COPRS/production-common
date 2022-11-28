package esa.s1pdgs.cpoc.prip.frontend.service.mapping;

import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Algorithm;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Checksum;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.ChecksumDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.ContentDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.ContentLength;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.ContentType;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Online;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.End;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.EvictionDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Footprint;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Id;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Name;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.ProductionType;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.PublicationDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Start;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Value;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.Geospatial;
import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;
import org.apache.olingo.commons.api.edm.geo.LineString;
import org.apache.olingo.commons.api.edm.geo.Point;
import org.apache.olingo.commons.api.edm.geo.Polygon;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;

import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.frontend.service.edm.QuicklookProperties;
import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.GeoShapeLineString;
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
				.addProperty(new Property(null, Online.name(), ValueType.PRIMITIVE, pripMetadata.isOnline()))
				.addProperty(new Property(null, ContentType.name(), ValueType.PRIMITIVE, pripMetadata.getContentType()))
				.addProperty(
						new Property(null, ContentLength.name(), ValueType.PRIMITIVE, pripMetadata.getContentLength()))
				.addProperty(new Property(null, ContentDate.name(), ValueType.COMPLEX,
						convertToContentDate(pripMetadata.getContentDateStart(), pripMetadata.getContentDateEnd())))
				.addProperty(new Property(null, PublicationDate.name(), ValueType.PRIMITIVE,
						convertLocalDateTimeToTimestamp(pripMetadata.getCreationDate())))
				.addProperty(new Property(null, EvictionDate.name(), ValueType.PRIMITIVE,
						convertLocalDateTimeToTimestamp(pripMetadata.getEvictionDate())))
				.addProperty(new Property(null, ProductionType.name(), ValueType.ENUM,
						mapToProductionType(esa.s1pdgs.cpoc.prip.model.ProductionType.SYSTEMATIC_PRODUCTION)))
				.addProperty(new Property(null, Checksum.name(), ValueType.COLLECTION_COMPLEX,
						mapToChecksumList(pripMetadata.getChecksums())))
				.addProperty(new Property(null, Footprint.name(), ValueType.GEOSPATIAL,
						mapToGeospatial(pripMetadata.getFootprint())));

		entity.setMediaContentType(pripMetadata.getContentType());
		entity.setId(uri);

		Map<FullQualifiedName, EntityCollection> entityCollections = new HashMap<>();
		entityCollections.put(EdmProvider.STRING_TYPE_FQN, new EntityCollection());
		entityCollections.put(EdmProvider.INT_64_TYPE_FQN, new EntityCollection());
		entityCollections.put(EdmProvider.DOUBLE_TYPE_FQN, new EntityCollection());
		entityCollections.put(EdmProvider.BOOLEAN_TYPE_FQN, new EntityCollection());
		entityCollections.put(EdmProvider.DATE_TIME_OFFSET_TYPE_FQN, new EntityCollection());

		// TODO sort attributes
		if (null != pripMetadata.getAttributes()) {
			for (Entry<String, Object> entrySet : pripMetadata.getAttributes().entrySet()) {
				final FullQualifiedName valueType;
				final int firstSeparatorPosition = entrySet.getKey().indexOf('_');
				final int lastSeparatorPosition = entrySet.getKey().lastIndexOf('_');
				switch (entrySet.getKey().substring(lastSeparatorPosition + 1)) {
				case "string":
					valueType = EdmProvider.STRING_TYPE_FQN;
					break;
				case "long":
					valueType = EdmProvider.INT_64_TYPE_FQN;
					break;
				case "double":
					valueType = EdmProvider.DOUBLE_TYPE_FQN;
					break;
				case "boolean":
					valueType = EdmProvider.BOOLEAN_TYPE_FQN;
					break;
				case "date":
					valueType = EdmProvider.DATE_TIME_OFFSET_TYPE_FQN;
					break;
				default:
					throw new RuntimeException(String.format(
							"Unsupported type extension specified for PRIP metadata mapping in %s", entrySet.getKey()));
				}
				final Object value = valueType == EdmProvider.DATE_TIME_OFFSET_TYPE_FQN
						? convertLocalDateTimeToTimestamp((LocalDateTime) entrySet.getValue())
						: entrySet.getValue();
				final String odataPropertyName = entrySet.getKey().substring(firstSeparatorPosition + 1,
						lastSeparatorPosition);
				final Entity attributeEntity = new Entity();
				attributeEntity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, odataPropertyName));
				attributeEntity.addProperty(new Property(null, "ValueType", ValueType.PRIMITIVE, valueType));
				attributeEntity.addProperty(new Property(null, "Value", ValueType.PRIMITIVE, value));
				final EntityCollection targetEntityCollection = entityCollections.get(valueType);
				targetEntityCollection.getEntities().add(attributeEntity);
			}
		}

		Link stringLink = new Link();
		stringLink.setTitle(EdmProvider.STRING_ATTRIBUTES_SET_NAME);
		stringLink.setInlineEntitySet(entityCollections.get(EdmProvider.STRING_TYPE_FQN));
		entity.getNavigationLinks().add(stringLink);

		Link longLink = new Link();
		longLink.setTitle(EdmProvider.INTEGER_ATTRIBUTES_SET_NAME);
		longLink.setInlineEntitySet(entityCollections.get(EdmProvider.INT_64_TYPE_FQN));
		entity.getNavigationLinks().add(longLink);

		Link doubleLink = new Link();
		doubleLink.setTitle(EdmProvider.DOUBLE_ATTRIBUTES_SET_NAME);
		doubleLink.setInlineEntitySet(entityCollections.get(EdmProvider.DOUBLE_TYPE_FQN));
		entity.getNavigationLinks().add(doubleLink);

		Link booleanLink = new Link();
		booleanLink.setTitle(EdmProvider.BOOLEAN_ATTRIBUTES_SET_NAME);
		booleanLink.setInlineEntitySet(entityCollections.get(EdmProvider.BOOLEAN_TYPE_FQN));
		entity.getNavigationLinks().add(booleanLink);

		Link dateLink = new Link();
		dateLink.setTitle(EdmProvider.DATE_ATTRIBUTES_SET_NAME);
		dateLink.setInlineEntitySet(entityCollections.get(EdmProvider.DATE_TIME_OFFSET_TYPE_FQN));
		entity.getNavigationLinks().add(dateLink);
      
      EntityCollection quicklookEntityCollection = quicklookEntityCollectionOf(pripMetadata);
      Link quicklookLink = new Link();
      quicklookLink.setTitle(EdmProvider.QUICKLOOK_SET_NAME);
      quicklookLink.setInlineEntitySet(quicklookEntityCollection);
      entity.getNavigationLinks().add(quicklookLink);
      
		return entity;
	}
	
	public static EntityCollection quicklookEntityCollectionOf(PripMetadata pripMetadata) {
	   final EntityCollection quicklookEntityCollection = new EntityCollection();
	   if (null != pripMetadata.getBrowseKeys()) {
         for (final String imageFilename : pripMetadata.getBrowseKeys()) {
            quicklookEntityCollection.getEntities().add(quicklookEntityOf(imageFilename));         
      	}
	   }
	   return quicklookEntityCollection; 
	}
	
	public static Entity quicklookEntityOf(String quicklookImageFilename) {
	   final Entity quicklookEntity = new Entity();
      quicklookEntity.addProperty(new Property(null, QuicklookProperties.Image.name(), ValueType.PRIMITIVE, quicklookImageFilename));
      return quicklookEntity; 
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
		complexValue.getValue().add(new Property(null, Start.name(), ValueType.PRIMITIVE,
				convertLocalDateTimeToTimestamp(contentDateStart)));
		complexValue.getValue().add(
				new Property(null, End.name(), ValueType.PRIMITIVE, convertLocalDateTimeToTimestamp(contentDateEnd)));
		return complexValue;
	}

	public static List<ComplexValue> mapToChecksumList(List<Checksum> checksums) {
		List<ComplexValue> listOfComplexValues = new ArrayList<>();
		if (null != checksums) {
			for (Checksum checksum : checksums) {
				ComplexValue complexValue = new ComplexValue();
				complexValue.getValue()
						.add(new Property(null, Algorithm.name(), ValueType.PRIMITIVE, checksum.getAlgorithm()));
				complexValue.getValue().add(new Property(null, Value.name(), ValueType.PRIMITIVE, checksum.getValue()));
				complexValue.getValue().add(new Property(null, ChecksumDate.name(), ValueType.PRIMITIVE,
						convertLocalDateTimeToTimestamp(checksum.getDate())));
				listOfComplexValues.add(complexValue);
			}
		}
		return listOfComplexValues;
	}

	public static Geospatial mapToGeospatial(PripGeoShape footprint) {
		Geospatial result = null;
		if (null != footprint) {
			SRID srid = SRID.valueOf(String.valueOf(footprint.getSRID()));
			List<Point> points = new ArrayList<>();
			for (PripGeoCoordinate coordinates : footprint.getCoordinates()) {
				Point p = new Point(Dimension.GEOGRAPHY, srid);
				p.setX(coordinates.getLongitude());
				p.setY(coordinates.getLatitude());
				points.add(p);
			}
			LineString lineString = new LineString(Dimension.GEOGRAPHY, srid, points);

			if (footprint instanceof GeoShapePolygon) {
				result = new Polygon(Dimension.GEOGRAPHY, srid, null, lineString);
			} else if (footprint instanceof GeoShapeLineString) {
				result = lineString;
			}
		}
		return result;
	}

	public static Integer mapToProductionType(esa.s1pdgs.cpoc.prip.model.ProductionType productionType) {
		return productionType.getValue();
	}

}
