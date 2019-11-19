package esa.s1pdgs.cpoc.ingestion.trigger;

import static org.junit.Assert.assertEquals;

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

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntryRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@TestPropertySource(properties = "scheduling.enable=false")
@Transactional
public class TestApplication {
	@Autowired
	private InboxPollingService service;

	@Autowired
	private InboxEntryRepository repo;

	@Test
	public void testPollAll_OnEmptyInboxAndPersistedEntries_ShallDeletePersistedEntries() throws InterruptedException {
		final InboxEntry content = new InboxEntry("bar", "bar", "/tmp/MPS_/S1A", "S1", "A", "MPS_");
		content.setUrl("/tmp/MPS_/S1A/bar");
		repo.save(content);

		final InboxEntry content2 = new InboxEntry("bar2", "bar2", "/tmp/WILE/S1B", "S1", "B", "WILE");
		content2.setUrl("/tmp/WILE/S1B/bar2");
		repo.save(content2);

		service.pollAll();

		final List<InboxEntry> actual = read();
		assertEquals(0, actual.size());
	}

	private final List<InboxEntry> read() {
		return StreamSupport.stream(repo.findAll().spliterator(), false).collect(Collectors.toList());
	}
}
