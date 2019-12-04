package esa.s1pdgs.cpoc.mdc.worker.extraction.files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.AuxDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.EdrsSessionFile;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.xml.XmlConverter;

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
	 * Directory containing the XSLT files
	 */
	private String xsltDirectory;

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
			final String xsltDirectory, final XmlConverter xmlConverter) {
		this.transFactory = TransformerFactory.newInstance();
		this.typeOverlap = typeOverlap;
		this.typeSliceLength = typeSliceLength;
		this.xsltDirectory = xsltDirectory;
		this.xmlConverter = xmlConverter;
		this.xsltMap = new HashMap<>();
		this.xsltMap.put(ProductFamily.L0_ACN, XSLT_L0_MANIFEST);
		this.xsltMap.put(ProductFamily.L0_SLICE, XSLT_L0_MANIFEST);
		this.xsltMap.put(ProductFamily.L1_ACN, XSLT_L1_MANIFEST);
		this.xsltMap.put(ProductFamily.L1_SLICE, XSLT_L1_MANIFEST);
		this.xsltMap.put(ProductFamily.L2_ACN, XSLT_L2_MANIFEST);
		this.xsltMap.put(ProductFamily.L2_SLICE, XSLT_L2_MANIFEST);
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
	// FIXEME probably it means SAFE AUX FILE ???
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
	public JSONObject processSESSIONFile(
			final EdrsSessionFileDescriptor descriptor,
			final File file
	) throws MetadataExtractionException {
		try {
			final JSONObject metadataJSONObject = putEdrsSessionMetadataToJSON(new JSONObject(), descriptor);

//			final String name = new File(descriptor.getRelativePath()).getName();
//			final File file = new File(localDirectory, name);

			final EdrsSessionFile edrsSessionFile = (EdrsSessionFile) xmlConverter.convertFromXMLToObject(file.getPath());

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
	 * @param descriptor The file descriptor of the file
	 * @param manifestFile The input manifest File
	 * @return json object with extracted metadata
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public JSONObject processL0Segment(final OutputFileDescriptor descriptor, final File manifestFile)
			throws MetadataExtractionException, MetadataMalformedException {

		final File xsltFile = new File(this.xsltDirectory + XSLT_L0_SEGMENT_MANIFEST);
		LOGGER.debug("extracting metadata for descriptor: {} ", descriptor);
		JSONObject metadataJSONObject = transformXMLWithXSLTToJSON(manifestFile, xsltFile);

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
						metadataJSONObject.put("segmentCoordinates",
								processCoordinates(manifestFile, descriptor, coords));
					}
				}
			}

			LOGGER.debug("composed Json: {} ", metadataJSONObject);
			return metadataJSONObject;

		} catch (final JSONException e) {
			LOGGER.error("Extraction of L0 segment file metadata failed", e);
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Function which extracts metadata from product
	 * 
	 * @param descriptor The file descriptor of the file
	 * @param productFamily product family
	 * @param manifestFile The input manifest file
	 * @return json object with extracted metadata
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public JSONObject processProduct(final OutputFileDescriptor descriptor, final ProductFamily productFamily, final File manifestFile)
			throws MetadataExtractionException, MetadataMalformedException {

		final File xsltFile = new File(this.xsltDirectory + xsltMap.get(productFamily));
		LOGGER.debug("extracting metadata for descriptor: {} ", descriptor);
		JSONObject metadataJSONObject = transformXMLWithXSLTToJSON(manifestFile, xsltFile);

		metadataJSONObject = putCommonMetadataToJSON(metadataJSONObject, descriptor);

		try {

			if (metadataJSONObject.has("sliceCoordinates")
					&& !metadataJSONObject.getString("sliceCoordinates").isEmpty()) {
				metadataJSONObject.put("sliceCoordinates",
						processCoordinates(manifestFile, descriptor, metadataJSONObject.getString("sliceCoordinates")));
			}

			if (ProductFamily.L0_ACN.equals(productFamily) || ProductFamily.L0_SLICE.equals(productFamily)) {

				if (!metadataJSONObject.has("sliceNumber")) {
					metadataJSONObject.put("sliceNumber", 1);
				} else if (StringUtils.isEmpty(metadataJSONObject.get("sliceNumber").toString())) {
					metadataJSONObject.put("sliceNumber", 1);
				}
				if (Arrays.asList("A", "C", "N").contains(descriptor.getProductClass())) {
					if (metadataJSONObject.has("startTime") && metadataJSONObject.has("stopTime")) {
						metadataJSONObject.put("totalNumberOfSlice", totalNumberOfSlice(
								metadataJSONObject.getString("startTime"), metadataJSONObject.getString("stopTime"),
								descriptor.getSwathtype().matches("S[1-6]") ? "SM" : descriptor.getSwathtype()));
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

	private JSONObject putConfigFileMetadataToJSON(final JSONObject metadataJSONObject, final AuxDescriptor descriptor)
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

			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productClass", descriptor.getProductClass());
			metadataJSONObject.put("productType", descriptor.getProductType());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
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

	private JSONObject putEdrsSessionMetadataToJSON(final JSONObject metadataJSONObject, final EdrsSessionFileDescriptor descriptor)
			throws MetadataExtractionException {
		try {
			metadataJSONObject.put("channelId", descriptor.getChannel());
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productType", descriptor.getEdrsSessionFileType().name());
			metadataJSONObject.put("sessionId", descriptor.getSessionIdentifier());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
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

	private JSONObject putCommonMetadataToJSON(final JSONObject metadataJSONObject, final OutputFileDescriptor descriptor)
			throws MetadataExtractionException, MetadataMalformedException {

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
			metadataJSONObject.put("productClass", descriptor.getProductClass());
			metadataJSONObject.put("productType", descriptor.getProductType());
			metadataJSONObject.put("resolution", descriptor.getResolution());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("swathtype", descriptor.getSwathtype());
			metadataJSONObject.put("polarisation", descriptor.getPolarisation());
			metadataJSONObject.put("dataTakeId", descriptor.getDataTakeId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", dt);
			metadataJSONObject.put("creationTime", dt);
			metadataJSONObject.put("productFamily", descriptor.getProductFamily().name());
			metadataJSONObject.put("processMode", descriptor.getMode());

			return metadataJSONObject;

		} catch (final JSONException e) {
			LOGGER.error("Error while extraction of common metadata", e);
			throw new MetadataExtractionException(e);
		}

	}

	private JSONObject transformXMLWithXSLTToJSON(final File inputXMLFile, final File xsltFile) throws MetadataExtractionException {

		try {
			final Transformer transformer = transFactory.newTransformer(new StreamSource(xsltFile));
			final ByteArrayOutputStream transformationStream = new ByteArrayOutputStream();

			transformer.transform(new StreamSource(inputXMLFile), new StreamResult(transformationStream));
			return XML.toJSONObject(transformationStream.toString(Charset.defaultCharset().name()));

		} catch (IOException | TransformerException | JSONException e) {
			LOGGER.error("Error while transformation of  input XML file to JSON", e);
			throw new MetadataExtractionException(e);
		}

	}

	/**
	 * Function which transform the raw coordinates in the good format
	 * 
	 * @param rawCoordinates
	 * @param productName
	 * @return the coordinates in good format
	 * @throws MetadataExtractionException
	 */
	private JSONObject processCoordinates(final File manifest, final OutputFileDescriptor descriptor, final String rawCoordinates)
			throws MetadataExtractionException {
		try {
			final String productType = descriptor.getProductType();
			// ------------ LEVEL 0 --------------------//
			if (productType.matches(".._RAW__0.")) {

				if (productType.startsWith("WV") || productType.startsWith("RF")) {
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
			else if (productType.matches(".._(GRD|SLC)._1.")) {

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
	private int totalNumberOfSlice(final String startTime, final String stopTime, final String type) {
		final Duration duration = Duration.between(DateUtils.parse(startTime), DateUtils.parse(stopTime));

		final float sliceLength = this.typeSliceLength.get(type);

		// Case of their is no slice information in manifest
		if (sliceLength <= 0) {
			return 1;
		}
		final float overlap = this.typeOverlap.get(type);

		final float tmpNumberOfSlices = (duration.get(ChronoUnit.SECONDS) - overlap) / sliceLength;
		final double fracNumberOfSlices = tmpNumberOfSlices - Math.floor(tmpNumberOfSlices);
		int totalNumberOfSlices = 0;
		if ((fracNumberOfSlices * sliceLength) < overlap) {
			totalNumberOfSlices = (int) Math.floor(tmpNumberOfSlices);
		} else {
			totalNumberOfSlices = (int) Math.ceil(tmpNumberOfSlices);
		}
		return Math.max(totalNumberOfSlices, 1);
	}

}
