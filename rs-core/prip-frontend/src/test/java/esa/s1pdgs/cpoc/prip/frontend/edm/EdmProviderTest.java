/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.prip.frontend.edm;

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

import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;

public class EdmProviderTest {

	EdmProvider uut;
	
	@Before
	public void setUp() {
		uut = new EdmProvider();
	}
	
	@Test
	public void testGetEntityType() throws ODataException {
		CsdlEntityType entityType = uut.getEntityType(new FullQualifiedName("OData.CSC", "Product"));
		List<CsdlProperty> properties = entityType.getProperties();
		
		assertEquals(Arrays.asList("Id", "Name", "Online", "ContentType", "ContentLength", "PublicationDate", "EvictionDate", "OriginDate", "Checksum", "ProductionType", "ContentDate", "Footprint", "GeoFootprint"),
				properties.stream().map(p -> p.getName()).collect(Collectors.toList()));
		
		assertEquals(Arrays.asList( //
				EdmPrimitiveTypeKind.Guid.getFullQualifiedName(), //				
				EdmPrimitiveTypeKind.String.getFullQualifiedName(), //
				EdmPrimitiveTypeKind.Boolean.getFullQualifiedName(), //
				EdmPrimitiveTypeKind.String.getFullQualifiedName(), //
				EdmPrimitiveTypeKind.Int64.getFullQualifiedName(), //
				EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName(), //
				EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName(), //
				EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName(), //
				new FullQualifiedName("OData.CSC", "Checksum"), //
				new FullQualifiedName("OData.CSC", "ProductionType"), //
				new FullQualifiedName("OData.CSC", "TimeRange"),
				EdmPrimitiveTypeKind.Geography.getFullQualifiedName(), //
				EdmPrimitiveTypeKind.Geography.getFullQualifiedName()), //
				properties.stream().map(p -> p.getTypeAsFQNObject()).collect(Collectors.toList()));
		
		assertEquals(Arrays.asList(false, false, false, false, false, false, false, false, true, false, false, false, false),
				properties.stream().map(p -> p.isCollection()).collect(Collectors.toList()));
	}
	
	@Test
	public void testGetComplexType() throws ODataException {
		CsdlComplexType complexType = uut.getComplexType(new FullQualifiedName("OData.CSC", "Checksum"));
		List<CsdlProperty> properties = complexType.getProperties();
		assertEquals("Algorithm", properties.get(0).getName());
		assertEquals(EdmPrimitiveTypeKind.String.getFullQualifiedName(), properties.get(0).getTypeAsFQNObject());
		assertEquals("Value", properties.get(1).getName());
		assertEquals(EdmPrimitiveTypeKind.String.getFullQualifiedName(), properties.get(1).getTypeAsFQNObject());
	}

}
