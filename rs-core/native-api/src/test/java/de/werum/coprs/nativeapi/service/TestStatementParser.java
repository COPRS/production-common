package de.werum.coprs.nativeapi.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.werum.coprs.nativeapi.service.StatementParserServiceImpl;
import de.werum.coprs.nativeapi.service.StatementParserServiceImpl.StatementType;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TestStatementParser {

	private static Map<String, List<String>> configs = new HashMap<>();

	@Autowired
	private StatementParserServiceImpl parser;

	@BeforeAll
	public static void init() {
		configs.put("bbox={value}",List.of("OData.CSC.Intersects(location=Footprint,area=geography'SRID=4326;POLYGON(({value}))')"));
		configs.put("point={value}",List.of("OData.CSC.Intersects(location=Footprint,area=geography'SRID=4326;POINT({value})')"));
		configs.put("productname={value}",List.of("contains(Name,'{value}')"));
		configs.put("datetime={start}/{stop}", List.of("ContentDate/Start gt {start}","ContentDate/End lt {stop}"));
		// configs.put("cloudCoverage={min}/{max}","cloudcoverage > {min}# cloudcoverage
		// < {max}");
	}

	private String encodeUrl(String decoded) {
		try {
			return new URI(null, null, (decoded), null).toASCIIString();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Test
	public void testStatementTypes() {
		StatementType t1 = parser.determinateType("{value}");
		Assert.assertTrue(t1 == StatementType.SINGLE);

		StatementType t2 = parser.determinateType("{min}/{max}");
		Assert.assertTrue(t2 == StatementType.RANGE);

		StatementType t3 = parser.determinateType("[{value}]");
		// FIXME: Missing scenario
	}

	@Test
	public void testProductNameQuery() {
		Map<String, String> params = Map.of("productname",
				"S3A_OL_2_LFR____20221009T094701_20221009T095001_20221010T124227_0179_090_364_3060_PS1_O_NT_002.SEN3");
		parser.parseConfig(configs);

		String query = parser.buildOdataQuery(params);
		Assert.assertEquals(
				"contains(Name,'S3A_OL_2_LFR____20221009T094701_20221009T095001_20221010T124227_0179_090_364_3060_PS1_O_NT_002.SEN3')",
				query);
	}

	@Test
	public void testBBoxQuery() {
		Map<String, String> params = Map.of("bbox",
				encodeUrl("44.8571 20.3411, 11.4484 49.9204, 2.4321 32.3625, 13.4321 1.3625, 44.8571 20.3411"));
		parser.parseConfig(configs);

		String query = parser.buildOdataQuery(params);
		Assert.assertEquals(
				"OData.CSC.Intersects(location=Footprint,area=geography'SRID=4326;POLYGON((44.8571%2020.3411,%2011.4484%2049.9204,%202.4321%2032.3625,%2013.4321%201.3625,%2044.8571%2020.3411))')",
				query);
	}

	@Test
	public void testPointQuery() {
		Map<String, String> params = Map.of("point", encodeUrl("44.8571 20.3411"));
		parser.parseConfig(configs);

		String query = parser.buildOdataQuery(params);
		Assert.assertEquals(
				"OData.CSC.Intersects(location=Footprint,area=geography'SRID=4326;POINT(44.8571%2020.3411)')", query);
	}
	
	@Test
	public void testDatetime() {
		Map<String, String> params = Map.of("datetime", "2010-10-18T14:33:00.000Z/2023-02-06T14:33:00.000Z");
		parser.parseConfig(configs);
		
		String query = parser.buildOdataQuery(params);
		Assert.assertEquals(
				"ContentDate/Start gt 2010-10-18T14:33:00.000Z and ContentDate/End lt 2023-02-06T14:33:00.000Z", query);
	}

}
