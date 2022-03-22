package esa.s1pdgs.cpoc.mdc.trigger.service;

import java.util.List;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mdc.trigger.config.MdcTriggerConfigurationProperties;
import esa.s1pdgs.cpoc.mdc.trigger.config.MdcTriggerConfigurationProperties.CategoryConfig;
import esa.s1pdgs.cpoc.mdc.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;

public class MetadataTriggerServiceTest {
	
	private MetadataTriggerService uut;
	
	@Mock
	private MdcTriggerConfigurationProperties properties;
	
	@Mock
	private MqiClient mqiClient;
	
	@Mock
	private List<MessageFilter> messageFilter;
	
	@Mock
	private AppStatus appStatus;
	
	@Mock
	private ErrorRepoAppender errorAppender;
	
	@Mock
	private ProcessConfiguration processConfig;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		uut = new MetadataTriggerService(properties, mqiClient, messageFilter, appStatus, errorAppender, processConfig);
	}
	
	@Test
	public final void newMqiConsumerFor_Ingestion() {
		MqiConsumer<?> consumer = uut.newMqiConsumerFor(ProductCategory.INGESTION_EVENT, new CategoryConfig());
		assertTrue(consumer.toString().contains(ProductCategory.INGESTION_EVENT.toString()));
	}
	
	@Test
	public final void newMqiConsumerFor_Production() {
		MqiConsumer<?> consumer = uut.newMqiConsumerFor(ProductCategory.PRODUCTION_EVENT, new CategoryConfig());
		assertTrue(consumer.toString().contains(ProductCategory.PRODUCTION_EVENT.toString()));
	}
	
	@Test
	public final void newMqiConsumerFor_Compression() {
		MqiConsumer<?> consumer = uut.newMqiConsumerFor(ProductCategory.COMPRESSED_PRODUCTS, new CategoryConfig());
		assertTrue(consumer.toString().contains(ProductCategory.COMPRESSED_PRODUCTS.toString()));
	}
	

}
