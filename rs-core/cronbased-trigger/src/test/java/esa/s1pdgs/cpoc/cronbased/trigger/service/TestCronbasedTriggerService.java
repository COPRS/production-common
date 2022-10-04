package esa.s1pdgs.cpoc.cronbased.trigger.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.cronbased.trigger.db.CronbasedTriggerEntry;
import esa.s1pdgs.cpoc.cronbased.trigger.db.CronbasedTriggerEntryRepository;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;

public class TestCronbasedTriggerService {
	
	@Mock
	private MetadataClient metadataClient;
	
	@Mock
	private CronbasedTriggerEntryRepository repository;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testGroupShallBeCheckedTrue() {
		String cronExpression = "0 0/15 * * * *";
		CronbasedTriggerEntry triggerEntry = new CronbasedTriggerEntry();
		triggerEntry.setLastCheckDate(new Date(2022, 10, 4, 10, 3, 15));
		
		Date now = new Date(2022, 10, 4, 10, 16, 21);
		
		CronbasedTriggerService service = new CronbasedTriggerService(null, metadataClient, repository);
		assertTrue(service.groupShallBeChecked(triggerEntry, now, cronExpression));
	}
	
	@Test
	public void testGroupShallBeCheckedFalse() {
		String cronExpression = "0 0/15 * * * *";
		CronbasedTriggerEntry triggerEntry = new CronbasedTriggerEntry();
		triggerEntry.setLastCheckDate(new Date(2022, 10, 4, 10, 3, 15));
		
		Date now = new Date(2022, 10, 4, 10, 14, 21);
		
		CronbasedTriggerService service = new CronbasedTriggerService(null, metadataClient, repository);
		assertFalse(service.groupShallBeChecked(triggerEntry, now, cronExpression));
	}
	
}
