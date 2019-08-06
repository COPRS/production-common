package esa.s1pdgs.cpoc.inbox;

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

import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;
import esa.s1pdgs.cpoc.inbox.entity.InboxEntryRepository;

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
		final InboxEntry content = new InboxEntry("bar", "file:///tmp/foo/bar");
		repo.save(content);
		
		final InboxEntry content2 = new InboxEntry("bar2", "file:///tmp/foo/bar2");
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
