package esa.s1pdgs.cpoc.ingestion.worker.inbox;

import static org.junit.Assert.assertEquals;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.ingestion.worker.config.IngestionWorkerServiceConfigurationProperties;

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
		final List<InboxAdapterEntry> in = uut.read(singleFile.toURI(), singleFile.getName());
		try {
			assertEquals(1, in.size());
			
			try (final InputStream is = in.get(0).inputStream()) {
				final List<String> lines = IOUtils.readLines(is, Charset.defaultCharset());
				assertEquals(1, lines.size());
				assertEquals(SINGLE_FILE_CONTENT, lines.get(0));
			}			
		}
		finally {
			for (final Closeable clsbl : in) {
				clsbl.close();
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
}
