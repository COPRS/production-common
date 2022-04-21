package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.AuxDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.EdrsSessionFile;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.S2FileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.S3FileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.report.TimelinessReportingInput;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.report.TimelinessReportingOutput;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFactory;
import esa.s1pdgs.cpoc.report.ReportingMessage;

/**
 * Class to extract the metadata from various types of files
 * 
 * @author Olivier Bex-Chauvet
 */
public class ExtractMetadata {

	private static final String XSLT_MPL_EOF = "XSLT_MPL_EOF.xslt";
	private static final String XSLT_AUX_XML = "XSLT_AUX_XML.xslt";
	private static final String XSLT_AUX_MANIFEST = "XSLT_AUX_MANIFEST.xslt";
	private static final String XSLT_L0_MANIFEST = "XSLT_L0_MANIFEST.xslt";
	private static final String XSLT_L0_SEGMENT_MANIFEST = "XSLT_L0_SEGMENT.xslt";
	private static final String XSLT_L1_MANIFEST = "XSLT_L1_MANIFEST.xslt";
	private static final String XSLT_L2_MANIFEST = "XSLT_L2_MANIFEST.xslt";
	private static final String XSLT_ETAD_MANIFEST = "XSLT_L1_MANIFEST.xslt";
	private static final String XSLT_S2_MANIFEST = "XSLT_S2_MANIFEST.xslt";
	private static final String XSLT_S2_INVENTORY = "XSLT_S2_INVENTORY.xslt";
	private static final String XSLT_S3_AUX_XFDU_XML = "XSLT_S3_AUX_XFDU_XML.xslt";
	private static final String XSLT_S3_XFDU_XML = "XSLT_S3_XFDU_XML.xslt";
	private static final String XSLT_S3_IIF_XML = "XSLT_S3_IIF_XML.xslt";
	
	// S1OPS-937: Filename based extraction of Validity Start / Stop and Generation Time
	private static final List<String> TYPES_WITH_PRODUCTNAME_BASED_VALIDITY_TIME_EXTRACTION = Arrays.asList("AUX_TEC", "AUX_TRO");
	private static final List<String> TYPES_WITH_PRODUCTNAME_BASED_GENERATION_TIME_EXTRACTION = Arrays.asList("AUX_TEC", "AUX_TRO");
	private static final Pattern AUX_TEC_AND_AUX_TRO_PRODUCTNAME_PATTERN =
			Pattern.compile("^S.._..._..._V([0-9]{8}T[0-9]{6})_([0-9]{8}T[0-9]{6})_G([0-9]{8}T[0-9]{6})\\.SAFE$", Pattern.CASE_INSENSITIVE);

	/**
	 * Mapping of family to XSLT file name
	 */
	private final Map<ProductFamily, String> xsltMap;

	/**
	 * XSLT transformer factory
	 */
	private TransformerFactory transFactory;

	/**
	 * Map of all the overlap for the different slice type
	 */
	private Map<String, Float> typeOverlap;

	/**
	 * Map of all the length for the different slice type
	 */
	private Map<String, Float> typeSliceLength;

	/**
	 * Map packet stores to packet store type
	 */
	private Map<String, String> packetStoreTypes;

	/**
	 * Map packet store type to timeliness
	 */
	private Map<String, String> packetStoreTypeTimelinesses;

	/**
	 * Timeliness prioritization
	 */
	private List<String> timelinessPriorityFromHighToLow;

	/**
	 * Directory containing the XSLT files
	 */
	private String xsltDirectory;

	/**
	 * Type definitions to enforce on field data extracted from XML
	 */
	final Map<String, String> fieldTypes;
	
	/**
	 * The XML converter to use
	 */
	private final XmlConverter xmlConverter;

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(ExtractMetadata.class);

	/**
	 * Constructor
	 */
	public ExtractMetadata(final Map<String, Float> typeOverlap, final Map<String, Float> typeSliceLength,
			final Map<String, String> fieldTypes, final Map<String, String> packetStoreTypes,
			final Map<String, String> packetStoreTypeTimelinesses, final List<String> timelinessPriorityFromHighToLow,
			final String xsltDirectory, final XmlConverter xmlConverter) {
		this.transFactory = TransformerFactory.newInstance();
		this.typeOverlap = typeOverlap;
		this.typeSliceLength = typeSliceLength;
		this.fieldTypes = fieldTypes;
		this.packetStoreTypes = packetStoreTypes;
		this.packetStoreTypeTimelinesses = packetStoreTypeTimelinesses;
		this.timelinessPriorityFromHighToLow = timelinessPriorityFromHighToLow;
		this.xsltDirectory = xsltDirectory;
		this.xmlConverter = xmlConverter;
		this.xsltMap = new HashMap<>();
		this.xsltMap.put(ProductFamily.L0_ACN, XSLT_L0_MANIFEST);
		this.xsltMap.put(ProductFamily.L0_SLICE, XSLT_L0_MANIFEST);
		this.xsltMap.put(ProductFamily.L1_ACN, XSLT_L1_MANIFEST);
		this.xsltMap.put(ProductFamily.L1_SLICE, XSLT_L1_MANIFEST);
		this.xsltMap.put(ProductFamily.L2_ACN, XSLT_L2_MANIFEST);
		this.xsltMap.put(ProductFamily.L2_SLICE, XSLT_L2_MANIFEST);
		this.xsltMap.put(ProductFamily.L1_ETAD, XSLT_ETAD_MANIFEST);
	}

