package esa.s1pdgs.cpoc.ingestion.trigger;

import esa.s1pdgs.cpoc.ingestion.trigger.config.IngestionTriggerConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntryRepository;
import esa.s1pdgs.cpoc.ingestion.trigger.fs.FilesystemInboxEntryFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.service.IngestionTriggerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@TestPropertySource(properties = "scheduling.enable=false")
public class TestApplication {

	//	 uncomment, if embedded mongo needs to be updated
//	{
//		System.setProperty("http.proxyHost", "proxy.net.werum");
//		System.setProperty("http.proxyPort", "8080");
//		System.setProperty("https.proxyHost", "proxy.net.werum");
//		System.setProperty("https.proxyPort", "8080");
//	}

	@Autowired
	private IngestionTriggerService service;
	
	@Autowired
	private IngestionTriggerConfigurationProperties props;

	@Autowired
	private InboxEntryRepository repo;

	@Autowired
	private FilesystemInboxEntryFactory factory;

	@Test
	public void testPollAll_OnEmptyInboxAndPersistedEntries_ShallDeletePersistedEntriesForInboxStation() throws URISyntaxException {
		
		final URI inboxURL = new URI(props.getPolling().get(0).getDirectory());
		final Path inboxPath = Paths.get(inboxURL.getPath());
		
		if (!inboxPath.toFile().exists()) {
			inboxPath.toFile().mkdirs();
		}
		
		final InboxEntry content = factory.newInboxEntry(
				inboxURL,
				inboxPath.resolve("WILE/S1B/L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSIB.xml"),
				2,
				new Date(),
				0,
				"WILE"
		);
		final InboxEntry content2 = factory.newInboxEntry(
				inboxURL,
				inboxPath.resolve("AUX/S1__AUX_ICE_V20160501T120000_G20160502T043607.SAFE"),
				1,
				new Date(),
				0,
				null
		);

		repo.save(content);
		repo.save(content2);
		assertEquals(2, read().size());

		service.pollAll();

		final List<InboxEntry> inboxEntries = read();
		assertEquals(1, inboxEntries.size());
		assertEquals(content, inboxEntries.get(0));

	}

	private List<InboxEntry> read() {
		return new ArrayList<>(repo.findAll());
	}
}
