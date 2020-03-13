package esa.s1pdgs.cpoc.ipf.preparation.worker.timeout;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings.InputWaitingConfig;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class TestInputTimeoutCheckerImpl {

	@Test
	public final void testIsMatchingConfiguredTimeliness_IfSameTimeliness_ShallReturnTrue() {		
		final InputWaitingConfig config = new InputWaitingConfig();
		config.setTimelinessRegexp("NRT");
		
		final InputTimeoutCheckerImpl uut = new InputTimeoutCheckerImpl(
				Collections.singletonList(config),
				null
		);
		final AppDataJob<CatalogEvent> job = newJobWithMetadata(
				Collections.singletonMap("timeliness", "NRT")
		);		
		assertEquals(true, uut.isMatchingConfiguredTimeliness(config, job));
	}
	
	@Test
	public final void testIsMatchingConfiguredTimeliness_IfDifferentTimeliness_ShallReturnFalse() {		
		final InputWaitingConfig config = new InputWaitingConfig();
		config.setTimelinessRegexp("NRT");
		
		final InputTimeoutCheckerImpl uut = new InputTimeoutCheckerImpl(
				Collections.singletonList(config),
				null
		);
		final AppDataJob<CatalogEvent> job = newJobWithMetadata(
				Collections.singletonMap("timeliness", "FOO")
		);		
		assertEquals(false, uut.isMatchingConfiguredTimeliness(config,job));
	}
	
	@Test
	public final void testIsMatchingConfiguredInputIdRegex_OnSameId_ShallReturnTrue() {		
		final InputWaitingConfig config = new InputWaitingConfig();
		config.setInputIdRegexp("123");
	
		final InputTimeoutCheckerImpl uut = new InputTimeoutCheckerImpl(
				Collections.singletonList(config),
				null
		);
		final TaskTableInput input = new TaskTableInput();
		input.setId("123");		
		assertEquals(true, uut.isMatchingConfiguredInputIdRegex(config,input));		
	}
	
	@Test
	public final void testIsMatchingConfiguredInputIdRegex_OnDifferentId_ShallReturnFalse() {		
		final InputWaitingConfig config = new InputWaitingConfig();
		config.setInputIdRegexp("123");

		final InputTimeoutCheckerImpl uut = new InputTimeoutCheckerImpl(
				Collections.singletonList(config),
				null
		);
		final TaskTableInput input = new TaskTableInput();
		input.setId("completetlyDifferentId");	
		assertEquals(false, uut.isMatchingConfiguredInputIdRegex(config,input));		
	}
	
	@Test
	public final void testIsTimeoutExpiredFor_NotExpired_ShallReturnFalse() {
		final InputWaitingConfig config = new InputWaitingConfig();
		config.setInputIdRegexp("123");
		config.setTimelinessRegexp("NRT");
				
		final InputTimeoutCheckerImpl uut = new InputTimeoutCheckerImpl(
				Collections.singletonList(config),
				() -> DateUtils.parse("2000-01-01T00:10:00.000000Z")
		);
		
		final TaskTableInput input = new TaskTableInput();
		input.setId("123");	
		
		final AppDataJob<CatalogEvent> job = newJobWithMetadata(
				Collections.singletonMap("timeliness", "NRT")
		);		
		assertEquals(false, uut.isTimeoutExpiredFor(job, input));
	}
	
	@Test
	public final void testIsTimeoutExpiredFor_Expired_ShallReturnTrue() {
		final InputWaitingConfig config = new InputWaitingConfig();
		config.setInputIdRegexp("123");
		config.setTimelinessRegexp("NRT");
		config.setDelayInSeconds(0L);
		config.setWaitingInSeconds(0L);
				
		final InputTimeoutCheckerImpl uut = new InputTimeoutCheckerImpl(
				Collections.singletonList(config),
				() -> DateUtils.parse("2920-01-01T01:10:00.000000Z")
		);
		
		final TaskTableInput input = new TaskTableInput();
		input.setId("123");	
		
		final AppDataJob<CatalogEvent> job = newJobWithMetadata(
				Collections.singletonMap("timeliness", "NRT")
		);		
		assertEquals(true, uut.isTimeoutExpiredFor(job, input));
	}
	
	@Test
	public final void testIsTimeoutExpiredFor_OnException_ShallReturnTrue() {
		final InputWaitingConfig config = new InputWaitingConfig();
		config.setInputIdRegexp("123");
		config.setTimelinessRegexp("NRT");
				
		final InputTimeoutCheckerImpl uut = new InputTimeoutCheckerImpl(
				Collections.singletonList(config),
				() -> {
					throw new IllegalArgumentException("Expected");
				}
		);
		final TaskTableInput input = new TaskTableInput();
		input.setId("123");	
		
		final AppDataJob<CatalogEvent> job = newJobWithMetadata(
				Collections.singletonMap("timeliness", "NRT")
		);	
		assertEquals(true, uut.isTimeoutExpiredFor(job, input));		
	}
	
	
		
	private final AppDataJob<CatalogEvent> newJobWithMetadata(final Map<String,Object> metadata) {
		final AppDataJob<CatalogEvent> job = new AppDataJob<>();
		final CatalogEvent event = new CatalogEvent();
		event.setMetadata(metadata);	
		
		final GenericMessageDto<CatalogEvent> mess = new GenericMessageDto<CatalogEvent>(1, "topic", event);		
		job.getMessages().add(mess);
		
		final AppDataJobProduct appDataJob = new AppDataJobProduct();
		appDataJob.setStartTime("2010-01-01T00:10:00.000000Z");		
		job.setProduct(appDataJob);		
		job.setCreationDate(new Date());
		
		return job;
	}
}
