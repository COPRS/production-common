package esa.s1pdgs.cpoc.prip.worker.service.edm;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.prip.worker.service.edm.EdmProvider;

public class EdmProviderTest {

	EdmProvider uut;
	
	@Before
	public void setUp() {
		uut = new EdmProvider();
	}
	
	@Test
	public void testGetEntityType() throws ODataException {
		CsdlEntityType entityType = uut.getEntityType(new FullQualifiedName("S1PDGS", "Product"));
		List<CsdlProperty> properties = entityType.getProperties();
		
		assertEquals(Arrays.asList("Id", "Name", "ContentType", "ContentLength", "CreationDate", "EvictionDate", "Checksums"),
				properties.stream().map(p -> p.getName()).collect(Collectors.toList()));
		
		assertEquals(Arrays.asList( //
				EdmPrimitiveTypeKind.String.getFullQualifiedName(), //
				EdmPrimitiveTypeKind.String.getFullQualifiedName(), //
				EdmPrimitiveTypeKind.String.getFullQualifiedName(), //
				EdmPrimitiveTypeKind.Int64.getFullQualifiedName(), //
				EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName(), //
				EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName(), //
				new FullQualifiedName("S1PDGS", "Checksums")), //
				properties.stream().map(p -> p.getTypeAsFQNObject()).collect(Collectors.toList()));
		
		assertEquals(Arrays.asList(false, false, false, false, false, false, true),
				properties.stream().map(p -> p.isCollection()).collect(Collectors.toList()));
	}
	
	@Test
	public void testGetComplexType() throws ODataException {
		CsdlComplexType complexType = uut.getComplexType(new FullQualifiedName("S1PDGS", "Checksums"));
		List<CsdlProperty> properties = complexType.getProperties();
		assertEquals("Algorithm", properties.get(0).getName());
		assertEquals(EdmPrimitiveTypeKind.String.getFullQualifiedName(), properties.get(0).getTypeAsFQNObject());
		assertEquals("Value", properties.get(1).getName());
		assertEquals(EdmPrimitiveTypeKind.String.getFullQualifiedName(), properties.get(1).getTypeAsFQNObject());
	}

}
