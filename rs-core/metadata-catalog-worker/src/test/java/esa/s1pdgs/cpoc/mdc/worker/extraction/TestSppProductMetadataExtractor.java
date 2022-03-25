package esa.s1pdgs.cpoc.mdc.worker.extraction;

import java.util.regex.Pattern;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;

public class TestSppProductMetadataExtractor {

	@Test
	public void putMetadataToJSON() throws MetadataExtractionException {

		Pattern pattern = Pattern.compile(
				"^(S1|AS)(A|B)_(__)_(OBS)(_)_(S)(S)(__)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_(\\w{1,})$",
				Pattern.CASE_INSENSITIVE);
		String file1 = "S1A____OBS__SS___20200828T135345_20200828T153229_034108_09E8";
		String file2 = "S1B____OBS__SS___20200828T094611_20200828T112456_023122_864E";

		JSONObject file1json = SppProductMetadataExtractor.putMetadataToJSON(file1, pattern);
		JSONObject file2json = SppProductMetadataExtractor.putMetadataToJSON(file2, pattern);

		Assert.assertEquals("2020-08-28T13:53:45.000000Z", file1json.get("startTime"));
		Assert.assertEquals("2020-08-28T15:32:29.000000Z", file1json.get("stopTime"));

		Assert.assertEquals("2020-08-28T09:46:11.000000Z", file2json.get("startTime"));
		Assert.assertEquals("2020-08-28T11:24:56.000000Z", file2json.get("stopTime"));

		Assert.assertEquals("A", file1json.get("satelliteId"));
		Assert.assertEquals("B", file2json.get("satelliteId"));

		Assert.assertEquals("S1A____OBS__SS___20200828T135345_20200828T153229_034108_09E8",
				file1json.get("productName"));
		Assert.assertEquals("S1B____OBS__SS___20200828T094611_20200828T112456_023122_864E",
				file2json.get("productName"));
	}

}
