package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

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
import esa.s1pdgs.cpoc.metadata.extraction.Utils;
import esa.s1pdgs.cpoc.metadata.extraction.config.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.ExtractMetadata;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.S3FileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class TestS3AuxMetadataExtractor {

	private static final String PATTERN = "^([a-zA-Z0-9][a-zA-Z0-9])(\\w{1})_((OL|SL|SR|DO|MW|GN|SY|TM|AX)_(0|1|2|_)_\\w{6})_(\\d{8}T\\d{6})_(\\d{8}T\\d{6})_(\\d{8}T\\d{6})_(\\w{17})_(\\w{3})_(\\w{8})\\.(\\w{1,4})\\/?(.+)?$";

	/**
	 * Object Storage Client
	 */
	@Mock
	protected ObsClient obsClient;

	/**
	 * 
	 */
	@Mock
	protected MetadataExtractorConfig extractorConfig;

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

		doReturn("config/xsltDir/").when(extractorConfig).getXsltDirectory();

		final FileDescriptorBuilder fileDescriptorBuilder = new FileDescriptorBuilder(testDir,
				Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE));

		final ExtractMetadata extract = new ExtractMetadata(extractorConfig.getTypeOverlap(),
				extractorConfig.getTypeSliceLength(), Collections.<String, String>emptyMap(),
				extractorConfig.getPacketStoreTypes(), extractorConfig.getPacketstoreTypeTimelinesses(),
				extractorConfig.getTimelinessPriorityFromHighToLow(), extractorConfig.getXsltDirectory(), xmlConverter);
		final MetadataBuilder mdBuilder = new MetadataBuilder(extract);

		extractor = new S3AuxMetadataExtractor(mdBuilder, fileDescriptorBuilder, testDir.getPath(),
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
		final CatalogJob message = Utils.newCatalogJob(
				"S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3",
				"S3A_AX___BA__AX_20040702T223000_20040704T042158_20171130T082116___________________WER_D_AL____.SEN3",
				ProductFamily.S3_AUX, "NRT");

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
		expectedDescriptor.setProductFamily(ProductFamily.S3_AUX);
		expectedDescriptor.setInstanceId("_________________");
		expectedDescriptor.setGeneratingCentre("WER");
		expectedDescriptor.setClassId("D_AL____");
		expectedDescriptor.setMode("NRT");

		final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.S3)
				.newReporting("TestMetadataExtraction");

		// Use MetadataBuilder. The method creating the JSON-Object is tested in
		// ExtractMetadataTest
		final ProductMetadata expected = extractor.mdBuilder.buildS3AuxFileMetadata(expectedDescriptor, files.get(0),
				message);
		final ProductMetadata result = extractor.extract(reporting, message);

		Iterator<String> it = expected.keys().iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (!("timeliness".equals(key))) {
				assertEquals(expected.get(key), result.get(key));
			}
		}
	}

}
