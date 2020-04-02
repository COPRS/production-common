package esa.s1pdgs.cpoc.disseminator;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.DisseminationTypeConfiguration;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration.Protocol;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mqi.client.config.MqiConfigurationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class TestApplication {
	
	@Autowired
	private DisseminationProperties properties;
	
	@Test
	public final void testDisseminationProperties() {
		// check that properties have been loaded successfully by spot checking some elements
		final MqiConfigurationProperties configProps;
		
		System.out.println(properties);
		
		assertEquals("localhost", properties.getHostname());
		
		final Map<String, OutboxConfiguration> outboxes = properties.getOutboxes();
		assertEquals(1, outboxes.size());		
		
		final Map.Entry<String, OutboxConfiguration> entry = outboxes.entrySet().iterator().next();
		assertEquals("myLittleOutbox", entry.getKey());	
		final OutboxConfiguration outbox = entry.getValue();		
		assertEquals(Protocol.FILE, outbox.getProtocol());
		assertEquals("/tmp/foo/bar", outbox.getPath());
		
		final Map<ProductCategory, List<DisseminationTypeConfiguration>> catConfig = properties.getCategories();
		assertEquals(1, catConfig.size());			
		final Map.Entry<ProductCategory, List<DisseminationTypeConfiguration>> catEntry = catConfig.entrySet().iterator().next();
		assertEquals(ProductCategory.AUXILIARY_FILES, catEntry.getKey());		
		final List<DisseminationTypeConfiguration> diss = catEntry.getValue();
		assertEquals(1, diss.size());
		final DisseminationTypeConfiguration dissConfig = diss.get(0);
		assertEquals("^([0-9a-z][0-9a-z]){1}([0-9a-z_]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_PP2|AUX_CAL|AUX_INS|AUX_RESORB|AUX_WND|AUX_ICE|AUX_SCS|AMV_ERRMAT|AMH_ERRMAT|AUX_WAV|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$", dissConfig.getRegex());
		assertEquals("myLittleOutbox", dissConfig.getTarget());
	}
	
	@Test
	public final void testConfigsFor() {
		/////final DisseminationService uut = new DisseminationService(null, null, properties, ErrorRepoAppender.NULL, AppStatus.NULL);
		
		final DisseminationTriggerListener<ProductionEvent> uut = new DisseminationTriggerListener<>(null, properties, ErrorRepoAppender.NULL);
		
		// noting should be configured for EDRS_SESSION
		assertEquals(Collections.emptyList(), uut.configsFor(ProductFamily.EDRS_SESSION));
		
		final List<DisseminationTypeConfiguration> actual = uut.configsFor(ProductFamily.AUXILIARY_FILE);
		assertEquals(1, actual.size());
	}
	
	@Test
	public final void testOnMessage_OnNotConfiguredFamily_ShallDoNothing() {	
		final DisseminationTriggerListener<ProductionEvent> uut = new DisseminationTriggerListener<>(null, properties, ErrorRepoAppender.NULL);

		final ProductionEvent fakeProduct = new ProductionEvent("fakeProduct", "my/key", ProductFamily.BLANK);
		final GenericMessageDto<ProductionEvent> fakeMessage = new GenericMessageDto<ProductionEvent>(123, "myKey", fakeProduct); 
		uut.onMessage(fakeMessage);
	}
	
	@Test
	public final void testOnMessage_OnConfiguredFamily_ShallEvaluatedConfiguredRegex() {
		final FakeObsClient fakeObsClient = new FakeObsClient() {
			@Override public final boolean exists(final ObsObject object) throws SdkClientException, ObsServiceException {
				return true;
			}			
		};		
		final DisseminationTriggerListener<ProductionEvent> uut = new DisseminationTriggerListener<>(fakeObsClient, properties, ErrorRepoAppender.NULL);

		final ProductionEvent fakeProduct = new ProductionEvent("fakeProduct", "my/key", ProductFamily.BLANK);
		final GenericMessageDto<ProductionEvent> fakeMessage = new GenericMessageDto<ProductionEvent>(123, "myKey", fakeProduct); 
		uut.onMessage(fakeMessage);
	}
}
