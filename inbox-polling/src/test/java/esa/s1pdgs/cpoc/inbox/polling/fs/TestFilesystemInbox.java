package esa.s1pdgs.cpoc.inbox.polling.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.inbox.polling.InboxEntry;
import esa.s1pdgs.cpoc.inbox.polling.filter.InboxFilter;
import esa.s1pdgs.cpoc.inbox.polling.fs.FilesystemInbox;
import esa.s1pdgs.cpoc.inbox.polling.fs.FilesystemInboxEntry;

public class TestFilesystemInbox {
	
	private File testDir;
	private FilesystemInbox uut;
	
	@Before
	public final void setUp() throws IOException {
		testDir = Files.createTempDirectory(TestFilesystemInbox.class.getSimpleName()).toFile();
		uut = new FilesystemInbox(testDir);
	}
	
	@After
	public final void tearDown() throws IOException {
		FileUtils.delete(testDir.getPath());		
	}
	
	@Test
	public final void testRead_OnEmptyDirectory_ShallReturnNoElements()
	{
		assertEquals(0, uut.read(InboxFilter.ALLOW_ALL).size());		
	}
	
	@Test
	public final void testRead_NoFilterDefined_ShallReturnAllElements() throws IOException
	{
		// create some content in test directory
		final File product1 = new File(testDir, "foo");
		assertTrue(product1.createNewFile());
		final File product2 = new File(testDir, "bar");
		assertTrue(product2.createNewFile());
		final File product3 = new File(testDir, "fooBarDirectory");
		assertTrue(product3.mkdir());
		
		final Collection<InboxEntry> actual = uut.read(InboxFilter.ALLOW_ALL);
		assertEquals(true, actual.contains(new FilesystemInboxEntry(product1)));
		assertEquals(true, actual.contains(new FilesystemInboxEntry(product2)));
		assertEquals(true, actual.contains(new FilesystemInboxEntry(product3)));
	}
	
	@Test
	public final void testRead_FilterAllElements_ShallReturnNoElements() throws IOException
	{
		// create some content in test directory
		final File product1 = new File(testDir, "foo");
		assertTrue(product1.createNewFile());
		final File product2 = new File(testDir, "bar");
		assertTrue(product2.createNewFile());
		final File product3 = new File(testDir, "fooBarDirectory");
		assertTrue(product3.mkdir());
		
		final Collection<InboxEntry> actual = uut.read(InboxFilter.ALLOW_NONE);
		assertEquals(0, actual.size());
	}
}
