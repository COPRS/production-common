package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;

public class TestSppMbuProductMetadataExtractor {

	@Test
	public void newProductMetadataFromProductName() throws MetadataExtractionException,
			MetadataMalformedException {

		Pattern pattern = Pattern.compile(
				"^(s1)(a|b)-([0-9a-z]{2})([0-9a-z]{1})-(mbu)-()()(vv|hh)-([0-9a-z]{15})-([0-9a-z]{15})-([0-9]{6})-([a-z0-9]{6})-([0-9]{3})_([A-Z0-9]{4})\\.(bufr)$",
				Pattern.CASE_INSENSITIVE);
		String file1 = "s1a-wv1-mbu-vv-20190628t190957-20190628t191000-016900-01fce3-001_C26C.bufr";
		String file2 = "s1b-wv2-mbu-hh-20190628t191011-20190628t191014-016900-01fce3-002_C26C.bufr";

		ProductMetadata metadata1 = SppMbuProductMetadataExtractor.newProductMetadataFromProductName(file1, pattern);
		ProductMetadata metadata2 = SppMbuProductMetadataExtractor.newProductMetadataFromProductName(file2, pattern);
		
		Assert.assertEquals("s1a-wv1-mbu-vv-20190628t190957-20190628t191000-016900-01fce3-001_C26C.bufr",
				metadata1.get("productName"));
		Assert.assertEquals("S1", metadata1.get("missionId"));
		Assert.assertEquals("A", metadata1.get("satelliteId"));
		Assert.assertEquals("WV", metadata1.get("mode"));
		Assert.assertEquals("1", metadata1.get("columnId"));
		Assert.assertEquals("REP_MBU_", metadata1.get("productType"));
		Assert.assertEquals("VV", metadata1.get("polarisation"));
		Assert.assertEquals("2019-06-28T19:09:57.000000Z", metadata1.get("startTime"));
		Assert.assertEquals("2019-06-28T19:10:00.000000Z", metadata1.get("stopTime"));
		Assert.assertEquals("016900", metadata1.get("absoluteOrbitNumber"));
		Assert.assertEquals("01FCE3", metadata1.get("missionDataTakeId"));
		Assert.assertEquals("001", metadata1.get("idInColumn"));
		
		Assert.assertEquals("s1b-wv2-mbu-hh-20190628t191011-20190628t191014-016900-01fce3-002_C26C.bufr",
				metadata2.get("productName"));
		Assert.assertEquals("S1", metadata2.get("missionId"));
		Assert.assertEquals("B", metadata2.get("satelliteId"));
		Assert.assertEquals("WV", metadata2.get("mode"));
		Assert.assertEquals("2", metadata2.get("columnId"));
		Assert.assertEquals("REP_MBU_", metadata2.get("productType"));
		Assert.assertEquals("HH", metadata2.get("polarisation"));
		Assert.assertEquals("2019-06-28T19:10:11.000000Z", metadata2.get("startTime"));
		Assert.assertEquals("2019-06-28T19:10:14.000000Z", metadata2.get("stopTime"));
		Assert.assertEquals("016900", metadata2.get("absoluteOrbitNumber"));
		Assert.assertEquals("01FCE3", metadata2.get("missionDataTakeId"));
		Assert.assertEquals("002", metadata2.get("idInColumn"));
	}
}
