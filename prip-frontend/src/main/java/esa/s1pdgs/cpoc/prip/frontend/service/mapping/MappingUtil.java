package esa.s1pdgs.cpoc.prip.frontend.service.mapping;

import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Algorithm;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Checksums;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.ContentLength;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.ContentType;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.EvictionDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Id;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Name;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.ProductionType;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.PublicationDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Value;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;

import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;

public class MappingUtil {
	public static Entity pripMetadataToEntity(PripMetadata pripMetadata, String rawBaseUri) {
		URI uri = MappingUtil.createId(rawBaseUri, EdmProvider.ES_PRODUCTS_NAME, pripMetadata.getId());
		Entity entity = new Entity()
				.addProperty(new Property(null, Id.name(), ValueType.PRIMITIVE, pripMetadata.getId().toString()))
				.addProperty(new Property(null, Name.name(), ValueType.PRIMITIVE, pripMetadata.getName()))
				.addProperty(new Property(null, ContentType.name(), ValueType.PRIMITIVE, pripMetadata.getContentType()))
				.addProperty(new Property(null, ContentLength.name(), ValueType.PRIMITIVE, pripMetadata.getContentLength()))
				.addProperty(new Property(null, PublicationDate.name(), ValueType.PRIMITIVE, convertLocalDateTimeToTimestamp(pripMetadata.getCreationDate())))
				.addProperty(new Property(null, EvictionDate.name(), ValueType.PRIMITIVE, convertLocalDateTimeToTimestamp(pripMetadata.getEvictionDate())))
				.addProperty(new Property(null, ProductionType.name(), ValueType.ENUM, mapToProductionType(esa.s1pdgs.cpoc.prip.model.ProductionType.SYSTEMATIC_PRODUCTION)))
				.addProperty(new Property(null, Checksums.name(), ValueType.COLLECTION_COMPLEX, mapToChecksumList(pripMetadata.getChecksums())));
		entity.setMediaContentType(pripMetadata.getContentType());
		entity.setId(uri);
		return entity;
	}

	public static URI createId(String rawBaseUri, String entitySetName, UUID id) {
		try {
			return new URI(rawBaseUri + "/" + entitySetName + "('" + id.toString() + "')");
		} catch (URISyntaxException e) {
			throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
		}
	}
	
	public static Timestamp convertLocalDateTimeToTimestamp(LocalDateTime localDateTime) {
		return null == localDateTime ? null : Timestamp.from(localDateTime.toInstant(ZoneOffset.UTC));
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
	
	public static Integer mapToProductionType(esa.s1pdgs.cpoc.prip.model.ProductionType productionType) {
		return productionType.getValue();
	}

}
