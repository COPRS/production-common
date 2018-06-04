package fr.viveris.s1pdgs.ingestor.files.model.filter;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import fr.viveris.s1pdgs.ingestor.files.model.filter.ExclusionRegexpPatternFileListFilter;

public class ExclusionRegexpPatternFileListFilterTest {
	
	private File fileWriting;
	private File fileMpeg;
	private File fileHidden;
	private File fileOk;
	
	@Before
	public void init() {
		fileWriting = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml.writing");
		fileMpeg = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.mpeg");
		fileHidden = new File("workDir/.S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
		fileOk = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
	}

	@Test
	public void testMatchingRegexWriting() {
		String pattern = "^.*\\.writing$";
		ExclusionRegexpPatternFileListFilter filter = new ExclusionRegexpPatternFileListFilter(
				Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
		File fileWritingOk1 = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml.writing.raw");
		File fileWritingOk2 = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xmlwriting");
		File fileWritingOk3 = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xmlwriting.");
		
		assertTrue("File writing shall be filtered", !filter.accept(fileWriting));
		assertTrue("File writing shall not be filtered", filter.accept(fileMpeg));
		assertTrue("File hidden shall not be filtered", filter.accept(fileHidden));
		assertTrue("File ok shall not be filtered", filter.accept(fileOk));
		assertTrue("File 1 shall not be filtered", filter.accept(fileWritingOk1));
		assertTrue("File 2 shall not be filtered", filter.accept(fileWritingOk2));
		assertTrue("File 3 shall not be filtered", filter.accept(fileWritingOk3));
	}

	@Test
	public void testMatchingRegexHidden() {
		String pattern = "^.*\\.writing$";
		ExclusionRegexpPatternFileListFilter filter = new ExclusionRegexpPatternFileListFilter(
				Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
		File fileWritingOk1 = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml.writing.raw");
		File fileWritingOk2 = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xmlwriting");
		File fileWritingOk3 = new File("workDir/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xmlwriting.");
		
		assertTrue("File writing shall be filtered", !filter.accept(fileWriting));
		assertTrue("File writing shall not be filtered", filter.accept(fileMpeg));
		assertTrue("File hidden shall not be filtered", filter.accept(fileHidden));
		assertTrue("File ok shall not be filtered", filter.accept(fileOk));
		assertTrue("File 1 shall not be filtered", filter.accept(fileWritingOk1));
		assertTrue("File 2 shall not be filtered", filter.accept(fileWritingOk2));
		assertTrue("File 3 shall not be filtered", filter.accept(fileWritingOk3));
	}
}