	/**
	 * Function which extracts metadata from MPL EOF file
	 * 
	 * @param descriptor        The file descriptor of the auxiliary file
	 * @param inputMetadataFile The file containing the metadata
	 * @return the json object with extracted metadata
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public JSONObject processEOFFile(final AuxDescriptor descriptor, final File inputMetadataFile)
			throws MetadataExtractionException, MetadataMalformedException {

		JSONObject metadataJSONObject = transformXMLWithXSLTToJSON(inputMetadataFile,
				new File(this.xsltDirectory + XSLT_MPL_EOF));

		metadataJSONObject = putConfigFileMetadataToJSON(metadataJSONObject, descriptor);
		LOGGER.debug("composed Json: {} ", metadataJSONObject);
		return metadataJSONObject;
	}

	/**
	 * Function which extracts metadata from AUX EOF file
	 * 
	 * @param descriptor        The file descriptor of the auxiliary file
	 * @param inputMetadataFile The file containing the metadata
	 * @return the json object with extracted metadata
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public JSONObject processEOFFileWithoutNamespace(final AuxDescriptor descriptor, final File inputMetadataFile)
			throws MetadataExtractionException, MetadataMalformedException {

		JSONObject metadataJSONObject = transformXMLWithXSLTToJSON(inputMetadataFile,
				new File(this.xsltDirectory + XSLT_MPL_EOF));

		metadataJSONObject = putConfigFileMetadataToJSON(metadataJSONObject, descriptor);
		LOGGER.debug("composed Json: {} ", metadataJSONObject);
		return metadataJSONObject;
	}

	/**
	 * Function which extracts metadata from AUX XML file
	 * 
	 * @param descriptor        The file descriptor of the auxiliary file
	 * @param inputMetadataFile The file containing the metadata
	 * @return the json object with extracted metadata
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public JSONObject processXMLFile(final AuxDescriptor descriptor, final File inputMetadataFile)
			throws MetadataExtractionException, MetadataMalformedException {

		JSONObject metadataJSONObject = transformXMLWithXSLTToJSON(inputMetadataFile,
				new File(this.xsltDirectory + XSLT_AUX_XML));

		metadataJSONObject = putConfigFileMetadataToJSON(metadataJSONObject, descriptor);
		LOGGER.debug("composed Json: {} ", metadataJSONObject);
		return metadataJSONObject;
	}

	/**
	 * Function which extracts metadata from AUX MANIFEST file
	 * 
	 * @param descriptor        The file descriptor of the auxiliary file
	 * @param inputMetadataFile The file containing the metadata
	 * @return the json object with extracted metadata
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	// FIXME probably it means SAFE AUX FILE ???
	public JSONObject processSAFEFile(final AuxDescriptor descriptor, final File inputMetadataFile)
			throws MetadataExtractionException, MetadataMalformedException {

		JSONObject metadataJSONObject = transformXMLWithXSLTToJSON(inputMetadataFile,
				new File(this.xsltDirectory + XSLT_AUX_MANIFEST));

		metadataJSONObject = putConfigFileMetadataToJSON(metadataJSONObject, descriptor);
		LOGGER.debug("composed Json: {} ", metadataJSONObject);
		return metadataJSONObject;
	}

	/**
	 * Function which extracts metadata from RAW file
	 * 
	 * @param descriptor The file descriptor of the raw file
	 * @return the json object with extracted metadata
	 * @throws MetadataExtractionException
	 */
	public JSONObject processRAWFile(final EdrsSessionFileDescriptor descriptor) throws MetadataExtractionException {
		final JSONObject metadataJSONObject = putEdrsSessionMetadataToJSON(new JSONObject(), descriptor);
		LOGGER.debug("composed Json: {} ", metadataJSONObject);
		return metadataJSONObject;
	}

