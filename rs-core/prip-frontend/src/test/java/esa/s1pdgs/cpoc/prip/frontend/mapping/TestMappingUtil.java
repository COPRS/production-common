package esa.s1pdgs.cpoc.prip.frontend.mapping;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.geo.Geospatial;
import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;
import org.apache.olingo.commons.api.edm.geo.LineString;
import org.apache.olingo.commons.api.edm.geo.Point;
import org.apache.olingo.commons.api.edm.geo.Polygon;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.junit.Assert;
import org.junit.Test;

import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.frontend.service.edm.QuicklookProperties;
import esa.s1pdgs.cpoc.prip.frontend.service.mapping.MappingUtil;
import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.GeoShapePolygon;
import esa.s1pdgs.cpoc.prip.model.PripGeoCoordinate;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;

public class TestMappingUtil {

	@Test
	public void TestConvertLocalDateTimeToTimeStamp_OnNominal_ShallReturnNominal() {
		Instant instant = Instant.ofEpochMilli(946684800000L);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, TimeZone.getTimeZone("UTC").toZoneId());
		Assert.assertEquals(new Timestamp(946684800000L), MappingUtil.convertLocalDateTimeToTimestamp(localDateTime));
	}

	@Test
	public void TestConvertLocalDateTimeToTimeStamp_OnNull_ShallReturnNull() {
		Assert.assertEquals(null, MappingUtil.convertLocalDateTimeToTimestamp(null));
	}

	@Test
	public void TestCreateId() throws URISyntaxException {
		URI expectedResult = new URI("http://example.org/Entity(00000000-0000-0000-0000-000000000001)");
		URI actualResult = MappingUtil.createId("http://example.org", "Entity", UUID.fromString("00000000-0000-0000-0000-000000000001"));
		Assert.assertEquals(expectedResult, actualResult);
	}

	@Test
	public void TestMapToChecksum() {
		final LocalDateTime checksumDate = LocalDateTime.of(2021, Month.JANUARY, 01, 14, 44, 59);
		
		ComplexValue cv1 = new ComplexValue();
		cv1.getValue().add(new Property(null, "Algorithm", ValueType.PRIMITIVE, "MD5"));
		cv1.getValue().add(new Property(null, "Value", ValueType.PRIMITIVE, "d41d8cd98f00b204e9800998ecf8427e"));
		cv1.getValue().add(new Property(null, "ChecksumDate", ValueType.PRIMITIVE, MappingUtil.convertLocalDateTimeToTimestamp(checksumDate)));
		ComplexValue cv2 = new ComplexValue();
		cv2.getValue().add(new Property(null, "Algorithm", ValueType.PRIMITIVE, "SHA256"));
		cv2.getValue().add(new Property(null, "Value", ValueType.PRIMITIVE, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
		cv2.getValue().add(new Property(null, "ChecksumDate", ValueType.PRIMITIVE, MappingUtil.convertLocalDateTimeToTimestamp(checksumDate)));
		List<ComplexValue> expectedResult = Arrays.asList(cv1, cv2);

		Checksum c1 = new Checksum();
		c1.setAlgorithm("MD5");
		c1.setValue("d41d8cd98f00b204e9800998ecf8427e");
		c1.setDate(checksumDate);
		Checksum c2 = new Checksum();
		c2.setAlgorithm("SHA256");
		c2.setValue("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
		c2.setDate(checksumDate);
		List<Checksum> checksums = Arrays.asList(c1, c2);
		List<ComplexValue> actualResult = MappingUtil.mapToChecksumList(checksums);
		
		Assert.assertEquals(expectedResult, actualResult);
	}

	@Test
	public void TestPripMetadataToEntity() {		
		final LocalDateTime checksumDate = LocalDateTime.of(2021, Month.JANUARY, 01, 20, 15, 01);
		URI uri = MappingUtil.createId("http://example.org", "Products", UUID.fromString("00000000-0000-0000-0000-000000000001"));
		ComplexValue contentDate = new ComplexValue();
		contentDate.getValue().add(new Property(null, "Start", ValueType.PRIMITIVE, new Timestamp(111111111111L)));
		contentDate.getValue().add(new Property(null, "End", ValueType.PRIMITIVE,new Timestamp(222222222222L)));
		SRID srid = SRID.valueOf("4326");
		Point p1 = new Point(Dimension.GEOGRAPHY, srid);
		p1.setX(0.0);
		p1.setY(1.0);
		Point p2 = new Point(Dimension.GEOGRAPHY, srid);
		p2.setX(2.0);
		p2.setY(3.0);
		Point p3 = new Point(Dimension.GEOGRAPHY, srid);
		p3.setX(4.0);
		p3.setY(5.0);
		Point p4 = new Point(Dimension.GEOGRAPHY, srid);
		p4.setX(0.0);
		p4.setY(1.0);
		LineString lineString = new LineString(Dimension.GEOGRAPHY, srid, Arrays.asList(p1, p2, p3, p4));
		Geospatial footprint = new Polygon(Dimension.GEOGRAPHY, srid, null, lineString);
		ComplexValue cv1 = new ComplexValue();
		cv1.getValue().add(new Property(null, "Algorithm", ValueType.PRIMITIVE, "MD5"));
		cv1.getValue().add(new Property(null, "Value", ValueType.PRIMITIVE, "d41d8cd98f00b204e9800998ecf8427e"));
		cv1.getValue().add(new Property(null, "ChecksumDate", ValueType.PRIMITIVE, MappingUtil.convertLocalDateTimeToTimestamp(checksumDate)));
		ComplexValue cv2 = new ComplexValue();
		cv2.getValue().add(new Property(null, "Algorithm", ValueType.PRIMITIVE, "SHA256"));
		cv2.getValue().add(new Property(null, "Value", ValueType.PRIMITIVE, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
		cv2.getValue().add(new Property(null, "ChecksumDate", ValueType.PRIMITIVE, MappingUtil.convertLocalDateTimeToTimestamp(checksumDate)));
		Entity expectedEntity = new Entity()
				.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, UUID.fromString("00000000-0000-0000-0000-000000000001")))
				.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Name"))
				.addProperty(new Property(null, "Online", ValueType.PRIMITIVE, true))
				.addProperty(new Property(null, "ContentType", ValueType.PRIMITIVE, "application/octet-stream"))
				.addProperty(new Property(null, "ContentLength", ValueType.PRIMITIVE, 123L))
				.addProperty(new Property(null, "ContentDate", ValueType.COMPLEX, contentDate))
				.addProperty(new Property(null, "PublicationDate", ValueType.PRIMITIVE, new Timestamp(100000000000L)))
				.addProperty(new Property(null, "EvictionDate", ValueType.PRIMITIVE, new Timestamp(200000000000L)))
				.addProperty(new Property(null, "OriginDate", ValueType.PRIMITIVE, new Timestamp(200000000000L)))
				.addProperty(new Property(null, "ProductionType", ValueType.ENUM, 0))
				.addProperty(new Property(null, "Checksum", ValueType.COLLECTION_COMPLEX, Arrays.asList(cv1, cv2)))
				.addProperty(new Property(null, "GeoFootprint", ValueType.GEOSPATIAL, footprint));
		expectedEntity.setMediaContentType("application/octet-stream");
		expectedEntity.setId(uri);
		
		Link attributesLink = new Link();
		attributesLink.setTitle(EdmProvider.ATTRIBUTES_SET_NAME);
		attributesLink.setInlineEntitySet(new EntityCollection());
		expectedEntity.getNavigationLinks().add(attributesLink);

		PripMetadata inputPripMetadata = new PripMetadata();
		inputPripMetadata.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
		inputPripMetadata.setName("Name");
		inputPripMetadata.setContentType("application/octet-stream");
		inputPripMetadata.setContentLength(123L);
		inputPripMetadata.setContentDateStart(LocalDateTime.ofInstant(Instant.ofEpochMilli(111111111111L), TimeZone.getTimeZone("UTC").toZoneId()));
		inputPripMetadata.setContentDateEnd(LocalDateTime.ofInstant(Instant.ofEpochMilli(222222222222L), TimeZone.getTimeZone("UTC").toZoneId()));
		inputPripMetadata.setCreationDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(100000000000L), TimeZone.getTimeZone("UTC").toZoneId()));
		inputPripMetadata.setEvictionDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(200000000000L), TimeZone.getTimeZone("UTC").toZoneId()));
		inputPripMetadata.setOriginDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(200000000000L), TimeZone.getTimeZone("UTC").toZoneId()));
		inputPripMetadata.setAttributes(new LinkedHashMap<String,Object>());
		inputPripMetadata.setOnline(true);
		
		GeoShapePolygon inputPolygon = new GeoShapePolygon(Arrays.asList(
				new PripGeoCoordinate(0.0, 1.0), new PripGeoCoordinate(2.0, 3.0),
				new PripGeoCoordinate(4.0, 5.0), new PripGeoCoordinate(0.0, 1.0)));
		inputPripMetadata.setFootprint(inputPolygon);
		
		Checksum checksum1 = new Checksum();
		checksum1.setAlgorithm("MD5");
		checksum1.setValue("d41d8cd98f00b204e9800998ecf8427e");
		checksum1.setDate(checksumDate);
		Checksum checksum2 = new Checksum();
		checksum2.setAlgorithm("SHA256");
		checksum2.setValue("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
		checksum2.setDate(checksumDate);
		inputPripMetadata.setChecksums(Arrays.asList(checksum1, checksum2));
		
      Link quicklookLink = new Link();
      Entity quicklookEntity1 = new Entity();
      Entity quicklookEntity2 = new Entity();
      quicklookEntity1.addProperty(new Property(null, QuicklookProperties.Image.name(), ValueType.PRIMITIVE, "foo.png"));
      quicklookEntity2.addProperty(new Property(null, QuicklookProperties.Image.name(), ValueType.PRIMITIVE, "bar.png"));
      quicklookLink.setTitle(EdmProvider.QUICKLOOK_SET_NAME);
      EntityCollection quicklookEntityCollection = new EntityCollection();
      quicklookEntityCollection.getEntities().add(quicklookEntity1);
      quicklookEntityCollection.getEntities().add(quicklookEntity2);
      quicklookLink.setInlineEntitySet(quicklookEntityCollection);
      expectedEntity.getNavigationLinks().add(quicklookLink);
      inputPripMetadata.setBrowseKeys(List.of("foo.png", "bar.png"));

      Entity actualEntity = MappingUtil.pripMetadataToEntity(inputPripMetadata, "http://example.org");
		Assert.assertEquals(expectedEntity, actualEntity);
	}
	
	@Test
	public void TestConvertLocalDateTimeToTimestampShallReturnPrecisionXXX000Z() {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
		LocalDateTime source = LocalDateTime.parse("2000-01-01T00:00:00.123456Z", dateTimeFormatter);
		Timestamp destination = MappingUtil.convertLocalDateTimeToTimestamp(source);
		assertTrue(dateTimeFormatter.format(source).endsWith("123456Z"));
		assertTrue(dateTimeFormatter.format(destination.toLocalDateTime()).endsWith("123000Z"));
	}

}
