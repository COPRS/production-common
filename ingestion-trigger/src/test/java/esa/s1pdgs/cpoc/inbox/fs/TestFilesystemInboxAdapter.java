package esa.s1pdgs.cpoc.inbox.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.inbox.config.InboxPathInformation;
import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;
import esa.s1pdgs.cpoc.inbox.filter.InboxFilter;

public class TestFilesystemInboxAdapter {
	private final FilesystemInboxEntryFactory factory = new FilesystemInboxEntryFactory();

	private File testDir;
	private FilesystemInboxAdapter uut;

	@Before
	public final void setUp() throws IOException {
		testDir = Files.createDirectories(Paths.get("target/MPS_/S1A")).toFile();
		uut = new FilesystemInboxAdapter(testDir, factory);
	}

	@After
	public final void tearDown() throws IOException {
		FileUtils.delete(testDir.getPath());
	}

	@Test
	public final void testRead_OnEmptyDirectory_ShallReturnNoElements() {
		assertEquals(0, uut.read(Lists.list(InboxFilter.ALLOW_ALL)).size());
	}

	@Test
	public final void testRead_NoFilterDefined_ShallReturnAllElements() throws IOException {
		// create some content in test directory
		final File product1 = new File(testDir, "foo");
		assertTrue(product1.createNewFile());
		final File product2 = new File(testDir, "bar");
		assertTrue(product2.createNewFile());
		final File product3 = new File(testDir, "fooBarDirectory");
		assertTrue(product3.mkdir());

		final Collection<InboxEntry> actual = uut.read(Lists.list(InboxFilter.ALLOW_ALL));

		InboxPathInformation pathInformation = new InboxPathInformation();
		pathInformation.setMissionId("S1");
		pathInformation.setSatelliteId("A");
		pathInformation.setStationCode("MPS_");

		assertEquals(true,
				actual.contains(factory.newInboxEntry(pathInformation, testDir.toPath().relativize(product1.toPath()), testDir.toPath())));
		assertEquals(true,
				actual.contains(factory.newInboxEntry(pathInformation, testDir.toPath().relativize(product2.toPath()), testDir.toPath())));
		assertEquals(true,
				actual.contains(factory.newInboxEntry(pathInformation, testDir.toPath().relativize(product3.toPath()), testDir.toPath())));
	}

	@Test
	public final void testRead_FilterAllElements_ShallReturnNoElements() throws IOException {
		// create some content in test directory
		final File product1 = new File(testDir, "foo");
		assertTrue(product1.createNewFile());
		final File product2 = new File(testDir, "bar");
		assertTrue(product2.createNewFile());
		final File product3 = new File(testDir, "fooBarDirectory");
		assertTrue(product3.mkdir());

		final Collection<InboxEntry> actual = uut.read(Lists.list(InboxFilter.ALLOW_NONE));
		assertEquals(0, actual.size());
	}
}
