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

package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.rfi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.metadata.extraction.config.RfiConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.RfiMitigationPerformed;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.xml.XmlConverter;

@Ignore
@RunWith(SpringRunner.class)
@EnableConfigurationProperties
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
		rfiFiles.add(new File("src/test/resources/rfi/rfi-s1a-ew-grd-hv-20200120t190115-20200120t190209-030888-038b84-002.xml"));
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
	public void calculateRfiNbPolarisationsDetected_0_2() throws MetadataExtractionException {
		
		List<File> rfiFiles = new ArrayList<>();
		
		rfiFiles.add(new File("src/test/resources/rfi/rfi-s1a-ew-grd-hh-20200120t190115-20200120t190209-030888-038b84-001.xml"));
		rfiFiles.add(new File("src/test/resources/rfi/rfi-s1a-ew-grd-hv-20200120t190115-20200120t190209-030888-038b84-002.xml"));
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
