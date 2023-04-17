package esa.s1pdgs.cpoc.ingestion.trigger.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.matchers.Times.once;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.time.ZoneOffset;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ingestion.trigger.config.IngestionTriggerConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.trigger.config.TestConfig;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@ComponentScan("esa.s1pdgs.cpoc")
@Import(TestConfig.class)
@PropertySource("classpath:stream-parameters--auxip.properties")
public class ITIngestionTriggerService {
	@Autowired
	private IngestionTriggerService ingestionTriggerService;
	
	@Autowired
	private IngestionTriggerConfigurationProperties props;
	
	private ClientAndServer mockServer;

    @Before
    public void before() {
    	mockServer = new ClientAndServer(1080);
    }
 
    @After
    public void after() { 
    	mockServer.stop();
    }
	
	@Test
	public void testIngestionTriggerService_auxip() {
    	HttpRequest oAuthRequest = request()
    			.withMethod("POST")
    			.withPath("/oauth")
    			.withHeaders(new Header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"))
    			.withBody("grant_type=password&client_id=foo2&client_secret=bar2&username=foo1&password=bar1");
    	HttpResponse oAuthResponse = response().withStatusCode(200)
    			.withHeaders(new Header("Content-Type", "application/json; charset=utf-8"))
    			.withBody("{\"access_token\": \"foo\"}");
    	mockServer.when(oAuthRequest, exactly(2)).respond(oAuthResponse);
		
		HttpRequest queryRequest1 = request()
    			.withMethod("GET")
    			.withPath("/odata/v1/Products")
    			.withQueryStringParameter("$filter", "((PublicationDate ge 1999-12-31T23:55:00Z) and " +
    						"(PublicationDate lt 2000-01-01T23:55:00Z))")
				.withQueryStringParameter("$top", "100")
				.withQueryStringParameter("$skip", "0")
				.withQueryStringParameter("$orderby", "PublicationDate asc")
				.withHeaders(
						new Header("OAUTH2-ACCESS-TOKEN", "foo"),
						new Header("OData-Version", "4.0"),
						new Header("Accept", "application/json")
				);
    	HttpResponse response1 = response().withStatusCode(200)
    			.withHeaders(
    					new Header("OData-Version", "4.0"),
    					new Header("Content-Type", "application/json;odata.metadata=minimal"))
    			.withBody("{\"@odata.context\":\"$metadata#Products\",\"value\":[{\"@odata.mediaContentType\":\"application/zip\"," +
    						"\"Id\":\"00000000-0000-0000-0000-000000000002\"," + 
    						"\"Name\":\"S1A_OPER_AUX_RESORB_OPOD_20200119T163449_V20200119T112525_20200119T144255.EOF.zip\"," +
    						"\"ContentType\":\"application/zip\",\"ContentLength\":3600003," +
    						"\"PublicationDate\":\"2000-01-01T00:00:00Z\",\"EvictionDate\":\"2048-01-01T00:00:00Z\"," +
    						"\"Checksum\":[{\"Algorithm\":\"MD5\",\"Value\":\"4d21b35de4619315e8ba36dfa596eb44\"," +
    						"\"ChecksumDate\":\"2020-04-04T17:07:07.777Z\"}],\"ProductionType\":\"systematic_production\"," +
    						"\"ContentDate\":{\"Start\":\"1999-01-01T00:00:00Z\",\"End\":\"2000-01-01T00:00:00Z\"}," +    						
    						// "\"Footprint\": null}]}");
    						// not part of the AUX_RESORB, but we want to test footprint anyway
    						"\"Footprint\":{\"type\":\"Polygon\",\"coordinates\":[[[-74.8571,-120.3411],[-75.4484,-121.9204],[-76.4321,-122.3625]," +
    						"[-74.8571,-120.3411]]],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}}]}");
    	mockServer.when(queryRequest1, once()).respond(response1);

		HttpRequest queryRequest2 = request()
    			.withMethod("GET")
    			.withPath("/odata/v1/Products")
    			.withQueryStringParameter("$filter", "((PublicationDate ge 1999-12-31T23:55:00Z) and " +
    						"(PublicationDate lt 2000-01-01T23:55:00Z))")
				.withQueryStringParameter("$top", "100")
				.withQueryStringParameter("$skip", "100")
				.withQueryStringParameter("$orderby", "PublicationDate asc")
				.withHeaders(
						new Header("OAUTH2-ACCESS-TOKEN", "foo"),
						new Header("OData-Version", "4.0"),
						new Header("Accept", "application/json")
				);
    	HttpResponse response2 = response().withStatusCode(200)
    			.withHeaders(
    					new Header("OData-Version", "4.0"),
    					new Header("Content-Type", "application/json;odata.metadata=minimal"))
    			.withBody("{\"@odata.context\":\"$metadata#Products\",\"value\":[]}");
    	mockServer.when(queryRequest2, once()).respond(response2);

		assertNotNull(props.getPolling().get("itIngestionTriggerService_auxip"));
		List<IngestionJob> result = ingestionTriggerService.get();
		
		assertEquals(1, result.size());		
		IngestionJob jobs = result.get(0);
		assertEquals("auxip", jobs.getInboxType());
		assertEquals(ProductFamily.AUXILIARY_FILE, jobs.getProductFamily());
		assertEquals("00000000-0000-0000-0000-000000000002", jobs.getProductName()); // Seems to be intended to store the ID this way, see: esa.s1pdgs.cpoc.ingestion.trigger.auxip.AuxipInboxAdapter.toInboxEntry(AuxipProductMetadata)
		assertEquals("00000000-0000-0000-0000-000000000002", jobs.getKeyObjectStorage());
		assertEquals("S1A_OPER_AUX_RESORB_OPOD_20200119T163449_V20200119T112525_20200119T144255.EOF.zip", jobs.getRelativePath());
		assertEquals("NOT_DEFINED", jobs.getStoragePath());
		assertEquals("http://localhost:1080/odata/v1", jobs.getPickupBaseURL());
		assertEquals(3600003L, jobs.getProductSizeByte());
		assertEquals(DateUtils.parse("2000-01-01T00:00:00.000000Z").toInstant(ZoneOffset.UTC), jobs.getLastModified().toInstant());
		assertEquals("FOO", jobs.getStationName());
		assertNull(jobs.getMode());
		assertEquals("", jobs.getTimeliness());
		assertEquals(0, jobs.getAdditionalMetadata().size());
	}	
}
