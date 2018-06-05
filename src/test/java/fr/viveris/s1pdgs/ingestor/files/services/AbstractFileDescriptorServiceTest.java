package fr.viveris.s1pdgs.ingestor.files.services;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fr.viveris.s1pdgs.ingestor.FileUtils;
import fr.viveris.s1pdgs.ingestor.exceptions.FilePathException;
import fr.viveris.s1pdgs.ingestor.exceptions.IgnoredFileException;
import fr.viveris.s1pdgs.ingestor.files.model.FileDescriptor;

/**
 * Test the service around FileDescriptor
 * 
 * @author Cyrielle Gailliard
 *
 */
public class AbstractFileDescriptorServiceTest {

	public static final String TEST_DIR_PATH = FileUtils.TEST_DIR_PATH;

	/**
	 * Root directory for tests
	 */
	private File rootDir = new File(TEST_DIR_PATH + "/file_descriptor");

	/**
	 * Root directory for tests
	 */
	private File child1Dir = new File(TEST_DIR_PATH + "/file_descriptor/child1");

	/**
	 * File
	 */
	private File file1 = new File(TEST_DIR_PATH + "/file_descriptor/child1/file1.txt");

	/**
	 * File
	 */
	private File file2 = new File(TEST_DIR_PATH + "/file_descriptor/file2.txt");

	/**
	 * File
	 */
	private File file3 = new File(TEST_DIR_PATH + "/file3.txt");

	/**
	 * To check the raised custom exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Initialization
	 * 
	 * @throws IOException
	 */
	@Before
	public void init() throws IOException {
		child1Dir.mkdirs();
		file1.createNewFile();
		file2.createNewFile();
		file3.createNewFile();
	}

	/**
	 * Cleaning
	 */
	@After
	public void clean() {
		if (file1.exists()) {
			file1.delete();
		}
		if (file2.exists()) {
			file2.delete();
		}
		if (file3.exists()) {
			file3.delete();
		}
		if (child1Dir.exists()) {
			child1Dir.delete();
		}
		if (rootDir.exists()) {
			rootDir.delete();
		}
	}

	/**
	 * Test getters and constructor
	 */
	@Test
	public void testGettersConstrucotrs() {
		FileDescriptorServiceImpl service = new FileDescriptorServiceImpl("dir", "fam");
		assertEquals("dir", service.getDirectory());
		assertEquals("fam", service.getFamily());
	}

	/**
	 * Test the extraction when the given file is in root directory but is a
	 * directory
	 * 
	 * @throws FilePathException
	 * @throws IgnoredFileException
	 */
	@Test
	public void testExtractDescriptorDirectory() throws FilePathException, IgnoredFileException {
		FileDescriptorServiceImpl service = new FileDescriptorServiceImpl(FileUtils.TEST_DIR_ABS_PATH_SEP, "fam");

		thrown.expect(IgnoredFileException.class);
		thrown.expect(hasProperty("productName", is("file_descriptor/child1")));
		thrown.expectMessage(containsString("child1"));
		service.extractDescriptor(child1Dir);
	}

	/**
	 * Test the extraction of the relative path when file OK
	 * 
	 * @throws FilePathException
	 * @throws IgnoredFileException
	 */
	@Test
	public void testExtractDescriptorFileOk() throws FilePathException, IgnoredFileException {
		FileDescriptorServiceImpl service = new FileDescriptorServiceImpl(FileUtils.TEST_DIR_ABS_PATH_SEP, "fam");

		FileDescriptor desc1 = service.extractDescriptor(file1);
		assertEquals("file_descriptor/child1/file1.txt", desc1.getRelativePath());

		FileDescriptor desc2 = service.extractDescriptor(file2);
		assertEquals("file_descriptor/file2.txt", desc2.getRelativePath());

		FileDescriptor desc3 = service.extractDescriptor(file3);
		assertEquals("file3.txt", desc3.getRelativePath());
	}

	/**
	 * Test the extraction when the given file is not in the root directory
	 * 
	 * @throws FilePathException
	 * @throws IgnoredFileException
	 */
	@Test
	public void testExtractDescriptorFileNotInDirectory() throws FilePathException, IgnoredFileException {
		FileDescriptorServiceImpl service = new FileDescriptorServiceImpl(FileUtils.TEST_DIR_ABS_PATH_SEP, "fam");

		thrown.expect(FilePathException.class);
		thrown.expect(hasProperty("productName", is("pom.xml")));
		thrown.expectMessage(containsString("not in root directory"));
		service.extractDescriptor(new File("pom.xml"));
	}
}

/**
 * Implementation of FileDescriptorService for tests
 */
class FileDescriptorServiceImpl extends AbstractFileDescriptorService {

	/**
	 * Constructor
	 * 
	 * @param directory
	 * @param family
	 */
	public FileDescriptorServiceImpl(String directory, String family) {
		super(directory, family);
	}

	/**
	 * 
	 */
	@Override
	protected FileDescriptor buildDescriptor(String relativePath) throws FilePathException {
		FileDescriptor ret = new FileDescriptor();
		ret.setRelativePath(relativePath);
		ret.setProductName(relativePath);
		return ret;
	}

}