package esa.s1pdgs.cpoc.mdcatalog.extraction.files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
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
import esa.s1pdgs.cpoc.mdcatalog.extraction.WVFootPrintExtension;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.ConfigFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.OutputFileDescriptor;

/**
 * Class to extract the metadata from various types of files
 * 
 * @author Olivier Bex-Chauvet
 */
public class ExtractMetadata {

	private static final String XSLT_MPL_EOF = "XSLT_MPL_EOF.xslt";
	private static final String XSLT_AUX_EOF = "XSLT_AUX_EOF.xslt";
	private static final String XSLT_AUX_XML = "XSLT_AUX_XML.xslt";
	private static final String XSLT_AUX_MANIFEST = "XSLT_AUX_MANIFEST.xslt";
	private static final String XSLT_L0_MANIFEST = "XSLT_L0_MANIFEST.xslt";
	private static final String XSLT_L0_SEGMENT_MANIFEST = "XSLT_L0_SEGMENT.xslt";
	private static final String XSLT_L1_MANIFEST = "XSLT_L1_MANIFEST.xslt";
	private static final String XSLT_L2_MANIFEST = "XSLT_L2_MANIFEST.xslt";
	private static final String OUTPUT_XML = "tmp/output.xml";
	private static final String OUTPUT_L0_SEGMENT_XML = "tmp/outputl0seg.xml";

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

	private String xsltDirectory;
	
	 
	private static final Logger LOGGER = LogManager.getLogger(ExtractMetadata.class);

	/**
	 * Constructor
	 */
	public ExtractMetadata(Map<String, Float> typeOverlap,
			Map<String, Float> typeSliceLength, String xsltDirectory) {
		this.transFactory = TransformerFactory.newInstance();
		this.typeOverlap = typeOverlap;
		this.typeSliceLength = typeSliceLength;
		this.xsltDirectory = xsltDirectory;

		this.xsltMap = new HashMap<>();
		this.xsltMap.put(ProductFamily.L0_ACN, XSLT_L0_MANIFEST);
		this.xsltMap.put(ProductFamily.L0_SLICE, XSLT_L0_MANIFEST);
		this.xsltMap.put(ProductFamily.L1_ACN, XSLT_L1_MANIFEST);
		this.xsltMap.put(ProductFamily.L1_SLICE, XSLT_L1_MANIFEST);
		this.xsltMap.put(ProductFamily.L2_ACN, XSLT_L2_MANIFEST);
		this.xsltMap.put(ProductFamily.L2_SLICE, XSLT_L2_MANIFEST);
	}

