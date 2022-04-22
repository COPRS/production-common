package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONObject;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;

public class TestS2ProductNameUtil {
	
	@Test
	public void testExtractMetadata() throws MetadataExtractionException, MetadataMalformedException {
		
		// synthetic file name with all optional attributes (granule producttype here because then _S is for sensing here instead of validity start)
		
		final JSONObject m1 = S2ProductNameUtil.extractMetadata("S2A_OPER_MSI_L1A_GR_MTI__20200101T125959_S20130401T123000_O123456_123457_V20091210T235134_20091210T235224_D05_A123456_R123_T15SWC_N01.01_B8A_WP_LD");
		System.out.println(m1.toString(4));
		assertEquals("S2A_OPER_MSI_L1A_GR_MTI__20200101T125959_S20130401T123000_O123456_123457_V20091210T235134_20091210T235224_D05_A123456_R123_T15SWC_N01.01_B8A_WP_LD", m1.getString("productName"));
		assertEquals("S2", m1.getString("missionId"));
		assertEquals("A", m1.getString("satelliteId"));
		assertEquals("OPER", m1.getString("productClass"));
		assertEquals("MSI_L1A_GR", m1.getString("productType"));
		assertEquals("2020-01-01T12:59:59.000000Z", m1.getString("creationTime"));
		assertEquals("2013-04-01T12:30:00.000000Z", m1.getString("sensingTime"));
		assertEquals("123456", m1.getString("firstAbsoluteOrbitNumber"));
		assertEquals("123457", m1.getString("lastAbsoluteOrbitNumber"));
		assertEquals("2009-12-10T23:51:34.000000Z", m1.getString("startTime"));
		assertEquals("2009-12-10T23:52:24.000000Z", m1.getString("stopTime"));
		assertEquals("05", m1.getString("detectorId"));
		assertEquals("123456", m1.getString("absoluteOrbit"));
		assertEquals("123", m1.getString("relativeOrbit"));
		assertEquals("15SWC", m1.getString("tileNumber"));
		assertEquals("01.01", m1.getString("processingBaselineNumber"));
		assertEquals("8A", m1.getString("bandIndexId"));
		assertEquals("P", m1.getString("completenessId"));
		assertEquals("D", m1.getString("degradationId"));
		assertEquals(19, m1.length());
		
		// synthetic file name for AUX
		
		final JSONObject m2 = S2ProductNameUtil.extractMetadata("S2B_TEST_AUX_DUMMY_______20200101T125959_S20130401T123000_O123456_123457_D05_A823456_R123_T15SWC_N01.01_B8A_WF_LN");
		System.out.println(m2.toString(4));
		assertEquals("S2", m2.getString("missionId"));
		assertEquals("B", m2.getString("satelliteId"));
		assertEquals("TEST", m2.getString("productClass"));
		assertEquals("AUX_DUMMY_", m2.getString("productType"));
		assertEquals("2020-01-01T12:59:59.000000Z", m2.getString("creationTime"));
		assertEquals("123456", m2.getString("firstAbsoluteOrbitNumber"));
		assertEquals("123457", m2.getString("lastAbsoluteOrbitNumber"));
		assertEquals("2013-04-01T12:30:00.000000Z", m2.getString("validityStartTime"));
		assertEquals("9999-12-31T23:59:59.999999Z", m2.getString("validityStopTime"));
		assertEquals("05", m2.getString("detectorId"));
		assertEquals("823456", m2.getString("absoluteOrbit"));
		assertEquals("123", m2.getString("relativeOrbit"));
		assertEquals("15SWC", m2.getString("tileNumber"));
		assertEquals("01.01", m2.getString("processingBaselineNumber"));
		assertEquals("8A", m2.getString("bandIndexId"));
		assertEquals("F", m2.getString("completenessId"));
		assertEquals("N", m2.getString("degradationId"));
		assertEquals(18, m2.length());
		
		// from existing file names	
		
		final JSONObject m3 = S2ProductNameUtil.extractMetadata("S2A_OPER_AUX_PREORB_OPOD_20170617T092226_V20170617T110120_20170617T142243.EOF");		
		System.out.println(m3.toString(4));
		assertEquals("S2A_OPER_AUX_PREORB_OPOD_20170617T092226_V20170617T110120_20170617T142243.EOF", m3.getString("productName"));
		assertEquals("S2", m3.getString("missionId"));
		assertEquals("A", m3.getString("satelliteId"));
		assertEquals("OPER", m3.getString("productClass"));
		assertEquals("AUX_PREORB", m3.getString("productType"));
		assertEquals("2017-06-17T09:22:26.000000Z", m3.getString("creationTime"));
		assertEquals("2017-06-17T11:01:20.000000Z", m3.getString("validityStartTime"));
		assertEquals("2017-06-17T14:22:43.000000Z", m3.getString("validityStopTime"));		
		assertEquals(8, m3.length());

		final JSONObject m4 = S2ProductNameUtil.extractMetadata("S2A_OPER_AUX_SADATA_EPAE_20190222T003515_V20190221T190438_20190221T204519_A019158_WF_LN.zip");
		System.out.println(m4.toString(4));
		assertEquals("S2A_OPER_AUX_SADATA_EPAE_20190222T003515_V20190221T190438_20190221T204519_A019158_WF_LN.zip", m4.getString("productName"));
		assertEquals("S2", m4.getString("missionId"));
		assertEquals("A", m4.getString("satelliteId"));
		assertEquals("OPER", m4.getString("productClass"));
		assertEquals("AUX_SADATA", m4.getString("productType"));
		assertEquals("2019-02-22T00:35:15.000000Z", m4.getString("creationTime"));
		assertEquals("2019-02-21T19:04:38.000000Z", m4.getString("validityStartTime"));
		assertEquals("2019-02-21T20:45:19.000000Z", m4.getString("validityStopTime"));
		assertEquals("019158", m4.getString("absoluteOrbit"));
		assertEquals("F", m4.getString("completenessId"));
		assertEquals("N", m4.getString("degradationId"));
		assertEquals(11, m4.length());
		
		final JSONObject m5 = S2ProductNameUtil.extractMetadata("S2A_OPER_AUX_SADATA_EPAE_20190222T003515_V20190221T190438_20190221T204519_A019158_WF_LN.tar");
		System.out.println(m5.toString(4));
		assertEquals("S2", m5.getString("missionId"));
		assertEquals("A", m5.getString("satelliteId"));		
		assertEquals("S2A_OPER_AUX_SADATA_EPAE_20190222T003515_V20190221T190438_20190221T204519_A019158_WF_LN.tar", m5.getString("productName"));
		assertEquals("OPER", m5.getString("productClass"));
		assertEquals("AUX_SADATA", m5.getString("productType"));
		assertEquals("2019-02-22T00:35:15.000000Z", m5.getString("creationTime"));
		assertEquals("2019-02-21T19:04:38.000000Z", m5.getString("validityStartTime"));
		assertEquals("2019-02-21T20:45:19.000000Z", m5.getString("validityStopTime"));
		assertEquals("019158", m5.getString("absoluteOrbit"));
		assertEquals("F", m5.getString("completenessId"));
		assertEquals("N", m5.getString("degradationId"));
		assertEquals(11, m5.length());
		
		final JSONObject m6 = S2ProductNameUtil.extractMetadata("S2B_OPER_AUX_RESORB_OPOD_20181016T014456_V20181015T211347_20181016T003717.EOF");
		System.out.println(m6.toString(4));
		assertEquals("S2B_OPER_AUX_RESORB_OPOD_20181016T014456_V20181015T211347_20181016T003717.EOF", m6.getString("productName"));
		assertEquals("S2", m6.getString("missionId"));
		assertEquals("B", m6.getString("satelliteId"));
		assertEquals("OPER", m6.getString("productClass"));
		assertEquals("AUX_RESORB", m6.getString("productType"));
		assertEquals("2018-10-16T01:44:56.000000Z", m6.getString("creationTime"));
		assertEquals("2018-10-15T21:13:47.000000Z", m6.getString("validityStartTime"));
		assertEquals("2018-10-16T00:37:17.000000Z", m6.getString("validityStopTime"));
		assertEquals(8, m6.length());
		
		final JSONObject m7 = S2ProductNameUtil.extractMetadata("S2B_OPER_GIP_R2DEFI_MPC__20170206T103039_V20170101T000000_21000101T000000_B8A.TGZ");
		System.out.println(m7.toString(4));
		assertEquals("S2B_OPER_GIP_R2DEFI_MPC__20170206T103039_V20170101T000000_21000101T000000_B8A.TGZ", m7.getString("productName"));
		assertEquals("S2", m7.getString("missionId"));
		assertEquals("B", m7.getString("satelliteId"));
		assertEquals("OPER", m7.getString("productClass"));
		assertEquals("GIP_R2DEFI", m7.getString("productType"));
		assertEquals("2017-02-06T10:30:39.000000Z", m7.getString("creationTime"));
		assertEquals("2017-01-01T00:00:00.000000Z", m7.getString("startTime"));
		assertEquals("2100-01-01T00:00:00.000000Z", m7.getString("stopTime"));
		assertEquals("8A", m7.getString("bandIndexId"));
		assertEquals(9, m7.length());
		
		final JSONObject m8 = S2ProductNameUtil.extractMetadata("S2__OPER_AUX_ECMWFD_PDMC_20190216T120000_V20190217T090000_20190217T210000.TGZ");
		System.out.println(m8.toString(4));
		assertEquals("S2__OPER_AUX_ECMWFD_PDMC_20190216T120000_V20190217T090000_20190217T210000.TGZ", m8.getString("productName"));
		assertEquals("S2", m8.getString("missionId"));
		assertEquals("_", m8.getString("satelliteId"));
		assertEquals("OPER", m8.getString("productClass"));
		assertEquals("AUX_ECMWFD", m8.getString("productType"));
		assertEquals("2019-02-16T12:00:00.000000Z", m8.getString("creationTime"));
		assertEquals("2019-02-17T09:00:00.000000Z", m8.getString("validityStartTime"));
		assertEquals("2019-02-17T21:00:00.000000Z", m8.getString("validityStopTime"));
		assertEquals(8, m8.length());
		
		final JSONObject m9 = S2ProductNameUtil.extractMetadata("S2__OPER_AUX_UT1UTC_PDMC_20191024T000000_V20191025T000000_20201024T000000.TGZ");
		System.out.println(m9.toString(4));
		assertEquals("S2__OPER_AUX_UT1UTC_PDMC_20191024T000000_V20191025T000000_20201024T000000.TGZ", m9.getString("productName"));
		assertEquals("S2", m9.getString("missionId"));
		assertEquals("_", m9.getString("satelliteId"));
		assertEquals("OPER", m9.getString("productClass"));
		assertEquals("AUX_UT1UTC", m9.getString("productType"));
		assertEquals("2019-10-24T00:00:00.000000Z", m9.getString("creationTime"));
		assertEquals("2019-10-25T00:00:00.000000Z", m9.getString("validityStartTime"));
		assertEquals("2020-10-24T00:00:00.000000Z", m9.getString("validityStopTime"));
		assertEquals(8, m9.length());
		
		final JSONObject m10 = S2ProductNameUtil.extractMetadata("S2A_OPER_MSI_L1A_GR_MTI__20141104T134012_S20141104T134012_D03_N01.12");
		System.out.println(m10.toString(4));
		assertEquals("S2A_OPER_MSI_L1A_GR_MTI__20141104T134012_S20141104T134012_D03_N01.12", m10.getString("productName"));
		assertEquals("S2", m10.getString("missionId"));
		assertEquals("A", m10.getString("satelliteId"));
		assertEquals("OPER", m10.getString("productClass"));
		assertEquals("MSI_L1A_GR", m10.getString("productType"));
		assertEquals("2014-11-04T13:40:12.000000Z", m10.getString("creationTime"));
		assertEquals("2014-11-04T13:40:12.000000Z", m10.getString("sensingTime"));
		assertEquals("03", m10.getString("detectorId"));
		assertEquals("01.12", m10.getString("processingBaselineNumber"));
		assertEquals(9, m10.length());
		
		final JSONObject m11 = S2ProductNameUtil.extractMetadata("S2B_OPER_MSI_L1A_GR_EPAE_20200114T014601_S20200114T002121_D12_N02.08.tar");
		System.out.println(m11.toString(4));
		assertEquals("S2B_OPER_MSI_L1A_GR_EPAE_20200114T014601_S20200114T002121_D12_N02.08.tar", m11.getString("productName"));
		assertEquals("S2", m11.getString("missionId"));
		assertEquals("B", m11.getString("satelliteId"));
		assertEquals("OPER", m11.getString("productClass"));
		assertEquals("MSI_L1A_GR", m11.getString("productType"));
		assertEquals("2020-01-14T01:46:01.000000Z", m11.getString("creationTime"));
		assertEquals("2020-01-14T00:21:21.000000Z", m11.getString("sensingTime"));
		assertEquals("12", m11.getString("detectorId"));
		assertEquals("02.08", m11.getString("processingBaselineNumber"));
		assertEquals(9, m11.length());
		
		final JSONObject m12 = S2ProductNameUtil.extractMetadata("S2B_OPER_MSI_L1C_DS_MTI__20200226T105549_S20200226T070436_N02.09.tar");
		System.out.println(m12.toString(4));
		assertEquals("S2B_OPER_MSI_L1C_DS_MTI__20200226T105549_S20200226T070436_N02.09.tar", m12.getString("productName"));
		assertEquals("S2", m12.getString("missionId"));
		assertEquals("B", m12.getString("satelliteId"));
		assertEquals("OPER", m12.getString("productClass"));
		assertEquals("MSI_L1C_DS", m12.getString("productType"));
		assertEquals("2020-02-26T10:55:49.000000Z", m12.getString("creationTime"));
		assertEquals("2020-02-26T07:04:36.000000Z", m12.getString("sensingTime"));
		assertEquals("02.09", m12.getString("processingBaselineNumber"));
		assertEquals(8, m12.length());
		
		final JSONObject m13 = S2ProductNameUtil.extractMetadata("S2B_OPER_MSI_L1C_TC_EPAE_20191001T102654_A013417_T39UWP_N02.08.jp2");
		System.out.println(m13.toString(4));
		assertEquals("S2B_OPER_MSI_L1C_TC_EPAE_20191001T102654_A013417_T39UWP_N02.08.jp2", m13.getString("productName"));
		assertEquals("S2", m13.getString("missionId"));
		assertEquals("B", m13.getString("satelliteId"));
		assertEquals("OPER", m13.getString("productClass"));
		assertEquals("MSI_L1C_TC", m13.getString("productType"));
		assertEquals("2019-10-01T10:26:54.000000Z", m13.getString("creationTime"));
		assertEquals("013417", m13.getString("absoluteOrbit"));
		assertEquals("39UWP", m13.getString("tileNumber"));
		assertEquals("02.08", m13.getString("processingBaselineNumber"));
		assertEquals(9, m13.length());
		
		// HKTM
		
		final JSONObject m14 = S2ProductNameUtil.extractMetadata("S2A_OPER_PRD_HKTM___20191203T051837_20191203T051842_0001.tar");
		System.out.println(m14.toString(4));
		assertEquals("S2A_OPER_PRD_HKTM___20191203T051837_20191203T051842_0001.tar", m14.getString("productName"));
		assertEquals("S2", m14.getString("missionId"));
		assertEquals("A", m14.getString("satelliteId"));
		assertEquals("OPER", m14.getString("productClass"));
		assertEquals("PRD_HKTM__", m14.getString("productType"));
		assertEquals("2019-12-03T05:18:37.000000Z", m14.getString("validityStartTime"));
		assertEquals("2019-12-03T05:18:42.000000Z", m14.getString("validityStopTime"));
		assertEquals(7, m14.length());
		
		// Level-1C User Product tree defined using the Standard Naming Convention
		
		final JSONObject m15 = S2ProductNameUtil.extractMetadata("S2A_OPER_PRD_MSIL1C_PDMC_20160615T141550_R121_V20160615T082012_20160615T083135.SAFE");
		System.out.println(m15.toString(4));
		assertEquals("S2A_OPER_PRD_MSIL1C_PDMC_20160615T141550_R121_V20160615T082012_20160615T083135.SAFE", m15.getString("productName"));
		assertEquals("S2", m15.getString("missionId"));
		assertEquals("A", m15.getString("satelliteId"));
		assertEquals("OPER", m15.getString("productClass"));
		assertEquals("PRD_MSIL1C", m15.getString("productType"));
		assertEquals("121", m15.getString("relativeOrbit"));
		assertEquals("2016-06-15T08:20:12.000000Z", m15.getString("startTime"));
		assertEquals("2016-06-15T08:31:35.000000Z", m15.getString("stopTime"));
		assertEquals(9, m15.length());
		
		// Level-1C User Product tree defined using the Compact Naming Convention
		
		final JSONObject m16 = S2ProductNameUtil.extractMetadata("S2A_MSIL1C_20150802T105414_N0102_R008_20150803T124046.SAFE.tar.gz");
		System.out.println(m16.toString(4));
		assertEquals("S2A_MSIL1C_20150802T105414_N0102_R008_20150803T124046.SAFE.tar.gz", m16.getString("productName"));
		assertEquals("S2", m16.getString("missionId"));
		assertEquals("A", m16.getString("satelliteId"));
		assertEquals("MSIL1C", m16.getString("productType"));
		assertEquals("2015-08-02T10:54:14.000000Z", m16.getString("startTime"));
		assertEquals("0102", m16.getString("processingBaselineNumber"));
		assertEquals("008", m16.getString("relativeOrbit"));
		assertEquals("2015-08-03T12:40:46.000000Z", m16.getString("productDiscriminator"));
		assertEquals(8, m16.length());
		
		// Single Title Naming Convention. Example S2 L2A single tile product main directory
		
		final JSONObject m17 = S2ProductNameUtil.extractMetadata("S2A_MSIL2A_20171103T102201_N0206_R065_T32TNS_20171106T195236.SAFE.tar.gz");
		System.out.println(m17.toString(4));
		assertEquals("S2A_MSIL2A_20171103T102201_N0206_R065_T32TNS_20171106T195236.SAFE.tar.gz", m17.getString("productName"));
		assertEquals("S2", m17.getString("missionId"));
		assertEquals("A", m17.getString("satelliteId"));
		assertEquals("MSIL2A", m17.getString("productType"));
		assertEquals("2017-11-03T10:22:01.000000Z", m17.getString("startTime"));
		assertEquals("0206", m17.getString("processingBaselineNumber"));
		assertEquals("065", m17.getString("relativeOrbit"));
		assertEquals("32TNS", m17.getString("tileNumber"));
		assertEquals("2017-11-06T19:52:36.000000Z", m17.getString("productDiscriminator"));
		assertEquals(9, m17.length());
	}

}
