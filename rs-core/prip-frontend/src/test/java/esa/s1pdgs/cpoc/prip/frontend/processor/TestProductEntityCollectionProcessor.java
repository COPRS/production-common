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

package esa.s1pdgs.cpoc.prip.frontend.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.EdmPropertyImpl;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Method;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.core.uri.UriInfoImpl;
import org.apache.olingo.server.core.uri.UriResourcePrimitivePropertyImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.LiteralImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.MemberImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.MethodImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.RecoverableDataAccessException;

import esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties;
import esa.s1pdgs.cpoc.prip.frontend.service.processor.ProductEntityCollectionProcessor;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripMetadata.FIELD_NAMES;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter;

public class TestProductEntityCollectionProcessor {
	
	ProductEntityCollectionProcessor uut;

	@Mock
	PripMetadataRepository pripMetadataRepositoryMock;

	@Mock
	OData odataMock;
	
	@Mock
	UriInfo uriInfoMock;
	
	@Mock
	FilterOption filterOption;
	
	@Mock
	UriResourceEntitySet uriResourceEntitySetMock;
	
	@Mock
	UriResourceNavigation uriResourceNavigationMock;
	
	@Mock
	UriParameter UriParameterMock;

	@Mock
	EdmNavigationProperty edmNavigationPropertyMock;

	@Mock
	EdmEntitySet edmEntitySetMock;

   @Mock
   EdmEntitySet secondLevelEdmEntitySetMock;

   @Mock
	ODataRequest odataRequestMock;

	@Mock
	ODataSerializer odataSerializerMock;

