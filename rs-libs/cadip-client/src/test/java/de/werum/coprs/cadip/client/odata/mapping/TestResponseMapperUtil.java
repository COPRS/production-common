package de.werum.coprs.cadip.client.odata.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.api.serialization.ClientODataDeserializer;
import org.apache.olingo.client.api.serialization.ODataBinder;
import org.apache.olingo.client.api.serialization.ODataReader;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.serialization.ODataReaderImpl;
import org.apache.olingo.commons.api.format.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.werum.coprs.cadip.client.model.CadipFile;
import de.werum.coprs.cadip.client.model.CadipSession;

public class TestResponseMapperUtil {

	@Mock
	private ODataClient odataClient;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		
		ClientODataDeserializer deserializer = ODataClientFactory.getClient().getDeserializer(ContentType.JSON);
		ODataBinder binder = ODataClientFactory.getClient().getBinder();
		ODataReader reader = ODataClientFactory.getClient().getReader();
		
		doReturn(deserializer).when(odataClient).getDeserializer(any());
		doReturn(binder).when(odataClient).getBinder();
		doReturn(reader).when(odataClient).getReader();
	}
	
	@Test
	public void test_mapResponseToListOfSessions() {
		// @formatter:off
		InputStream jsonStream = new ByteArrayInputStream("{\"@odata.context\": \"$metadata#Sessions\",\"value\": [{\"Id\": \"00000000-0000-0000-0000-000000000001\",\"SessionId\": \"1\",\"NumChannels\": 10,\"PublicationDate\": \"2014-01-01T00:00:00.123Z\",\"Satellite\": \"S1A\",\"StationUnitId\": \"123\",\"DownlinkOrbit\": 123,\"AcquisitionId\": \"123\",\"AntennaId\": \"123\",\"FrontEndId\": \"123\",\"Retransfer\": false,\"AntennaStatusOK\": true,\"FrontEndStatusOK\": true,\"PlannedDataStart\": \"2014-01-01T01:20:00.123Z\",\"PlannedDataStop\": \"2014-01-01T01:30:00.001123Z\",\"DownlinkStart\": \"2014-01-01T02:10:00.001Z\",\"DownlinkStop\": \"2014-01-01T02:21:00.002Z\",\"DownlinkStatusOK\": true,\"DeliveryPushOK\": true},{\"Id\": \"00000000-0000-0000-0000-000000000002\",\"SessionId\": \"2\",\"NumChannels\": 20,\"PublicationDate\": \"2014-01-02T00:00:00.123Z\",\"Satellite\": \"S2A\",\"StationUnitId\": \"234\",\"DownlinkOrbit\": 234,\"AcquisitionId\": \"234\",\"AntennaId\": \"234\",\"FrontEndId\": \"234\",\"Retransfer\": false,\"AntennaStatusOK\": true,\"FrontEndStatusOK\": true,\"PlannedDataStart\": \"2014-01-02T01:20:00.001Z\",\"PlannedDataStop\": \"2014-01-02T01:30:00.002Z\",\"DownlinkStart\": \"2014-01-02T02:10:00.003Z\",\"DownlinkStop\": \"2014-01-02T02:21:00.004Z\",\"DownlinkStatusOK\": true,\"DeliveryPushOK\": true},{\"Id\": \"00000000-0000-0000-0000-000000000003\",\"SessionId\": \"3\",\"NumChannels\": 30,\"PublicationDate\": \"2014-01-03T00:00:00.123Z\",\"Satellite\": \"S3A\",\"StationUnitId\": \"345\",\"DownlinkOrbit\": 345,\"AcquisitionId\": \"345\",\"AntennaId\": \"345\",\"FrontEndId\": \"345\",\"Retransfer\": false,\"AntennaStatusOK\": true,\"FrontEndStatusOK\": true,\"PlannedDataStart\": \"2014-01-03T01:20:00.001Z\",\"PlannedDataStop\": \"2014-01-03T01:30:00.002Z\",\"DownlinkStart\": \"2014-01-03T02:10:00.003Z\",\"DownlinkStop\": \"2014-01-03T02:21:00.004Z\",\"DownlinkStatusOK\": true,\"DeliveryPushOK\": true}]}".getBytes());
		// @formatter:on

		ClientEntitySetIterator<ClientEntitySet, ClientEntity> clientEntitySetIterator = new ClientEntitySetIterator<>(
				odataClient, jsonStream, ContentType.APPLICATION_JSON);
		
		List<CadipSession> result = ResponseMapperUtil.mapResponseToListOfSessions(clientEntitySetIterator);

		assertNotNull(result);
		assertEquals(3, result.size());
	}
	
	@Test
	public void test_mapResponseToListOfFiles() {
		// @formatter:off
		InputStream jsonStream = new ByteArrayInputStream("{\"@odata.context\":\"$metadata#Files\",\"value\":[{\"Id\":\"00000000-0000-0000-0000-000000000001\",\"Name\":\"blub\",\"SessionId\":\"1\",\"Channel\":1,\"BlockNumber\":1,\"FinalBlock\":false,\"PublicationDate\":\"2014-01-03T01:00:00.123Z\",\"EvictionDate\":null,\"Size\":100,\"Retransfer\":false},{\"Id\":\"00000000-0000-0000-0000-000000000002\",\"Name\":\"blab\",\"SessionId\":\"2\",\"Channel\":2,\"BlockNumber\":2,\"FinalBlock\":false,\"PublicationDate\":\"2014-01-03T02:00:00.123Z\",\"EvictionDate\":\"2016-01-03T00:00:00.123Z\",\"Size\":200,\"Retransfer\":false},{\"Id\":\"00000000-0000-0000-0000-000000000003\",\"Name\":\"blab\",\"SessionId\":\"3\",\"Channel\":3,\"BlockNumber\":3,\"FinalBlock\":false,\"PublicationDate\":\"2014-01-03T03:00:00.123Z\",\"EvictionDate\":\"2017-01-03T00:00:00.123Z\",\"Size\":300,\"Retransfer\":false}]}".getBytes());
		// @formatter:on
		
		ClientEntitySetIterator<ClientEntitySet, ClientEntity> clientEntitySetIterator = new ClientEntitySetIterator<>(
				odataClient, jsonStream, ContentType.APPLICATION_JSON);
		
		List<CadipFile> result = ResponseMapperUtil.mapResponseToListOfFiles(clientEntitySetIterator);

		assertNotNull(result);
		assertEquals(3, result.size());
	}
}
