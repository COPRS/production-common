package esa.s1pdgs.cpoc.disseminator.outbox;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.disseminator.FakeObsClient;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.path.IsipPathEvaluater;
import esa.s1pdgs.cpoc.disseminator.path.PathEvaluater;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public class TestLocalOutboxClient {
	private File testDir;
	
	@Before
	public final void setUp() throws IOException {
		testDir = Files.createTempDirectory("foo").toFile();
	}
	
	@After
	public final void tearDown() throws IOException {
		FileUtils.delete(testDir.getPath());
	}
	
	@Test
	public final void testTransfer() throws Exception {
		final FakeObsClient fakeObsClient = new FakeObsClient() {
			@Override
			public File downloadFile(ProductFamily family, String key, String targetDir) {
				final File file = new File(targetDir, key);
				try {
					FileUtils.writeFile(file, "expected content");
				} catch (InternalErrorException e) {
					throw new RuntimeException("foo bar");
				}
				return file;
			}			
		};
		final OutboxConfiguration config = new OutboxConfiguration();
		config.setPath(testDir.getPath());
		
		final LocalOutboxClient outbox = new LocalOutboxClient(fakeObsClient, config, PathEvaluater.NULL);
		outbox.transfer(new ObsObject("foo.bar", ProductFamily.BLANK));
		
		final File expected = new File(testDir, "foo.bar");
		assertEquals(true, expected.exists());
		
		assertEquals("expected content", FileUtils.readFile(expected));		
	}
	
	@Test
	public final void testTransfer_ISIP() throws Exception {
		final FakeObsClient fakeObsClient = new FakeObsClient() {
			@Override
			public File downloadFile(ProductFamily family, String key, String targetDir) {
				final File file = new File(targetDir, key);
				try {
					FileUtils.writeFile(file, "expected content");
				} catch (InternalErrorException e) {
					e.printStackTrace();
					throw new RuntimeException("foo bar");
				}
				return file;
			}			
		};
		final OutboxConfiguration config = new OutboxConfiguration();
		config.setPath(testDir.getPath());
		
		final LocalOutboxClient outbox = new LocalOutboxClient(fakeObsClient, config, new IsipPathEvaluater());
		outbox.transfer(new ObsObject("S1A_AUX_CAL_V20171017T080000_G20180622T082918.SAFE", ProductFamily.BLANK));
		
		final File expected = new File(testDir, "S1A_AUX_CAL_V20171017T080000_G20180622T082918.ISIP/S1A_AUX_CAL_V20171017T080000_G20180622T082918.SAFE");
		assertEquals(true, expected.exists());
		
		assertEquals("expected content", FileUtils.readFile(expected));		
	}
	

}
