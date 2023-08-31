package de.werum.coprs.cadip.client.odata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetIteratorRequest;
import org.apache.olingo.client.api.communication.request.retrieve.RetrieveRequestFactory;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.api.serialization.ClientODataDeserializer;
import org.apache.olingo.client.api.serialization.ODataBinder;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.serialization.ClientODataDeserializerImpl;
import org.apache.olingo.client.core.serialization.ODataReaderImpl;
import org.apache.olingo.client.core.uri.FilterFactoryImpl;
import org.apache.olingo.client.core.uri.URIBuilderImpl;
import org.apache.olingo.commons.api.format.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.werum.coprs.cadip.client.config.CadipClientConfigurationProperties.CadipHostConfiguration;
import de.werum.coprs.cadip.client.model.CadipSession;

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
	
	@Test
	public final void getSessions() {
		ClientODataDeserializer deserializer = ODataClientFactory.getClient().getDeserializer(ContentType.JSON);
		ODataBinder binder = ODataClientFactory.getClient().getBinder();
		
		RetrieveRequestFactory retrieveRequestFactory = mock(RetrieveRequestFactory.class);
		
		ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> odataRequest = mock(ODataEntitySetIteratorRequest.class);
		
		ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> odataResponse = mock(ODataRetrieveResponse.class);

		doReturn(retrieveRequestFactory).when(odataClient).getRetrieveRequestFactory();
		doReturn(new FilterFactoryImpl()).when(odataClient).getFilterFactory();
		doReturn(deserializer).when(odataClient).getDeserializer(any());
		doReturn(binder).when(odataClient).getBinder();
		doReturn(new URIBuilderImpl(null, null)).when(odataClient).newURIBuilder(any());
		doReturn(new ODataReaderImpl(odataClient)).when(odataClient).getReader();
		doReturn(new ClientODataDeserializerImpl(false, ContentType.APPLICATION_JSON)).when(odataClient).getDeserializer(ContentType.APPLICATION_JSON);
		doReturn(odataRequest).when(retrieveRequestFactory).getEntitySetIteratorRequest(any());
		doReturn(odataResponse).when(odataRequest).execute();

		InputStream jsonStream = new ByteArrayInputStream("{\"@odata.context\": \"$metadata#Sessions\",\"value\": [{\"Id\": \"00000000-0000-0000-0000-000000000001\",\"SessionId\": \"1\",\"NumChannels\": 10,\"PublicationDate\": \"2014-01-01T00:00:00.123Z\",\"Satellite\": \"S1A\",\"StationUnitId\": \"123\",\"DownlinkOrbit\": 123,\"AcquisitionId\": \"123\",\"AntennaId\": \"123\",\"FrontEndId\": \"123\",\"Retransfer\": false,\"AntennaStatusOK\": true,\"FrontEndStatusOK\": true,\"PlannedDataStart\": \"2014-01-01T01:20:00.123Z\",\"PlannedDataStop\": \"2014-01-01T01:30:00.001123Z\",\"DownlinkStart\": \"2014-01-01T02:10:00.001Z\",\"DownlinkStop\": \"2014-01-01T02:21:00.002Z\",\"DownlinkStatusOK\": true,\"DeliveryPushOK\": true},{\"Id\": \"00000000-0000-0000-0000-000000000002\",\"SessionId\": \"2\",\"NumChannels\": 20,\"PublicationDate\": \"2014-01-02T00:00:00.123Z\",\"Satellite\": \"S2A\",\"StationUnitId\": \"234\",\"DownlinkOrbit\": 234,\"AcquisitionId\": \"234\",\"AntennaId\": \"234\",\"FrontEndId\": \"234\",\"Retransfer\": false,\"AntennaStatusOK\": true,\"FrontEndStatusOK\": true,\"PlannedDataStart\": \"2014-01-02T01:20:00.001Z\",\"PlannedDataStop\": \"2014-01-02T01:30:00.002Z\",\"DownlinkStart\": \"2014-01-02T02:10:00.003Z\",\"DownlinkStop\": \"2014-01-02T02:21:00.004Z\",\"DownlinkStatusOK\": true,\"DeliveryPushOK\": true},{\"Id\": \"00000000-0000-0000-0000-000000000003\",\"SessionId\": \"3\",\"NumChannels\": 30,\"PublicationDate\": \"2014-01-03T00:00:00.123Z\",\"Satellite\": \"S3A\",\"StationUnitId\": \"345\",\"DownlinkOrbit\": 345,\"AcquisitionId\": \"345\",\"AntennaId\": \"345\",\"FrontEndId\": \"345\",\"Retransfer\": false,\"AntennaStatusOK\": true,\"FrontEndStatusOK\": true,\"PlannedDataStart\": \"2014-01-03T01:20:00.001Z\",\"PlannedDataStop\": \"2014-01-03T01:30:00.002Z\",\"DownlinkStart\": \"2014-01-03T02:10:00.003Z\",\"DownlinkStop\": \"2014-01-03T02:21:00.004Z\",\"DownlinkStatusOK\": true,\"DeliveryPushOK\": true}]}".getBytes());

		ClientEntitySetIterator<ClientEntitySet, ClientEntity> clientEntitySetIterator = new ClientEntitySetIterator<>(
				odataClient, jsonStream, ContentType.APPLICATION_JSON);
		doReturn(clientEntitySetIterator).when(odataResponse).getBody();

		final String satelliteId = "S1A";
		final LocalDateTime publicationDate = LocalDateTime.parse("2020-01-01T01:00:00.000");

		
		List<CadipSession> result = uut.getSessions(satelliteId, null, publicationDate);
		assertNotNull(result);
		assertEquals(3, result.size());
	}	
}
