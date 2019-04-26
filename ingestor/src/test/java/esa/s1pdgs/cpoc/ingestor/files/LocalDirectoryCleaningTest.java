package esa.s1pdgs.cpoc.ingestor.files;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import esa.s1pdgs.cpoc.ingestor.files.LocalDirectoryCleaning;

/**
 * Test the class LocalDirectoryCleaning
 * @author Cyrielle
 *
 */
public class LocalDirectoryCleaningTest {

	private static final String CLEAN_DIR = "./test/cleaning";
	private static final String ROOT_DIR = "./test/cleaning/root";
	
	/**
	 * 
	 */
	private File cleaning;
	
	/**
	 * root/
	 */
	private File root;
	
	/**
	 * root/child/
	 */
	private File rootchild;
	
	/**
	 * root/file1
	 */
	private File file1;
	
	/**
	 * root/child/file2
	 */
	private File file2;
	
	/**
	 * root/child/file3
	 */
	private File file3;

	/**
	 * To check the raised custom exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	/**
	 * Initialization
	 * @throws IOException
	 */
	@Before
	public void init() throws IOException {
		cleaning = new File(CLEAN_DIR);
		root = new File(ROOT_DIR);
		rootchild = new File(ROOT_DIR + "/child");
		rootchild.mkdirs();
		file1 = new File(ROOT_DIR + "/file1.txt");
		file1.createNewFile();
		file2 = new File(ROOT_DIR + "/child/file2.txt");
		file2.createNewFile();
		file3 = new File(ROOT_DIR + "/child/file3.txt");
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
		if (rootchild.exists()) {
			rootchild.delete();
		}
		if (root.exists()) {
			root.delete();
		}
		if (cleaning.exists()) {
			cleaning.delete();
		}
	}

	/**
	 * Test the function of resursive deletion of empty directories
	 * @throws InterruptedException
	 */
	@Test
	public void testRecursiveDelete() throws InterruptedException {
		LocalDirectoryCleaning task = new LocalDirectoryCleaning("", 1, "", 1);
		task.recursiveEmptyDirectoriesDeletion(root, System.currentTimeMillis());
		assertTrue(rootchild.exists());

		file1.delete();
		task.recursiveEmptyDirectoriesDeletion(root, System.currentTimeMillis());
		assertTrue(rootchild.exists());
		
		file2.delete();
		file3.delete();
		task.recursiveEmptyDirectoriesDeletion(root, System.currentTimeMillis() - 5000);
		assertTrue(rootchild.exists());
		
		Thread.sleep(2000);
		task.recursiveEmptyDirectoriesDeletion(root, System.currentTimeMillis() - 2000);
		assertTrue(root.exists());
		assertFalse(rootchild.exists());
		
		task.recursiveEmptyDirectoriesDeletion(root, System.currentTimeMillis() - 2000);
		assertTrue(root.exists());
		assertFalse(rootchild.exists());
		
		Thread.sleep(2000);
		task.recursiveEmptyDirectoriesDeletion(root, System.currentTimeMillis() - 2000);
		assertFalse(root.exists());
	}

	/**
	 * Test the function of resursive deletion of empty directories
	 * @throws InterruptedException
	 */
	@Test
	public void testCleanDirectory() throws InterruptedException {
		LocalDirectoryCleaning task = new LocalDirectoryCleaning("", 1, "", 1);
		task.cleanDirectory(CLEAN_DIR, 0);
		assertTrue(rootchild.exists());

		file1.delete();
		task.cleanDirectory(CLEAN_DIR, 0);
		assertTrue(rootchild.exists());
		
		file2.delete();
		file3.delete();
		task.cleanDirectory(CLEAN_DIR, 5000);
		assertTrue(rootchild.exists());
		
		Thread.sleep(2000);
		task.cleanDirectory(CLEAN_DIR, 2000);
		assertTrue(root.exists());
		assertFalse(rootchild.exists());
		
		task.cleanDirectory(CLEAN_DIR, 2000);
		assertTrue(root.exists());
		assertFalse(rootchild.exists());
		
		Thread.sleep(2000);
		task.cleanDirectory(CLEAN_DIR, 2000);
		assertFalse(root.exists());
		assertTrue(cleaning.exists());
		
		task.cleanDirectory(CLEAN_DIR, 0);
		assertTrue(cleaning.exists());
	}
}
