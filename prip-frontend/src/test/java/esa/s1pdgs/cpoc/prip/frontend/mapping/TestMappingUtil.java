package esa.s1pdgs.cpoc.prip.frontend.mapping;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.junit.Assert;
import org.junit.Test;

import esa.s1pdgs.cpoc.prip.frontend.service.mapping.MappingUtil;
import esa.s1pdgs.cpoc.prip.model.Checksum;
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
		URI expectedResult = new URI("http://example.org/Entity('00000000-0000-0000-0000-000000000001')");
		URI actualResult = MappingUtil.createId("http://example.org", "Entity", UUID.fromString("00000000-0000-0000-0000-000000000001"));
		Assert.assertEquals(expectedResult, actualResult);
	}

	@Test
	public void TestMapToChecksum() {
		ComplexValue cv1 = new ComplexValue();
		cv1.getValue().add(new Property(null, "Algorithm", ValueType.PRIMITIVE, "MD5"));
		cv1.getValue().add(new Property(null, "Value", ValueType.PRIMITIVE, "d41d8cd98f00b204e9800998ecf8427e"));
		ComplexValue cv2 = new ComplexValue();
		cv2.getValue().add(new Property(null, "Algorithm", ValueType.PRIMITIVE, "SHA256"));
		cv2.getValue().add(new Property(null, "Value", ValueType.PRIMITIVE, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
		List<ComplexValue> expectedResult = Arrays.asList(cv1, cv2);

		Checksum c1 = new Checksum();
		c1.setAlgorithm("MD5");
		c1.setValue("d41d8cd98f00b204e9800998ecf8427e");
		Checksum c2 = new Checksum();
		c2.setAlgorithm("SHA256");
		c2.setValue("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
		List<Checksum> checksums = Arrays.asList(c1, c2);
		List<ComplexValue> actualResult = MappingUtil.mapToChecksumList(checksums);
		
		Assert.assertEquals(expectedResult, actualResult);
	}

	@Test
	public void TestPripMetadataToEntity() {		
		URI uri = MappingUtil.createId("http://example.org", "Products", UUID.fromString("00000000-0000-0000-0000-000000000001"));
		ComplexValue contentDate = new ComplexValue();
		contentDate.getValue().add(new Property(null, "Start", ValueType.PRIMITIVE, new Timestamp(111111111111L)));
		contentDate.getValue().add(new Property(null, "End", ValueType.PRIMITIVE,new Timestamp(222222222222L)));
		ComplexValue cv1 = new ComplexValue();
		cv1.getValue().add(new Property(null, "Algorithm", ValueType.PRIMITIVE, "MD5"));
		cv1.getValue().add(new Property(null, "Value", ValueType.PRIMITIVE, "d41d8cd98f00b204e9800998ecf8427e"));
		ComplexValue cv2 = new ComplexValue();
		cv2.getValue().add(new Property(null, "Algorithm", ValueType.PRIMITIVE, "SHA256"));
		cv2.getValue().add(new Property(null, "Value", ValueType.PRIMITIVE, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
		Entity expectedEntity = new Entity()
				.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, "00000000-0000-0000-0000-000000000001"))
				.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Name"))
				.addProperty(new Property(null, "ContentType", ValueType.PRIMITIVE, "application/octet-stream"))
				.addProperty(new Property(null, "ContentLength", ValueType.PRIMITIVE, 123L))
				.addProperty(new Property(null, "ContentDate", ValueType.COMPLEX, contentDate))
				.addProperty(new Property(null, "PublicationDate", ValueType.PRIMITIVE, new Timestamp(100000000000L)))
				.addProperty(new Property(null, "EvictionDate", ValueType.PRIMITIVE, new Timestamp(200000000000L)))
				.addProperty(new Property(null, "ProductionType", ValueType.ENUM, 0))
				.addProperty(new Property(null, "Checksums", ValueType.COLLECTION_COMPLEX, Arrays.asList(cv1, cv2)));
		expectedEntity.setMediaContentType("application/octet-stream");
		expectedEntity.setId(uri);
		
		PripMetadata inputPripMetadata = new PripMetadata();
		inputPripMetadata.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
		inputPripMetadata.setName("Name");
		inputPripMetadata.setContentType("application/octet-stream");
		inputPripMetadata.setContentLength(123L);
		inputPripMetadata.setContentDateStart(LocalDateTime.ofInstant(Instant.ofEpochMilli(111111111111L), TimeZone.getTimeZone("UTC").toZoneId()));
		inputPripMetadata.setContentDateEnd(LocalDateTime.ofInstant(Instant.ofEpochMilli(222222222222L), TimeZone.getTimeZone("UTC").toZoneId()));
		inputPripMetadata.setCreationDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(100000000000L), TimeZone.getTimeZone("UTC").toZoneId()));
		inputPripMetadata.setEvictionDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(200000000000L), TimeZone.getTimeZone("UTC").toZoneId()));
		Checksum c1 = new Checksum();
		c1.setAlgorithm("MD5");
		c1.setValue("d41d8cd98f00b204e9800998ecf8427e");
		Checksum c2 = new Checksum();
		c2.setAlgorithm("SHA256");
		c2.setValue("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
		inputPripMetadata.setChecksums(Arrays.asList(c1, c2));
		Entity actualEntity = MappingUtil.pripMetadataToEntity(inputPripMetadata, "http://example.org");
		
		Assert.assertEquals(expectedEntity, actualEntity);
	}

}
