package esa.s1pdgs.cpoc.validation.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class ValidationServiceTest {

	@Mock
	private MetadataClient metadataClient;

	@Mock
	private ObsClient obsClient;
	
	@Mock
	private DataLifecycleMetadataRepository lifecycleMetadataRepo;

	private ValidationService validationService;
	
	final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("ValidationService");

	@Mock
	Reporting report;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		validationService = new ValidationService(metadataClient, obsClient, lifecycleMetadataRepo);
	}

	@Test
	public void testQueryFamily() {
		assertEquals("AUXILIARY_FILE", validationService.getQueryFamily(ProductFamily.AUXILIARY_FILE));
		assertEquals("AUXILIARY_FILE", validationService.getQueryFamily(ProductFamily.AUXILIARY_FILE_ZIP));
	}

	@Test
	public void testObsValidation() throws Exception {
		final LocalDateTime localDateTimeStart = LocalDateTime.parse("2000-01-01T00:00:00.000000Z",
				DateUtils.METADATA_DATE_FORMATTER);
		final LocalDateTime localDateTimeStop = LocalDateTime.parse("2020-01-03T00:00:00.000000Z",
				DateUtils.METADATA_DATE_FORMATTER);

		final SearchMetadata md1 = new SearchMetadata();
		md1.setKeyObjectStorage("S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml");

		final SearchMetadata md2 = new SearchMetadata();
		md2.setKeyObjectStorage("S1B_OPER_AUX_RESORB_OPOD_20180227T082030_V20180227T040822_20180227T072552.EOF");

		final SearchMetadata md3 = new SearchMetadata();
		md3.setKeyObjectStorage("S1B_WV_RAW__0NSV_20181001T145340_20181001T151214_012960_017F00_584F.SAFE");

		final SearchMetadata md4 = new SearchMetadata();
		md4.setKeyObjectStorage("S1B_WV_RAW__0NSV_20181001T134430_20181001T135939_012959_017EF8_789A.SAFE");
		
		final SearchMetadata md5 = new SearchMetadata();
		md5.setKeyObjectStorage("IAMNOTINTHEOBS");

		final List<SearchMetadata> metadataResults = new ArrayList<>();
		metadataResults.add(md1);
		metadataResults.add(md2);
		metadataResults.add(md3);
		metadataResults.add(md4);
		metadataResults.add(md5);

		final ObsObject ob1 = new ObsObject(ProductFamily.AUXILIARY_FILE, "S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml");
		final ObsObject ob2 = new ObsObject(ProductFamily.AUXILIARY_FILE,
				"S1B_OPER_AUX_RESORB_OPOD_20180227T082030_V20180227T040822_20180227T072552.EOF");
		final ObsObject ob3 = new ObsObject(ProductFamily.AUXILIARY_FILE,
				"S1B_WV_RAW__0NSV_20181001T145340_20181001T151214_012960_017F00_584F.SAFE.zip");
		final ObsObject ob4 = new ObsObject(ProductFamily.AUXILIARY_FILE,
				"S1B_WV_RAW__0NSV_20181001T134430_20181001T135939_012959_017EF8_789A.SAFE/manifest.safe");
		final ObsObject ob5 = new ObsObject(ProductFamily.AUXILIARY_FILE,
				"S1B_WV_RAW__0NSV_20181001T134430_20181001T135939_012959_017EF8_789A.SAFE/s1b-wv-raw-n-vv-20181001t134430-20181001t135939-012959-017ef8.dat");
		final ObsObject ob6 = new ObsObject(ProductFamily.AUXILIARY_FILE, "IAMNOTINTHEMETADATA");

		final Map<String, ObsObject> obsResults = new HashMap<>();
		obsResults.put("S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml", ob1);
		obsResults.put("S1B_OPER_AUX_RESORB_OPOD_20180227T082030_V20180227T040822_20180227T072552.EOF", ob2);
		obsResults.put("S1B_WV_RAW__0NSV_20181001T145340_20181001T151214_012960_017F00_584F.SAFE.zip", ob3);
		obsResults.put("S1B_WV_RAW__0NSV_20181001T134430_20181001T135939_012959_017EF8_789A.SAFE/manifest.safe", ob4);
		obsResults.put("S1B_WV_RAW__0NSV_20181001T134430_20181001T135939_012959_017EF8_789A.SAFE/s1b-wv-raw-n-vv-20181001t134430-20181001t135939-012959-017ef8.dat",
				ob5);
		obsResults.put("IAMNOTINTHEMETADATA", ob6);

		doReturn(metadataResults).when(metadataClient).query(ProductFamily.AUXILIARY_FILE, localDateTimeStart,
				localDateTimeStop);
		doReturn(obsResults).when(obsClient).listInterval(Mockito.eq(ProductFamily.AUXILIARY_FILE),
				Mockito.eq(Date.from(localDateTimeStart.atZone(ZoneId.of("UTC")).toInstant())),
				Mockito.eq(Date.from(localDateTimeStop.atZone(ZoneId.of("UTC")).toInstant())));
		
		// OBS does have 6 elements, but two are actually the same product
		assertEquals(5,validationService.extractRealKeysForMDC(obsResults.values(),ProductFamily.AUXILIARY_FILE).size());

		assertEquals(6, validationService.validateProductFamily(reporting, ProductFamily.AUXILIARY_FILE, localDateTimeStart,
				localDateTimeStop));
		
		doReturn(Optional.of(new DataLifecycleMetadata())).when(lifecycleMetadataRepo).findByProductName("S1B_OPER_AUX_OBMEMC_PDMC_20140212T000000.xml");
		doReturn(Optional.of(new DataLifecycleMetadata())).when(lifecycleMetadataRepo).findByProductName("S1B_WV_RAW__0NSV_20181001T145340_20181001T151214_012960_017F00_584F.SAFE.zip");
		doReturn(Optional.of(new DataLifecycleMetadata())).when(lifecycleMetadataRepo).findByProductName("S1B_WV_RAW__0NSV_20181001T134430_20181001T135939_012959_017EF8_789A.SAFE");
		
		assertEquals(3, validationService.validateProductFamily(reporting, ProductFamily.AUXILIARY_FILE, localDateTimeStart,
				localDateTimeStop));
	}
}
