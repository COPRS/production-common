package esa.s1pdgs.cpoc.ingestor.files.model.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.ingestor.files.model.filter.ExclusionRegexpPatternFileListFilter;

/**
 * Test the class ExclusionRegexpPatternFileListFilter
 * 
 * @author Cyrielle Gailliard
 *
 */
public class ExclusionRegexpPatternFileListFilterTest {
	
	private static final String PATTERN = "^.*\\.writing$";
	private File fileWriting;
	private File fileMpeg;
	private File fileHidden;
	private File fileOk;
	private ExclusionRegexpPatternFileListFilter filter;
	
	@Before
	public void init() throws IOException {
		fileWriting = new File("build/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml.writing");
		fileMpeg = new File("build/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.mpeg");
		fileHidden = new File("build/.S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		fileOk = new File("build/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		
		fileWriting.createNewFile();
		fileMpeg.createNewFile();
		fileHidden.createNewFile();
		fileOk.createNewFile();
		
		filter = new ExclusionRegexpPatternFileListFilter(
				Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE));
		filter.setAlwaysAcceptDirectories(true);
	}
	
	@After
	public void clean() {
		fileWriting.delete();
		fileMpeg.delete();
		fileHidden.delete();
		fileOk.delete();
	}

	@Test
	public void testAccept() {
		File fileWritingOk1 = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml.writing.raw");
		File fileWritingOk2 = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xmlwriting");
		File fileWritingOk3 = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xmlwriting.");
		
		assertFalse("File writing shall be filtered", filter.accept(fileWriting));
		assertTrue("File writing shall not be filtered", filter.accept(fileMpeg));
		assertTrue("File hidden shall not be filtered", filter.accept(fileHidden));
		assertTrue("File ok shall not be filtered", filter.accept(fileOk));
		assertTrue("File 1 shall not be filtered", filter.accept(fileWritingOk1));
		assertTrue("File 2 shall not be filtered", filter.accept(fileWritingOk2));
		assertTrue("File 3 shall not be filtered", filter.accept(fileWritingOk3));

		ExclusionRegexpPatternFileListFilter filter2 = new ExclusionRegexpPatternFileListFilter(
				Pattern.compile("^build$", Pattern.CASE_INSENSITIVE));
		
		File file = new File("build/log4j2-test.yml");
		File dir = new File("build");
		filter2.setAlwaysAcceptDirectories(true);
		assertTrue(filter2.accept(file));
		assertTrue(filter2.accept(dir));
		assertFalse(filter2.accept(null));

		filter2.setAlwaysAcceptDirectories(false);
		assertTrue(filter2.accept(file));
		assertFalse(filter2.accept(dir));
		assertFalse(filter2.accept(null));
	}
	
	@Test
	public void testGetFilename() {
		File file = new File("build/log4j2-test.yml");
		File dir = new File("build");
		assertEquals("log4j2-test.yml", filter.getFilename(file));
		assertEquals("build", filter.getFilename(dir));
		assertNull(filter.getFilename(null));
	}
	
	@Test
	public void testIsDirectory() {
		File file = new File("build/log4j2-test.yml");
		File dir = new File("build");
		assertFalse(filter.isDirectory(file));
		assertTrue(filter.isDirectory(dir));
	}
}
