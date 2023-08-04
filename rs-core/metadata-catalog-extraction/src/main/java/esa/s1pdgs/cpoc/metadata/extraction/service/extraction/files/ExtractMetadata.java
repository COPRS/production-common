package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.FootprintUtil;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.AuxDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.EdrsSessionFile;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.S2FileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.S3FileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.report.TimelinessReportingInput;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.report.TimelinessReportingOutput;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.util.S2ProductNameUtil;
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
	private static final String XSLT_S2_XML = "XSLT_S2_XMLS.xslt";
	private static final String XSLT_S2_HKTM_XML = "XSLT_S2_MANIFEST.xslt";
	private static final String XSLT_S2_SAD_XML = "XSLT_S2_SAD_INVENTORY.xslt";
	private static final String XSLT_S3_AUX_XFDU_XML = "XSLT_S3_AUX_XFDU_XML.xslt";
	private static final String XSLT_S3_XFDU_XML = "XSLT_S3_XFDU_XML.xslt";
	private static final String XSLT_S3_IIF_XML = "XSLT_S3_IIF_XML.xslt";
	private static final String XSLT_FILE_PREFIX = "XSLT_";
	private static final String XSLT_FILE_SUFFIX = ".xslt";
	
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
	public ProductMetadata processEOFFile(final AuxDescriptor descriptor, final File inputMetadataFile)
			throws MetadataExtractionException, MetadataMalformedException {

		ProductMetadata metadata = transformXMLWithXSLTToJSON(inputMetadataFile,
				new File(this.xsltDirectory + XSLT_MPL_EOF));

		metadata = putConfigFileMetadataToJSON(metadata, descriptor);
		LOGGER.debug("composed Json: {} ", metadata);
		return metadata;
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
	public ProductMetadata processEOFFileWithoutNamespace(final AuxDescriptor descriptor, final File inputMetadataFile)
			throws MetadataExtractionException, MetadataMalformedException {

		ProductMetadata metadata = transformXMLWithXSLTToJSON(inputMetadataFile,
				new File(this.xsltDirectory + XSLT_MPL_EOF));

		metadata = putConfigFileMetadataToJSON(metadata, descriptor);
		LOGGER.debug("composed Json: {} ", metadata);
		return metadata;
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
	public ProductMetadata processXMLFile(final AuxDescriptor descriptor, final File inputMetadataFile)
			throws MetadataExtractionException, MetadataMalformedException {

		ProductMetadata metadata = transformXMLWithXSLTToJSON(inputMetadataFile,
				new File(this.xsltDirectory + XSLT_AUX_XML));

		metadata = putConfigFileMetadataToJSON(metadata, descriptor);
		LOGGER.debug("composed Json: {} ", metadata);
		return metadata;
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
	public ProductMetadata processSAFEFile(final AuxDescriptor descriptor, final File inputMetadataFile)
			throws MetadataExtractionException, MetadataMalformedException {

		ProductMetadata metadata = transformXMLWithXSLTToJSON(inputMetadataFile,
				new File(this.xsltDirectory + XSLT_AUX_MANIFEST));

		metadata = putConfigFileMetadataToJSON(metadata, descriptor);
		LOGGER.debug("composed Json: {} ", metadata);
		return metadata;
	}

	/**
	 * Function which extracts metadata from RAW file
	 * 
	 * @param descriptor The file descriptor of the raw file
	 * @return the json object with extracted metadata
	 * @throws MetadataExtractionException
	 */
	public ProductMetadata processRAWFile(final EdrsSessionFileDescriptor descriptor) throws MetadataExtractionException {
		final ProductMetadata metadata = putEdrsSessionMetadataToJSON(new ProductMetadata(), descriptor);
		LOGGER.debug("composed Json: {} ", metadata);
		return metadata;
	}

	/**
	 * Function which extracts metadata from SESSION file
	 * 
	 * @param descriptor The file descriptor of the session file
	 * @return the json object with extracted metadata
	 * @throws MetadataExtractionException
	 */
	public ProductMetadata processSESSIONFile(final EdrsSessionFileDescriptor descriptor, final File file)
			throws MetadataExtractionException {
		try {
			final ProductMetadata metadata = putEdrsSessionMetadataToJSON(new ProductMetadata(), descriptor);

//			final String name = new File(descriptor.getRelativePath()).getName();
//			final File file = new File(localDirectory, name);

			final EdrsSessionFile edrsSessionFile = (EdrsSessionFile) xmlConverter
					.convertFromXMLToObject(file.getPath());

			metadata.put("startTime", DateUtils.convertToAnotherFormat(edrsSessionFile.getStartTime(),
					EdrsSessionFile.TIME_FORMATTER, DateUtils.METADATA_DATE_FORMATTER));

			metadata.put("stopTime", DateUtils.convertToAnotherFormat(edrsSessionFile.getStopTime(),
					EdrsSessionFile.TIME_FORMATTER, DateUtils.METADATA_DATE_FORMATTER));

			metadata.put("rawNames",
					edrsSessionFile.getRawNames().stream().map(r -> r.getFileName()).collect(Collectors.toList()));

			LOGGER.debug("composed Json: {} ", metadata);
			return metadata;

		} catch (MetadataMalformedException | IOException | JAXBException e) {
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
	public ProductMetadata processL0Segment(final OutputFileDescriptor descriptor, final File manifestFile,
			final ReportingFactory reportingFactory) throws MetadataExtractionException {

		final File xsltFile = new File(this.xsltDirectory + XSLT_L0_SEGMENT_MANIFEST);
		LOGGER.debug("extracting metadata for descriptor: {} ", descriptor);

		try {
			ProductMetadata metadata = transformXMLWithXSLTToJSON(manifestFile, xsltFile);
			
			metadata = removeEmptyStringElementsFromPolarisationChannelsArray(metadata);

			metadata = putCommonMetadataToJSON(metadata, descriptor);

			final String productType = descriptor.getProductType();

			if (productType.contains("GP_RAW_") || productType.contains("HK_RAW_")) {
				metadata.remove("segmentCoordinates");
				LOGGER.debug("segment coordinates removed for product {}", descriptor.getFilename());
				// no coord
			} else {

				if (metadata.has("segmentCoordinates")) {
					final String coords = metadata.getString("segmentCoordinates");
					
					if (!coords.trim().isEmpty()) {
						
						//S1PRO-2732,S1OPS-673,S1OPS-1212: expected number of coordinates is 2 for ZS, ZE, ZI and ZW
						if (productType.matches("(Z[1-6]|ZE|ZI|ZW)_RAW__0.") && coords.trim().split(" ").length != 2) {
								metadata.remove("segmentCoordinates");
								LOGGER.debug("segment coordinates removed for product {}", descriptor.getFilename());
						} else {
							metadata.put("segmentCoordinates",
								processCoordinates(manifestFile, descriptor, coords));
						}
					}
				}
			}

			if (metadata.has("packetStoreID")) {

				final Reporting reporting = reportingFactory.newReporting("SegmentTimeliness");

				final List<String> packetStoreIDs = new ArrayList<>();
				if (metadata.get("packetStoreID") instanceof List) {
					@SuppressWarnings("unchecked")
					final List<Long> jsonArray = (List<Long>) metadata.get("packetStoreID");
					for(Long id : jsonArray) {
						packetStoreIDs.add(Long.toString(id));
					}
				} else {
					packetStoreIDs.add(Long.toString(metadata.getLong("packetStoreID")));
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
				metadata.put("timeliness", timeliness);
				metadata.remove("packetStoreID"); // the packetStoreID was only needed to compute timeliness
			}
			// S1PRO-1030 GP and HKTM products
			else if (productType.contains("GP_RAW_") || productType.contains("HK_RAW_")) {
				LOGGER.debug("Setting timeliness to NRT for {} product {}", productType, descriptor.getFilename());
				// FIXME S1PRO-1030 should be taken from the application.yaml ??
				metadata.put("timeliness", "NRT");
			} else {
				// FIXME S1PRO-1030 what should be exactly done if it is missing
				LOGGER.error("No packetStoreID found for product in manifest: {} ", manifestFile);
			}

			LOGGER.debug("composed Json: {} ", metadata);
			return metadata;

		} catch (final MetadataMalformedException e) {
			LOGGER.error("Extraction of L0 segment file metadata failed", e);
			throw new MetadataExtractionException(e);
		}
	}

	public ProductMetadata processS2Metadata(S2FileDescriptor descriptor, List<File> metadataFiles, ProductFamily family, String productName)
			throws MetadataExtractionException, MetadataMalformedException {

		File xsltFile = new File(this.xsltDirectory + XSLT_S2_XML);
		
		if (!xsltFile.exists()) {
			throw new MetadataExtractionException("Unable to find S2 XSLT file '" + XSLT_S2_XML + "'");
		}
		
		List<ProductMetadata> metadataList = new ArrayList<>();
		for (File metadataFile : metadataFiles) {
			metadataList.add(transformXMLWithXSLTToJSON(metadataFile, xsltFile));
		}
		
		ProductMetadata additionalMetadata = S2ProductNameUtil.extractMetadata(productName);
		
		ProductMetadata metadata = checkS2Metadata(metadataList, additionalMetadata);
		metadata = processS2Coordinates(metadata, family);
		metadata = putS2FileMetadataToJSON(metadata, descriptor);
		
		LOGGER.debug("composed Json: {} ", metadata);
		return metadata;
	}
	
	public ProductMetadata processS2HKTMMetadata(S2FileDescriptor descriptor, File metadataFile, ProductFamily family, String productName)
			throws MetadataExtractionException, MetadataMalformedException {

		File xsltFile = new File(this.xsltDirectory + XSLT_S2_HKTM_XML);
		
		if (!xsltFile.exists()) {
			throw new MetadataExtractionException("Unable to find S2 XSLT file '" + XSLT_S2_HKTM_XML + "'");
		}
		
		ProductMetadata metadata = transformXMLWithXSLTToJSON(metadataFile, xsltFile);
		
		ProductMetadata additionalMetadata = S2ProductNameUtil.extractMetadata(productName);
		
		metadata = checkS2Metadata(Arrays.asList(metadata), additionalMetadata);
		metadata = putS2FileMetadataToJSON(metadata, descriptor);
		
		LOGGER.debug("composed Json: {} ", metadata);
		return metadata;
	}
	
	private String extractProductInventoryFromJP2(Path productFile) {
		try {
            String xml = null;

            try(FileChannel fc = FileChannel.open(productFile, StandardOpenOption.READ);) {

               // assume XML is in first MB of file
               long bufferSize = (fc.size() < 1024 * 1024) ? fc.size() : 1024*1024;
               ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, bufferSize);
               CharBuffer cb = Charset.forName("8859_1").newDecoder().decode(bb);

               Pattern p = Pattern.compile("(<Inventory_Metadata.*</Inventory_Metadata>?)", Pattern.DOTALL);
               Matcher m = p.matcher(cb);
               while(m.find()) {
                  xml = m.group();
                  break;
               }               
            }
            
            // Unable to find the embedded metadata file in the first Mb of the file.
            if (xml == null) {
            	LOGGER.error("Unable to extract metadata from the first MB of the jp2 file. The structure of the file does not match the expectations");
            	throw new RuntimeException("Unable to extract the metadata from the first MB of the jp2 file " + productFile);
            }
            
            LOGGER.info("Extracted Inventory Metadata from jp2 file");
            return xml;
         } catch(IOException e) {
            throw new RuntimeException("Could not read metadata from file " + productFile, e );
         }
	}
	
	public ProductMetadata processS2L1TCI(S2FileDescriptor descriptor, File productFile, ProductFamily family, String productName)
			throws MetadataExtractionException, MetadataMalformedException {
		String metadataJP2 = extractProductInventoryFromJP2(productFile.toPath());
		File xsltFile = new File(this.xsltDirectory + XSLT_S2_XML);
		
		if (!xsltFile.exists()) {
			throw new MetadataExtractionException("Unable to find S2 XSLT file '" + XSLT_S2_HKTM_XML + "'");
		}
		
		ProductMetadata metadata = transformXMLStringWithXSLTToJSON(metadataJP2, xsltFile);
		
		ProductMetadata additionalMetadata = S2ProductNameUtil.extractMetadata(productName);
		
		metadata = processS2Coordinates(metadata, family);		
		metadata = checkS2Metadata(Arrays.asList(metadata), additionalMetadata);
		metadata = putS2FileMetadataToJSON(metadata, descriptor);
		
		LOGGER.debug("composed Json: {} ", metadata);
		return metadata;
	}
	
	public ProductMetadata processS2SADMetadata(S2FileDescriptor descriptor, File metadataFile, ProductFamily family, String productName)
			throws MetadataExtractionException, MetadataMalformedException {

		File xsltFile = new File(this.xsltDirectory + XSLT_S2_SAD_XML);
		
		if (!xsltFile.exists()) {
			throw new MetadataExtractionException("Unable to find S2 XSLT file '" + XSLT_S2_SAD_XML + "'");
		}
		
		ProductMetadata metadata = transformXMLWithXSLTToJSON(metadataFile, xsltFile);
		
		ProductMetadata additionalMetadata = S2ProductNameUtil.extractMetadata(productName);
		
		metadata = checkS2Metadata(Arrays.asList(metadata), additionalMetadata);
		metadata = putS2FileMetadataToJSON(metadata, descriptor);
		
		LOGGER.debug("composed Json: {} ", metadata);
		return metadata;
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
	public ProductMetadata processIIFFile(S3FileDescriptor descriptor, File file)
			throws MetadataExtractionException, MetadataMalformedException {
		ProductMetadata metadata = transformXMLWithXSLTToJSON(file,
				new File(this.xsltDirectory + XSLT_S3_IIF_XML));

		// Add metadata from file descriptor
		metadata = checkS3MetadataForLevelProducts(metadata);
		metadata = putS3FileMetadataToJSON(metadata, descriptor);

		LOGGER.debug("composed Json: {} ", metadata);
		return metadata;
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
	public ProductMetadata processAuxXFDUFile(final S3FileDescriptor descriptor, final File file)
			throws MetadataExtractionException, MetadataMalformedException {
		ProductMetadata metadata = transformXMLWithXSLTToJSON(file,
				new File(this.xsltDirectory + XSLT_S3_AUX_XFDU_XML));

		// Add metadata from file descriptor
		metadata = checkS3MetadataForAux(metadata);
		metadata = putS3FileMetadataToJSON(metadata, descriptor);

		LOGGER.debug("composed Json: {} ", metadata);
		return metadata;
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
	public ProductMetadata processProductXFDUFile(S3FileDescriptor descriptor, File file)
			throws MetadataExtractionException, MetadataMalformedException {
		ProductMetadata metadata = transformXMLWithXSLTToJSON(file,
				new File(this.xsltDirectory + XSLT_S3_XFDU_XML));

		// Add metadata from file descriptor
		metadata = checkS3MetadataForLevelProducts(metadata);
		metadata = processS3Coordinates(metadata);
		metadata = putS3FileMetadataToJSON(metadata, descriptor);

		LOGGER.debug("composed Json: {} ", metadata);
		return metadata;
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
	public ProductMetadata processProduct(final OutputFileDescriptor descriptor, final ProductFamily productFamily,
			final File manifestFile) throws MetadataExtractionException {

		final File xsltFile = new File(this.xsltDirectory + xsltMap.get(productFamily));
		LOGGER.debug("extracting metadata for descriptor: {} ", descriptor);

		try {
			ProductMetadata metadata = transformXMLWithXSLTToJSON(manifestFile, xsltFile);
			
			metadata = removeEmptyStringElementsFromPolarisationChannelsArray(metadata);

			metadata = putCommonMetadataToJSON(metadata, descriptor);

			if (metadata.has("sliceCoordinates") // for use as polygon
					&& !metadata.getString("sliceCoordinates").isEmpty()) {
				metadata.put("sliceCoordinates",
						processCoordinates(manifestFile, descriptor, metadata.getString("sliceCoordinates")));
			}

			if (metadata.has("coordinates") // for use as as-is metadata attribute (PRIP)
					&& !metadata.getString("coordinates").isEmpty()) {
				metadata.put("coordinates",
						convertCoordinatesToClosedForm(metadata.getString("coordinates")));
			}

			if (ProductFamily.L0_ACN.equals(productFamily) || ProductFamily.L0_SLICE.equals(productFamily)) {

				if (!metadata.has("sliceNumber")
						|| "".equals(metadata.get("sliceNumber").toString())) {
					metadata.put("sliceNumber", 1);
				} else if (!StringUtils.hasLength(metadata.get("sliceNumber").toString())) {
					metadata.put("sliceNumber", 1);
				}
				if (!metadata.has("totalNumberOfSlice")
						|| "".equals(metadata.get("totalNumberOfSlice").toString())) {
					if (Arrays.asList("A", "C", "N").contains(descriptor.getProductClass())) {
						if (metadata.has("startTime") && metadata.has("stopTime")) {
							metadata.put("totalNumberOfSlice", totalNumberOfSlice(
									metadata.getString("startTime"), metadata.getString("stopTime"),
									descriptor.getSwathtype().matches("S[1-6]") ? "SM" : descriptor.getSwathtype()));
						}
					}
				}
			}

			LOGGER.debug("composed Json: {} ", metadata);
			return metadata;

		} catch (final MetadataMalformedException e) {
			LOGGER.error("Extraction of metadata failed", e);
			throw new MetadataExtractionException(e);
		}
	}

	private ProductMetadata checkS2Metadata(final List<ProductMetadata> metadataList, final ProductMetadata additionalMetadata) 
			throws MetadataExtractionException {
		try {
			ProductMetadata metadata = metadataList.get(0);
			for (int i = 1; i < metadataList.size(); i++) {
				metadata.asMap().putAll(metadataList.get(i).asMap());
			}
			
			// Add additional metadata
			if (!metadata.has("productType") && additionalMetadata.has("productType")) {
				metadata.put("productType", additionalMetadata.get("productType"));
			}
			if (!metadata.has("creationTime") && additionalMetadata.has("creationTime")) {
				metadata.put("creationTime", additionalMetadata.get("creationTime"));
			}
			if (!metadata.has("platformSerialIdentifier") && additionalMetadata.has("platformSerialIdentifier")) {
				metadata.put("platformSerialIdentifier", additionalMetadata.get("platformSerialIdentifier"));
			}
			if (!metadata.has("tileNumber") && additionalMetadata.has("tileNumber")) {
				metadata.put("tileNumber", additionalMetadata.get("tileNumber"));
			}
			
			// Fix format of timestamps
			if (metadata.has("startTime")) {
				try {
					metadata.put("startTime",
							DateUtils.convertToMetadataDateTimeFormat((String) metadata.get("startTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("startTime");
				}
			}

			if (metadata.has("stopTime")) {
				try {
					metadata.put("stopTime",
							DateUtils.convertToMetadataDateTimeFormat((String) metadata.get("stopTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("stopTime");
				}
			}
			
			if (metadata.has("validityStartTime")) {
				try {
					metadata.put("validityStartTime",
							DateUtils.convertToMetadataDateTimeFormat((String) metadata.get("validityStartTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("validityStartTime");
				}
			}

			if (metadata.has("validityStopTime")) {
				try {
					metadata.put("validityStopTime",
							DateUtils.convertToMetadataDateTimeFormat((String) metadata.get("validityStopTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("validityStopTime");
				}
			}

			if (metadata.has("creationTime")) {
				try {
					metadata.put("creationTime",
							DateUtils.convertToMetadataDateTimeFormat(metadata.getString("creationTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("creationTime");
				}
			}

			return metadata;

		} catch (final MetadataMalformedException e) {
			LOGGER.error("Error while extraction of config file metadata ", e);
			throw new MetadataExtractionException(e);
		}
	}
	
	/**
	 * Check validityStartTime, validityStopTime and creationTime on
	 * S3-Aux-Metadata-Objects
	 */
	private ProductMetadata checkS3MetadataForAux(final ProductMetadata metadata)
			throws MetadataExtractionException {
		try {
			if (metadata.has("validityStartTime")) {
				try {
					metadata.put("validityStartTime", DateUtils
							.convertToMetadataDateTimeFormat((String) metadata.get("validityStartTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("validityStartTime");
				}
			}

			if (metadata.has("validityStopTime")) {

				final String validStopTime = (String) metadata.get("validityStopTime");

				if (validStopTime.contains("9999-")) {
					metadata.put("validityStopTime", "9999-12-31T23:59:59.999999Z");
				} else {
					try {
						metadata.put("validityStopTime",
								DateUtils.convertToMetadataDateTimeFormat(validStopTime));
					} catch (final DateTimeParseException e) {
						throw new MetadataMalformedException("validityStopTime");
					}
				}

			} else {
				metadata.put("validityStopTime", "9999-12-31T23:59:59.999999Z");
			}

			if (metadata.has("creationTime")) {
				try {
					metadata.put("creationTime",
							DateUtils.convertToMetadataDateTimeFormat(metadata.getString("creationTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("creationTime");
				}
			}

			return metadata;

		} catch (final MetadataMalformedException e) {
			LOGGER.error("Error while extraction of config file metadata ", e);
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Check startTime, stopTime and creationTime on
	 * S3-LevelProduct-Metadata-Objects
	 */
	private ProductMetadata checkS3MetadataForLevelProducts(final ProductMetadata metadata)
			throws MetadataExtractionException {
		try {

			if (metadata.has("startTime")) {
				try {
					metadata.put("startTime",
							DateUtils.convertToMetadataDateTimeFormat((String) metadata.get("startTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("startTime");
				}
			}

			if (metadata.has("stopTime")) {
				try {
					metadata.put("stopTime",
							DateUtils.convertToMetadataDateTimeFormat((String) metadata.get("stopTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("stopTime");
				}
			}

			if (metadata.has("creationTime")) {
				try {
					metadata.put("creationTime",
							DateUtils.convertToMetadataDateTimeFormat(metadata.getString("creationTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("creationTime");
				}
			}

			return metadata;

		} catch (final MetadataMalformedException e) {
			LOGGER.error("Error while extraction of config file metadata ", e);
			throw new MetadataExtractionException(e);
		}
	}
	
	ProductMetadata processS2Coordinates(ProductMetadata metadata, ProductFamily family)
			throws MetadataMalformedException {
		if (metadata.has("coordinates")) {
			String rawCoords = metadata.getString("coordinates");
			
			// On L0 Granules, the coordinates contain the height as additional information
			if (family == ProductFamily.S2_L0_GR) {
				rawCoords = removeHeightInformationFromCoords(rawCoords);
			}
			
			if (!rawCoords.trim().isEmpty()) {
				metadata.put("coordinates", transformFromOpengis(rawCoords));
			} else {
				metadata.remove("coordinates");
			}
		}

		return metadata;
	}
	
	ProductMetadata processS3Coordinates(ProductMetadata metadata)
			throws MetadataMalformedException {
		if (metadata.has("sliceCoordinates")) {
			final String rawCoords = metadata.getString("sliceCoordinates");
			if (!rawCoords.trim().isEmpty()) {
				metadata.put("sliceCoordinates", transformFromOpengis(rawCoords));
			} else {
				metadata.remove("sliceCoordinates");
			}
		}

		return metadata;
	}

	Map<String, Object> transformFromOpengis(String rawCoords) {
		if (rawCoords.indexOf(',') != -1) {
			throw new IllegalArgumentException("space separated values are expected but contains comma");
		}
		String[] coords = rawCoords.split(" ");
		if ((coords.length % 2) != 0) {
			throw new IllegalArgumentException("lat and lon values are expected");
		}
		
		LOGGER.debug("l0 coords: {} ", rawCoords);
		
		final Map<String, Object> geoShape = new HashMap<>();
		final List<List<Double>> geoShapeCoordinates = new ArrayList<>();
		geoShape.put("type", "Polygon");
		
		List<Double> longitudes = new ArrayList<>();
		for (int i = 0; i < coords.length; i = i + 2) {
			final String aLatitude = coords[i];
			final String aLongitude = coords[i + 1];
			longitudes.add(Double.parseDouble(aLongitude));
			geoShapeCoordinates.add(List.of(
					Double.parseDouble(aLongitude),
					Double.parseDouble(aLatitude)
			));
		}
		
		geoShape.put("coordinates", List.of(geoShapeCoordinates));

		// RS-280: Use Elasticsearch Dateline Support
		final String orientation = FootprintUtil.elasticsearchPolygonOrientation(longitudes.toArray(new Double[0]));
		geoShape.put("orientation", orientation);
		if ("clockwise".equals(orientation)) {
			LOGGER.info("Adding dateline crossing marker");
		}
		
		return geoShape;
	}

	private ProductMetadata putConfigFileMetadataToJSON(final ProductMetadata metadata, final AuxDescriptor descriptor)
			throws MetadataExtractionException {
		try {
			if (TYPES_WITH_PRODUCTNAME_BASED_VALIDITY_TIME_EXTRACTION.contains(descriptor.getProductType())) {
				Matcher matcher = AUX_TEC_AND_AUX_TRO_PRODUCTNAME_PATTERN.matcher(descriptor.getProductName());
				if (matcher.matches()) {
					try {
						metadata.put("validityStartTime", DateUtils.convertToMetadataDateTimeFormat(matcher.group(1)));
						metadata.put("validityStopTime", DateUtils.convertToMetadataDateTimeFormat(matcher.group(2)));						
					} catch (final DateTimeParseException e) {
						throw new MetadataMalformedException("validityStartTime/validityStopTime");
					}
				} else {
					throw new MetadataMalformedException("validityStartTime/validityStopTime");
				}
			} else {
			if (metadata.has("validityStartTime")) {
				try {
					metadata.put("validityStartTime", DateUtils
							.convertToMetadataDateTimeFormat((String) metadata.get("validityStartTime")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("validityStartTime");
				}
			}

			if (metadata.has("validityStopTime")) {

				final String validStopTime = (String) metadata.get("validityStopTime");

				if (validStopTime.contains("9999-")) {
					metadata.put("validityStopTime", "9999-12-31T23:59:59.999999Z");
				} else {
					try {
						metadata.put("validityStopTime",
								DateUtils.convertToMetadataDateTimeFormat(validStopTime));
					} catch (final DateTimeParseException e) {
						throw new MetadataMalformedException("validityStopTime");
					}
				}

				} else {
					metadata.put("validityStopTime", "9999-12-31T23:59:59.999999Z");
				}
			}

			if (TYPES_WITH_PRODUCTNAME_BASED_GENERATION_TIME_EXTRACTION.contains(descriptor.getProductType())) {
				Matcher matcher = AUX_TEC_AND_AUX_TRO_PRODUCTNAME_PATTERN.matcher(descriptor.getProductName());
				if (matcher.matches()) {
					try {
						metadata.put("creationTime", DateUtils.convertToMetadataDateTimeFormat(matcher.group(3)));
					} catch (final DateTimeParseException e) {
						throw new MetadataMalformedException("creationTime");
					}
				} else {
					throw new MetadataMalformedException("creationTime");
				}
			} else {
				if (metadata.has("creationTime")) {
					try {
						metadata.put("creationTime",
								DateUtils.convertToMetadataDateTimeFormat(metadata.getString("creationTime")));
					} catch (final DateTimeParseException e) {
						throw new MetadataMalformedException("creationTime");
					}
				}
			}

			if (metadata.has("selectedOrbitFirstAzimuthTimeUtc")) {
				try {
					metadata.put("selectedOrbitFirstAzimuthTimeUtc",
							DateUtils.convertToMetadataDateTimeFormat(
									metadata.getString("selectedOrbitFirstAzimuthTimeUtc")));
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("selectedOrbitFirstAzimuthTimeUtc");
				}
			}

			metadata.put("productName", descriptor.getProductName());

			if (!metadata.has("productClass") || "".equals((String) metadata.get("productClass"))) {
				metadata.put("productClass", descriptor.getProductClass());
			}

			if (!metadata.has("productType") || "".equals((String) metadata.get("productType"))) {
				metadata.put("productType", descriptor.getProductType());
			}

			metadata.put(MissionId.FIELD_NAME, descriptor.getMissionId());
			metadata.put("satelliteId", descriptor.getSatelliteId());
			metadata.put("url", descriptor.getKeyObjectStorage());
			metadata.put("insertionTime", DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.now()));
			metadata.put("productFamily", descriptor.getProductFamily().name());

			return metadata;

		} catch (final MetadataMalformedException e) {
			LOGGER.error("Error while extraction of config file metadata ", e);
			throw new MetadataExtractionException(e);
		}
	}

	private ProductMetadata putEdrsSessionMetadataToJSON(final ProductMetadata metadata,
			final EdrsSessionFileDescriptor descriptor) throws MetadataExtractionException {
		try {
			metadata.put("channelId", descriptor.getChannel());
			metadata.put("productName", descriptor.getProductName());
			metadata.put("productType", descriptor.getEdrsSessionFileType().name());
			metadata.put("sessionId", descriptor.getSessionIdentifier());
			metadata.put(MissionId.FIELD_NAME, descriptor.getMissionId());
			metadata.put("satelliteId", descriptor.getSatelliteId());
			metadata.put("stationCode", descriptor.getStationCode());
			metadata.put("url", descriptor.getKeyObjectStorage());
			metadata.put("insertionTime", DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.now()));
			metadata.put("productFamily", descriptor.getProductFamily().name());

			return metadata;

		} catch (final MetadataMalformedException e) {
			LOGGER.error("Error while extraction of EDRS session metadata ", e);
			throw new MetadataExtractionException(e);
		}
	}

	private ProductMetadata putCommonMetadataToJSON(final ProductMetadata metadata,
			final OutputFileDescriptor descriptor) throws MetadataExtractionException {

		try {
			if (metadata.has("startTime")) {
				try {
					final String t = DateUtils
							.convertToMetadataDateTimeFormat(metadata.getString("startTime"));
					metadata.put("startTime", t);
					metadata.put("validityStartTime", t);
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("validityStartTime");
				}
			}

			if (metadata.has("stopTime")) {
				try {
					final String t = DateUtils
							.convertToMetadataDateTimeFormat(metadata.getString("stopTime"));
					metadata.put("stopTime", t);
					metadata.put("validityStopTime", t);
				} catch (final DateTimeParseException e) {
					throw new MetadataMalformedException("validityStopTime");
				}
			}

			final String dt = DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.now());

			metadata.put("productName", descriptor.getProductName());

			if (!metadata.has("productClass") || "".equals((String) metadata.get("productClass"))) {
				metadata.put("productClass", descriptor.getProductClass());
			}

			if (!metadata.has("productType") || "".equals((String) metadata.get("productType"))) {
				metadata.put("productType", descriptor.getProductType());
			}

			metadata.put("resolution", descriptor.getResolution());
			metadata.put(MissionId.FIELD_NAME, descriptor.getMissionId());
			metadata.put("satelliteId", descriptor.getSatelliteId());

			if (!metadata.has("swathtype") || "".equals((String) metadata.get("swathtype"))) {
				metadata.put("swathtype", descriptor.getSwathtype());
			}

			metadata.put("polarisation", descriptor.getPolarisation());
			metadata.put("dataTakeId", descriptor.getDataTakeId());
			metadata.put("url", descriptor.getKeyObjectStorage());
			metadata.put("insertionTime", dt);
			
			if (metadata.has("creationTime")) {
				metadata.put("creationTime",
						DateUtils.convertToMetadataDateTimeFormat(metadata.getString("creationTime")));
			} else {
				metadata.put("creationTime", dt);
			}
			metadata.put("productFamily", descriptor.getProductFamily().name());
			// TODO S1PRO-1030 in future it can be DEBUG or REPROCESSING as well
			metadata.put("processMode", "NOMINAL");

			return metadata;

		} catch (final MetadataMalformedException e) {
			LOGGER.error("Error while extraction of common metadata", e);
			throw new MetadataExtractionException(e);
		}

	}

	private ProductMetadata removeEmptyStringElementsFromPolarisationChannelsArray(
			final ProductMetadata metadata) throws MetadataMalformedException {
		if (metadata.has("polarisationChannels")) {
			@SuppressWarnings("unchecked")
			List<String> polarisationChannels = (List<String>) metadata.get("polarisationChannels");
			int idx = polarisationChannels.size();
			while (--idx >= 0) {
				if ("".equals(polarisationChannels.get(idx))) {
					polarisationChannels.remove(idx);
				}
			}
		}
		return metadata;
	}
	
	/**
	 * Common metadata from FileDescriptor to insert into S2-Metadata-Objects
	 */
	private ProductMetadata putS2FileMetadataToJSON(final ProductMetadata metadata, final S2FileDescriptor descriptor)
			throws MetadataExtractionException {
		try {
			metadata.put("productName", descriptor.getProductName());

			if (!metadata.has("productClass") || "".equals((String) metadata.get("productClass"))) {
				metadata.put("productClass", descriptor.getProductClass());
			}

			if (!metadata.has("productType") || "".equals((String) metadata.get("productType"))) {
				metadata.put("productType", descriptor.getProductType());
			}
			
			if (!metadata.has("instrumentShortName") || "".equals((String) metadata.get("instrumentShortName"))) {
				metadata.put("instrumentShortName", descriptor.getInstrumentShortName());
			}

			metadata.put(MissionId.FIELD_NAME, descriptor.getMissionId());
			metadata.put("satelliteId", descriptor.getSatelliteId());
			metadata.put("url", descriptor.getKeyObjectStorage());
			metadata.put("productFamily", descriptor.getProductFamily().name());
			// TODO S1PRO-1030 in future it can be DEBUG or REPROCESSING as well
			metadata.put("processMode", "NOMINAL");

			return metadata;
		} catch (final MetadataMalformedException e) {
			LOGGER.error("Error while extraction of config file metadata ", e);
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Common metadata from FileDescriptor to insert into S3-Metadata-Objects
	 */
	private ProductMetadata putS3FileMetadataToJSON(final ProductMetadata metadata, final S3FileDescriptor descriptor)
			throws MetadataExtractionException {
		try {
			metadata.put("productName", descriptor.getProductName());

			if (!metadata.has("productClass") || "".equals((String) metadata.get("productClass"))) {
				metadata.put("productClass", descriptor.getProductClass());
			}

			if (!metadata.has("productType") || "".equals((String) metadata.get("productType"))) {
				metadata.put("productType", descriptor.getProductType());
			}

			metadata.put(MissionId.FIELD_NAME, descriptor.getMissionId());
			metadata.put("satelliteId", descriptor.getSatelliteId());
			metadata.put("url", descriptor.getKeyObjectStorage());
			metadata.put("productFamily", descriptor.getProductFamily().name());
			metadata.put("instanceId", descriptor.getInstanceId());
			metadata.put("generatingCentre", descriptor.getGeneratingCentre());
			metadata.put("classId", descriptor.getClassId());
			// TODO S1PRO-1030 in future it can be DEBUG or REPROCESSING as well
			metadata.put("processMode", "NOMINAL");

			return metadata;
		} catch (final MetadataMalformedException e) {
			LOGGER.error("Error while extraction of config file metadata ", e);
			throw new MetadataExtractionException(e);
		}
	}

	private ProductMetadata transformXMLWithXSLTToJSON(final File inputXMLFile, final File xsltFile)
			throws MetadataExtractionException, MetadataMalformedException {
		StreamSource stream = new StreamSource(inputXMLFile);
		return transformXMLStreamWithXSLTToJSON(stream, xsltFile);
	}
	
	private ProductMetadata transformXMLStringWithXSLTToJSON(final String metadataString, final File xsltFile)
			throws MetadataExtractionException, MetadataMalformedException {
		StreamSource stream = new StreamSource(new ByteArrayInputStream(metadataString.getBytes()));
		return transformXMLStreamWithXSLTToJSON(stream, xsltFile);
	}
	
	private ProductMetadata transformXMLStreamWithXSLTToJSON(final StreamSource source, final File xsltFile) throws MetadataExtractionException, MetadataMalformedException {
		try {
			final Transformer transformer = transFactory.newTransformer(new StreamSource(xsltFile));
			final ByteArrayOutputStream transformationStream = new ByteArrayOutputStream();

			transformer.transform(source, new StreamResult(transformationStream));
			ProductMetadata metadata = ProductMetadata.ofXml(transformationStream.toString(Charset.defaultCharset().name()));
			return enforceFieldTypes(metadata);

		} catch (IOException | TransformerException e) {
			LOGGER.error("Error while transformation of  input XML file to JSON", e);
			throw new MetadataExtractionException(e);
		}
	}
	
	ProductMetadata enforceFieldTypes(ProductMetadata metadata)
			throws MetadataMalformedException {
		ProductMetadata result = new ProductMetadata();
		Iterator<String> keys = metadata.keys().iterator();
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
	private Map<String, Object> processCoordinates(final File manifest, final OutputFileDescriptor descriptor,
			final String rawCoordinates) throws MetadataExtractionException {
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
	}

	public final static String convertCoordinatesToClosedForm(final String rawCoordinates) {
		final String[] elements = rawCoordinates.split(" ");
		return elements[0].equals(elements[elements.length - 1]) ? rawCoordinates : rawCoordinates + " " + elements[0];
	}

	private Map<String, Object> processCoordinatesforWVL0(final String rawCoordinatesFromManifest) {
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

		final Map<String, Object> geoShape = new HashMap<>();
		final List<List<Double>> geoShapeCoordinates = new ArrayList<>();
		geoShape.put("type", "linestring");
		geoShapeCoordinates.add(List.of(
				Double.parseDouble(startNadirLongitude),
				Double.parseDouble(startNadirLatitude)
		));
		geoShapeCoordinates.add(List.of(
				Double.parseDouble(stopNadirLongitude),
				Double.parseDouble(stopNadirLatitude)
		));
		geoShape.put("coordinates", geoShapeCoordinates);
		// geoShape.put("orientation", "counterclockwise");

		return geoShape;
	}

	private Map<String, Object> processCoordinatesforL1andL2(final String rawCoordinatesFromManifest) {

		LOGGER.debug("l1/l2 coords: {} ", rawCoordinatesFromManifest);
		// Snippet from manifest
		// 12.378114,48.279240 12.829241,50.603844 11.081389,50.958828
		// 10.625828,48.649940

		String[] points = rawCoordinatesFromManifest.split(" ");

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

		final Map<String, Object> geoShape = new HashMap<>();
		final List<List<Double>> geoShapeCoordinates = new ArrayList<>();

		geoShape.put("type", "polygon");

		geoShapeCoordinates.add(List.of(
				Double.parseDouble(aLongitude),
				Double.parseDouble(aLatitude)
		));
		
		geoShapeCoordinates.add(List.of(
				Double.parseDouble(dLongitude),
				Double.parseDouble(dLatitude)
		));
		
		geoShapeCoordinates.add(List.of(
				Double.parseDouble(cLongitude),
				Double.parseDouble(cLatitude)
		));
		
		geoShapeCoordinates.add(List.of(
				Double.parseDouble(bLongitude),
				Double.parseDouble(bLatitude)
		));

		geoShapeCoordinates.add(List.of(
				Double.parseDouble(aLongitude),
				Double.parseDouble(aLatitude)
		));
		
		geoShape.put("coordinates", List.of(geoShapeCoordinates));
		
		// RS-280: Use Elasticsearch Dateline Support
		final String orientation = FootprintUtil.elasticsearchPolygonOrientation(
				Double.parseDouble(aLongitude),
				Double.parseDouble(bLongitude),
				Double.parseDouble(cLongitude),
				Double.parseDouble(dLongitude)
		);
		geoShape.put("orientation", orientation);
		if ("clockwise".equals(orientation)) {
			LOGGER.info("Adding dateline crossing marker");
		}

		return geoShape;
	}
	
	private Map<String, Object> processCoordinatesAsIS(final String rawCoordinatesFromManifest) {
		// Snippet from manifest
		// 36.7787,86.8273 38.7338,86.4312 38.4629,83.6235 36.5091,84.0935
		// 36.7787,86.8273
		LOGGER.debug("l0 coords: {} ", rawCoordinatesFromManifest);
		String[] points = rawCoordinatesFromManifest.split(" ");

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

		final Map<String, Object> geoShape = new HashMap<>();
		final List<List<Double>> geoShapeCoordinates = new ArrayList<>();

		geoShape.put("type", "polygon");
		
		geoShapeCoordinates.add(List.of(
				Double.parseDouble(aLongitude),
				Double.parseDouble(aLatitude)
		));
		
		geoShapeCoordinates.add(List.of(
				Double.parseDouble(bLongitude),
				Double.parseDouble(bLatitude)
		));
		
		geoShapeCoordinates.add(List.of(
				Double.parseDouble(cLongitude),
				Double.parseDouble(cLatitude)
		));
		
		geoShapeCoordinates.add(List.of(
				Double.parseDouble(dLongitude),
				Double.parseDouble(dLatitude)
		));

		geoShapeCoordinates.add(List.of(
				Double.parseDouble(aLongitude),
				Double.parseDouble(aLatitude)
		));
		
		geoShape.put("coordinates", List.of(geoShapeCoordinates));
		
		// RS-280: Use Elasticsearch Dateline Support
		final String orientation = FootprintUtil.elasticsearchPolygonOrientation(
				Double.parseDouble(aLongitude),
				Double.parseDouble(bLongitude),
				Double.parseDouble(cLongitude),
				Double.parseDouble(dLongitude)
		);
		geoShape.put("orientation", orientation);
		if ("clockwise".equals(orientation)) {
			LOGGER.info("Adding dateline crossing marker");
		}

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
	
	private String removeHeightInformationFromCoords(String coords) {
		String[] list = coords.split(" ");
		String newCoords = "";
		for (int i = 0; i < list.length; i++) {
			if (i % 3 != 2) {
				newCoords = newCoords + " " + list[i];
			}
		}
		
		return newCoords.trim();
	}

}
