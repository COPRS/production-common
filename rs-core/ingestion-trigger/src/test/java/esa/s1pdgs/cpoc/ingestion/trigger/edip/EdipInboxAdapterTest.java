package esa.s1pdgs.cpoc.ingestion.trigger.edip;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ebip.client.EdipClient;
import esa.s1pdgs.cpoc.ebip.client.EdipEntry;
import esa.s1pdgs.cpoc.ebip.client.EdipEntryFilter;
import esa.s1pdgs.cpoc.ebip.client.EdipEntryImpl;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

public class EdipInboxAdapterTest {

	@Mock
	EdipClient edipClient;

	@Mock
	InboxEntryFactory inboxEntryFactory;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void list() throws IOException {

		List<EdipEntry> list = new ArrayList<>();

		EdipEntry e1 = new EdipEntryImpl("DCS_01_S1A_20200120185900030888_ch1_DSDB_00033.raw", Paths
				.get("/NOMINAL/S1A/S1A_20200120185900030888/ch_1/DCS_01_S1A_20200120185900030888_ch1_DSDB_00033.raw"),
				URI.create(
						"ftp://pedc:21/NOMINAL/S1A/S1A_20200120185900030888/ch_1/DCS_01_S1A_20200120185900030888_ch1_DSDB_00033.raw"),
				Date.from(Instant.parse("2020-01-02T00:00:00Z")), 123);

		list.add(e1);

		when(edipClient.list(EdipEntryFilter.ALLOW_ALL)).thenReturn(list);

		final String stationName = null;
		final URI inboxURL = URI.create("ftp://pedc:21/NOMINAL");

		final EdipInboxAdapter uut = new EdipInboxAdapter(inboxURL, edipClient, inboxEntryFactory, stationName, ProductFamily.EDRS_SESSION);

		Assert.assertEquals(Paths
				.get("/NOMINAL/S1A/S1A_20200120185900030888/ch_1/DCS_01_S1A_20200120185900030888_ch1_DSDB_00033.raw"),
				uut.list().collect(Collectors.toList()).get(0).getPath());

	}

}
