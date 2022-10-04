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
	
	private GeoIntersection uut;
	
	public GeoIntersectionTest() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		uut = new GeoIntersection(
//				new File("src/test/resources/S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF"),
				new File("src/test/resources/S1__OPER_MSK__LAND__V20140403T210200_G20200914T080808.EOF"),
				MaskType.LAND);
		
		uut.loadMaskFile();
	}

	@Test
	public void getCoverage_fullSeaCoverage() {

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

		long coverage = uut.getCoverage(buildCatalogEvent(p1, p2, p3, p4));

		LOG.info("sea coverage {}", coverage);
		Assert.assertEquals(100, coverage);
	}

	
	@Test
	public void getCoverage_fullLandCoverage() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

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

		long coverage = uut.getCoverage(buildCatalogEvent(p1, p2, p3, p4));
		
		LOG.info("sea coverage {}", coverage);
		
		Assert.assertEquals(0, coverage);
	}
	
	@Test
	public void getCoverage_partLandCoverage() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

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

		long coverage = uut.getCoverage(buildCatalogEvent(p1, p2, p3, p4));
		
		LOG.info("sea coverage {}", coverage);
		
		Assert.assertTrue(coverage > 80);
		Assert.assertTrue(coverage < 100);
	}
	
	@Test
	public void getCoverage_partLandCoverage2() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

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

		long coverage = uut.getCoverage(buildCatalogEvent(p1, p2, p3, p4));

		LOG.info("sea coverage {}", coverage);
		
		Assert.assertTrue(coverage > 0);
		Assert.assertTrue(coverage < 50);
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
