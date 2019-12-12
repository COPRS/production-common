package esa.s1pdgs.cpoc.ingestion.trigger;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.ingestion.trigger.config.IngestionTriggerConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntryRepository;
import esa.s1pdgs.cpoc.ingestion.trigger.fs.FilesystemInboxEntryFactory;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@TestPropertySource(properties = "scheduling.enable=false")
@Transactional
public class TestApplication {
	@Autowired
	private IngestionTriggerService service;
	
	@Autowired
	private IngestionTriggerConfigurationProperties props;

	@Autowired
	private InboxEntryRepository repo;

	@Test
	public void testPollAll_OnEmptyInboxAndPersistedEntries_ShallDeletePersistedEntries() throws InterruptedException {		
		final Path inbox = Paths.get(props.getPolling().get(0).getDirectory());
		if (!inbox.toFile().exists()) {
			inbox.toFile().mkdirs();
		}		
		final FilesystemInboxEntryFactory fact = new FilesystemInboxEntryFactory();	
		final InboxEntry content = fact.newInboxEntry(inbox, inbox.resolve("WILE/S1B/L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSIB.xml"), 2);
		final InboxEntry content2 = fact.newInboxEntry(inbox, inbox.resolve("S1__AUX_ICE_V20160501T120000_G20160502T043607.SAFE"), 0);
		
		repo.save(content);
		repo.save(content2);
		service.pollAll();
		
		final List<InboxEntry> actual = read();
		assertEquals(0, actual.size());
	}

	private final List<InboxEntry> read() {
		return StreamSupport.stream(repo.findAll().spliterator(), false)
				.collect(Collectors.toList());
	}
}