	/**
	 * Function which extracts metadata from SESSION file
	 * 
	 * @param descriptor The file descriptor of the session file
	 * @return the json object with extracted metadata
	 * @throws MetadataExtractionException
	 */
	public JSONObject processSESSIONFile(final EdrsSessionFileDescriptor descriptor, final File file)
			throws MetadataExtractionException {
		try {
			final JSONObject metadataJSONObject = putEdrsSessionMetadataToJSON(new JSONObject(), descriptor);

//			final String name = new File(descriptor.getRelativePath()).getName();
//			final File file = new File(localDirectory, name);

			final EdrsSessionFile edrsSessionFile = (EdrsSessionFile) xmlConverter
					.convertFromXMLToObject(file.getPath());

			metadataJSONObject.put("startTime", DateUtils.convertToAnotherFormat(edrsSessionFile.getStartTime(),
					EdrsSessionFile.TIME_FORMATTER, DateUtils.METADATA_DATE_FORMATTER));

			metadataJSONObject.put("stopTime", DateUtils.convertToAnotherFormat(edrsSessionFile.getStopTime(),
					EdrsSessionFile.TIME_FORMATTER, DateUtils.METADATA_DATE_FORMATTER));

			metadataJSONObject.put("rawNames",
					edrsSessionFile.getRawNames().stream().map(r -> r.getFileName()).collect(Collectors.toList()));

			LOGGER.debug("composed Json: {} ", metadataJSONObject);
			return metadataJSONObject;

		} catch (JSONException | IOException | JAXBException e) {
			LOGGER.error("Extraction of session file metadata failed", e);
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Extracts metadata for L0 segment files
	 * 
	 * @param descriptor   The file descriptor of the file
	 * @param manifestFile The input manifest File
	 * @return json object with extracted metadata
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public JSONObject processL0Segment(final OutputFileDescriptor descriptor, final File manifestFile,
			final ReportingFactory reportingFactory) throws MetadataExtractionException, MetadataMalformedException {

		final File xsltFile = new File(this.xsltDirectory + XSLT_L0_SEGMENT_MANIFEST);
		LOGGER.debug("extracting metadata for descriptor: {} ", descriptor);
		JSONObject metadataJSONObject = transformXMLWithXSLTToJSON(manifestFile, xsltFile);

		metadataJSONObject = removeEmptyStringElementsFromPolarisationChannelsArray(metadataJSONObject);

		metadataJSONObject = putCommonMetadataToJSON(metadataJSONObject, descriptor);

		try {
			final String productType = descriptor.getProductType();

			if (productType.contains("GP_RAW_") || productType.contains("HK_RAW_")) {
				metadataJSONObject.remove("segmentCoordinates");
				LOGGER.debug("segment coordinates removed for product {}", descriptor.getFilename());
				// no coord
			} else {

				if (metadataJSONObject.has("segmentCoordinates")) {
					final String coords = metadataJSONObject.getString("segmentCoordinates");
					
					if (!coords.trim().isEmpty()) {
						
						//S1PRO-2732,S1OPS-673,S1OPS-1212: expected number of coordinates is 2 for ZS, ZE, ZI and ZW
						if (productType.matches("(Z[1-6]|ZE|ZI|ZW)_RAW__0.") && coords.trim().split(" ").length != 2) {
								metadataJSONObject.remove("segmentCoordinates");
								LOGGER.debug("segment coordinates removed for product {}", descriptor.getFilename());
						} else {
							metadataJSONObject.put("segmentCoordinates",
								processCoordinates(manifestFile, descriptor, coords));
						}
					}
				}
			}

			if (metadataJSONObject.has("packetStoreID")) {

				final Reporting reporting = reportingFactory.newReporting("SegmentTimeliness");

				final List<String> packetStoreIDs = new ArrayList<>();
				if (metadataJSONObject.get("packetStoreID") instanceof JSONArray) {
					final JSONArray jsonArray = (JSONArray) metadataJSONObject.get("packetStoreID");
					for (int i = 0; i < jsonArray.length(); i++) {
						packetStoreIDs.add(Integer.toString(jsonArray.getInt(i)));
					}
				} else {
					packetStoreIDs.add(Integer.toString(metadataJSONObject.getInt("packetStoreID")));
				}

				final String satellite = descriptor.getMissionId() + descriptor.getSatelliteId();
				// e. g. S1A or S1B (used in configuration file as prefix before PacketStore ID)

				reporting.begin(new TimelinessReportingInput(descriptor.getDataTakeId(), packetStoreIDs, satellite),
						new ReportingMessage("Start timeliness lookup for %s", descriptor.getProductName()));

				final List<String> timelinesses = new ArrayList<>();

				for (final String packetStoreID : packetStoreIDs) {

					final String packetStoreType = packetStoreTypes.get(satellite + "-" + packetStoreID);
					final String timeliness = packetStoreTypeTimelinesses.get(packetStoreType);

					if (timeliness == null) {
						final String errMess = String.format(
								"No timeliness configured for packetStoreID %s with packetStoreType %s", packetStoreID,
								packetStoreType);
						reporting.error(new ReportingMessage(errMess));
						throw new MetadataExtractionException(new RuntimeException(errMess));
					}
					timelinesses.add(timeliness);
				}
				final String timeliness = maxTimeliness(timelinesses);
				reporting.end(new TimelinessReportingOutput(timeliness),
						new ReportingMessage("Timeliness for %s is: %s", descriptor.getProductName(), timeliness));
				metadataJSONObject.put("timeliness", timeliness);
				metadataJSONObject.remove("packetStoreID"); // the packetStoreID was only needed to compute timeliness
			}
			// S1PRO-1030 GP and HKTM products
			else if (productType.contains("GP_RAW_") || productType.contains("HK_RAW_")) {
				LOGGER.debug("Setting timeliness to NRT for {} product {}", productType, descriptor.getFilename());
				// FIXME S1PRO-1030 should be taken from the application.yaml ??
				metadataJSONObject.put("timeliness", "NRT");
			} else {
				// FIXME S1PRO-1030 what should be exactly done if it is missing
				LOGGER.error("No packetStoreID found for product in manifest: {} ", manifestFile);
			}

			LOGGER.debug("composed Json: {} ", metadataJSONObject);
			return metadataJSONObject;

		} catch (final JSONException e) {
			LOGGER.error("Extraction of L0 segment file metadata failed", e);
			throw new MetadataExtractionException(e);
		}
	}

	public JSONObject processS2Metadata(S2FileDescriptor descriptor, File safeMetadataFile, File inventoryMetadataFile)
			throws MetadataExtractionException, MetadataMalformedException {
		JSONObject safeMetadata = transformXMLWithXSLTToJSON(safeMetadataFile,
				new File(this.xsltDirectory + XSLT_S2_MANIFEST));
		JSONObject inventoryMetadata = transformXMLWithXSLTToJSON(inventoryMetadataFile,
				new File(this.xsltDirectory + XSLT_S2_INVENTORY));
		
		JSONObject metadataJSONObject = this.mergeJSONObjects(safeMetadata, inventoryMetadata);
		metadataJSONObject = checkS2Metadata(metadataJSONObject);
		metadataJSONObject = processS2Coordinates(metadataJSONObject);
		metadataJSONObject = putS2FileMetadataToJSON(metadataJSONObject, descriptor);
		
		LOGGER.debug("composed Json: {} ", metadataJSONObject);
		return metadataJSONObject;
	}

	/**
	 * Extracts metadata from S3 level product files (L0 - ISIP-format)
	 * 
	 * @param descriptor The file descriptor of the file
	 * @param file       The input manifest File
	 * @return json object with extracted metadata
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public JSONObject processIIFFile(S3FileDescriptor descriptor, File file)
			throws MetadataExtractionException, MetadataMalformedException {
		JSONObject metadataJSONObject = transformXMLWithXSLTToJSON(file,
				new File(this.xsltDirectory + XSLT_S3_IIF_XML));

		// Add metadata from file descriptor
		metadataJSONObject = checkS3MetadataForLevelProducts(metadataJSONObject);
		metadataJSONObject = putS3FileMetadataToJSON(metadataJSONObject, descriptor);

		LOGGER.debug("composed Json: {} ", metadataJSONObject);
		return metadataJSONObject;
	}

	/**
	 * Extracts metadata from S3 auxiliary files (xfdumanifest.xml)
	 * 
	 * @param descriptor The file descriptor of the file
	 * @param file       The input manifest File
	 * @return json object with extracted metadata
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public JSONObject processAuxXFDUFile(final S3FileDescriptor descriptor, final File file)
			throws MetadataExtractionException, MetadataMalformedException {
		JSONObject metadataJSONObject = transformXMLWithXSLTToJSON(file,
				new File(this.xsltDirectory + XSLT_S3_AUX_XFDU_XML));

		// Add metadata from file descriptor
		metadataJSONObject = checkS3MetadataForAux(metadataJSONObject);
		metadataJSONObject = putS3FileMetadataToJSON(metadataJSONObject, descriptor);

		LOGGER.debug("composed Json: {} ", metadataJSONObject);
		return metadataJSONObject;
	}

	/**
	 * Extracts metadata from S3 level product files (Intermediates - XFDU-Format)
	 * 
	 * @param descriptor The file descriptor of the file
	 * @param file       The input manifest File
	 * @return json object with extracted metadata
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public JSONObject processProductXFDUFile(S3FileDescriptor descriptor, File file)
			throws MetadataExtractionException, MetadataMalformedException {
		JSONObject metadataJSONObject = transformXMLWithXSLTToJSON(file,
				new File(this.xsltDirectory + XSLT_S3_XFDU_XML));

		// Add metadata from file descriptor
		metadataJSONObject = checkS3MetadataForLevelProducts(metadataJSONObject);
		metadataJSONObject = processS3Coordinates(metadataJSONObject);
		metadataJSONObject = putS3FileMetadataToJSON(metadataJSONObject, descriptor);

		LOGGER.debug("composed Json: {} ", metadataJSONObject);
		return metadataJSONObject;
	}

	public String maxTimeliness(final List<String> timelinesses) {
		for (final String currentPriorityTimeliness : timelinessPriorityFromHighToLow) {
			if (timelinesses.contains(currentPriorityTimeliness)) {
				return currentPriorityTimeliness;
			}
		}
		throw new RuntimeException("Invalid timeliness values: " + timelinesses);
	}

	/**
	 * Function which extracts metadata from product
	 * 
	 * @param descriptor    The file descriptor of the file
	 * @param productFamily product family
	 * @param manifestFile  The input manifest file
	 * @return json object with extracted metadata
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public JSONObject processProduct(final OutputFileDescriptor descriptor, final ProductFamily productFamily,
			final File manifestFile) throws MetadataExtractionException, MetadataMalformedException {

		final File xsltFile = new File(this.xsltDirectory + xsltMap.get(productFamily));
		LOGGER.debug("extracting metadata for descriptor: {} ", descriptor);
		JSONObject metadataJSONObject = transformXMLWithXSLTToJSON(manifestFile, xsltFile);

		metadataJSONObject = removeEmptyStringElementsFromPolarisationChannelsArray(metadataJSONObject);

		metadataJSONObject = putCommonMetadataToJSON(metadataJSONObject, descriptor);

		try {

			if (metadataJSONObject.has("sliceCoordinates") // for use as polygon
					&& !metadataJSONObject.getString("sliceCoordinates").isEmpty()) {
				metadataJSONObject.put("sliceCoordinates",
						processCoordinates(manifestFile, descriptor, metadataJSONObject.getString("sliceCoordinates")));
			}

			if (metadataJSONObject.has("coordinates") // for use as as-is metadata attribute (PRIP)
					&& !metadataJSONObject.getString("coordinates").isEmpty()) {
				metadataJSONObject.put("coordinates",
						convertCoordinatesToClosedForm(metadataJSONObject.getString("coordinates")));
			}

			if (ProductFamily.L0_ACN.equals(productFamily) || ProductFamily.L0_SLICE.equals(productFamily)) {

				if (!metadataJSONObject.has("sliceNumber")
						|| "".equals(metadataJSONObject.get("sliceNumber").toString())) {
					metadataJSONObject.put("sliceNumber", 1);
				} else if (StringUtils.isEmpty(metadataJSONObject.get("sliceNumber").toString())) {
					metadataJSONObject.put("sliceNumber", 1);
				}
				if (!metadataJSONObject.has("totalNumberOfSlice")
						|| "".equals(metadataJSONObject.get("totalNumberOfSlice").toString())) {
					if (Arrays.asList("A", "C", "N").contains(descriptor.getProductClass())) {
						if (metadataJSONObject.has("startTime") && metadataJSONObject.has("stopTime")) {
							metadataJSONObject.put("totalNumberOfSlice", totalNumberOfSlice(
									metadataJSONObject.getString("startTime"), metadataJSONObject.getString("stopTime"),
									descriptor.getSwathtype().matches("S[1-6]") ? "SM" : descriptor.getSwathtype()));
						}
					}
				}
			}

			LOGGER.debug("composed Json: {} ", metadataJSONObject);
			return metadataJSONObject;

		} catch (final JSONException e) {
			LOGGER.error("Extraction of metadata failed", e);
			throw new MetadataExtractionException(e);
		}
	}

	private JSONObject checkS2Metadata(final JSONObject metadataJSONObject) throws MetadataMalformedException, MetadataExtractionException {
		try {

			if (metadataJSONObject.has("startTime")) {
				try {
					metadataJSONObject.put("startTime",
							DateUtils.convertToMetadataDateTimeFormat((String) metadataJSONObject.get("startTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("startTime");
				}
			}

			if (metadataJSONObject.has("stopTime")) {
				try {
					metadataJSONObject.put("stopTime",
							DateUtils.convertToMetadataDateTimeFormat((String) metadataJSONObject.get("stopTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("stopTime");
				}
			}

			if (metadataJSONObject.has("creationTime")) {
				try {
					metadataJSONObject.put("creationTime",
							DateUtils.convertToMetadataDateTimeFormat(metadataJSONObject.getString("creationTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("creationTime");
				}
			}

			return metadataJSONObject;

		} catch (final JSONException e) {
			LOGGER.error("Error while extraction of config file metadata ", e);
			throw new MetadataExtractionException(e);
		}
	}
	
	/**
	 * Check validityStartTime, validityStopTime and creationTime on
	 * S3-Aux-Metadata-Objects
	 */
	private JSONObject checkS3MetadataForAux(final JSONObject metadataJSONObject)
			throws MetadataExtractionException, MetadataMalformedException {
		try {
			if (metadataJSONObject.has("validityStartTime")) {
				try {
					metadataJSONObject.put("validityStartTime", DateUtils
							.convertToMetadataDateTimeFormat((String) metadataJSONObject.get("validityStartTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("validityStartTime");
				}
			}

			if (metadataJSONObject.has("validityStopTime")) {

				final String validStopTime = (String) metadataJSONObject.get("validityStopTime");

				if (validStopTime.contains("9999-")) {
					metadataJSONObject.put("validityStopTime", "9999-12-31T23:59:59.999999Z");
				} else {
					try {
						metadataJSONObject.put("validityStopTime",
								DateUtils.convertToMetadataDateTimeFormat(validStopTime));
					} catch (final DateTimeParseException e) {
						throw new MetadataMalformedException("validityStopTime");
					}
				}

			} else {
				metadataJSONObject.put("validityStopTime", "9999-12-31T23:59:59.999999Z");
			}

			if (metadataJSONObject.has("creationTime")) {
				try {
					metadataJSONObject.put("creationTime",
							DateUtils.convertToMetadataDateTimeFormat(metadataJSONObject.getString("creationTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("creationTime");
				}
			}

			return metadataJSONObject;

		} catch (final JSONException e) {
			LOGGER.error("Error while extraction of config file metadata ", e);
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Check startTime, stopTime and creationTime on
	 * S3-LevelProduct-Metadata-Objects
	 */
	private JSONObject checkS3MetadataForLevelProducts(final JSONObject metadataJSONObject)
			throws MetadataExtractionException, MetadataMalformedException {
		try {

			if (metadataJSONObject.has("startTime")) {
				try {
					metadataJSONObject.put("startTime",
							DateUtils.convertToMetadataDateTimeFormat((String) metadataJSONObject.get("startTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("startTime");
				}
			}

			if (metadataJSONObject.has("stopTime")) {
				try {
					metadataJSONObject.put("stopTime",
							DateUtils.convertToMetadataDateTimeFormat((String) metadataJSONObject.get("stopTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("stopTime");
				}
			}

			if (metadataJSONObject.has("creationTime")) {
				try {
					metadataJSONObject.put("creationTime",
							DateUtils.convertToMetadataDateTimeFormat(metadataJSONObject.getString("creationTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("creationTime");
				}
			}

			return metadataJSONObject;

		} catch (final JSONException e) {
			LOGGER.error("Error while extraction of config file metadata ", e);
			throw new MetadataExtractionException(e);
		}
	}
	
	JSONObject processS2Coordinates(JSONObject metadataJSONObject) {

		if (metadataJSONObject.has("coordinates")) {
			final String rawCoords = metadataJSONObject.getString("coordinates");
			if (!rawCoords.trim().isEmpty()) {
				metadataJSONObject.put("coordinates", transformFromOpengis(rawCoords));
			} else {
				metadataJSONObject.remove("coordinates");
			}
		}

		return metadataJSONObject;
	}
	
	JSONObject processS3Coordinates(JSONObject metadataJSONObject) {

		if (metadataJSONObject.has("sliceCoordinates")) {
			final String rawCoords = metadataJSONObject.getString("sliceCoordinates");
			if (!rawCoords.trim().isEmpty()) {
				metadataJSONObject.put("sliceCoordinates", transformFromOpengis(rawCoords));
			} else {
				metadataJSONObject.remove("sliceCoordinates");
			}
		}

		return metadataJSONObject;
	}

	JSONObject transformFromOpengis(String rawCoords) {
		
		if (rawCoords.indexOf(',') != -1) {
			throw new IllegalArgumentException("space separated values are expected but contains comma");
		}
		String[] coords = rawCoords.split(" ");
		if ((coords.length % 2) != 0) {
			throw new IllegalArgumentException("lat and lon values are expected");
		}
		
		LOGGER.debug("l0 coords: {} ", rawCoords);
		
		final JSONObject geoShape = new JSONObject();
		final JSONArray geoShapeCoordinates = new JSONArray();
		geoShape.put("type", "polygon");
		
		for (int i = 0; i < coords.length; i = i + 2) {
			final String aLatitude = coords[i];
			final String aLongitude = coords[i + 1];
			geoShapeCoordinates.put(new JSONArray("[" + aLongitude + "," + aLatitude + "]"));
		}
		
		geoShape.put("coordinates", new JSONArray().put(geoShapeCoordinates));
		geoShape.put("orientation", "counterclockwise");
		return geoShape;
	}

	private JSONObject putConfigFileMetadataToJSON(final JSONObject metadataJSONObject, final AuxDescriptor descriptor)
			throws MetadataExtractionException, MetadataMalformedException {
		try {
			if (TYPES_WITH_PRODUCTNAME_BASED_VALIDITY_TIME_EXTRACTION.contains(descriptor.getProductType())) {
				Matcher matcher = AUX_TEC_AND_AUX_TRO_PRODUCTNAME_PATTERN.matcher(descriptor.getProductName());
				if (matcher.matches()) {
					try {
						metadataJSONObject.put("validityStartTime", DateUtils.convertToMetadataDateTimeFormat(matcher.group(1)));
						metadataJSONObject.put("validityStopTime", DateUtils.convertToMetadataDateTimeFormat(matcher.group(2)));						
					} catch (final DateTimeParseException e) {
						throw new MetadataMalformedException("validityStartTime/validityStopTime");
					}
				} else {
					throw new MetadataMalformedException("validityStartTime/validityStopTime");
				}
			} else {
			if (metadataJSONObject.has("validityStartTime")) {
				try {
					metadataJSONObject.put("validityStartTime", DateUtils
							.convertToMetadataDateTimeFormat((String) metadataJSONObject.get("validityStartTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("validityStartTime");
				}
			}

			if (metadataJSONObject.has("validityStopTime")) {

				final String validStopTime = (String) metadataJSONObject.get("validityStopTime");

				if (validStopTime.contains("9999-")) {
					metadataJSONObject.put("validityStopTime", "9999-12-31T23:59:59.999999Z");
				} else {
					try {
						metadataJSONObject.put("validityStopTime",
								DateUtils.convertToMetadataDateTimeFormat(validStopTime));
					} catch (final DateTimeParseException e) {
						throw new MetadataMalformedException("validityStopTime");
					}
				}

				} else {
					metadataJSONObject.put("validityStopTime", "9999-12-31T23:59:59.999999Z");
				}
			}

			if (TYPES_WITH_PRODUCTNAME_BASED_GENERATION_TIME_EXTRACTION.contains(descriptor.getProductType())) {
				Matcher matcher = AUX_TEC_AND_AUX_TRO_PRODUCTNAME_PATTERN.matcher(descriptor.getProductName());
				if (matcher.matches()) {
					try {
						metadataJSONObject.put("creationTime", DateUtils.convertToMetadataDateTimeFormat(matcher.group(3)));
					} catch (final DateTimeParseException e) {
						throw new MetadataMalformedException("creationTime");
					}
				} else {
					throw new MetadataMalformedException("creationTime");
				}
			} else {
				if (metadataJSONObject.has("creationTime")) {
					try {
						metadataJSONObject.put("creationTime",
								DateUtils.convertToMetadataDateTimeFormat(metadataJSONObject.getString("creationTime")));
					} catch (final DateTimeParseException e) {
						throw new MetadataMalformedException("creationTime");
					}
				}
			}

			if (metadataJSONObject.has("selectedOrbitFirstAzimuthTimeUtc")) {
				try {
					metadataJSONObject.put("selectedOrbitFirstAzimuthTimeUtc",
							DateUtils.convertToMetadataDateTimeFormat(
									metadataJSONObject.getString("selectedOrbitFirstAzimuthTimeUtc")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("selectedOrbitFirstAzimuthTimeUtc");
				}
			}

			metadataJSONObject.put("productName", descriptor.getProductName());

			if (!metadataJSONObject.has("productClass") || "".equals((String) metadataJSONObject.get("productClass"))) {
				metadataJSONObject.put("productClass", descriptor.getProductClass());
			}

			if (!metadataJSONObject.has("productType") || "".equals((String) metadataJSONObject.get("productType"))) {
				metadataJSONObject.put("productType", descriptor.getProductType());
			}

			metadataJSONObject.put(MissionId.FIELD_NAME, descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.now()));
			metadataJSONObject.put("productFamily", descriptor.getProductFamily().name());

			return metadataJSONObject;

		} catch (final JSONException e) {
			LOGGER.error("Error while extraction of config file metadata ", e);
			throw new MetadataExtractionException(e);
		}
	}

	private JSONObject putEdrsSessionMetadataToJSON(final JSONObject metadataJSONObject,
			final EdrsSessionFileDescriptor descriptor) throws MetadataExtractionException {
		try {
			metadataJSONObject.put("channelId", descriptor.getChannel());
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productType", descriptor.getEdrsSessionFileType().name());
			metadataJSONObject.put("sessionId", descriptor.getSessionIdentifier());
			metadataJSONObject.put(MissionId.FIELD_NAME, descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("stationCode", descriptor.getStationCode());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.now()));
			metadataJSONObject.put("productFamily", descriptor.getProductFamily().name());

			return metadataJSONObject;

		} catch (final JSONException e) {
			LOGGER.error("Error while extraction of EDRS session metadata ", e);
			throw new MetadataExtractionException(e);
		}
	}

	private JSONObject putCommonMetadataToJSON(final JSONObject metadataJSONObject,
			final OutputFileDescriptor descriptor) throws MetadataExtractionException, MetadataMalformedException {

		try {
			if (metadataJSONObject.has("startTime")) {
				try {
					final String t = DateUtils
							.convertToMetadataDateTimeFormat(metadataJSONObject.getString("startTime"));
					metadataJSONObject.put("startTime", t);
					metadataJSONObject.put("validityStartTime", t);
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("validityStartTime");
				}
			}

			if (metadataJSONObject.has("stopTime")) {
				try {
					final String t = DateUtils
							.convertToMetadataDateTimeFormat(metadataJSONObject.getString("stopTime"));
					metadataJSONObject.put("stopTime", t);
					metadataJSONObject.put("validityStopTime", t);
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("validityStopTime");
				}
			}

			final String dt = DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.now());

			metadataJSONObject.put("productName", descriptor.getProductName());

			if (!metadataJSONObject.has("productClass") || "".equals((String) metadataJSONObject.get("productClass"))) {
				metadataJSONObject.put("productClass", descriptor.getProductClass());
			}

			if (!metadataJSONObject.has("productType") || "".equals((String) metadataJSONObject.get("productType"))) {
				metadataJSONObject.put("productType", descriptor.getProductType());
			}

			metadataJSONObject.put("resolution", descriptor.getResolution());
			metadataJSONObject.put(MissionId.FIELD_NAME, descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());

			if (!metadataJSONObject.has("swathtype") || "".equals((String) metadataJSONObject.get("swathtype"))) {
				metadataJSONObject.put("swathtype", descriptor.getSwathtype());
			}

			metadataJSONObject.put("polarisation", descriptor.getPolarisation());
			metadataJSONObject.put("dataTakeId", descriptor.getDataTakeId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", dt);
			metadataJSONObject.put("creationTime", dt);
			metadataJSONObject.put("productFamily", descriptor.getProductFamily().name());
			// TODO S1PRO-1030 in future it can be DEBUG or REPROCESSING as well
			metadataJSONObject.put("processMode", "NOMINAL");

			return metadataJSONObject;

		} catch (final JSONException e) {
			LOGGER.error("Error while extraction of common metadata", e);
			throw new MetadataExtractionException(e);
		}

	}

	private JSONObject removeEmptyStringElementsFromPolarisationChannelsArray(final JSONObject metadataJSONObject) {
		if (metadataJSONObject.has("polarisationChannels")) {
			JSONArray polarisationChannels = (JSONArray) metadataJSONObject.get("polarisationChannels");
			int idx = polarisationChannels.length();
			while (--idx >= 0) {
				if ("".equals(polarisationChannels.get(idx))) {
					polarisationChannels.remove(idx);
				}
			}
		}
		return metadataJSONObject;
	}
	
	/**
	 * Common metadata from FileDescriptor to insert into S2-Metadata-Objects
	 */
	private JSONObject putS2FileMetadataToJSON(final JSONObject metadataJSONObject, final S2FileDescriptor descriptor)
			throws MetadataExtractionException {
		try {
			metadataJSONObject.put("productName", descriptor.getProductName());

			if (!metadataJSONObject.has("productClass") || "".equals((String) metadataJSONObject.get("productClass"))) {
				metadataJSONObject.put("productClass", descriptor.getProductClass());
			}

			if (!metadataJSONObject.has("productType") || "".equals((String) metadataJSONObject.get("productType"))) {
				metadataJSONObject.put("productType", descriptor.getProductType());
			}

			metadataJSONObject.put(MissionId.FIELD_NAME, descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("productFamily", descriptor.getProductFamily().name());
			// TODO S1PRO-1030 in future it can be DEBUG or REPROCESSING as well
			metadataJSONObject.put("processMode", "NOMINAL");

			return metadataJSONObject;
		} catch (final JSONException e) {
			LOGGER.error("Error while extraction of config file metadata ", e);
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Common metadata from FileDescriptor to insert into S3-Metadata-Objects
	 */
	private JSONObject putS3FileMetadataToJSON(final JSONObject metadataJSONObject, final S3FileDescriptor descriptor)
			throws MetadataExtractionException {
		try {
			metadataJSONObject.put("productName", descriptor.getProductName());

			if (!metadataJSONObject.has("productClass") || "".equals((String) metadataJSONObject.get("productClass"))) {
				metadataJSONObject.put("productClass", descriptor.getProductClass());
			}

			if (!metadataJSONObject.has("productType") || "".equals((String) metadataJSONObject.get("productType"))) {
				metadataJSONObject.put("productType", descriptor.getProductType());
			}

			metadataJSONObject.put(MissionId.FIELD_NAME, descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("productFamily", descriptor.getProductFamily().name());
			metadataJSONObject.put("instanceId", descriptor.getInstanceId());
			metadataJSONObject.put("generatingCentre", descriptor.getGeneratingCentre());
			metadataJSONObject.put("classId", descriptor.getClassId());
			// TODO S1PRO-1030 in future it can be DEBUG or REPROCESSING as well
			metadataJSONObject.put("processMode", "NOMINAL");

			return metadataJSONObject;
		} catch (final JSONException e) {
			LOGGER.error("Error while extraction of config file metadata ", e);
			throw new MetadataExtractionException(e);
		}
	}

	private JSONObject transformXMLWithXSLTToJSON(final File inputXMLFile, final File xsltFile)
			throws MetadataExtractionException {

		try {
			final Transformer transformer = transFactory.newTransformer(new StreamSource(xsltFile));
			final ByteArrayOutputStream transformationStream = new ByteArrayOutputStream();

			transformer.transform(new StreamSource(inputXMLFile), new StreamResult(transformationStream));
			JSONObject metadata = XML.toJSONObject(transformationStream.toString(Charset.defaultCharset().name()));
			return enforceFieldTypes(metadata);

		} catch (IOException | TransformerException | JSONException e) {
			LOGGER.error("Error while transformation of  input XML file to JSON", e);
			throw new MetadataExtractionException(e);
		}
	}
	
	JSONObject enforceFieldTypes(JSONObject metadata) {
		JSONObject result = new JSONObject();
		Iterator<String> keys = metadata.keys();
		while(keys.hasNext()) {
			String key = keys.next();
			String type = Objects.toString(fieldTypes.get(key), "undefined");
			if ("undefined".equals(type) || "string".equals(type) || !"".equals(metadata.get(key))) {
				switch(type) {
					case "long": result.put(key, metadata.getLong(key)); break;
					case "double": result.put(key, metadata.getDouble(key)); break;
					case "boolean": result.put(key, metadata.getBoolean(key)); break;
					case "string": result.put(key, String.valueOf(metadata.get(key))); break;
					case "date": result.put(key, metadata.getString(key)); break; // date string
					default: result.put(key, metadata.get(key)); // best guess
				}
			}
		}
		return result;
	}

	/**
	 * Function which transform the raw coordinates in the good format
	 * 
	 * @param rawCoordinates
	 * @param productName
	 * @return the coordinates in good format
	 * @throws MetadataExtractionException
	 */
	private JSONObject processCoordinates(final File manifest, final OutputFileDescriptor descriptor,
			final String rawCoordinates) throws MetadataExtractionException {
		try {
			final String productType = descriptor.getProductType();
			// ------------ LEVEL 0 --------------------//
			if (productType.matches(".._RAW__0.")) {

				if (productType.matches("(WV|RF|Z[1-6]|ZE|ZI|ZW)_RAW__0.")) {
					// Only 2 Nadir-Points in manifest -->
					// PIC HANDLES WRONGLY
					return processCoordinatesforWVL0(rawCoordinates);
				} else {
					// Should be 4 coordinates --> else exception
					// Copy from manifest --> manifest is counterclockwise
					return processCoordinatesAsIS(rawCoordinates);
				}
			}
			// ------------ LEVEL 1 --------------------//
			else if (productType.matches(".._(GRD|SLC)._1.") || productType.matches(".._ETA_...")) {

				if (productType.startsWith("WV")) {
					// WV L1: derive larger footprint from multiple smaller
					// patches
					return WVFootPrintExtension.getBoundingPolygon(manifest.getAbsolutePath());
				} else {
					// Should be 4 coordinates --> else exception
					// copy and adjust from manifest --> manifest is clockwise
					return processCoordinatesforL1andL2(rawCoordinates);
				}
			} // ------------ LEVEL 2 --------------------//
			else if (productType.matches(".._OCN__2.")) {

				if (productType.startsWith("WV_OCN")) {
					// WV L2:
					// derive larger footprint from multiple smaller patches
					return WVFootPrintExtension.getBoundingPolygon(manifest.getAbsolutePath());
				} else {
					// should be 4 coordinates --> else exception
					// copy and adjust from manifest --> manifest is clockwise
					return processCoordinatesforL1andL2(rawCoordinates);
				}
			} else {

				throw new MetadataExtractionException(new NotImplementedException(
						String.format("handling not implemented for productType %s ", productType)));
			}

		} catch (final JSONException e) {
			throw new MetadataExtractionException(e);
		}
	}

	public final static String convertCoordinatesToClosedForm(final String rawCoordinates) {
		final String[] elements = rawCoordinates.split(" ");
		return elements[0].equals(elements[elements.length - 1]) ? rawCoordinates : rawCoordinates + " " + elements[0];
	}

	/**
	 * Workaround for Elastic Search to be able to find a date line crossing polygon
	 * by an intersecting polygon:
	 * If maximum longitudal width is larger than 180° it is assumed that the polygon is crossing the date line. 
	 * Then if all but one of the longitude values are negative, the positive value is shifted by -360° or 
	 * if all but one of the longitude values are positive, the negative value is shifted by +360°.
	 * 
	 * @param rawCoordinatesFromManifest
	 * @return improved raw coordinates
	 */
	protected String improveRawCoordinatesIfDateLineCrossing(final String rawCoordinatesFromManifest) {

		String improvedRawCoordinatesFromManifest = rawCoordinatesFromManifest;
		boolean modified = false;

		String[] points = rawCoordinatesFromManifest.split(" ");

		if (points.length == 5 && points[0].equals(points[4])) {
			points = Arrays.copyOf(points, 4);
		}

		if (points.length != 4) {
			throw new IllegalArgumentException("4 coordinates are expected");
		} else {
			final String[] aPoint = points[0].split(",");
			Double aLongitude = Double.valueOf(aPoint[1]);

			final String[] bPoint = points[1].split(",");
			Double bLongitude = Double.valueOf(bPoint[1]);

			final String[] cPoint = points[2].split(",");
			Double cLongitude = Double.valueOf(cPoint[1]);

			final String[] dPoint = points[3].split(",");
			Double dLongitude = Double.valueOf(dPoint[1]);

			final int offset = 360;

			final Double maxLongitudeDiff = calculateMaxDifference(new Double[] {aLongitude, bLongitude, cLongitude, dLongitude});
			
			// Shifting coordinates only if crossing date line but not when crossing 0° meridian
			if (maxLongitudeDiff > 180.0) {
				// All negative but one -> decrease one by offset
				if (aLongitude >= 0 && bLongitude < 0 && cLongitude < 0 && dLongitude < 0) {
					aLongitude = aLongitude - offset;
					aPoint[1] = aLongitude.toString();
					points[0] = String.join(",", aPoint);
					modified = true;
				} else if (bLongitude >= 0 && aLongitude < 0 && cLongitude < 0 && dLongitude < 0) {
					bLongitude = bLongitude - offset;
					bPoint[1] = bLongitude.toString();
					points[1] = String.join(",", bPoint);
					modified = true;
				} else if (cLongitude >= 0 && aLongitude < 0 && bLongitude < 0 && dLongitude < 0) {
					cLongitude = cLongitude - offset;
					cPoint[1] = cLongitude.toString();
					points[2] = String.join(",", cPoint);
					modified = true;
				} else if (dLongitude >= 0 && aLongitude < 0 && bLongitude < 0 && cLongitude < 0) {
					dLongitude = dLongitude - offset;
					dPoint[1] = dLongitude.toString();
					points[3] = String.join(",", dPoint);
					modified = true;
				}

				// All positive but one -> increase one by offset
				if (aLongitude <= 0 && bLongitude > 0 && cLongitude > 0 && dLongitude > 0) {
					aLongitude = aLongitude + offset;
					aPoint[1] = aLongitude.toString();
					points[0] = String.join(",", aPoint);
					modified = true;
				} else if (bLongitude <= 0 && aLongitude > 0 && cLongitude > 0 && dLongitude > 0) {
					bLongitude = bLongitude + offset;
					bPoint[1] = bLongitude.toString();
					points[1] = String.join(",", bPoint);
					modified = true;
				} else if (cLongitude <= 0 && aLongitude > 0 && bLongitude > 0 && dLongitude > 0) {
					cLongitude = cLongitude + offset;
					cPoint[1] = cLongitude.toString();
					points[2] = String.join(",", cPoint);
					modified = true;
				} else if (dLongitude <= 0 && aLongitude > 0 && bLongitude > 0 && cLongitude > 0) {
					dLongitude = dLongitude + offset;
					dPoint[1] = dLongitude.toString();
					points[3] = String.join(",", dPoint);
					modified = true;
				}
				improvedRawCoordinatesFromManifest = String.join(" ", points);
			}
			
			if (modified) {
				LOGGER.info("Maximum longitudal width is " + maxLongitudeDiff + "° -> Assuming that the polygon is crossing the date line -> Shifting from {} to {} ", rawCoordinatesFromManifest,
						improvedRawCoordinatesFromManifest);
			}
		}

		return improvedRawCoordinatesFromManifest;
	}

	/**
	 * Calculate maximm difference of an array of values.
	 * 
	 * @param values
	 * @return maxDifference
	 */
	protected Double calculateMaxDifference(final Double[] values) {
		Double max = 0.0;
		for (int i = 0; i < values.length - 1; i++) {
			Double d = Math.abs(values[i] - values[i + 1]);
			if (d > max)
				max = d;
		}
		return max;
	}

	private JSONObject processCoordinatesforWVL0(final String rawCoordinatesFromManifest) {
		// Snippet from manifest
		// -74.8571,-120.3411 -75.4484,-121.9204
		final String[] points = rawCoordinatesFromManifest.split(" ");

		if (points.length != 2) {
			throw new IllegalArgumentException("2 coordinates are expected");
		}

		final String[] startNadirPoint = points[0].split(",");
		final String startNadirLatitude = startNadirPoint[0];
		final String startNadirLongitude = startNadirPoint[1];

		final String[] stopNadirPoint = points[1].split(",");
		final String stopNadirLatitude = stopNadirPoint[0];
		final String stopNadirLongitude = stopNadirPoint[1];

		final JSONObject geoShape = new JSONObject();
		final JSONArray geoShapeCoordinates = new JSONArray();
		geoShape.put("type", "linestring");
		geoShapeCoordinates.put(new JSONArray("[" + startNadirLongitude + "," + startNadirLatitude + "]"));
		geoShapeCoordinates.put(new JSONArray("[" + stopNadirLongitude + "," + stopNadirLatitude + "]"));
		geoShape.put("coordinates", geoShapeCoordinates);
		// geoShape.put("orientation", "counterclockwise");

		return geoShape;
	}

	private JSONObject processCoordinatesforL1andL2(final String rawCoordinatesFromManifest) {

		LOGGER.debug("l1/l2 coords: {} ", rawCoordinatesFromManifest);
		// Snippet from manifest
		// 12.378114,48.279240 12.829241,50.603844 11.081389,50.958828
		// 10.625828,48.649940

		String[] points = improveRawCoordinatesIfDateLineCrossing(rawCoordinatesFromManifest).split(" ");

		if (points.length == 5 && points[0].equals(points[4])) {
			points = Arrays.copyOf(points, 4);
		}

		if (points.length != 4) {
			throw new IllegalArgumentException("4 coordinates are expected");
		}

		// permutate DCBA to ADCBA
		final String[] dPoint = points[0].split(",");
		final String dLatitude = dPoint[0];
		final String dLongitude = dPoint[1];

		final String[] cPoint = points[1].split(",");
		final String cLatitude = cPoint[0];
		final String cLongitude = cPoint[1];

		final String[] bPoint = points[2].split(",");
		final String bLatitude = bPoint[0];
		final String bLongitude = bPoint[1];

		final String[] aPoint = points[3].split(",");
		final String aLatitude = aPoint[0];
		final String aLongitude = aPoint[1];

		final JSONObject geoShape = new JSONObject();
		final JSONArray geoShapeCoordinates = new JSONArray();

		geoShape.put("type", "polygon");

		geoShapeCoordinates.put(new JSONArray("[" + aLongitude + "," + aLatitude + "]"));
		geoShapeCoordinates.put(new JSONArray("[" + dLongitude + "," + dLatitude + "]"));
		geoShapeCoordinates.put(new JSONArray("[" + cLongitude + "," + cLatitude + "]"));
		geoShapeCoordinates.put(new JSONArray("[" + bLongitude + "," + bLatitude + "]"));
		geoShapeCoordinates.put(new JSONArray("[" + aLongitude + "," + aLatitude + "]"));

		geoShape.put("coordinates", new JSONArray().put(geoShapeCoordinates));
		geoShape.put("orientation", "counterclockwise");

		return geoShape;
	}

	private JSONObject processCoordinatesAsIS(final String rawCoordinatesFromManifest) {
		// Snippet from manifest
		// 36.7787,86.8273 38.7338,86.4312 38.4629,83.6235 36.5091,84.0935
		// 36.7787,86.8273
		LOGGER.debug("l0 coords: {} ", rawCoordinatesFromManifest);
		String[] points = improveRawCoordinatesIfDateLineCrossing(rawCoordinatesFromManifest).split(" ");

		if (points.length == 5 && points[0].equals(points[4])) {
			points = Arrays.copyOf(points, 4);
		}

		if (points.length != 4) {
			throw new IllegalArgumentException("4 coordinates are expected");
		}

		// permutate ABCD to ABCDAbuildEdrsSessionFileMetadata
		final String[] aPoint = points[0].split(",");
		final String aLatitude = aPoint[0];
		final String aLongitude = aPoint[1];

		final String[] bPoint = points[1].split(",");
		final String bLatitude = bPoint[0];
		final String bLongitude = bPoint[1];

		final String[] cPoint = points[2].split(",");
		final String cLatitude = cPoint[0];
		final String cLongitude = cPoint[1];

		final String[] dPoint = points[3].split(",");
		final String dLatitude = dPoint[0];
		final String dLongitude = dPoint[1];

		final JSONObject geoShape = new JSONObject();
		final JSONArray geoShapeCoordinates = new JSONArray();

		geoShape.put("type", "polygon");

		geoShapeCoordinates.put(new JSONArray("[" + aLongitude + "," + aLatitude + "]"));
		geoShapeCoordinates.put(new JSONArray("[" + bLongitude + "," + bLatitude + "]"));
		geoShapeCoordinates.put(new JSONArray("[" + cLongitude + "," + cLatitude + "]"));
		geoShapeCoordinates.put(new JSONArray("[" + dLongitude + "," + dLatitude + "]"));
		geoShapeCoordinates.put(new JSONArray("[" + aLongitude + "," + aLatitude + "]"));

		geoShape.put("coordinates", new JSONArray().put(geoShapeCoordinates));
		geoShape.put("orientation", "counterclockwise");

		return geoShape;
	}

	/**
	 * Function which return the number of slices in a segment
	 * 
	 * @param startTimeLong
	 * @param stopTimeLong
	 * @param type
	 * @return an int which is the number of Slices
	 */
	int totalNumberOfSlice(final String startTime, final String stopTime, final String type) {
		LOGGER.trace("start time: {}, stop time: {}", startTime, stopTime);

		int totalNumberOfSlices = 1;

		final float sliceLength = this.typeSliceLength.get(type);
		LOGGER.trace("slice length for type {}: {}s", type, sliceLength);

		if (sliceLength <= 0) {
			LOGGER.info("no slice information or slice length = 0s");
			LOGGER.info("total number of slices: {}", totalNumberOfSlices);
			return totalNumberOfSlices;
		}
		final float overlap = this.typeOverlap.get(type);
		LOGGER.trace("slice overlap for type {}: {}s", type, overlap);

		final float durationSeconds = ChronoUnit.MICROS.between(DateUtils.parse(startTime), DateUtils.parse(stopTime))
				/ 1000000f;
		LOGGER.trace("duration: {}s", durationSeconds);

		final float numberOfSlices = (durationSeconds - overlap) / sliceLength;
		LOGGER.trace("number of slices: {}", numberOfSlices);

		final double fracNumberOfSlicesMultipliedBySliceLength = (numberOfSlices - Math.floor(numberOfSlices))
				* sliceLength;
		LOGGER.trace("FRAC(number of slices) * slice length = {}", fracNumberOfSlicesMultipliedBySliceLength);

		if (fracNumberOfSlicesMultipliedBySliceLength < overlap) {
			LOGGER.trace("{} < {} (slice overlap) ==> total number of slices = FLOOR({})",
					fracNumberOfSlicesMultipliedBySliceLength, overlap, numberOfSlices);
			totalNumberOfSlices = (int) Math.floor(numberOfSlices);
		} else {
			LOGGER.trace("{} >= {} (slice overlap) ==> total number of slices = CEIL({})",
					fracNumberOfSlicesMultipliedBySliceLength, overlap, numberOfSlices);
			totalNumberOfSlices = (int) Math.ceil(numberOfSlices);
		}
		totalNumberOfSlices = Math.max(totalNumberOfSlices, 1);
		LOGGER.info("total number of slices: {}", totalNumberOfSlices);
		return totalNumberOfSlices;
	}
	
	private JSONObject mergeJSONObjects(JSONObject json1, JSONObject json2) {
		JSONObject mergedJSON = new JSONObject();
		try {
			mergedJSON = new JSONObject(json1, JSONObject.getNames(json1));
			for (String key : JSONObject.getNames(json2)) {
				mergedJSON.put(key, json2.get(key));
			}
		} catch (JSONException e) {
			throw new RuntimeException("JSON Exception" + e);
		}
		return mergedJSON;
	}

}
