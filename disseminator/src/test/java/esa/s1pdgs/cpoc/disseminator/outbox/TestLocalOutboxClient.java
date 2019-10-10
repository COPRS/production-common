package esa.s1pdgs.cpoc.disseminator.outbox;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.disseminator.FakeObsClient;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.path.IsipPathEvaluater;
import esa.s1pdgs.cpoc.disseminator.path.PathEvaluater;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

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
			public Map<String, InputStream> getAllAsInputStream(ProductFamily family, String keyPrefix)
					throws SdkClientException {
				return Collections.singletonMap(keyPrefix, new ByteArrayInputStream("expected content".getBytes()));
			}		
		};
		final OutboxConfiguration config = new OutboxConfiguration();
		config.setPath(testDir.getPath());
		
		final LocalOutboxClient outbox = new LocalOutboxClient(fakeObsClient, config, PathEvaluater.NULL);
		outbox.transfer(new ObsObject(ProductFamily.BLANK, "foo.bar"));
		
		final File expected = new File(testDir, "foo.bar");
		assertEquals(true, expected.exists());
		
		assertEquals("expected content", FileUtils.readFile(expected));		
	}
	
	@Test
	public final void testTransfer_ISIP() throws Exception {
		final FakeObsClient fakeObsClient = new FakeObsClient() {
			@Override
			public Map<String, InputStream> getAllAsInputStream(ProductFamily family, String keyPrefix)
					throws SdkClientException {
				return Collections.singletonMap(keyPrefix, new ByteArrayInputStream("expected content".getBytes()));
			}			
		};
		final OutboxConfiguration config = new OutboxConfiguration();
		config.setPath(testDir.getPath());
		
		final LocalOutboxClient outbox = new LocalOutboxClient(fakeObsClient, config, new IsipPathEvaluater());
		outbox.transfer(new ObsObject(ProductFamily.BLANK, "S1A_AUX_CAL_V20171017T080000_G20180622T082918.SAFE"));
		
		final File expected = new File(testDir, "S1A_AUX_CAL_V20171017T080000_G20180622T082918.ISIP/S1A_AUX_CAL_V20171017T080000_G20180622T082918.SAFE");
		assertEquals(true, expected.exists());
		
		assertEquals("expected content", FileUtils.readFile(expected));		
	}
	

}
