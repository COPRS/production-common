package esa.s1pdgs.cpoc.prip.service.mapping;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.server.api.ODataRequest;

import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;

public class MappingUtil {

	public final static String PRIP_METADATA_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	public final static DateTimeFormatter PRIP_METADATA_DATE_FORMATTER = DateTimeFormatter.ofPattern(PRIP_METADATA_DATE_FORMAT);
	
	private MappingUtil() {
	}
	
	public static Entity pripMetadataToEntity(PripMetadata pripMetadata, ODataRequest request) {
		URI id = MappingUtil.createId(request, "Products", pripMetadata.getId());
		Entity entity = new Entity()
				.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, id))
				.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, pripMetadata.getName()))
				.addProperty(new Property(null, "ContentType", ValueType.PRIMITIVE, pripMetadata.getContentType()))
				.addProperty(new Property(null, "ContentLength", ValueType.PRIMITIVE, pripMetadata.getContentLength()))
				.addProperty(new Property(null, "CreationDate", ValueType.PRIMITIVE, convertLocalDateTimeToTimestamp(pripMetadata.getCreationDate())))
				.addProperty(new Property(null, "EvictionDate", ValueType.PRIMITIVE, convertLocalDateTimeToTimestamp(pripMetadata.getEvictionDate())))
				.addProperty(new Property(null, "Checksums", ValueType.COLLECTION_COMPLEX, mapToChecksumList(pripMetadata.getChecksums())));
		entity.setMediaContentType(pripMetadata.getContentType());
		entity.setId(id);
		
		return entity;
	}

	public static URI createId(ODataRequest request, String entitySetName, UUID id) {
		try {
			return new URI(request.getRawBaseUri() + "/" + entitySetName + "('" + id.toString() + "')");
		} catch (URISyntaxException e) {
			throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
		}
	}
	
	public static Timestamp convertLocalDateTimeToTimestamp(LocalDateTime localDateTime) {
		return null == localDateTime ? null : Timestamp.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
	
	public static List<ComplexValue> mapToChecksumList(List<Checksum> checksums) {
		List<ComplexValue> listOfComplexValues = new ArrayList<>();
		for (Checksum checksum : checksums) {
			ComplexValue complexValue = new ComplexValue();
			complexValue.getValue().add(new Property(null, "Algorithm", ValueType.PRIMITIVE, checksum.getAlgorithm()));
			complexValue.getValue().add(new Property(null, "Value", ValueType.PRIMITIVE, checksum.getValue()));
			listOfComplexValues.add(complexValue);
		}
		return listOfComplexValues;
	}

}
