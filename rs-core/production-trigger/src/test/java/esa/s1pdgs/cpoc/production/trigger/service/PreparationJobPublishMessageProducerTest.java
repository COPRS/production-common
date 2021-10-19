package esa.s1pdgs.cpoc.production.trigger.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.production.trigger.config.ProcessSettings;
import esa.s1pdgs.cpoc.production.trigger.taskTableMapping.SingleTasktableMapper;
import esa.s1pdgs.cpoc.production.trigger.taskTableMapping.TasktableMapper;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class PreparationJobPublishMessageProducerTest {

	private PreparationJobPublishMessageProducer uut;

	@Mock
	private MetadataClient metadataClient;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		ProcessSettings processSettings  = new ProcessSettings();
		processSettings.setL0EwSlcTaskTableName("ttname");
	    Pattern seaCoverageCheckPattern = Pattern.compile("oversea");
	    Pattern l0EwSlcCheckPattern = Pattern.compile("l0ewslc");
		uut = new PreparationJobPublishMessageProducer(processSettings, seaCoverageCheckPattern, l0EwSlcCheckPattern, metadataClient);
	}
	
	@Test
	public final void createPublishingJob() {
		
		final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("Test");
		final GenericMessageDto<CatalogEvent> mqiMessage = new GenericMessageDto<CatalogEvent>();
		CatalogEvent catalogEvent = new CatalogEvent();
		catalogEvent.setProductFamily(ProductFamily.L0_SLICE);
		catalogEvent.setKeyObjectStorage("l0slice");
		catalogEvent.setProductName("l0slice");
		Map<String,Object> metadata = new HashMap<>();
		metadata.put("startTime", "2017-12-13T14:59:48.123456Z");
		metadata.put("stopTime", "2017-12-13T15:17:25.142536Z");
		catalogEvent.setMetadata(metadata);
		mqiMessage.setBody(catalogEvent);
		
		final TasktableMapper ttMapper = new SingleTasktableMapper("ttname");
		final String outputProductType = "outputType";
		
		MqiPublishingJob<IpfPreparationJob> job = null;
		
		try {
			 job = uut.createPublishingJob(reporting, mqiMessage, ttMapper, outputProductType);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
		
		assertNotNull(job);
		assertTrue(job.getMessages().size() == 1);
	}
	
	@Test
	public final void createPublishingJob_oversea_true() throws MetadataQueryException {
		
		final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("Test");
		final GenericMessageDto<CatalogEvent> mqiMessage = new GenericMessageDto<CatalogEvent>();
		CatalogEvent catalogEvent = new CatalogEvent();
		catalogEvent.setProductFamily(ProductFamily.L0_SLICE);
		catalogEvent.setKeyObjectStorage("oversea");
		catalogEvent.setProductName("oversea");
		Map<String,Object> metadata = new HashMap<>();
		metadata.put("startTime", "2017-12-13T14:59:48.123456Z");
		metadata.put("stopTime", "2017-12-13T15:17:25.142536Z");
		catalogEvent.setMetadata(metadata);
		mqiMessage.setBody(catalogEvent);
		
		final TasktableMapper ttMapper = new SingleTasktableMapper("ttname");
		final String outputProductType = "outputType";
		
		doReturn(1).when(metadataClient).getSeaCoverage(any(), any());
		
		MqiPublishingJob<IpfPreparationJob> job = null;
		
		try {
			 job = uut.createPublishingJob(reporting, mqiMessage, ttMapper, outputProductType);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
		
		assertNotNull(job);
		assertTrue(job.getMessages().size() == 1);
	}
	
	@Test
	public final void createPublishingJob_oversea_false() throws MetadataQueryException {
		
		final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("Test");
		final GenericMessageDto<CatalogEvent> mqiMessage = new GenericMessageDto<CatalogEvent>();
		CatalogEvent catalogEvent = new CatalogEvent();
		catalogEvent.setProductFamily(ProductFamily.L0_SLICE);
		catalogEvent.setKeyObjectStorage("oversea");
		catalogEvent.setProductName("oversea");
		Map<String,Object> metadata = new HashMap<>();
		metadata.put("startTime", "2017-12-13T14:59:48.123456Z");
		metadata.put("stopTime", "2017-12-13T15:17:25.142536Z");
		catalogEvent.setMetadata(metadata);
		mqiMessage.setBody(catalogEvent);
		
		final TasktableMapper ttMapper = new SingleTasktableMapper("ttname");
		final String outputProductType = "outputType";
		
		doReturn(0).when(metadataClient).getSeaCoverage(any(), any());
		
		MqiPublishingJob<IpfPreparationJob> job = null;
		
		try {
			 job = uut.createPublishingJob(reporting, mqiMessage, ttMapper, outputProductType);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
		
		assertNotNull(job);
		assertTrue(job.getMessages().size() == 0);
	}
	
	@Test
	public final void createPublishingJob_l0ewslc_true() throws MetadataQueryException {
		
		final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("Test");
		final GenericMessageDto<CatalogEvent> mqiMessage = new GenericMessageDto<CatalogEvent>();
		CatalogEvent catalogEvent = new CatalogEvent();
		catalogEvent.setProductFamily(ProductFamily.L0_SLICE);
		catalogEvent.setKeyObjectStorage("l0ewslc");
		catalogEvent.setProductName("l0ewslc");
		Map<String,Object> metadata = new HashMap<>();
		metadata.put("startTime", "2017-12-13T14:59:48.123456Z");
		metadata.put("stopTime", "2017-12-13T15:17:25.142536Z");
		catalogEvent.setMetadata(metadata);
		mqiMessage.setBody(catalogEvent);
		
		final TasktableMapper ttMapper = new SingleTasktableMapper("ttname");
		final String outputProductType = "outputType";
		
		doReturn(true).when(metadataClient).isIntersectingEwSlcMask(any(), any());
		
		MqiPublishingJob<IpfPreparationJob> job = null;
		
		try {
			 job = uut.createPublishingJob(reporting, mqiMessage, ttMapper, outputProductType);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
		
		assertNotNull(job);
		assertTrue(job.getMessages().size() == 1);
	}
	
	@Test
	public final void createPublishingJob_l0ewslc_false() throws MetadataQueryException {
		
		final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("Test");
		final GenericMessageDto<CatalogEvent> mqiMessage = new GenericMessageDto<CatalogEvent>();
		CatalogEvent catalogEvent = new CatalogEvent();
		catalogEvent.setProductFamily(ProductFamily.L0_SLICE);
		catalogEvent.setKeyObjectStorage("l0ewslc");
		catalogEvent.setProductName("l0ewslc");
		Map<String,Object> metadata = new HashMap<>();
		metadata.put("startTime", "2017-12-13T14:59:48.123456Z");
		metadata.put("stopTime", "2017-12-13T15:17:25.142536Z");
		catalogEvent.setMetadata(metadata);
		mqiMessage.setBody(catalogEvent);
		
		final TasktableMapper ttMapper = new SingleTasktableMapper("ttname");
		final String outputProductType = "outputType";
		
		doReturn(false).when(metadataClient).isIntersectingEwSlcMask(any(), any());
		
		MqiPublishingJob<IpfPreparationJob> job = null;
		
		try {
			 job = uut.createPublishingJob(reporting, mqiMessage, ttMapper, outputProductType);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
		
		assertNotNull(job);
		assertTrue(job.getMessages().size() == 0);
	}
	
	

}
