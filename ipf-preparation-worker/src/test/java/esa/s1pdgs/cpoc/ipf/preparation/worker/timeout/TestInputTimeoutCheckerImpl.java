package esa.s1pdgs.cpoc.ipf.preparation.worker.timeout;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
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

	public final static String METADATA_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
	
	@Test
	public final void testIsMatchingConfiguredTimeliness_IfSameTimeliness_ShallReturnTrue() throws ParseException {		
		final InputWaitingConfig config = new InputWaitingConfig();
		config.setTimelinessRegexp("NRT");
		
		final InputTimeoutCheckerImpl uut = new InputTimeoutCheckerImpl(
				Collections.singletonList(config),
				null
		);
		final AppDataJob<CatalogEvent> job = newJobWithMetadata(
				"2000-01-01T00:00:00.000000Z",
				"2000-01-01T00:10:00.000000Z",
				Collections.singletonMap("timeliness", "NRT")
		);		
		assertEquals(true, uut.isMatchingConfiguredTimeliness(config, job));
	}
	
	@Test
	public final void testIsMatchingConfiguredTimeliness_IfDifferentTimeliness_ShallReturnFalse() throws ParseException {		
		final InputWaitingConfig config = new InputWaitingConfig();
		config.setTimelinessRegexp("NRT");
		
		final InputTimeoutCheckerImpl uut = new InputTimeoutCheckerImpl(
				Collections.singletonList(config),
				null
		);
		final AppDataJob<CatalogEvent> job = newJobWithMetadata(
				"2000-01-01T00:00:00.000000Z",
				"2000-01-01T00:10:00.000000Z",
				Collections.singletonMap("timeliness", "FOO")
		);		
		assertEquals(false, uut.isMatchingConfiguredTimeliness(config,job));
	}
	
	@Test
	public final void testIsMatchingConfiguredInputIdRegex_OnSameId_ShallReturnTrue() throws ParseException {		
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
	public final void testIsMatchingConfiguredInputIdRegex_OnDifferentId_ShallReturnFalse() throws ParseException {		
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
	public final void testIsTimeoutExpiredFor_NotExpired_WhenNowBeforeIngestionwaitBeforeDownlinkwait_ShallReturnFalse() throws ParseException {
		final InputWaitingConfig config = new InputWaitingConfig();
		config.setInputIdRegexp("123");
		config.setTimelinessRegexp("NRT");
		config.setWaitingFromIngestionInSeconds(300L);
		config.setWaitingFromDownlinkInSeconds(600L);
				
		final InputTimeoutCheckerImpl uut = new InputTimeoutCheckerImpl(
				Collections.singletonList(config),
				() -> DateUtils.parse("2000-01-01T00:04:59.999999Z")
		);
		
		final TaskTableInput input = new TaskTableInput();
		input.setId("123");	
		
		final AppDataJob<CatalogEvent> job = newJobWithMetadata(
				"2000-01-01T00:00:00.000000Z",
				"2000-01-01T00:10:00.000000Z",
				Collections.singletonMap("timeliness", "NRT")
		);		
		assertEquals(false, uut.isTimeoutExpiredFor(job, input));
	}
	
	@Test
	public final void testIsTimeoutExpiredFor_NotExpired_WhenNowBeforeDownlinkwaitBeforeIngestionwait_ShallReturnFalse() throws ParseException {
		final InputWaitingConfig config = new InputWaitingConfig();
		config.setInputIdRegexp("123");
		config.setTimelinessRegexp("NRT");
		config.setWaitingFromIngestionInSeconds(1200L);
		config.setWaitingFromDownlinkInSeconds(300L);
				
		final InputTimeoutCheckerImpl uut = new InputTimeoutCheckerImpl(
				Collections.singletonList(config),
				() -> DateUtils.parse("2000-01-01T00:14:59.999999Z")
		);
		
		final TaskTableInput input = new TaskTableInput();
		input.setId("123");	
		
		final AppDataJob<CatalogEvent> job = newJobWithMetadata(
				"2000-01-01T00:00:00.000000Z",
				"2000-01-01T00:10:00.000000Z",
				Collections.singletonMap("timeliness", "NRT")
		);		
		assertEquals(false, uut.isTimeoutExpiredFor(job, input));
	}
	
	@Test
	public final void testIsTimeoutExpiredFor_NotExpired_WhenIngestionwaitBeforeNowBeforeDownlinkwait_ShallReturnFalse() throws ParseException {
		final InputWaitingConfig config = new InputWaitingConfig();
		config.setInputIdRegexp("123");
		config.setTimelinessRegexp("NRT");
		config.setWaitingFromIngestionInSeconds(300L);
		config.setWaitingFromDownlinkInSeconds(600L);
				
		final InputTimeoutCheckerImpl uut = new InputTimeoutCheckerImpl(
				Collections.singletonList(config),
				() -> DateUtils.parse("2000-01-01T00:05:00.000000Z")
		);
		
		final TaskTableInput input = new TaskTableInput();
		input.setId("123");	
		
		final AppDataJob<CatalogEvent> job = newJobWithMetadata(
				"2000-01-01T00:00:00.000000Z",
				"2000-01-01T00:10:00.000000Z",
				Collections.singletonMap("timeliness", "NRT")
		);		
		assertEquals(false, uut.isTimeoutExpiredFor(job, input));
	}
	
	@Test
	public final void testIsTimeoutExpiredFor_NotExpired_WhenDownlinkwaitBeforeNowBeforeIngestionwait_ShallReturnFalse() throws ParseException {
		final InputWaitingConfig config = new InputWaitingConfig();
		config.setInputIdRegexp("123");
		config.setTimelinessRegexp("NRT");
		config.setWaitingFromIngestionInSeconds(1200L);
		config.setWaitingFromDownlinkInSeconds(300L);
				
		final InputTimeoutCheckerImpl uut = new InputTimeoutCheckerImpl(
				Collections.singletonList(config),
				() -> DateUtils.parse("2000-01-01T00:15:00.000000Z")
		);
		
		final TaskTableInput input = new TaskTableInput();
		input.setId("123");	
		
		final AppDataJob<CatalogEvent> job = newJobWithMetadata(
				"2000-01-01T00:00:00.000000Z",
				"2000-01-01T00:10:00.000000Z",
				Collections.singletonMap("timeliness", "NRT")
		);		
		assertEquals(false, uut.isTimeoutExpiredFor(job, input));
	}
	
	@Test
	public final void testIsTimeoutExpiredFor_Expired_WhenIngestionwaitBeforeDownlinkwaitBeforeNow_ShallReturnTrue() throws ParseException {
		final InputWaitingConfig config = new InputWaitingConfig();
		config.setInputIdRegexp("123");
		config.setTimelinessRegexp("NRT");
		config.setWaitingFromIngestionInSeconds(300L);
		config.setWaitingFromDownlinkInSeconds(600L);
				
		final InputTimeoutCheckerImpl uut = new InputTimeoutCheckerImpl(
				Collections.singletonList(config),
				() -> DateUtils.parse("2000-01-01T00:20:00.000000Z")
		);
		
		final TaskTableInput input = new TaskTableInput();
		input.setId("123");	
		
		final AppDataJob<CatalogEvent> job = newJobWithMetadata(
				"2000-01-01T00:00:00.000000Z",
				"2000-01-01T00:10:00.000000Z",
				Collections.singletonMap("timeliness", "NRT")
		);		
		assertEquals(true, uut.isTimeoutExpiredFor(job, input));
	}
	
	@Test
	public final void testIsTimeoutExpiredFor_Expired_WhenDownlinkwaitBeforeIngestionwaitBeforeNow_ShallReturnTrue() throws ParseException {
		final InputWaitingConfig config = new InputWaitingConfig();
		config.setInputIdRegexp("123");
		config.setTimelinessRegexp("NRT");
		config.setWaitingFromIngestionInSeconds(1200L);
		config.setWaitingFromDownlinkInSeconds(300L);
				
		final InputTimeoutCheckerImpl uut = new InputTimeoutCheckerImpl(
				Collections.singletonList(config),
				() -> DateUtils.parse("2000-01-01T00:20:00.000000Z")
		);
		
		final TaskTableInput input = new TaskTableInput();
		input.setId("123");	
		
		final AppDataJob<CatalogEvent> job = newJobWithMetadata(
				"2000-01-01T00:00:00.000000Z",
				"2000-01-01T00:10:00.000000Z",
				Collections.singletonMap("timeliness", "NRT")
		);		
		assertEquals(true, uut.isTimeoutExpiredFor(job, input));
	}
	
	@Test
	public final void testIsTimeoutExpiredFor_OnException_ShallReturnTrue() throws ParseException {
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
				"2000-01-01T00:00:00.000000Z",
				"2000-01-01T00:10:00.000000Z",
				Collections.singletonMap("timeliness", "NRT")
		);	
		assertEquals(true, uut.isTimeoutExpiredFor(job, input));		
	}
		
	private final AppDataJob<CatalogEvent> newJobWithMetadata(final String jobCreationDate,
			final String sensingStartTime, final Map<String,Object> metadata) throws ParseException {
		final AppDataJob<CatalogEvent> job = new AppDataJob<>();
		final CatalogEvent event = new CatalogEvent();
		event.setMetadata(metadata);	
		
		final GenericMessageDto<CatalogEvent> mess = new GenericMessageDto<CatalogEvent>(1, "topic", event);		
		job.getMessages().add(mess);
		
		final AppDataJobProduct appDataJob = new AppDataJobProduct();
		appDataJob.setStartTime(sensingStartTime);		
		job.setProduct(appDataJob);
		job.setCreationDate(new SimpleDateFormat(METADATA_DATE_FORMAT).parse(jobCreationDate));
		return job;
	}
}
