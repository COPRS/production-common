package de.werum.coprs.cadip.client.odata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.olingo.client.api.ODataClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.werum.coprs.cadip.client.config.CadipClientConfigurationProperties.CadipHostConfiguration;

public class TestCadipOdataClient {
	
	private CadipOdataClient uut;
	
	@Mock
	private ODataClient odataClient;
	
	@Mock
	private CloseableHttpClient downloadClient;
	
	@Mock
	private HttpClientContext context;
	
	private CadipHostConfiguration hostConfig;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		
		hostConfig = new CadipHostConfiguration();
		hostConfig.setServiceRootUri("prip.uri");
		hostConfig.setAuthType("basic");
		
		uut = new CadipOdataClient(odataClient, hostConfig, downloadClient, context);
	}

	@Test
	public final void testToNormalizedUri_OnTrailingSlash_ShallUseOriginalUri() {
		final String uriString = "http://localhost/odata/v1/";
		assertEquals(uriString, CadipOdataClient.toNormalizedUri(uriString).toString());
	}

	@Test
	public final void testToNormalizedUri_OnMissingTrailingSlash_ShallAppendSlash() {
		final String uriString = "http://localhost/odata/v1";
		assertEquals(uriString + "/", CadipOdataClient.toNormalizedUri(uriString).toString());
	}
	
	@Test
	public final void testUriResolution() {
		final String uriString = "http://localhost/odata/v1";
		final URI uri = CadipOdataClient.toNormalizedUri(uriString);
		
		assertEquals("http://localhost/odata/v1/foo", uri.resolve("foo").toString());
		
		// undesired behavior as 'v1' is missing
		assertEquals("http://localhost/odata/foo", URI.create(uriString).resolve("foo").toString());
	}
	
//	@Test
//	public final void getMetadata() {
//		
//		RetrieveRequestFactory retrieveRequestFactory = mock(RetrieveRequestFactory.class);
//		
//		ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> odataRequest = mock(ODataEntitySetIteratorRequest.class);
//		
//		ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> odataResponse = mock(ODataRetrieveResponse.class);
//
//		doReturn(retrieveRequestFactory).when(odataClient).getRetrieveRequestFactory();
//		doReturn(new FilterFactoryImpl()).when(odataClient).getFilterFactory();
//		doReturn(new URIBuilderImpl(null, null)).when(odataClient).newURIBuilder(any());
//		doReturn(new ODataReaderImpl(odataClient)).when(odataClient).getReader();
//		doReturn(new ClientODataDeserializerImpl(false, ContentType.APPLICATION_JSON)).when(odataClient).getDeserializer(ContentType.APPLICATION_JSON);
//		doReturn(odataRequest).when(retrieveRequestFactory).getEntitySetIteratorRequest(any());
//		doReturn(odataResponse).when(odataRequest).execute();
//
//		InputStream jsonStream = new ByteArrayInputStream("{[{\"productName\":\"name\"}]}".getBytes());
//
//		ClientEntitySetIterator<ClientEntitySet, ClientEntity> clientEntitiySetIterator = new ClientEntitySetIterator<>(
//				odataClient, jsonStream, ContentType.APPLICATION_JSON);
//		doReturn(clientEntitiySetIterator).when(odataResponse).getBody();
//
//		final LocalDateTime start = LocalDateTime.parse("2020-01-01T01:00:00.000");
//		final LocalDateTime stop = start.plus(Duration.ofSeconds(235));
//
//		assertNotNull(uut.getMetadata(start, stop, null, null));
//
//	}
//	
//	@Test
//	public final void mapToMetadata() {
//
//		ClientEntitySetIterator<ClientEntitySet, ClientEntity> clientEntitiySetIterator = mock(
//				ClientEntitySetIterator.class);
//		ClientEntity clientEntity = new ClientEntityImpl(new FullQualifiedName("a.b"));
//
//		UUID expectedUUID = UUID.fromString("99744380-d93d-4ba1-b7db-71438ea736a1");
//		String expectedName = "expectedName";
//		String expectedCreationDate = "2007-12-03T10:15:30.00Z";
//		String expectedContentLength = "1000";
//		ClientPrimitiveValue idValue = new ClientPrimitiveValueImpl.BuilderImpl().setValue(expectedUUID.toString())
//				.build();
//		ClientPrimitiveValue nameValue = new ClientPrimitiveValueImpl.BuilderImpl().setValue(expectedName).build();
//		ClientPrimitiveValue creationDateValue = new ClientPrimitiveValueImpl.BuilderImpl()
//				.setValue(expectedCreationDate).build();
//		ClientPrimitiveValue contentLengthValue = new ClientPrimitiveValueImpl.BuilderImpl()
//				.setValue(expectedContentLength).build();
//
//		clientEntity.getProperties().add(new ClientPropertyImpl("id", idValue));
//		clientEntity.getProperties().add(new ClientPropertyImpl("name", nameValue));
//		clientEntity.getProperties().add(new ClientPropertyImpl("creationDate", creationDateValue));
//		clientEntity.getProperties().add(new ClientPropertyImpl("contentLength", contentLengthValue));
//
//		doReturn(true, false).when(clientEntitiySetIterator).hasNext();
//		doReturn(clientEntity).when(clientEntitiySetIterator).next();
//
//		List<CadipProductMetadata> mapToMetadata = uut.mapToMetadata(clientEntitiySetIterator);
//
//		assertEquals(expectedUUID, mapToMetadata.get(0).getId());
//		assertEquals(expectedName, mapToMetadata.get(0).getProductName());
//		assertEquals(LocalDateTime.ofInstant(Instant.parse(expectedCreationDate), ZoneId.of("UTC")), mapToMetadata.get(0).getCreationDate());
//		assertEquals(Long.parseLong(expectedContentLength), mapToMetadata.get(0).getContentLength());
//		
//	}
	
}
