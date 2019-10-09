package esa.s1pdgs.cpoc.prip.service.mapping;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.server.api.ODataRequest;

public class MappingUtil {

	private MappingUtil() {
	}

	public static EntityCollection wrap(Entity entity) {
		EntityCollection collection = new EntityCollection();
		collection.getEntities().add(entity);
		return collection;
	}

	public static Timestamp map(Instant instantTime) {
		if (instantTime == null)
			return null;
		return Timestamp.from(instantTime);
	}

	public static String removeBoundingSingleQuote(String possibleQuotedValue) {
		if (possibleQuotedValue.startsWith("'") && possibleQuotedValue.endsWith("'")) {
			return possibleQuotedValue.substring(1, possibleQuotedValue.length() - 1);
		}
		return possibleQuotedValue;
	}

	public static URI createId(ODataRequest request, String entitySetName, UUID id) {
		try {
			return new URI(request.getRawBaseUri() + "/" + entitySetName + "(" + id.toString() + ")");
		} catch (URISyntaxException e) {
			throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
		}
	}

	public static Object mapToChecksumList(String algorithm, String checksum) {
		ComplexValue complexValue = new ComplexValue();
		complexValue.getValue().add(new Property(null, "Algorithm", ValueType.PRIMITIVE, algorithm));
		complexValue.getValue().add(new Property(null, "Value", ValueType.PRIMITIVE, checksum));
		return Arrays.asList(complexValue);
	}

}
