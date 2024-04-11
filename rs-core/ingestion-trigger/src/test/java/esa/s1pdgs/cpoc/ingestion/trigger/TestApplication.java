/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.ingestion.trigger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.config.InboxConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.config.IngestionTriggerConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.trigger.config.TestConfig;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntryRepository;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.service.IngestionTriggerService;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@ComponentScan({"esa.s1pdgs.cpoc","de.werum.coprs"})
@Import(TestConfig.class)
@PropertySource("classpath:stream-parameters--fs.properties")
public class TestApplication {
	@Autowired
	private IngestionTriggerService service;
	
	@Autowired
	private IngestionTriggerConfigurationProperties props;

	@Autowired
	private InboxEntryRepository repo;

	@Autowired
	private InboxEntryFactory factory;

	@Test
	public void testPollAll_OnEmptyInboxAndPersistedEntries_ShallDeletePersistedEntriesForInboxStation() throws URISyntaxException {	
		final InboxConfiguration edrsInboxConf = props.getPolling().get("testApplicationInboxForEDRS");
		final URI inboxURL = new URI(edrsInboxConf.getDirectory());
		final Path inboxPath = Paths.get(inboxURL.getPath());
		
		if (!inboxPath.toFile().exists()) {
			inboxPath.toFile().mkdirs();
		}

		final InboxEntry edrsEntry = factory.newInboxEntry(
				inboxURL,
				inboxPath.resolve("WILE/S1B/L20180724144436762001030/ch01/DCS_02_L20180724144436762001030_ch1_DSIB.xml"),
				new Date(),
				0,
				"WILE",
				"S1",
				null,
				ProductFamily.EDRS_SESSION
		);

		final InboxEntry auxEntry = factory.newInboxEntry(
				inboxURL,
				inboxPath.resolve("AUX/S1__AUX_ICE_V20160501T120000_G20160502T043607.SAFE"),
				new Date(),
				0,
				null,
				null,
				null,
				ProductFamily.AUXILIARY_FILE
		);

		repo.save(edrsEntry);
		repo.save(auxEntry);
		assertEquals(2, repo.findAll().size());
		List<IngestionJob> jobs = service.get();
		assertNull(jobs); // is null when no new job has been created

		final List<InboxEntry> inboxEntries = repo.findAll();
		assertEquals(1, inboxEntries.size());
		assertEquals(auxEntry, inboxEntries.get(0));
	}

}
