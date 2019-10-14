package esa.s1pdgs.cpoc.prip.service.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.prip.service.metadata.PripMetadataRepository;

public class TestProductEntityCollectionProcessor {
	
	ProductEntityCollectionProcessor uut;

	@Mock
	PripMetadataRepository pripMetadataRepositoryMock;

	@Mock
	OData odataMock;
	
	@Mock
	UriInfo uriInfoMock;

	@Mock
	UriResourceEntitySet uriResourceEntitySetMock;

	@Mock
	EdmEntitySet edmEntitySetMock;

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
	public void TestReadEntityCollection() throws ODataApplicationException, ODataLibraryException, IOException {
		String entitySetName = "Products";
		String baseUri = "http://example.org";
		String odataPath = "/" + entitySetName;
		
		doReturn(Collections.emptyList()).when(pripMetadataRepositoryMock).findAll();
		
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
		
		Mockito.verify(pripMetadataRepositoryMock, times(1)).findAll();
		assertEquals(HttpStatusCode.OK.getStatusCode(), odataResponse.getStatusCode());
		assertEquals("expected result", IOUtils.toString(odataResponse.getContent(), StandardCharsets.UTF_8));
	}

}
