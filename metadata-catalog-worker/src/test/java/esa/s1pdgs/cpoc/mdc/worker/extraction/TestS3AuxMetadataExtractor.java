package esa.s1pdgs.cpoc.mdc.worker.extraction;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.mdc.worker.Utils;
import esa.s1pdgs.cpoc.mdc.worker.config.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.ExtractMetadata;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.S3FileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.mdc.worker.status.AppStatusImpl;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class TestS3AuxMetadataExtractor {

	private static final String PATTERN = "^([a-zA-Z0-9][a-zA-Z0-9])(\\w{1})_((OL|SL|SR|DO|MW|GN|SY|TM|AX)_(0|1|2|_)_\\w{6})_(\\d{8}T\\d{6})_(\\d{8}T\\d{6})_(\\d{8}T\\d{6})_(\\w{17})_(\\w{3})_(\\w{8})\\.(\\w{1,4})\\/?(.+)?$";

	/**
	 * Elasticsearch services
	 */
	@Mock
	protected EsServices esServices;

	/**
	 * Object Storage Client
	 */
	@Mock
	protected ObsClient obsClient;

	/**
	 * MQI service
	 */
	@Mock
	private GenericMqiClient mqiService;

	/**
	 * 
	 */
	@Mock
	protected MetadataExtractorConfig extractorConfig;

	/**
	 * Application status
	 */
	@Mock
	protected AppStatusImpl appStatus;

	/**
	 * Extractor
	 */
	protected S3AuxMetadataExtractor extractor;

	private static final File inputDir = new File("src/test/resources/workDir/");
	private final File testDir = FileUtils.createTmpDir();

	@Mock
	XmlConverter xmlConverter;

	/**
	 * Initialization
	 * 
	 * @throws AbstractCodedException
	 */
	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
        Utils.copyFolder(inputDir.toPath(), testDir.toPath());

		doNothing().when(appStatus).setError(Mockito.anyString());
		doReturn(true).when(mqiService).ack(Mockito.any(), Mockito.any());
		doReturn("config/xsltDir/").when(extractorConfig).getXsltDirectory();

		final FileDescriptorBuilder fileDescriptorBuilder = new FileDescriptorBuilder(testDir,
				Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE));

		final ExtractMetadata extract = new ExtractMetadata(extractorConfig.getTypeOverlap(),
				extractorConfig.getTypeSliceLength(), extractorConfig.getPacketStoreTypes(),
				extractorConfig.getPacketstoreTypeTimelinesses(), extractorConfig.getTimelinessPriorityFromHighToLow(),
				extractorConfig.getXsltDirectory(), xmlConverter);
		final MetadataBuilder mdBuilder = new MetadataBuilder(extract);

		extractor = new S3AuxMetadataExtractor(esServices, mdBuilder, fileDescriptorBuilder, testDir.getPath(),
				new ProcessConfiguration(), obsClient);
	}

	@Test
	public void testExtractMetadata() throws MetadataExtractionException, AbstractCodedException {
		// Prepare OBS returnValue
		final List<File> files = Arrays.asList(new File(testDir,
				"S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3"
						+ File.separator + "xfdumanifest.xml"));
		doReturn(files).when(obsClient).download(Mockito.anyList(), Mockito.any());

		// Prepare MQI message
		final GenericMessageDto<CatalogJob> message = new GenericMessageDto<CatalogJob>(123, "", Utils.newCatalogJob(
				"S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3",
				"S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3",
				ProductFamily.S3_AUXILIARY_FILE, "NRT"));

		// Set up FileDescriptor to create expected result
		final S3FileDescriptor expectedDescriptor = new S3FileDescriptor();
		expectedDescriptor.setProductType("AX___BA__AX");
		expectedDescriptor.setProductClass("AX");
		expectedDescriptor.setRelativePath(
				"S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3/xfdumanifest.xml");
		expectedDescriptor.setFilename("xfdumanifest.xml");
		expectedDescriptor.setExtension(FileExtension.SEN3);
		expectedDescriptor.setProductName(
				"S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3");
		expectedDescriptor.setMissionId("S3");
		expectedDescriptor.setSatelliteId("A");
		expectedDescriptor.setKeyObjectStorage(
				"S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3");
		expectedDescriptor.setProductFamily(ProductFamily.S3_AUXILIARY_FILE);
		expectedDescriptor.setInstanceId("_________________");
		expectedDescriptor.setGeneratingCentre("WER");
		expectedDescriptor.setClassId("D_AL____");
		expectedDescriptor.setMode("NRT");

		final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("TestMetadataExtraction");

		// Use MetadataBuilder. The method creating the JSON-Object is tested in
		// ExtractMetadataTest
		final JSONObject expected = extractor.mdBuilder.buildS3AuxFileMetadata(expectedDescriptor, files.get(0),
				message.getBody());
		final JSONObject result = extractor.extract(reporting, message);

		for (final String key : expected.keySet()) {
			if (!"timeliness".equals(key)) {
				assertEquals(expected.get(key), result.get(key));
			}
		}

	}

}
