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

package esa.s1pdgs.cpoc.preparation.worker.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import esa.s1pdgs.cpoc.common.MaskType;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

public class GeoIntersectionTest {

	private static final Logger LOG = LogManager.getLogger(GeoIntersectionTest.class);

	@Test
	public void getCoverage_fullSeaCoverage() throws Exception {

		List<Double> p1 = new ArrayList<>();
		p1.add(-31.9482421875);
		p1.add(42.00032514831621);

		List<Double> p2 = new ArrayList<>();
		p2.add(-23.37890625);
		p2.add(39.977120098439634);

		List<Double> p3 = new ArrayList<>();
		p3.add(-26.323242187499996);
		p3.add(44.49650533109345);

		List<Double> p4 = new ArrayList<>();
		p4.add(-31.9482421875);
		p4.add(42.00032514831621);

		GeoIntersection uut = new GeoIntersection(
				new File("src/test/resources/S1__OPER_MSK__LAND__V20140403T210200_G20200914T080808.EOF"),
				MaskType.LAND);

		uut.loadMaskFile();

		long coverage = uut.getCoverage(buildCatalogEvent(p1, p2, p3, p4));

		LOG.info("sea coverage {}", coverage);
		Assert.assertEquals(100, coverage);
	}

	@Test
	public void getCoverage_fullLandCoverage()
			throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

		List<Double> p1 = new ArrayList<>();
		p1.add(1.40625);
		p1.add(15.961329081596647);

		List<Double> p2 = new ArrayList<>();
		p2.add(13.18359375);
		p2.add(12.382928338487396);

		List<Double> p3 = new ArrayList<>();
		p3.add(12.12890625);
		p3.add(20.797201434307);

		List<Double> p4 = new ArrayList<>();
		p4.add(1.40625);
		p4.add(15.961329081596647);
		
		GeoIntersection uut = new GeoIntersection(
				new File("src/test/resources/S1__OPER_MSK__LAND__V20140403T210200_G20200914T080808.EOF"),
				MaskType.LAND);

		uut.loadMaskFile();

		long coverage = uut.getCoverage(buildCatalogEvent(p1, p2, p3, p4));

		LOG.info("sea coverage {}", coverage);

