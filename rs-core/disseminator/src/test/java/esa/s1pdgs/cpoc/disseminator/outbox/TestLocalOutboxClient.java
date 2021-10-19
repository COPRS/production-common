package esa.s1pdgs.cpoc.disseminator.outbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.disseminator.FakeObsClient;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.path.IsipPathEvaluator;
import esa.s1pdgs.cpoc.disseminator.path.PathEvaluator;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class TestLocalOutboxClient {
	private File testDir;
	
	@Before
	public final void setUp() throws IOException {
		testDir = Files.createTempDirectory("foo").toFile();
	}
	
	@After
	public final void tearDown() {
		FileUtils.delete(testDir.getPath());
	}
	
	@Test
	public final void testTransfer() throws Exception {
		final FakeObsClient fakeObsClient = new FakeObsClient() {			
			@Override
			public List<String> list(final ProductFamily family, final String keyPrefix) {
				return Collections.singletonList(keyPrefix);
			}

			@Override
			public InputStream getAsStream(ProductFamily family, String key) {
				return new ByteArrayInputStream("expected content".getBytes());
			}
		};
		final OutboxConfiguration config = new OutboxConfiguration();
		config.setPath(testDir.getPath());
		
		final LocalOutboxClient outbox = new LocalOutboxClient(fakeObsClient, config, PathEvaluator.NULL);
		outbox.transfer(new ObsObject(ProductFamily.BLANK, "foo.bar"), ReportingFactory.NULL);
		
		final File expected = new File(testDir, "foo.bar");
		assertTrue(expected.exists());
		assertEquals("expected content", FileUtils.readFile(expected));
	}
	
	@Test
	public final void testTransfer_ISIP() throws Exception {
		final FakeObsClient fakeObsClient = new FakeObsClient() {
			@Override
			public List<String> list(final ProductFamily family, final String keyPrefix) {
				return Collections.singletonList(keyPrefix);
			}

			@Override
			public InputStream getAsStream(ProductFamily family, String key) {
				return new ByteArrayInputStream("expected content".getBytes());
			}
		};
		final OutboxConfiguration config = new OutboxConfiguration();
		config.setPath(testDir.getPath());
		
		final LocalOutboxClient outbox = new LocalOutboxClient(fakeObsClient, config, new IsipPathEvaluator());
		outbox.transfer(new ObsObject(ProductFamily.BLANK, "S1A_AUX_CAL_V20171017T080000_G20180622T082918.SAFE"), ReportingFactory.NULL);
		
		final File expected = new File(testDir, "S1A_AUX_CAL_V20171017T080000_G20180622T082918.ISIP/S1A_AUX_CAL_V20171017T080000_G20180622T082918.SAFE");
		assertTrue(expected.exists());
		assertEquals("expected content", FileUtils.readFile(expected));
	}
	

}
