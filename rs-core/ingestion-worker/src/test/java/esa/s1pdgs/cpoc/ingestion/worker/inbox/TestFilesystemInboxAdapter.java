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

package esa.s1pdgs.cpoc.ingestion.worker.inbox;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.ingestion.worker.config.IngestionWorkerServiceConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.worker.product.IngestionJobs;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

public class TestFilesystemInboxAdapter {
	private static final String SINGLE_FILE_CONTENT = "Hello123";
	
	private final File tmpDir = FileUtils.createTmpDir();	
	private final File singleFile = new File(tmpDir, "singleFile.txt");
	
	private final FilesystemInboxAdapter uut = new FilesystemInboxAdapter(new IngestionWorkerServiceConfigurationProperties());
		
	@Before
	public final void setUp() throws Exception {
		FileUtils.writeFile(singleFile, SINGLE_FILE_CONTENT);
	}
	
	@After
	public final void tearDown() throws Exception {
		FileUtils.delete(tmpDir.getPath());
	}	
	
	@Test
	public final void testRead_OnSingleNonDirectoryFile_ShallReturnOneStream() throws Exception {
		try (final InboxAdapterResponse response = uut.read(singleFile.toURI(), singleFile.getName(), "", 123L)) {
			final List<InboxAdapterEntry> in = response.getResult();
			assertEquals(1, in.size());
			
			try (final InputStream is = in.get(0).inputStream()) {
				final List<String> lines = IOUtils.readLines(is, Charset.defaultCharset());
				assertEquals(1, lines.size());
				assertEquals(SINGLE_FILE_CONTENT, lines.get(0));
			}
		}
	}

	@Test
	public final void testDelete_OnSingleNonDirectoryFile_ShallDeleteFile() throws Exception {
		assertEquals(true, singleFile.exists());
		uut.delete(singleFile.toURI());
		assertEquals(false, singleFile.exists());
		assertEquals(true, tmpDir.exists());		
	}
	
	@Test(expected = RuntimeException.class)
	public final void testToInputStream_OnNonExistingFile_ShallThrowException() throws Exception {
		FilesystemInboxAdapter.toInputStream(new File("/tmp/totally/Non/Existing/file"));
	}
	
	/*
	 * 
	 * productFamily=EDRS_SESSION, 
	 * keyObjectStorage=S1B__MPS__________017080/ch01/DCS_95_S1B__MPS__________017080_ch1_DSIB.xml, 
	 * creationDate=Tue Apr 07 11:25:57 GMT 2020, hostname=s1pro-ingestion-0, 
	 * relativePath=MPS_/S1B/S1B__MPS__________017080/ch01/DCS_95_S1B__MPS__________017080_ch1_DSIB.xml, 
	 * pickupBaseURL=file:///data/inbox, 
	 * productName=S1B__MPS__________017080/ch01/DCS_95_S1B__MPS__________017080_ch1_DSIB.xml, 
	 * uid=c7fe6f46-ab9f-4265-a953-1a35712e014d, 
	 * productSizeByte=592, 
	 * stationName=MPS_]} 
	 * from MQI 

	 * 
	 */
	
	
	@Test
	public final void testRead_OnSingleFile_ShallReturnCorrectObsKey() throws Exception {				
		final File inbox = new File(tmpDir, "inbox");
		final File prod = new File(inbox, "MPS_/S1B/S1B__MPS__________017080/ch01");
		prod.mkdirs();		
		final File dsib = new File(prod, "DCS_95_S1B__MPS__________017080_ch1_DSIB.xml");
		FileUtils.writeFile(dsib, "foo");
		
		final IngestionJob job = new IngestionJob(
				ProductFamily.EDRS_SESSION, 
				"S1B__MPS__________017080/ch01/DCS_95_S1B__MPS__________017080_ch1_DSIB.xml", 
				"file://" + inbox.getPath(), 
				"MPS_/S1B/S1B__MPS__________017080/ch01/DCS_95_S1B__MPS__________017080_ch1_DSIB.xml", 
				42L,
				null,
				UUID.randomUUID(),
				"S1",
				"MPS_",
				"NOMINAL",
                "FAST24",
				"file",
				Collections.emptyMap(),
				""
		);		
		final URI uri = IngestionJobs.toUri(job);
		
		try (final InboxAdapterResponse response = uut.read(uri, job.getProductName(), "", 123L)) {
			final List<InboxAdapterEntry> entries = response.getResult();
			assertEquals(1, entries.size());
			final InboxAdapterEntry entry = entries.get(0);
			assertEquals("S1B__MPS__________017080/ch01/DCS_95_S1B__MPS__________017080_ch1_DSIB.xml", entry.key());
		}
	}
	
	@Test
	public final void testRead_OnDirectory_ShallReturnCorrectObsKeyForEachElement() throws Exception {				
		final File inbox = new File(tmpDir, "inbox/AUX");
		final File prod = new File(inbox, "S1__AUX_ICE_V20180930T120000_G20181001T042911.SAFE/data");
		prod.mkdirs();		
		final File mnf = new File(prod.getParentFile(), "manifest.safe");
		FileUtils.writeFile(mnf, "foo");		
		final File d1 = new File(prod, "ice_edge_sh_polstere-100_multi_201809301200.nc");
		FileUtils.writeFile(d1, "bar");		
		final File dat = new File(prod, "ice_edge_nh_polstere-100_multi_201809301200.nc");
		FileUtils.writeFile(dat, "baz");
		
		final File toBeIgnored = new File(inbox, "ignoreMe");
		FileUtils.writeFile(toBeIgnored, "baz");
		
		final IngestionJob job = new IngestionJob(
				ProductFamily.EDRS_SESSION, 
				"S1__AUX_ICE_V20180930T120000_G20181001T042911.SAFE", 
				"file://" + inbox.getPath(), 
				"S1__AUX_ICE_V20180930T120000_G20181001T042911.SAFE", 
				42L,
				null,
				UUID.randomUUID(),
				"S1",
				"MPS_",
				"NOMINAL",
                "FAST24",
				"file",
				Collections.emptyMap(),
				""
		);		
		final URI uri = IngestionJobs.toUri(job);
		
		try (final InboxAdapterResponse response = uut.read(uri, job.getProductName(), "", 123L)) {
			final List<InboxAdapterEntry> entries = response.getResult();
			entries.stream().forEach(e -> System.out.println(e));
		}
		
//		assertEquals(1, entries.size());		
//		final InboxAdapterEntry entry = entries.get(0);
//		assertEquals("S1B__MPS__________017080/ch01/DCS_95_S1B__MPS__________017080_ch1_DSIB.xml", entry.key());
	}
}