		Assert.assertEquals(0, coverage);
	}

	@Test
	public void getCoverage_partLandCoverage()
			throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

		List<Double> p1 = new ArrayList<>();
		p1.add(-11.22802734375);
		p1.add(41.12074559016745);

		List<Double> p2 = new ArrayList<>();
		p2.add(-12.183837890625);
		p2.add(38.64261790634527);

		List<Double> p3 = new ArrayList<>();
		p3.add(-7.745361328125);
		p3.add(39.85072092501597);

		List<Double> p4 = new ArrayList<>();
		p4.add(-11.22802734375);
		p4.add(41.12074559016745);
		
		GeoIntersection uut = new GeoIntersection(
				new File("src/test/resources/S1__OPER_MSK__LAND__V20140403T210200_G20200914T080808.EOF"),
				MaskType.LAND);

		uut.loadMaskFile();

		long coverage = uut.getCoverage(buildCatalogEvent(p1, p2, p3, p4));

		LOG.info("sea coverage {}", coverage);

		Assert.assertTrue(coverage > 80);
		Assert.assertTrue(coverage < 100);
	}

	@Test
	public void getCoverage_partLandCoverage2()
			throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

		List<Double> p1 = new ArrayList<>();
		p1.add(39.2431640625);
		p1.add(5.309766171943691);

		List<Double> p2 = new ArrayList<>();
		p2.add(46.40625);
		p2.add(0.21972602392080884);

		List<Double> p3 = new ArrayList<>();
		p3.add(52.3828125);
		p3.add(6.970049417296232);

		List<Double> p4 = new ArrayList<>();
		p4.add(39.2431640625);
		p4.add(5.309766171943691);
		
		GeoIntersection uut = new GeoIntersection(
				new File("src/test/resources/S1__OPER_MSK__LAND__V20140403T210200_G20200914T080808.EOF"),
				MaskType.LAND);

		uut.loadMaskFile();

		long coverage = uut.getCoverage(buildCatalogEvent(p1, p2, p3, p4));

		LOG.info("sea coverage {}", coverage);

		Assert.assertTrue(coverage > 0);
		Assert.assertTrue(coverage < 50);
	}
	
	
	@Test
	public void isIntersecting_false() throws Exception {

		List<Double> p1 = new ArrayList<>();
		p1.add(-31.9482421875);
		p1.add(42.00032514831621);

		List<Double> p2 = new ArrayList<>();
		p2.add(-23.37890625);
		p2.add(39.977120098439634);

		List<Double> p3 = new ArrayList<>();
		p3.add(-26.323242187499996);
		p3.add(44.49650533109345);

		List<Double> p4 = new ArrayList<>();
		p4.add(-31.9482421875);
		p4.add(42.00032514831621);

		GeoIntersection uut = new GeoIntersection(
				new File("src/test/resources/S1__OPER_MSK_EW_SLC_V20140427T000000_G20210108T170000.EOF"),
				MaskType.LAND);

		uut.loadMaskFile();

		boolean intersecting = uut.isIntersecting(buildCatalogEvent(p1, p2, p3, p4));
		
		LOG.info("intersecting EW SLC mask: {}", intersecting);

		Assert.assertEquals(false, intersecting);
	}
	
	@Test
	public void isIntersecting_true() throws Exception {

		List<Double> p1 = new ArrayList<>();
		p1.add(-43.2421875);
		p1.add(23.563987128451217);

		List<Double> p2 = new ArrayList<>();
		p2.add(-44.29687499999999);
		p2.add(7.36246686553575);

		List<Double> p3 = new ArrayList<>();
		p3.add(-31.9921875);
		p3.add(20.3034175184893);

		List<Double> p4 = new ArrayList<>();
		p4.add(-43.2421875);
		p4.add(23.563987128451217);

		GeoIntersection uut = new GeoIntersection(
				new File("src/test/resources/S1__OPER_MSK_EW_SLC_V20140427T000000_G20210108T170000.EOF"),
				MaskType.LAND);

		uut.loadMaskFile();

		boolean intersecting = uut.isIntersecting(buildCatalogEvent(p1, p2, p3, p4));
		
		LOG.info("intersecting EW SLC mask: {}", intersecting);

		Assert.assertEquals(true, intersecting);
	}
	
	@Test
	public void isIntersecting_true2() throws Exception {

		List<Double> p1 = new ArrayList<>();
		p1.add(-46.36230468749999);
		p1.add(5.572249801113887);

		List<Double> p2 = new ArrayList<>();
		p2.add(-37.79296874999999);
		p2.add(2.8991526985043006);

		List<Double> p3 = new ArrayList<>();
		p3.add(-40.737304687499986);
		p3.add(8.971897294082988);

		List<Double> p4 = new ArrayList<>();
		p4.add(-46.36230468749999);
		p4.add(5.572249801113887);

		GeoIntersection uut = new GeoIntersection(
				new File("src/test/resources/S1__OPER_MSK_EW_SLC_V20140427T000000_G20210108T170000.EOF"),
				MaskType.LAND);

		uut.loadMaskFile();

		boolean intersecting = uut.isIntersecting(buildCatalogEvent(p1, p2, p3, p4));
		
		LOG.info("intersecting EW SLC mask: {}", intersecting);

		Assert.assertEquals(true, intersecting);
	}

	private CatalogEvent buildCatalogEvent(List<Double> p1, List<Double> p2, List<Double> p3, List<Double> p4) {
		List<List<List<Double>>> coordinates = new ArrayList<>();
		coordinates.add(List.of(p1, p2, p3, p4));

		Map<String, Object> geometry = new HashMap<>();
		geometry.put("coordinates", coordinates);
		geometry.put("type", "Polygon");

		Map<String, Object> metadata = new HashMap<>();
		metadata.put("sliceCoordinates", geometry);

		CatalogEvent catalogEvent = new CatalogEvent();
		catalogEvent.setMetadata(metadata);
		return catalogEvent;
	}
}
