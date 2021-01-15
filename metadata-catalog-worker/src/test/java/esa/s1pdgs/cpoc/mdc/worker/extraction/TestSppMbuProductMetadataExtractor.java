package esa.s1pdgs.cpoc.mdc.worker.extraction;

import java.util.regex.Pattern;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;

public class TestSppMbuProductMetadataExtractor {

	@Test
	public void putMetadataToJSON() throws MetadataExtractionException {

		Pattern pattern = Pattern.compile(
				"^(s1)(a|b)-([0-9a-z]{2})([0-9a-z]{1})-(mbu)-()()(vv|hh)-([0-9a-z]{15})-([0-9a-z]{15})-([0-9]{6})-([a-z0-9]{6})-([0-9]{3})_([A-Z0-9]{4})\\.(bufr)$",
				Pattern.CASE_INSENSITIVE);
		String file1 = "s1a-wv1-mbu-vv-20190628t190957-20190628t191000-016900-01fce3-001_C26C.bufr";
		String file2 = "s1b-wv2-mbu-hh-20190628t191011-20190628t191014-016900-01fce3-002_C26C.bufr";

		JSONObject file1json = SppMbuProductMetadataExtractor.putMetadataToJSON(file1, pattern);
		JSONObject file2json = SppMbuProductMetadataExtractor.putMetadataToJSON(file2, pattern);
		
		Assert.assertEquals("s1a-wv1-mbu-vv-20190628t190957-20190628t191000-016900-01fce3-001_C26C.bufr",
				file1json.get("productName"));
		Assert.assertEquals("S1", file1json.get("missionId"));
		Assert.assertEquals("A", file1json.get("satelliteId"));
		Assert.assertEquals("WV", file1json.get("mode"));
		Assert.assertEquals("1", file1json.get("columnId"));
		Assert.assertEquals("REP_MBU_", file1json.get("productType"));
		Assert.assertEquals("VV", file1json.get("polarisation"));
		Assert.assertEquals("2019-06-28T19:09:57.000000Z", file1json.get("startTime"));
		Assert.assertEquals("2019-06-28T19:10:00.000000Z", file1json.get("stopTime"));
		Assert.assertEquals("016900", file1json.get("absoluteOrbitNumber"));
		Assert.assertEquals("01FCE3", file1json.get("missionDataTakeId"));
		
		Assert.assertEquals("s1b-wv2-mbu-hh-20190628t191011-20190628t191014-016900-01fce3-002_C26C.bufr",
				file2json.get("productName"));
		Assert.assertEquals("S1", file2json.get("missionId"));
		Assert.assertEquals("B", file2json.get("satelliteId"));
		Assert.assertEquals("WV", file2json.get("mode"));
		Assert.assertEquals("2", file2json.get("columnId"));
		Assert.assertEquals("REP_MBU_", file2json.get("productType"));
		Assert.assertEquals("HH", file2json.get("polarisation"));
		Assert.assertEquals("2019-06-28T19:10:11.000000Z", file2json.get("startTime"));
		Assert.assertEquals("2019-06-28T19:10:14.000000Z", file2json.get("stopTime"));
		Assert.assertEquals("016900", file2json.get("absoluteOrbitNumber"));
		Assert.assertEquals("01FCE3", file2json.get("missionDataTakeId"));
	}
}
