package esa.s1pdgs.cpoc.mdcatalog.extraction.files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.ConfigFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L0OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L1OutputFileDescriptor;

/**
 * Class to extract the metadata from various types of files
 * 
 * @author Olivier Bex-Chauvet
 * 
 */
public class ExtractMetadata {

	/**
	 * XSLT transformer factory
	 */
	private TransformerFactory transFactory;
	/**
	 * Date Format
	 */
	private SimpleDateFormat dateFormat;
	
	/**
	 * Map of all the overlap for the different slice type
	 */
	private Map<String, Float> typeOverlap;
	
	/**
	 * Map of all the length for the different slice type
	 */
	private Map<String, Float> typeSliceLength;
	
	private String xsltDirectory;

	/**
	 * Constructor
	 */
	public ExtractMetadata(Map<String, Float> typeOverlap, Map<String, Float> typeSliceLength, String xsltDirectory) {
		this.transFactory = TransformerFactory.newInstance();
		this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		this.typeOverlap = typeOverlap;
		this.typeSliceLength = typeSliceLength;
		this.xsltDirectory = xsltDirectory;
	}

	/**
	 * Tool function which returns the content of a file
	 * 
	 * @param FileName
	 * @param encoding
	 * 
	 * @return the content of the file
	 * 
	 * @throws IOException
	 */
	private String readFile(String fileName, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(fileName));
		return new String(encoded, encoding);
	}
	
	/**
	 * Function which transform the raw coordinates in the good format
	 * 
	 * @param rawCoordinates
	 * @param productName
	 * 
	 * @return the coordinates in good format
	 * @throws MetadataExtractionException 
	 */
	private JSONObject processCoordinates(String productName, String rawCoordinates) throws MetadataExtractionException {
	    JSONObject geoShape = new JSONObject();
		JSONArray coordinates = new JSONArray();
		try {
		    String[] coordSplitSpace = rawCoordinates.split(" ");
		    if(coordSplitSpace.length == 2) { //BBOX type (envelope in ES)
		        geoShape.put("type", "envelope");
		        for (String coord : coordSplitSpace) {
                    coordinates.put(new JSONArray("[" + (coord.split(","))[1] + "," + (coord.split(","))[0] + "]"));
                }
		    } else { //Polygon type
		        geoShape.put("type", "polygon");
		        for (String coord : coordSplitSpace) {
	                coordinates.put(new JSONArray("[" + (coord.split(","))[1] + "," + (coord.split(","))[0] + "]"));
	            }
	            if(!coordSplitSpace[0].equals(coordSplitSpace[coordSplitSpace.length-1])) {
	                coordinates.put(new JSONArray("[" + (coordSplitSpace[0].split(","))[1] + "," + (coordSplitSpace[0].split(","))[0] + "]"));
	            }
		    }
            geoShape.put("coordinates", new JSONArray().put(coordinates));
		} catch (JSONException e) {
			throw new MetadataExtractionException(e);
		}
		return geoShape;
	}
	
	/**
	 * Function which return the number of slices in a segment
	 * 
	 * @param startTimeLong
	 * @param stopTimeLong
	 * @param type
	 * 
	 * @return an int which is the number of Slices
	 */
	private int totalNumberOfSlice(Long startTimeLong, Long stopTimeLong, String type) {
		int totalNumberOfSlices = 0;
		float tmpNumberOfSlices = 0F;
		double fracNumberOfSlices = 0.0;
		tmpNumberOfSlices = (stopTimeLong - startTimeLong - this.typeOverlap.get(type))/this.typeSliceLength.get(type);
		fracNumberOfSlices = tmpNumberOfSlices - Math.floor(tmpNumberOfSlices);
		if((fracNumberOfSlices*this.typeSliceLength.get(type)) < this.typeOverlap.get(type)){
			totalNumberOfSlices = (int) Math.floor(tmpNumberOfSlices);
		}
		else {
			totalNumberOfSlices = (int) Math.ceil(tmpNumberOfSlices);
		}
		return Math.max(totalNumberOfSlices, 1);
	}

	/**
	 * Function which extracts metadata from MPL EOF file
	 * 
	 * @param descriptor The file descriptor of the auxiliary file
	 * @param file The file containing the metadata
	 * 
	 * @return the json object with extracted metadata
	 * 
	 * @throws MetadataExtractionException
	 */
	public JSONObject processEOFFile(ConfigFileDescriptor descriptor, File file) throws MetadataExtractionException {
		try {
			// XSLT Transformation
			String xsltFilename = this.xsltDirectory + "XSLT_MPL_EOF.xslt";
			Source xsltMPLEOF = new StreamSource(new File(xsltFilename));
			Transformer transformerMPL = transFactory.newTransformer(xsltMPLEOF);
			Source mplMetadataFile = new StreamSource(file);
			transformerMPL.transform(mplMetadataFile, new StreamResult(new File("tmp/output.xml")));
			// JSON creation
			JSONObject metadataJSONObject = XML.toJSONObject(readFile("tmp/output.xml", Charset.defaultCharset()));
			if (metadataJSONObject.getString("validityStopTime").equals("UTC=9999-99-99T99:99:99")) {
				metadataJSONObject.put("validityStopTime", "9999-12-31T23:59:59");
			} else {
				metadataJSONObject.put("validityStopTime",
				        metadataJSONObject.getString("validityStopTime").substring(4));
			}
			metadataJSONObject.put("creationTime",
			        metadataJSONObject.getString("creationTime").substring(4));
            metadataJSONObject.put("validityStartTime",
                    metadataJSONObject.getString("validityStartTime").substring(4));
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productClass", descriptor.getProductClass());
			metadataJSONObject.put("productType", descriptor.getProductType());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
			metadataJSONObject.put("productFamily", descriptor.getProductFamily().name());
			return metadataJSONObject;
		} catch (IOException | TransformerException | JSONException e) {
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Function which extracts metadata from AUX EOF file
	 * 
	 * @param descriptor The file descriptor of the auxiliary file
	 * @param file The file containing the metadata
	 * 
	 * @return the json object with extracted metadata
	 * 
	 * @throws MetadataExtractionException
	 */
	public JSONObject processEOFFileWithoutNamespace(ConfigFileDescriptor descriptor, File file)
			throws MetadataExtractionException {
		try {
			// XSLT Transformation
			String xsltFilename = this.xsltDirectory + "XSLT_AUX_EOF.xslt";
			Source xsltAUXEOF = new StreamSource(new File(xsltFilename));
			Transformer transformerAUX = transFactory.newTransformer(xsltAUXEOF);
			Source auxMetadataFile = new StreamSource(file);
			transformerAUX.transform(auxMetadataFile, new StreamResult(new File("tmp/output.xml")));
			// JSON creation
			JSONObject metadataJSONObject = XML.toJSONObject(readFile("tmp/output.xml", Charset.defaultCharset()));
			metadataJSONObject.put("validityStopTime", metadataJSONObject.getString("validityStopTime").toString()
					.substring(4, metadataJSONObject.getString("validityStopTime").toString().length()));
			metadataJSONObject.put("creationTime", metadataJSONObject.getString("creationTime").toString().substring(4,
					metadataJSONObject.getString("creationTime").toString().length()));
			metadataJSONObject.put("validityStartTime", metadataJSONObject.getString("validityStartTime").toString()
					.substring(4, metadataJSONObject.getString("validityStartTime").toString().length()));
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productClass", descriptor.getProductClass());
			metadataJSONObject.put("productType", descriptor.getProductType());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
            metadataJSONObject.put("productFamily", descriptor.getProductFamily().name());
			return metadataJSONObject;
		} catch (IOException | TransformerException | JSONException e) {
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Function which extracts metadata from AUX XML file
	 * 
	 * @param descriptor The file descriptor of the auxiliary file
	 * @param file The file containing the metadata
	 * 
	 * @return the json object with extracted metadata
	 * 
	 * @throws MetadataExtractionException
	 */
	public JSONObject processXMLFile(ConfigFileDescriptor descriptor, File file) throws MetadataExtractionException {
		try {
			// XSLT Transformation
			String xsltFilename = this.xsltDirectory + "XSLT_AUX_XML.xslt";
			Source xsltAUXXML = new StreamSource(new File(xsltFilename));
			Transformer transformerAUX = transFactory.newTransformer(xsltAUXXML);
			Source auxMetadataFile = new StreamSource(file);
			transformerAUX.transform(auxMetadataFile, new StreamResult(new File("tmp/output.xml")));
			// JSON creation
			JSONObject metadataJSONObject = XML.toJSONObject(readFile("tmp/output.xml", Charset.defaultCharset()));
			if (metadataJSONObject.getString("validityStopTime").equals("UTC=9999-99-99T99:99:99")) {
				metadataJSONObject.put("validityStopTime", "9999-12-31T23:59:59");
			} else {
				metadataJSONObject.put("validityStopTime", metadataJSONObject.getString("validityStopTime").substring(4));
			}
			if(metadataJSONObject.getString("validityStartTime").substring(0, 4).equals("UTC=")) {
				metadataJSONObject.put("validityStartTime", metadataJSONObject.getString("validityStartTime").substring(4));
			}
			if(metadataJSONObject.getString("creationTime").substring(0, 4).equals("UTC=")) {
				metadataJSONObject.put("creationTime", metadataJSONObject.getString("creationTime").substring(4));
			}
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productClass", descriptor.getProductClass());
			metadataJSONObject.put("productType", descriptor.getProductType());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
            metadataJSONObject.put("productFamily", descriptor.getProductFamily().name());
			return metadataJSONObject;

		} catch (IOException | TransformerException | JSONException e) {
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Function which extracts metadata from AUX MANIFEST file
	 * 
	 * @param descriptor The file descriptor of the auxiliary file
	 * @param file The file containing the metadata
	 * 
	 * @return the json object with extracted metadata
	 * 
	 * @throws MetadataExtractionException
	 */
	public JSONObject processSAFEFile(ConfigFileDescriptor descriptor, File file) throws MetadataExtractionException {
		try {
			// XSLT Transformation
			String xsltFilename = this.xsltDirectory + "XSLT_AUX_MANIFEST.xslt";
			Source xsltAUXMANIFEST = new StreamSource(new File(xsltFilename));
			Transformer transformerAUX = transFactory.newTransformer(xsltAUXMANIFEST);
			Source auxMetadataFile = new StreamSource(file);
			transformerAUX.transform(auxMetadataFile, new StreamResult(new File("tmp/output.xml")));
			// JSON creation
			JSONObject metadataJSONObject = XML.toJSONObject(readFile("tmp/output.xml", Charset.defaultCharset()));
			metadataJSONObject.put("validityStopTime", "9999-12-31T23:59:59");
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productType", descriptor.getProductType());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
            metadataJSONObject.put("productFamily", descriptor.getProductFamily().name());
			return metadataJSONObject;
		} catch (IOException | TransformerException | JSONException e) {
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Function which extracts metadata from RAW file
	 * 
	 * @param descriptor The file descriptor of the raw file
	 * 
	 * @return the json object with extracted metadata
	 * 
	 * @throws MetadataExtractionException
	 */
	public JSONObject processRAWFile(EdrsSessionFileDescriptor descriptor) throws MetadataExtractionException {
		try {
			JSONObject metadataJSONObject = new JSONObject();
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productType", descriptor.getProductType().name());
			metadataJSONObject.put("sessionId", descriptor.getSessionIdentifier());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
            metadataJSONObject.put("productFamily", descriptor.getProductFamily().name());
			return metadataJSONObject;
		} catch (JSONException e) {
			throw new MetadataExtractionException(e);
		}
	}

	/**
	 * Function which extracts metadata from SESSION file
	 * 
	 * @param descriptor The file descriptor of the session file
	 * 
	 * @return the json object with extracted metadata
	 * 
	 * @throws MetadataExtractionException
	 */
	public JSONObject processSESSIONFile(EdrsSessionFileDescriptor descriptor) throws MetadataExtractionException {
		try {
			JSONObject metadataJSONObject = new JSONObject();
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productType", descriptor.getProductType().name());
			metadataJSONObject.put("sessionId", descriptor.getSessionIdentifier());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
            metadataJSONObject.put("productFamily", descriptor.getProductFamily().name());
			return metadataJSONObject;
		} catch (JSONException e) {
			throw new MetadataExtractionException(e);
		}
	}
	
	public JSONObject processL0SliceProd(L0OutputFileDescriptor descriptor, File file) throws MetadataExtractionException {
		return this.processL0Prod(descriptor, file, "tmp/outputl0slices.xml");
	}
	
	public JSONObject processL0AcnProd(L0OutputFileDescriptor descriptor, File file) throws MetadataExtractionException {
		return this.processL0Prod(descriptor, file, "tmp/outputl0acns.xml");
	}
	
	/**
	 * Function which extracts metadata from L0 product
	 * 
	 * @param descriptor
	 * @param file
	 * @param output
	 * 
	 * @return the json object with extracted metadata
	 * 
	 * @throws MetadataExtractionException
	 */
	private JSONObject processL0Prod(L0OutputFileDescriptor descriptor, File file, String output) throws MetadataExtractionException {
		try {
			//XSLT Transformation
			String xsltFilename = this.xsltDirectory + "XSLT_L0_MANIFEST.xslt";
			Source xsltL1MANIFEST = new StreamSource(new File(xsltFilename));
	        Transformer transformerL0 = transFactory.newTransformer(xsltL1MANIFEST);
	        Source l1File = new StreamSource(file);
	        transformerL0.transform(l1File, new StreamResult(new File(output)));
	        //JSON creation
	        JSONObject metadataJSONObject = XML.toJSONObject(readFile(output, Charset.defaultCharset()));
	        if(metadataJSONObject.has("startTime")) {
                metadataJSONObject.put("validityStartTime", metadataJSONObject.getString("startTime"));
            }
            if(metadataJSONObject.has("stopTime")) {
                metadataJSONObject.put("validityStopTime", metadataJSONObject.getString("stopTime"));
            }
	        if(metadataJSONObject.has("sliceCoordinates") && !metadataJSONObject.getString("sliceCoordinates").isEmpty()) {
	        	metadataJSONObject.put("sliceCoordinates", processCoordinates(descriptor.getProductName(), metadataJSONObject.getString("sliceCoordinates"))); 
	        }	
	        if(descriptor.getProductClass().equals("A") || descriptor.getProductClass().equals("C") || descriptor.getProductClass().equals("N")) {
	        	if(metadataJSONObject.has("startTime") && metadataJSONObject.has("stopTime")) {
	        		if(descriptor.getSwathtype().matches("IW")) {
	            		metadataJSONObject.put("totalNumberOfSlice", 
							totalNumberOfSlice(
									dateFormat.parse(metadataJSONObject.getString("startTime")).getTime()/1000, 
									dateFormat.parse(metadataJSONObject.getString("stopTime")).getTime()/1000,
									descriptor.getSwathtype()));
	        		}	
		        	else if (descriptor.getSwathtype().matches("EW")) {
						metadataJSONObject.put("totalNumberOfSlice", 
								totalNumberOfSlice(
										dateFormat.parse(metadataJSONObject.getString("startTime")).getTime()/1000, 
										dateFormat.parse(metadataJSONObject.getString("stopTime")).getTime()/1000,
										descriptor.getSwathtype()));
		        	}
		        	else if (descriptor.getSwathtype().matches("WV")) {
						metadataJSONObject.put("totalNumberOfSlice", 
								totalNumberOfSlice(
										dateFormat.parse(metadataJSONObject.getString("startTime")).getTime()/1000, 
										dateFormat.parse(metadataJSONObject.getString("stopTime")).getTime()/1000,
										descriptor.getSwathtype()));
	
		        	}
		        	else if (descriptor.getSwathtype().matches("S[1-6]")) {
						metadataJSONObject.put("totalNumberOfSlice", 
								totalNumberOfSlice(
										dateFormat.parse(metadataJSONObject.getString("startTime")).getTime()/1000, 
										dateFormat.parse(metadataJSONObject.getString("stopTime")).getTime()/1000,
										"SM"));
		        	}
	        	}
	        }
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
	        metadataJSONObject.put("processMode", descriptor.getMode());
	        String dt = dateFormat.format(new Date());
	        metadataJSONObject.put("insertionTime", dt);
	        metadataJSONObject.put("creationTime", dt);
            metadataJSONObject.put("productFamily", descriptor.getProductFamily().name());
	        return metadataJSONObject;
		} catch (IOException | TransformerException | JSONException | ParseException e) {
			throw new MetadataExtractionException(e);
		}
	}
	
	public JSONObject processL0Segment(L0OutputFileDescriptor descriptor, File file) throws MetadataExtractionException {
	    try {
            //XSLT Transformation
            String xsltFilename = this.xsltDirectory + "XSLT_L0_SEGMENT.xslt";
            Source xsltL1MANIFEST = new StreamSource(new File(xsltFilename));
            Transformer transformerL0 = transFactory.newTransformer(xsltL1MANIFEST);
            Source l1File = new StreamSource(file);
            transformerL0.transform(l1File, new StreamResult(new File("tmp/outputl0seg.xml")));
            //JSON creation
            JSONObject metadataJSONObject = XML.toJSONObject(readFile("tmp/outputl0seg.xml", Charset.defaultCharset()));
            if(metadataJSONObject.has("startTime")) {
                metadataJSONObject.put("validityStartTime", metadataJSONObject.getString("startTime"));
            }
            if(metadataJSONObject.has("stopTime")) {
                metadataJSONObject.put("validityStopTime", metadataJSONObject.getString("stopTime"));
            }
            if(metadataJSONObject.has("segmentCoordinates")) {
                metadataJSONObject.put("segmentCoordinates", processCoordinates(descriptor.getProductName(), metadataJSONObject.getString("segmentCoordinates"))); 
            }
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
            metadataJSONObject.put("processMode", descriptor.getMode());
            String dt = dateFormat.format(new Date());
            metadataJSONObject.put("insertionTime", dt);
            metadataJSONObject.put("creationTime", dt);
            metadataJSONObject.put("productFamily", descriptor.getProductFamily().name());
            return metadataJSONObject;
        } catch (IOException | TransformerException | JSONException e) {
            throw new MetadataExtractionException(e);
        }
	}
	
	public JSONObject processL1SliceProd(L1OutputFileDescriptor descriptor, File file) throws MetadataExtractionException {
		return this.processL1Prod(descriptor, file, "tmp/outputl1slices.xml");
	}
	
	public JSONObject processL1AProd(L1OutputFileDescriptor descriptor, File file) throws MetadataExtractionException {
		return this.processL1Prod(descriptor, file, "tmp/outputl1a.xml");
	}
	
	/**
	 * Function which extracts metadata from L1 product
	 * 
	 * @param descriptor
	 * @param file
	 * @param output
	 * 
	 * @return the json object with extracted metadata
	 * 
	 * @throws MetadataExtractionException
	 */
	private JSONObject processL1Prod(L1OutputFileDescriptor descriptor, File file, String output)
			throws MetadataExtractionException {
		try {
			//XSLT Transformation
			String xsltFilename = this.xsltDirectory + "XSLT_L1_MANIFEST.xslt";
			Source xsltL1MANIFEST = new StreamSource(new File(xsltFilename));
	        Transformer transformerL1 = transFactory.newTransformer(xsltL1MANIFEST);
	        Source l1File = new StreamSource(file);
	        transformerL1.transform(l1File, new StreamResult(new File(output)));
	        //JSON creation
	        JSONObject metadataJSONObject = XML.toJSONObject(readFile(output, Charset.defaultCharset()));
	        if(metadataJSONObject.has("sliceCoordinates") && !metadataJSONObject.getString("sliceCoordinates").isEmpty()) {
	        	metadataJSONObject.put("sliceCoordinates", processCoordinates(descriptor.getProductName(), metadataJSONObject.getString("sliceCoordinates")));       	
	        }
	        if(metadataJSONObject.has("startTime")) {
	        	metadataJSONObject.put("validityStartTime", metadataJSONObject.getString("startTime"));
	        }
	        if(metadataJSONObject.has("stopTime")) {
	        	metadataJSONObject.put("validityStopTime", metadataJSONObject.getString("stopTime"));
	        }
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
	        String dt = dateFormat.format(new Date());
	        metadataJSONObject.put("insertionTime", dt);
	        metadataJSONObject.put("creationTime", dt);
            metadataJSONObject.put("productFamily", descriptor.getProductFamily().name());
            metadataJSONObject.put("processMode", descriptor.getMode());
	        return metadataJSONObject;
		} catch (IOException | TransformerException | JSONException e) {
			throw new MetadataExtractionException(e);
		}	
	}
}
