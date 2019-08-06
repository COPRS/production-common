package esa.s1pdgs.cpoc.inbox.fs;

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
import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;
import esa.s1pdgs.cpoc.inbox.filter.InboxFilter;
import esa.s1pdgs.cpoc.inbox.fs.FilesystemInboxAdapter;
import esa.s1pdgs.cpoc.inbox.fs.FilesystemInboxEntryFactory;

public class TestFilesystemInboxAdapter {	
	private final FilesystemInboxEntryFactory factory = new FilesystemInboxEntryFactory();
	
	private File testDir;
	private FilesystemInboxAdapter uut;
	
	@Before
	public final void setUp() throws IOException {
		testDir = Files.createTempDirectory(TestFilesystemInboxAdapter.class.getSimpleName()).toFile();
		uut = new FilesystemInboxAdapter(testDir, factory);
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
		assertEquals(true, actual.contains(factory.newInboxEntry(product1.getPath())));
		assertEquals(true, actual.contains(factory.newInboxEntry(product2.getPath())));
		assertEquals(true, actual.contains(factory.newInboxEntry(product3.getPath())));
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
