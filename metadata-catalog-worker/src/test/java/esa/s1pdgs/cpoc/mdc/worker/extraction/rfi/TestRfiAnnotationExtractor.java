package esa.s1pdgs.cpoc.mdc.worker.extraction.rfi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.mdc.worker.config.RfiConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.RfiMitigationPerformed;
import esa.s1pdgs.cpoc.mdc.worker.extraction.xml.XmlConverter;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class TestRfiAnnotationExtractor {
	
	@Autowired
	private XmlConverter xmlConverter;
	
	private RfiAnnotationExtractor uut;
	
	@Before
	public void init() {
		uut = new RfiAnnotationExtractor(null, new RfiConfiguration(), null, xmlConverter);
	}
	
	@Test
	public void calculateRfiNbPolarisationsDetected_0() throws MetadataExtractionException {
		
		List<File> rfiFiles = new ArrayList<>();
		
		rfiFiles.add(new File("src/test/resources/rfi/rfi-s1a-ew-grd-hh-20200120t190115-20200120t190209-030888-038b84-001.xml"));
		rfiFiles.add(new File("src/test/resources/rfi/rfi-s1a-ew-grd-hv-20200120t190115-20200120t190209-030888-038b84-002.xml"));
		
		Assert.assertEquals(0, uut.calculateRfiNbPolarisationsDetected(rfiFiles));
		
	}
	
	@Test
	public void calculateRfiNbPolarisationsDetected_1_HH() throws MetadataExtractionException {
		
		List<File> rfiFiles = new ArrayList<>();
		
		rfiFiles.add(new File("src/test/resources/rfi/rfi-s1a-ew-grd-hh-20200120t190115-20200120t190209-030888-038b84-003.xml"));
		rfiFiles.add(new File("src/test/resources/rfi/rfi-s1a-ew-grd-hv-20200120t190115-20200120t190209-030888-038b84-002.xml"));
		
		Assert.assertEquals(1, uut.calculateRfiNbPolarisationsDetected(rfiFiles));
		
	}
	
	@Test
	public void calculateRfiNbPolarisationsDetected_1_HV() throws MetadataExtractionException {
		
		List<File> rfiFiles = new ArrayList<>();
		
		rfiFiles.add(new File("src/test/resources/rfi/rfi-s1a-ew-grd-hh-20200120t190115-20200120t190209-030888-038b84-001.xml"));
		rfiFiles.add(new File("src/test/resources/rfi/rfi-s1a-ew-grd-hv-20200120t190115-20200120t190209-030888-038b84-004.xml"));
		
		Assert.assertEquals(1, uut.calculateRfiNbPolarisationsDetected(rfiFiles));
		
	}
	
	@Test
	public void calculateRfiNbPolarisationsDetected_2() throws MetadataExtractionException {
		
		List<File> rfiFiles = new ArrayList<>();
		
		rfiFiles.add(new File("src/test/resources/rfi/rfi-s1a-ew-grd-hh-20200120t190115-20200120t190209-030888-038b84-003.xml"));
		rfiFiles.add(new File("src/test/resources/rfi/rfi-s1a-ew-grd-hv-20200120t190115-20200120t190209-030888-038b84-004.xml"));
		
		Assert.assertEquals(2, uut.calculateRfiNbPolarisationsDetected(rfiFiles));
		
	}
	
	@Test
	public void getRfiMitigationPerformedFromAnnotationFile() throws MetadataExtractionException {
		List<File> annotationFiles = new ArrayList<>();
		annotationFiles.add(new File("src/test/resources/rfi/s1a-ew-grd-hh-20200120t190115-20200120t190209-030888-038b84-001.xml"));
		
		Assert.assertEquals(RfiMitigationPerformed.NEVER, uut.getRfiMitigationPerformedFromAnnotationFile(annotationFiles));
	}

}