	@Mock
	SerializerResult serializerResultMock;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		uut = new ProductEntityCollectionProcessor(pripMetadataRepositoryMock);
		uut.init(odataMock, null);
	}

	@Test
	public void testReadProductEntityCollection_OnResult_ShallReturnStatusOk()
	      throws ODataApplicationException, ODataLibraryException, IOException {
		String entitySetName = "Products";
		String baseUri = "http://example.org";
		String odataPath = "/" + entitySetName;
		
		doReturn(Collections.emptyList()).when(pripMetadataRepositoryMock).findAll(Mockito.any(), Mockito.any());
		
		doReturn(baseUri).when(odataRequestMock).getRawBaseUri();
		doReturn(odataPath).when(odataRequestMock).getRawODataPath();
		doReturn(baseUri + odataPath).when(odataRequestMock).getRawRequestUri();

		doReturn(Arrays.asList(uriResourceEntitySetMock)).when(uriInfoMock).getUriResourceParts();
		
		doReturn(edmEntitySetMock).when(uriResourceEntitySetMock).getEntitySet();
		
		doReturn(entitySetName).when(edmEntitySetMock).getName();
		
		doReturn(odataSerializerMock).when(odataMock).createSerializer(Mockito.any());
		
		doReturn(serializerResultMock).when(odataSerializerMock).entityCollection(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		
		doReturn(new ByteArrayInputStream("expected result".getBytes())).when(serializerResultMock).getContent();
		
		ODataResponse odataResponse = new ODataResponse();
		uut.readEntityCollection(odataRequestMock, odataResponse, uriInfoMock, ContentType.JSON_FULL_METADATA);
		
		Mockito.verify(pripMetadataRepositoryMock, times(1)).findAll(Optional.empty(), Optional.empty(), Collections.emptyList());
		assertEquals(HttpStatusCode.OK.getStatusCode(), odataResponse.getStatusCode());
		assertEquals("expected result", IOUtils.toString(odataResponse.getContent(), StandardCharsets.UTF_8));
	}
	
	@Test
	public void TestReadProductEntityCollectionWithoutFilters_OnRecoverableDataAccessException_ShallReturnStatusServiceUnavailable()
	      throws ODataLibraryException {
		String entitySetName = "Products";
		String baseUri = "http://example.org";
		String odataPath = "/" + entitySetName;
		
		doThrow(RecoverableDataAccessException.class).when(pripMetadataRepositoryMock).findAll(Mockito.any(), Mockito.any(), Mockito.any());

		doReturn(baseUri).when(odataRequestMock).getRawBaseUri();
		doReturn(odataPath).when(odataRequestMock).getRawODataPath();
		doReturn(baseUri + odataPath).when(odataRequestMock).getRawRequestUri();

		doReturn(Arrays.asList(uriResourceEntitySetMock)).when(uriInfoMock).getUriResourceParts();
		
		doReturn(edmEntitySetMock).when(uriResourceEntitySetMock).getEntitySet();
		
		doReturn(entitySetName).when(edmEntitySetMock).getName();

		doReturn(odataSerializerMock).when(odataMock).createSerializer(Mockito.any());
		
		doReturn(serializerResultMock).when(odataSerializerMock).entityCollection(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		
		try {
			uut.readEntityCollection(odataRequestMock, new ODataResponse(), uriInfoMock, ContentType.JSON_FULL_METADATA);
			fail("Required exception wasn't thrown");
		} catch (ODataApplicationException e) {
			assertEquals(HttpStatusCode.SERVICE_UNAVAILABLE.getStatusCode(), e.getStatusCode());
		}
		Mockito.verify(pripMetadataRepositoryMock, times(1)).findAll(Optional.empty(), Optional.empty(), Collections.emptyList());
	}
	
	@Test
	public void TestReadProductEntityCollectionWithFilters_OnRecoverableDataAccessException_ShallReturnStatusServiceUnavailable()
	      throws ODataLibraryException {
		String entitySetName = "Products";
		String baseUri = "http://example.org";
		String odataPath = "/" + entitySetName;
		
		doThrow(RecoverableDataAccessException.class).when(pripMetadataRepositoryMock).findWithFilter(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

		doReturn(baseUri).when(odataRequestMock).getRawBaseUri();
		doReturn(odataPath).when(odataRequestMock).getRawODataPath();
		doReturn(baseUri + odataPath).when(odataRequestMock).getRawRequestUri();

		doReturn(Arrays.asList(uriResourceEntitySetMock)).when(uriInfoMock).getUriResourceParts();
		
		UriInfo uriInfo = new UriInfoImpl().addResourcePart(
				new UriResourcePrimitivePropertyImpl(new EdmPropertyImpl(null, new CsdlProperty().setName(ProductProperties.Name.name()))));

		Method method = new MethodImpl(MethodKind.CONTAINS, Arrays.asList(
				new MemberImpl(uriInfo, null), new LiteralImpl("'foobar'", EdmString.getInstance())));

		doReturn(method).when(filterOption).getExpression();
		doReturn(SystemQueryOptionKind.FILTER).when(filterOption).getKind();
		
		doReturn(Arrays.asList(filterOption)).when(uriInfoMock).getSystemQueryOptions();
		
		doReturn(edmEntitySetMock).when(uriResourceEntitySetMock).getEntitySet();
		
		doReturn(entitySetName).when(edmEntitySetMock).getName();

		doReturn(odataSerializerMock).when(odataMock).createSerializer(Mockito.any());
		
		doReturn(serializerResultMock).when(odataSerializerMock).entityCollection(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		
		try {
			uut.readEntityCollection(odataRequestMock, new ODataResponse(), uriInfoMock, ContentType.JSON_FULL_METADATA);
			fail("Required exception wasn't thrown");
		} catch (ODataApplicationException e) {
			assertEquals(HttpStatusCode.SERVICE_UNAVAILABLE.getStatusCode(), e.getStatusCode());
		}
		
		PripTextFilter filter = new PripTextFilter(FIELD_NAMES.NAME.fieldName(), PripTextFilter.Function.CONTAINS, "foobar");
		Mockito.verify(pripMetadataRepositoryMock, times(1)).findWithFilter(filter, Optional.empty(), Optional.empty(), Collections.emptyList());
	}
	
	@Test
   public void testReadQuicklookEntityCollection_OnResult_ShallReturnStatusOk()
         throws ODataApplicationException, ODataLibraryException, IOException {
      String rootEntitySetName = "Products";
      String secondaryEntitySetName = "Quicklooks";
      String baseUri = "http://example.org";
      String productId = "00000000-0000-0000-0000-000000000001";
      String odataPath = "/" + rootEntitySetName + "(" + productId + ")/" + secondaryEntitySetName;
      
      final PripMetadata pripMetadata = new PripMetadata();
      pripMetadata.setId(UUID.fromString(productId));
      doReturn(pripMetadata).when(pripMetadataRepositoryMock).findById(Mockito.eq(productId));
      
      doReturn(baseUri).when(odataRequestMock).getRawBaseUri();
      doReturn(odataPath).when(odataRequestMock).getRawODataPath();
      doReturn(baseUri + odataPath).when(odataRequestMock).getRawRequestUri();

      doReturn(List.of(uriResourceEntitySetMock, uriResourceNavigationMock))
            .when(uriInfoMock).getUriResourceParts();
      
      doReturn(edmEntitySetMock).when(uriResourceEntitySetMock).getEntitySet();
      doReturn(List.of(UriParameterMock)).when(uriResourceEntitySetMock).getKeyPredicates();
      
      doReturn(productId).when(UriParameterMock).getText();
      
      doReturn(rootEntitySetName).when(edmEntitySetMock).getName();
      doReturn(secondLevelEdmEntitySetMock).when(edmEntitySetMock).getRelatedBindingTarget(Mockito.anyString());

      doReturn(edmNavigationPropertyMock).when(uriResourceNavigationMock).getProperty();

      doReturn(secondaryEntitySetName).when(edmNavigationPropertyMock).getName();

      doReturn(secondaryEntitySetName).when(secondLevelEdmEntitySetMock).getName();

      doReturn(odataSerializerMock).when(odataMock).createSerializer(Mockito.any());
      
      doReturn(serializerResultMock).when(odataSerializerMock).entityCollection(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
      
      doReturn(new ByteArrayInputStream("expected result".getBytes())).when(serializerResultMock).getContent();
      
      ODataResponse odataResponse = new ODataResponse();
      uut.readEntityCollection(odataRequestMock, odataResponse, uriInfoMock, ContentType.JSON_FULL_METADATA);
      
      Mockito.verify(pripMetadataRepositoryMock, times(1)).findById(Mockito.eq(productId));
      assertEquals(HttpStatusCode.OK.getStatusCode(), odataResponse.getStatusCode());
      assertEquals("expected result", IOUtils.toString(odataResponse.getContent(), StandardCharsets.UTF_8));
   }

}