	/**
	 * Tool function which returns the content of a file
	 * 
	 * @param FileName
	 * @param encoding
	 * @return the content of the file
	 * @throws IOException
	 */
	private String readFile(String fileName, Charset encoding)
			throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(fileName));
		return new String(encoded, encoding);
	}

	/**
	 * Function which transform the raw coordinates in the good format
	 * 
	 * @param rawCoordinates
	 * @param productName
	 * @return the coordinates in good format
	 * @throws MetadataExtractionException
	 */
	private JSONObject processCoordinates(File manifest,
			OutputFileDescriptor descriptor, String rawCoordinates)
			throws MetadataExtractionException {
		try {
			String productType = descriptor.getProductType();
			// ------------ LEVEL 0 --------------------//
			if (productType.matches(".._RAW__0.")) {

				if (productType.startsWith("WV")||
					productType.startsWith("RF")) {
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
					return WVFootPrintExtension
							.getBoundingPolygon(manifest.getAbsolutePath());
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
					return WVFootPrintExtension
							.getBoundingPolygon(manifest.getAbsolutePath());
				} else {
					// should be 4 coordinates --> else exception
					// copy and adjust from manifest --> manifest is clockwise
					return processCoordinatesforL1andL2(rawCoordinates);
				}
			} else {

				throw new MetadataExtractionException(
						new NotImplementedException(String.format(
								"handling not implemented for productType %s ",
								productType)));
			}

		} catch (JSONException e) {
			throw new MetadataExtractionException(e);
		}
	}

	private JSONObject processCoordinatesforWVL0(
			String rawCoordinatesFromManifest) {
		// Snippet from manifest
		// -74.8571,-120.3411 -75.4484,-121.9204
		String[] points = rawCoordinatesFromManifest.split(" ");

		if (points.length != 2) {
			throw new IllegalArgumentException("2 coordinates are expected");
		}

		final String[] startNadirPoint = points[0].split(",");
		final String startNadirLatitude = startNadirPoint[0];
		final String startNadirLongitude = startNadirPoint[1];

		final String[] stopNadirPoint = points[1].split(",");
		final String stopNadirLatitude = stopNadirPoint[0];
		final String stopNadirLongitude = stopNadirPoint[1];

		JSONObject geoShape = new JSONObject();
		final JSONArray geoShapeCoordinates = new JSONArray();
		geoShape.put("type", "linestring");
		geoShapeCoordinates.put(new JSONArray(
				"[" + startNadirLongitude + "," + startNadirLatitude + "]"));
		geoShapeCoordinates.put(new JSONArray(
				"[" + stopNadirLongitude + "," + stopNadirLatitude + "]"));
		geoShape.put("coordinates", geoShapeCoordinates);
		//geoShape.put("orientation", "counterclockwise");

		return geoShape;
	}

	private JSONObject processCoordinatesforL1andL2(
			String rawCoordinatesFromManifest) {

		LOGGER.debug("l1/l2 coords: {} ",rawCoordinatesFromManifest);
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

		geoShapeCoordinates
				.put(new JSONArray("[" + aLongitude + "," + aLatitude + "]"));
		geoShapeCoordinates
				.put(new JSONArray("[" + dLongitude + "," + dLatitude + "]"));
		geoShapeCoordinates
				.put(new JSONArray("[" + cLongitude + "," + cLatitude + "]"));
		geoShapeCoordinates
				.put(new JSONArray("[" + bLongitude + "," + bLatitude + "]"));
		geoShapeCoordinates
				.put(new JSONArray("[" + aLongitude + "," + aLatitude + "]"));
		

		geoShape.put("coordinates", new JSONArray().put(geoShapeCoordinates));
		geoShape.put("orientation", "counterclockwise");

		return geoShape;
	}

	private JSONObject processCoordinatesAsIS(
			String rawCoordinatesFromManifest) {
		// Snippet from manifest
		// 36.7787,86.8273 38.7338,86.4312 38.4629,83.6235 36.5091,84.0935
		// 36.7787,86.8273
		LOGGER.debug("l0 coords: {} ",rawCoordinatesFromManifest);
		String[] points = rawCoordinatesFromManifest.split(" ");

		if (points.length == 5 && points[0].equals(points[4])) {
			points = Arrays.copyOf(points, 4);
		}

		if (points.length != 4) {
			throw new IllegalArgumentException("4 coordinates are expected");
		}

		// permutate ABCD to ABCDA
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

		geoShapeCoordinates
				.put(new JSONArray("[" + aLongitude + "," + aLatitude + "]"));
		geoShapeCoordinates
				.put(new JSONArray("[" + bLongitude + "," + bLatitude + "]"));
		geoShapeCoordinates
				.put(new JSONArray("[" + cLongitude + "," + cLatitude + "]"));
		geoShapeCoordinates
				.put(new JSONArray("[" + dLongitude + "," + dLatitude + "]"));
		geoShapeCoordinates
				.put(new JSONArray("[" + aLongitude + "," + aLatitude + "]"));
		
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
	private int totalNumberOfSlice(String startTime, String stopTime,
			String type) {
		final Duration duration = Duration.between(DateUtils.parse(stopTime),
				DateUtils.parse(startTime));

		float sliceLength = this.typeSliceLength.get(type);

		// Case of their is no slice information in manifest
		if (sliceLength <= 0) {
			return 1;
		}
		float overlap = this.typeOverlap.get(type);

		float tmpNumberOfSlices = (duration.get(ChronoUnit.SECONDS) - overlap)
				/ sliceLength;
		double fracNumberOfSlices = tmpNumberOfSlices
				- Math.floor(tmpNumberOfSlices);
		int totalNumberOfSlices = 0;
		if ((fracNumberOfSlices * sliceLength) < overlap) {
			totalNumberOfSlices = (int) Math.floor(tmpNumberOfSlices);
		} else {
			totalNumberOfSlices = (int) Math.ceil(tmpNumberOfSlices);
		}
		return Math.max(totalNumberOfSlices, 1);
	}

	/**
	 * Function which extracts metadata from MPL EOF file
	 * 
	 * @param descriptor
	 *            The file descriptor of the auxiliary file
	 * @param file
	 *            The file containing the metadata
	 * @return the json object with extracted metadata
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public JSONObject processEOFFile(ConfigFileDescriptor descriptor, File file)
			throws MetadataExtractionException, MetadataMalformedException {
		try {
			// XSLT Transformation
			String xsltFilename = this.xsltDirectory + XSLT_MPL_EOF;
			Source xsltMPLEOF = new StreamSource(new File(xsltFilename));
			Transformer transformerMPL = transFactory
					.newTransformer(xsltMPLEOF);
			Source mplMetadataFile = new StreamSource(file);
			transformerMPL.transform(mplMetadataFile,
					new StreamResult(new File("tmp/output.xml")));
			// JSON creation
			JSONObject metadataJSONObject = XML.toJSONObject(
					readFile("tmp/output.xml", Charset.defaultCharset()));
			
			// Adding also max validity stop for EOF files as it is done in SAFE
			metadataJSONObject.put("validityStopTime",
					"9999-12-31T23:59:59.999999Z");

			try {
				metadataJSONObject.put("validityStopTime",
						DateUtils.convertToMetadataDateTimeFormat(
								metadataJSONObject
										.getString("validityStopTime")));
			} catch (DateTimeParseException e) {
				throw new MetadataMalformedException("validityStopTime");
			}

			try {
				metadataJSONObject.put("creationTime",
						DateUtils.convertToMetadataDateTimeFormat(
								metadataJSONObject.getString("creationTime")));
			} catch (DateTimeParseException e) {
				throw new MetadataMalformedException("creationTime");
			}

			try {
				metadataJSONObject.put("validityStartTime",
						DateUtils.convertToMetadataDateTimeFormat(
								metadataJSONObject
										.getString("validityStartTime")));
			} catch (DateTimeParseException e) {
				throw new MetadataMalformedException("validityStartTime");
			}
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productClass",
					descriptor.getProductClass());
			metadataJSONObject.put("productType", descriptor.getProductType());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", DateUtils
					.formatToMetadataDateTimeFormat(LocalDateTime.now()));
			metadataJSONObject.put("productFamily",
					descriptor.getProductFamily().name());
			return metadataJSONObject;
		} catch (IOException | TransformerException | JSONException e) {
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Function which extracts metadata from AUX EOF file
	 * 
	 * @param descriptor
	 *            The file descriptor of the auxiliary file
	 * @param file
	 *            The file containing the metadata
	 * @return the json object with extracted metadata
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public JSONObject processEOFFileWithoutNamespace(
			ConfigFileDescriptor descriptor, File file)
			throws MetadataExtractionException, MetadataMalformedException {
		try {
			// XSLT Transformation
			String xsltFilename = this.xsltDirectory + XSLT_AUX_EOF;
			Source xsltAUXEOF = new StreamSource(new File(xsltFilename));
			Transformer transformerAUX = transFactory
					.newTransformer(xsltAUXEOF);
			Source auxMetadataFile = new StreamSource(file);
			transformerAUX.transform(auxMetadataFile,
					new StreamResult(new File("tmp/output.xml")));
			// JSON creation
			JSONObject metadataJSONObject = XML.toJSONObject(
					readFile("tmp/output.xml", Charset.defaultCharset()));

			try {
				metadataJSONObject.put("validityStopTime",
						DateUtils.convertToMetadataDateTimeFormat(
								metadataJSONObject
										.getString("validityStopTime")));
			} catch (DateTimeParseException e) {
				throw new MetadataMalformedException("validityStopTime");
			}

			try {
				metadataJSONObject.put("creationTime",
						DateUtils.convertToMetadataDateTimeFormat(
								metadataJSONObject.getString("creationTime")));
			} catch (DateTimeParseException e) {
				throw new MetadataMalformedException("creationTime");
			}

			try {
				metadataJSONObject.put("validityStartTime",
						DateUtils.convertToMetadataDateTimeFormat(
								metadataJSONObject
										.getString("validityStartTime")));
			} catch (DateTimeParseException e) {
				throw new MetadataMalformedException("validityStartTime");
			}

			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productClass",
					descriptor.getProductClass());
			metadataJSONObject.put("productType", descriptor.getProductType());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", DateUtils
					.formatToMetadataDateTimeFormat(LocalDateTime.now()));
			metadataJSONObject.put("productFamily",
					descriptor.getProductFamily().name());
			return metadataJSONObject;
		} catch (IOException | TransformerException | JSONException e) {
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Function which extracts metadata from AUX XML file
	 * 
	 * @param descriptor
	 *            The file descriptor of the auxiliary file
	 * @param file
	 *            The file containing the metadata
	 * @return the json object with extracted metadata
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public JSONObject processXMLFile(ConfigFileDescriptor descriptor, File file)
			throws MetadataExtractionException, MetadataMalformedException {
		try {
			// XSLT Transformation
			String xsltFilename = this.xsltDirectory + XSLT_AUX_XML;
			Source xsltAUXXML = new StreamSource(new File(xsltFilename));
			Transformer transformerAUX = transFactory
					.newTransformer(xsltAUXXML);
			Source auxMetadataFile = new StreamSource(file);
			transformerAUX.transform(auxMetadataFile,
					new StreamResult(new File("tmp/output.xml")));
			// JSON creation
			JSONObject metadataJSONObject = XML.toJSONObject(
					readFile("tmp/output.xml", Charset.defaultCharset()));
			
			// Adding also max validity stop for EOF files as it is done in SAFE
			metadataJSONObject.put("validityStopTime",
					"9999-12-31T23:59:59.999999Z");

			try {
				metadataJSONObject.put("validityStopTime",
						DateUtils.convertToMetadataDateTimeFormat(
								metadataJSONObject
										.getString("validityStopTime")));
			} catch (DateTimeParseException e) {
				throw new MetadataMalformedException("validityStopTime");
			}

			try {
				metadataJSONObject.put("validityStartTime",
						DateUtils.convertToMetadataDateTimeFormat(
								metadataJSONObject
										.getString("validityStartTime")));
			} catch (DateTimeParseException e) {
				throw new MetadataMalformedException("validityStartTime");
			}

			try {
				metadataJSONObject.put("creationTime",
						DateUtils.convertToMetadataDateTimeFormat(
								metadataJSONObject.getString("creationTime")));
			} catch (DateTimeParseException e) {
				throw new MetadataMalformedException("creationTime");
			}

			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productClass",
					descriptor.getProductClass());
			metadataJSONObject.put("productType", descriptor.getProductType());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", DateUtils
					.formatToMetadataDateTimeFormat(LocalDateTime.now()));
			metadataJSONObject.put("productFamily",
					descriptor.getProductFamily().name());
			return metadataJSONObject;

		} catch (IOException | TransformerException | JSONException e) {
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Function which extracts metadata from AUX MANIFEST file
	 * 
	 * @param descriptor
	 *            The file descriptor of the auxiliary file
	 * @param file
	 *            The file containing the metadata
	 * @return the json object with extracted metadata
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	//FIXEME probably it means SAFE AUX FILE ???
	public JSONObject processSAFEFile(ConfigFileDescriptor descriptor,
			File file)
			throws MetadataExtractionException, MetadataMalformedException {
		try {
			// XSLT Transformation
			String xsltFilename = this.xsltDirectory + XSLT_AUX_MANIFEST;
			Source xsltAUXMANIFEST = new StreamSource(new File(xsltFilename));
			Transformer transformerAUX = transFactory
					.newTransformer(xsltAUXMANIFEST);
			Source auxMetadataFile = new StreamSource(file);
			transformerAUX.transform(auxMetadataFile,
					new StreamResult(new File("tmp/output.xml")));
			// JSON creation
			JSONObject metadataJSONObject = XML.toJSONObject(
					readFile("tmp/output.xml", Charset.defaultCharset()));
			metadataJSONObject.put("validityStopTime",
					"9999-12-31T23:59:59.999999Z");
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productType", descriptor.getProductType());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", DateUtils
					.formatToMetadataDateTimeFormat(LocalDateTime.now()));
			metadataJSONObject.put("productFamily",
					descriptor.getProductFamily().name());

			if (metadataJSONObject.has("validityStartTime")) {
				try {
					metadataJSONObject.put("validityStartTime",
							DateUtils.convertToMetadataDateTimeFormat(
									(String) metadataJSONObject
											.get("validityStartTime")));
				} catch (DateTimeParseException e) {
					throw new MetadataMalformedException("validityStartTime");
				}
			}

			if (metadataJSONObject.has("validityStopTime")) {
				try {
					metadataJSONObject.put("validityStopTime",
							DateUtils.convertToMetadataDateTimeFormat(
									(String) metadataJSONObject
											.get("validityStopTime")));
				} catch (DateTimeParseException e) {
					throw new MetadataMalformedException("validityStopTime");
				}
			}

			return metadataJSONObject;
		} catch (IOException | TransformerException | JSONException e) {
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Function which extracts metadata from RAW file
	 * 
	 * @param descriptor
	 *            The file descriptor of the raw file
	 * @return the json object with extracted metadata
	 * @throws MetadataExtractionException
	 */
	public JSONObject processRAWFile(EdrsSessionFileDescriptor descriptor)
			throws MetadataExtractionException {
		try {
			JSONObject metadataJSONObject = new JSONObject();
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productType",
					descriptor.getEdrsSessionFileType().name());
			metadataJSONObject.put("sessionId",
					descriptor.getSessionIdentifier());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", DateUtils
					.formatToMetadataDateTimeFormat(LocalDateTime.now()));
			metadataJSONObject.put("productFamily",
					descriptor.getProductFamily().name());
			return metadataJSONObject;
		} catch (JSONException e) {
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Function which extracts metadata from SESSION file
	 * 
	 * @param descriptor
	 *            The file descriptor of the session file
	 * @return the json object with extracted metadata
	 * @throws MetadataExtractionException
	 */
	public JSONObject processSESSIONFile(EdrsSessionFileDescriptor descriptor)
			throws MetadataExtractionException {
		try {
			JSONObject metadataJSONObject = new JSONObject();
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productType",
					descriptor.getEdrsSessionFileType().name());
			metadataJSONObject.put("sessionId",
					descriptor.getSessionIdentifier());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", DateUtils
					.formatToMetadataDateTimeFormat(LocalDateTime.now()));
			metadataJSONObject.put("productFamily",
					descriptor.getProductFamily().name());
			return metadataJSONObject;
		} catch (JSONException e) {
			throw new MetadataExtractionException(e);
		}
	}

	public JSONObject processL0Segment(OutputFileDescriptor descriptor,
			File manifestFile)
			throws MetadataExtractionException, MetadataMalformedException {
		try {
			// XSLT Transformation
			String xsltFilename = this.xsltDirectory + XSLT_L0_SEGMENT_MANIFEST;
			Source xsltL1MANIFEST = new StreamSource(new File(xsltFilename));
			Transformer transformerL0 = transFactory
					.newTransformer(xsltL1MANIFEST);
			Source l1File = new StreamSource(manifestFile);
			transformerL0.transform(l1File,
					new StreamResult(new File(OUTPUT_L0_SEGMENT_XML)));
			// JSON creation
			JSONObject metadataJSONObject = XML.toJSONObject(
					readFile(OUTPUT_L0_SEGMENT_XML, Charset.defaultCharset()));
			if (metadataJSONObject.has("startTime")) {
				try {
					String t = DateUtils.convertToMetadataDateTimeFormat(
							(String) metadataJSONObject.getString("startTime"));
					metadataJSONObject.put("startTime", t);
					metadataJSONObject.put("validityStartTime", t);
				} catch (DateTimeParseException e) {
					throw new MetadataMalformedException("validityStartTime");
				}
			}
			if (metadataJSONObject.has("stopTime")) {
				try {
					String t = DateUtils.convertToMetadataDateTimeFormat(
							(String) metadataJSONObject.getString("stopTime"));
					metadataJSONObject.put("stopTime", t);
					metadataJSONObject.put("validityStopTime", t);
				} catch (DateTimeParseException e) {
					throw new MetadataMalformedException("validityStopTime");
				}
			}

			if (metadataJSONObject.has("segmentCoordinates")) {
				metadataJSONObject.put("segmentCoordinates", processCoordinates(
						manifestFile, descriptor,
						metadataJSONObject.getString("segmentCoordinates")));
			}
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productClass",
					descriptor.getProductClass());
			metadataJSONObject.put("productType", descriptor.getProductType());
			metadataJSONObject.put("resolution", descriptor.getResolution());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("swathtype", descriptor.getSwathtype());
			metadataJSONObject.put("polarisation",
					descriptor.getPolarisation());
			metadataJSONObject.put("dataTakeId", descriptor.getDataTakeId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("processMode", descriptor.getMode());
			String dt = DateUtils
					.formatToMetadataDateTimeFormat(LocalDateTime.now());
			metadataJSONObject.put("insertionTime", dt);
			metadataJSONObject.put("creationTime", dt);
			metadataJSONObject.put("productFamily",
					descriptor.getProductFamily().name());

			LOGGER.debug("composed Json: {} ",metadataJSONObject);
			return metadataJSONObject;
		} catch (IOException | TransformerException | JSONException e) {
			throw new MetadataExtractionException(e);
		}
	}
	/**
	 * Function which extracts metadata from product
	 * 
	 * @param descriptor
	 * @param manifestFile
	 * @param output
	 * 
	 * @return the json object with extracted metadata
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public JSONObject processProduct(OutputFileDescriptor descriptor,
			ProductFamily productFamily, File manifestFile)
			throws MetadataExtractionException, MetadataMalformedException {
		try {

			// XSLT Transformation
			Source xsltMANIFEST = new StreamSource(
					new File(this.xsltDirectory + xsltMap.get(productFamily)));
			Transformer transformer = transFactory.newTransformer(xsltMANIFEST);
			Source inputFile = new StreamSource(manifestFile);
			transformer.transform(inputFile,
					new StreamResult(new File(OUTPUT_XML)));
			// JSON creation
			JSONObject metadataJSONObject = XML.toJSONObject(
					readFile(OUTPUT_XML, Charset.defaultCharset()));

			if (metadataJSONObject.has("sliceCoordinates")
					&& !metadataJSONObject.getString("sliceCoordinates")
							.isEmpty()) {
				metadataJSONObject.put("sliceCoordinates", processCoordinates(
						manifestFile, descriptor,
						metadataJSONObject.getString("sliceCoordinates")));
			}

			if (metadataJSONObject.has("startTime")) {
				try {
					String t = DateUtils.convertToMetadataDateTimeFormat(
							(String) metadataJSONObject.getString("startTime"));
					metadataJSONObject.put("startTime", t);
					metadataJSONObject.put("validityStartTime", t);
				} catch (DateTimeParseException e) {
					throw new MetadataMalformedException("validityStartTime");
				}
			}

			if (metadataJSONObject.has("stopTime")) {
				try {
					String t = DateUtils.convertToMetadataDateTimeFormat(
							(String) metadataJSONObject.getString("stopTime"));
					metadataJSONObject.put("stopTime", t);
					metadataJSONObject.put("validityStopTime", t);
				} catch (DateTimeParseException e) {
					throw new MetadataMalformedException("validityStopTime");
				}
			}

			if (ProductFamily.L0_ACN.equals(productFamily)
					|| ProductFamily.L0_SLICE.equals(productFamily)) {

				if (!metadataJSONObject.has("sliceNumber")) {
					metadataJSONObject.put("sliceNumber", 1);
				} else if (StringUtils.isEmpty(
						metadataJSONObject.get("sliceNumber").toString())) {
					metadataJSONObject.put("sliceNumber", 1);
				}
				if (Arrays.asList("A", "C", "N")
						.contains(descriptor.getProductClass())) {
					if (metadataJSONObject.has("startTime")
							&& metadataJSONObject.has("stopTime")) {
						metadataJSONObject.put("totalNumberOfSlice",
								totalNumberOfSlice(
										metadataJSONObject
												.getString("startTime"),
										metadataJSONObject
												.getString("stopTime"),
										descriptor.getSwathtype()
												.matches("S[1-6]")
														? "SM"
														: descriptor
																.getSwathtype()));
					}
				}
			}

			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productClass",
					descriptor.getProductClass());
			metadataJSONObject.put("productType", descriptor.getProductType());
			metadataJSONObject.put("resolution", descriptor.getResolution());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("swathtype", descriptor.getSwathtype());
			metadataJSONObject.put("polarisation",
					descriptor.getPolarisation());
			metadataJSONObject.put("dataTakeId", descriptor.getDataTakeId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			String dt = DateUtils
					.formatToMetadataDateTimeFormat(LocalDateTime.now());
			metadataJSONObject.put("insertionTime", dt);
			metadataJSONObject.put("creationTime", dt);
			metadataJSONObject.put("productFamily",
					descriptor.getProductFamily().name());
			metadataJSONObject.put("processMode", descriptor.getMode());
			LOGGER.debug("composed Json: {} ",metadataJSONObject);
			return metadataJSONObject;
		} catch (IOException | TransformerException | JSONException e) {
			throw new MetadataExtractionException(e);
		}
	}

}
