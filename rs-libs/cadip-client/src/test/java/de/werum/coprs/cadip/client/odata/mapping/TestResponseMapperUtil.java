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

package de.werum.coprs.cadip.client.odata.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.data.ResWrap;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.api.serialization.ClientODataDeserializer;
import org.apache.olingo.client.api.serialization.ODataBinder;
import org.apache.olingo.client.api.serialization.ODataDeserializerException;
import org.apache.olingo.client.api.serialization.ODataReader;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.data.Entity;
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
		InputStream jsonStream = new ByteArrayInputStream("{\"@odata.context\": \"$metadata#Sessions\",\"value\": [{\"Id\": \"00000000-0000-0000-0000-000000000001\",\"SessionId\": \"1\",\"NumChannels\": 10,\"PublicationDate\": \"2014-01-01T00:00:00.123Z\",\"Satellite\": \"S1A\",\"StationUnitId\": \"123\",\"DownlinkOrbit\": 123,\"AcquisitionId\": \"123\",\"AntennaId\": \"123\",\"FrontEndId\": \"123\",\"Retransfer\": true,\"AntennaStatusOK\": true,\"FrontEndStatusOK\": true,\"PlannedDataStart\": \"2014-01-01T01:20:00.123Z\",\"PlannedDataStop\": \"2014-01-01T01:30:00.001123Z\",\"DownlinkStart\": \"2014-01-01T02:10:00.001Z\",\"DownlinkStop\": \"2014-01-01T02:21:00.002Z\",\"DownlinkStatusOK\": true,\"DeliveryPushOK\": true},{\"Id\": \"00000000-0000-0000-0000-000000000002\",\"SessionId\": \"2\",\"NumChannels\": 20,\"PublicationDate\": \"2014-01-02T00:00:00.123Z\",\"Satellite\": \"S2A\",\"StationUnitId\": \"234\",\"DownlinkOrbit\": 234,\"AcquisitionId\": \"234\",\"AntennaId\": \"234\",\"FrontEndId\": \"234\",\"Retransfer\": false,\"AntennaStatusOK\": true,\"FrontEndStatusOK\": true,\"PlannedDataStart\": \"2014-01-02T01:20:00.001Z\",\"PlannedDataStop\": \"2014-01-02T01:30:00.002Z\",\"DownlinkStart\": \"2014-01-02T02:10:00.003Z\",\"DownlinkStop\": \"2014-01-02T02:21:00.004Z\",\"DownlinkStatusOK\": true,\"DeliveryPushOK\": true},{\"Id\": \"00000000-0000-0000-0000-000000000003\",\"SessionId\": \"3\",\"NumChannels\": 30,\"PublicationDate\": \"2014-01-03T00:00:00.123Z\",\"Satellite\": \"S3A\",\"StationUnitId\": \"345\",\"DownlinkOrbit\": 345,\"AcquisitionId\": \"345\",\"AntennaId\": \"345\",\"FrontEndId\": \"345\",\"Retransfer\": false,\"AntennaStatusOK\": true,\"FrontEndStatusOK\": true,\"PlannedDataStart\": \"2014-01-03T01:20:00.001Z\",\"PlannedDataStop\": \"2014-01-03T01:30:00.002Z\",\"DownlinkStart\": \"2014-01-03T02:10:00.003Z\",\"DownlinkStop\": \"2014-01-03T02:21:00.004Z\",\"DownlinkStatusOK\": true,\"DeliveryPushOK\": true}]}".getBytes());
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
	
	@Test
	public void test_mapSessionResponseToListOfFiles() throws ODataDeserializerException {
		// @formatter:off
		InputStream jsonStream = new ByteArrayInputStream("{\"@odata.context\":\"$metadata#Sessions(Files())/$entity\",\"Id\":\"34828481-af97-43c0-ac72-def7cf63144c\",\"SessionId\":\"S1A_20200120185900030889\",\"NumChannels\":2,\"PublicationDate\":\"2023-10-12T09:46:54.246655Z\",\"Satellite\":\"S1A\",\"StationUnitId\":\"123stationUnitId321\",\"DownlinkOrbit\":30889,\"AcquisitionId\":\"54321\",\"AntennaId\":\"54322\",\"FrontEndId\":\"54323\",\"Retransfer\":true,\"AntennaStatusOK\":true,\"FrontEndStatusOK\":true,\"PlannedDataStart\":\"2020-01-20T18:59:00Z\",\"PlannedDataStop\":\"2020-01-20T19:15:23Z\",\"DownlinkStart\":\"2020-01-20T18:59:00Z\",\"DownlinkStop\":\"2020-01-20T19:15:23Z\",\"DownlinkStatusOK\":true,\"DeliveryPushOK\":true,\"Files\":[{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"46528e36-c18f-4030-a665-1c4b4799579b\",\"Name\":\"DCS_01_S1A_20200120185900030888_ch1_DSDB_00005.raw\",\"SessionId\":\"S1A_20200120185900030888\",\"Channel\":1,\"BlockNumber\":5,\"FinalBlock\":false,\"PublicationDate\":\"2023-10-12T09:48:54.295631Z\",\"EvictionDate\":null,\"Size\":2,\"Retransfer\":true},{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"616a3c71-db56-4866-98bc-0a636c134a65\",\"Name\":\"DCS_01_S1A_20200120185900030888_ch2_DSDB_00000.raw\",\"SessionId\":\"S1A_20200120185900030888\",\"Channel\":2,\"BlockNumber\":0,\"FinalBlock\":false,\"PublicationDate\":\"2023-10-12T09:48:54.292438Z\",\"EvictionDate\":null,\"Size\":2,\"Retransfer\":true},{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"7badf064-fd34-4e82-a2f7-ce63333512cc\",\"Name\":\"DCS_01_S1A_20200120185900030888_ch1_DSDB_00002.raw\",\"SessionId\":\"S1A_20200120185900030888\",\"Channel\":1,\"BlockNumber\":2,\"FinalBlock\":false,\"PublicationDate\":\"2023-10-12T09:48:54.293347Z\",\"EvictionDate\":null,\"Size\":2,\"Retransfer\":true},{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"8fcb89f6-5c30-4d0d-9129-2e3d63a275b5\",\"Name\":\"DCS_01_S1A_20200120185900030888_ch2_DSDB_00003.raw\",\"SessionId\":\"S1A_20200120185900030888\",\"Channel\":2,\"BlockNumber\":3,\"FinalBlock\":false,\"PublicationDate\":\"2023-10-12T09:48:54.295925Z\",\"EvictionDate\":null,\"Size\":2,\"Retransfer\":true},{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"0adfd275-ebde-488a-b8ce-fb3b45f329db\",\"Name\":\"DCS_01_S1A_20200120185900030888_ch2_DSDB_00001.raw\",\"SessionId\":\"S1A_20200120185900030888\",\"Channel\":2,\"BlockNumber\":1,\"FinalBlock\":false,\"PublicationDate\":\"2023-10-12T09:48:54.293051Z\",\"EvictionDate\":null,\"Size\":2,\"Retransfer\":true},{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"85d0d6dd-d2a7-4280-90f0-bc7a17d50b43\",\"Name\":\"DCS_01_S1A_20200120185900030888_ch3_DSDB_00002.raw\",\"SessionId\":\"S1A_20200120185900030888\",\"Channel\":3,\"BlockNumber\":2,\"FinalBlock\":false,\"PublicationDate\":\"2023-10-12T09:48:54.293598Z\",\"EvictionDate\":null,\"Size\":2,\"Retransfer\":true},{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"7ff2cda6-d199-4bd6-8733-884a52c3ee0a\",\"Name\":\"DCS_01_S1A_20200120185900030888_ch1_DSDB_00001.raw\",\"SessionId\":\"S1A_20200120185900030888\",\"Channel\":1,\"BlockNumber\":1,\"FinalBlock\":false,\"PublicationDate\":\"2023-10-12T09:48:54.294344Z\",\"EvictionDate\":null,\"Size\":7,\"Retransfer\":true},{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"2b5fd5a8-ac08-42d7-b617-99cea38bd7ac\",\"Name\":\"DCS_01_S1A_20200120185900030888_ch1_DSDB_00000.raw\",\"SessionId\":\"S1A_20200120185900030888\",\"Channel\":1,\"BlockNumber\":0,\"FinalBlock\":false,\"PublicationDate\":\"2023-10-12T09:48:54.294981Z\",\"EvictionDate\":null,\"Size\":2203290,\"Retransfer\":true},{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"077d1dd9-9245-498a-8a9f-131e262b2467\",\"Name\":\"DCS_01_S1A_20200120185900030888_ch2_DSDB_00002.raw\",\"SessionId\":\"S1A_20200120185900030888\",\"Channel\":2,\"BlockNumber\":2,\"FinalBlock\":false,\"PublicationDate\":\"2023-10-12T09:48:54.295333Z\",\"EvictionDate\":null,\"Size\":2,\"Retransfer\":true},{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"2548a458-648b-4713-a900-484010d30613\",\"Name\":\"DCS_01_S1A_20200120185900030888_ch1_DSDB_00004.raw\",\"SessionId\":\"S1A_20200120185900030888\",\"Channel\":1,\"BlockNumber\":4,\"FinalBlock\":false,\"PublicationDate\":\"2023-10-12T09:48:54.294599Z\",\"EvictionDate\":null,\"Size\":2,\"Retransfer\":true},{\"@odata.mediaContentType\":\"application/octet-stream\",\"Id\":\"ce321a90-3bc7-459c-8b5a-c964101a6d15\",\"Name\":\"DCS_01_S1A_20200120185900030888_ch3_DSDB_00012.raw\",\"SessionId\":\"S1A_20200120185900030888\",\"Channel\":3,\"BlockNumber\":12,\"FinalBlock\":false,\"PublicationDate\":\"2023-10-12T09:48:54.293968Z\",\"EvictionDate\":null,\"Size\":2,\"Retransfer\":true}]}".getBytes());
		// @formatter:on
		
		ClientODataDeserializer deserializer = odataClient.getDeserializer(ContentType.JSON);
		ResWrap<Entity> response = deserializer.toEntity(jsonStream);
		ClientEntity entity = odataClient.getBinder().getODataEntity(response);
		
		List<CadipFile> result = ResponseMapperUtil.mapSessionResponseToListOfFiles(entity);
		
		assertNotNull(result);
		assertEquals(11, result.size());
	}
}
