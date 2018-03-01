/**
 * 
 */
package fr.viveris.s1pdgs.mdcatalog.services.files;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

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

import fr.viveris.s1pdgs.mdcatalog.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.EdrsSessionFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.L0OutputFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.exception.MetadataExtractionException;

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
	 * Constructor
	 * 
	 * @param factory
	 * @param dateFormat
	 * @param pathToWorkingDir
	 */
	public ExtractMetadata() {
		this.transFactory = TransformerFactory.newInstance();
		this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
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
	 * @param descriptor
	 * 
	 * @return the coordinates in good format
	 * @throws MetadataExtractionException 
	 */
	private JSONArray processCoordinates(L0OutputFileDescriptor descriptor, String rawCoordinates) throws MetadataExtractionException {
		JSONArray coordinates = new JSONArray();
		try {
			for (String coord : rawCoordinates.split(" ")) {
				coordinates.put(new JSONArray("[" + (coord.split(","))[1] + "," + (coord.split(","))[0] + "]"));
			}
		} catch (JSONException e) {
			throw new MetadataExtractionException(descriptor.getProductName(), e);
		}
		return new JSONArray().put(coordinates);
	}

	/**
	 * Function which extracts metadata from MPL EOF file
	 * 
	 * @param MD_FileName
	 * 
	 * @return the json object with extracted metadata
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws TransformerException
	 * @throws JSONException
	 */
	public JSONObject processEOFFile(ConfigFileDescriptor descriptor, File file) throws MetadataExtractionException {
		try {
			// XSLT Transformation
			Source xsltMPLEOF = new StreamSource(new File("xsltDir/XSLT_MPL_EOF.xslt"));
			Transformer transformerMPL = transFactory.newTransformer(xsltMPLEOF);
			Source mplMetadataFile = new StreamSource(file);
			transformerMPL.transform(mplMetadataFile, new StreamResult(new File("tmp/output.xml")));
			// JSON creation
			JSONObject jsonFromXmlTmp = XML.toJSONObject(readFile("tmp/output.xml", Charset.defaultCharset()));
			JSONObject metadataJSONObject = new JSONObject();
			if (jsonFromXmlTmp.getJSONObject("validityStopTime").getString("content")
					.equals("UTC=9999-99-99T99:99:99")) {
				metadataJSONObject.put("validityStopTime", "9999-12-31T23:59:59");
			} else {
				metadataJSONObject.put("validityStopTime",
						jsonFromXmlTmp.getJSONObject("validityStopTime").get("content").toString().substring(4));
			}
			metadataJSONObject.put("creationTime",
					jsonFromXmlTmp.getJSONObject("creationTime").get("content").toString().substring(4));
			metadataJSONObject.put("validityStartTime",
					jsonFromXmlTmp.getJSONObject("validityStartTime").get("content").toString().substring(4));
			metadataJSONObject.put("version", jsonFromXmlTmp.getJSONObject("version").get("content"));
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productClass", descriptor.getProductClass());
			metadataJSONObject.put("productType", descriptor.getProductType());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
			return metadataJSONObject;
		} catch (IOException | TransformerException | JSONException e) {
			throw new MetadataExtractionException(descriptor.getProductName(), e);
		}
	}

	/**
	 * Function which extracts metadata from AUX EOF file
	 * 
	 * @param MD_FileName
	 * 
	 * @return the json object with extracted metadata
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws TransformerException
	 * @throws JSONException
	 */
	public JSONObject processEOFFileWithoutNamespace(ConfigFileDescriptor descriptor, File file)
			throws MetadataExtractionException {
		try {
			// XSLT Transformation
			Source xsltAUXEOF = new StreamSource(new File("xsltDir/XSLT_AUX_EOF.xslt"));
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
			return metadataJSONObject;
		} catch (IOException | TransformerException | JSONException e) {
			throw new MetadataExtractionException(descriptor.getProductName(), e);
		}
	}

	/**
	 * Function which extracts metadata from AUX XML file
	 * 
	 * @param MD_FileName
	 * 
	 * @return the json object with extracted metadata
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws TransformerException
	 * @throws JSONException
	 */
	public JSONObject processXMLFile(ConfigFileDescriptor descriptor, File file) throws MetadataExtractionException {
		try {
			// XSLT Transformation
			Source xsltAUXXML = new StreamSource(new File("xsltDir/XSLT_AUX_XML.xslt"));
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
			return metadataJSONObject;

		} catch (IOException | TransformerException | JSONException e) {
			throw new MetadataExtractionException(descriptor.getProductName(), e);
		}
	}

	/**
	 * Function which extracts metadata from AUX MANIFEST file
	 * 
	 * @param MD_FileName
	 * 
	 * @return the json object with extracted metadata
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws TransformerException
	 * @throws JSONException
	 */
	public JSONObject processSAFEFile(ConfigFileDescriptor descriptor, File file) throws MetadataExtractionException {
		try {
			// XSLT Transformation
			Source xsltAUXMANIFEST = new StreamSource(new File("xsltDir/XSLT_AUX_MANIFEST.xslt"));
			Transformer transformerAUX = transFactory.newTransformer(xsltAUXMANIFEST);
			Source auxMetadataFile = new StreamSource(file);
			transformerAUX.transform(auxMetadataFile, new StreamResult(new File("tmp/output.xml")));
			// JSON creation
			JSONObject jsonFromXmlTmp = XML.toJSONObject(readFile("tmp/output.xml", Charset.defaultCharset()));
			JSONObject metadataJSONObject = new JSONObject();
			metadataJSONObject.put("site", jsonFromXmlTmp.getJSONObject("site").getString("site"));
			metadataJSONObject.put("instrumentConfigurationId",
					jsonFromXmlTmp.getJSONObject("instrumentConfigurationId").getString("content"));
			metadataJSONObject.put("creationTime", jsonFromXmlTmp.getJSONObject("creationTime").getString("content"));
			metadataJSONObject.put("validityStartTime",
					jsonFromXmlTmp.getJSONObject("validityStartTime").getString("content"));
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productType", descriptor.getProductType());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
			return metadataJSONObject;
		} catch (IOException | TransformerException | JSONException e) {
			throw new MetadataExtractionException(descriptor.getProductName(), e);
		}
	}

	/**
	 * Function which extracts metadata from RAW file
	 * 
	 * @param MD_FileName
	 * 
	 * @return the json object with extracted metadata
	 * @throws JSONException
	 */
	public JSONObject processRAWFile(EdrsSessionFileDescriptor descriptor) throws MetadataExtractionException {
		try {
			JSONObject metadataJSONObject = new JSONObject();
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productType", descriptor.getProductType());
			metadataJSONObject.put("sessionId", descriptor.getSessionIdentifier());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
			return metadataJSONObject;
		} catch (JSONException e) {
			throw new MetadataExtractionException(descriptor.getProductName(), e);
		}
	}

	/**
	 * Function which extracts metadata from SESSION file
	 * 
	 * @param MD_FileName
	 * 
	 * @return the json object with extracted metadata
	 * @throws JSONException
	 */
	public JSONObject processSESSIONFile(EdrsSessionFileDescriptor descriptor) throws MetadataExtractionException {
		try {
			JSONObject metadataJSONObject = new JSONObject();
			metadataJSONObject.put("productName", descriptor.getProductName());
			metadataJSONObject.put("productType", descriptor.getProductType());
			metadataJSONObject.put("sessionId", descriptor.getSessionIdentifier());
			metadataJSONObject.put("missionId", descriptor.getMissionId());
			metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
			metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
			metadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
			return metadataJSONObject;
		} catch (JSONException e) {
			throw new MetadataExtractionException(descriptor.getProductName(), e);
		}
	}
	
	public JSONObject processL0Prod(L0OutputFileDescriptor descriptor, File file) throws MetadataExtractionException {
		try {
			//XSLT Transformation
			Source xsltL0MANIFEST = new StreamSource(new File("XSLT_L0_MANIFEST.xslt"));
	        Transformer transformerL0 = transFactory.newTransformer(xsltL0MANIFEST);
	        Source l0File = new StreamSource(file);
	        transformerL0.transform(l0File, new StreamResult(new File("tmp/output.xml")));
	        //JSON creation
	        JSONObject jsonFromXmlTmp = XML.toJSONObject(readFile("tmp/output.xml", Charset.defaultCharset()));
	        JSONObject metadataJSONObject = new JSONObject();
	        if(jsonFromXmlTmp.getJSONObject("missionDataTakeId").has("content")) {
	        	metadataJSONObject.put("missionDataTakeId", jsonFromXmlTmp.getJSONObject("missionDataTakeId").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("totalNumberOfSlice").has("content")) {
	        	metadataJSONObject.put("totalNumberOfSlice", jsonFromXmlTmp.getJSONObject("totalNumberOfSlice").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("totalNumberOfSlice").has("content")) {
	        	metadataJSONObject.put("totalNumberOfSlice", jsonFromXmlTmp.getJSONObject("totalNumberOfSlice").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("theoreticalSliceLength").has("content")) {
	        	metadataJSONObject.put("theoreticalSliceLength", jsonFromXmlTmp.getJSONObject("theoreticalSliceLength").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("pass").has("content")) {
	        	metadataJSONObject.put("pass", jsonFromXmlTmp.getJSONObject("pass").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("stopTimeANX").has("content")) {
	        	metadataJSONObject.put("stopTimeANX", jsonFromXmlTmp.getJSONObject("stopTimeANX").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("sliceCoordinates").has("content")) {
	        	JSONObject coordinates = new JSONObject();
	        	coordinates.put("type", "Polygon");
	        	coordinates.put("coordinates", processCoordinates(descriptor, jsonFromXmlTmp.getJSONObject("sliceCoordinates").getString("content")));
	        	metadataJSONObject.put("sliceCoordinates", coordinates);       	
	        }
	        if(jsonFromXmlTmp.getJSONObject("sliceNumber").has("content")) {
	        	metadataJSONObject.put("sliceNumber", jsonFromXmlTmp.getJSONObject("sliceNumber").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("missionDataTakeId").has("content")) {
	        	metadataJSONObject.put("missionDataTakeId", jsonFromXmlTmp.getJSONObject("missionDataTakeId").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("sliceNumber").has("content")) {
	        	metadataJSONObject.put("sliceNumber", jsonFromXmlTmp.getJSONObject("sliceNumber").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("absoluteStopOrbit").has("content")) {
	        	metadataJSONObject.put("absoluteStopOrbit", jsonFromXmlTmp.getJSONObject("absoluteStopOrbit").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("circulationFlag").has("content")) {
	        	metadataJSONObject.put("circulationFlag", jsonFromXmlTmp.getJSONObject("circulationFlag").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("productConsolidation").has("content")) {
	        	metadataJSONObject.put("productConsolidation", jsonFromXmlTmp.getJSONObject("productConsolidation").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("absoluteStartOrbit").has("content")) {
	        	metadataJSONObject.put("absoluteStartOrbit", jsonFromXmlTmp.getJSONObject("absoluteStartOrbit").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("instrumentConfigurationId").has("content")) {
	        	metadataJSONObject.put("instrumentConfigurationId", jsonFromXmlTmp.getJSONObject("instrumentConfigurationId").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("sliceOverlap").has("content")) {
	        	metadataJSONObject.put("sliceOverlap", jsonFromXmlTmp.getJSONObject("sliceOverlap").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("startTimeANX").has("content")) {
	        	metadataJSONObject.put("startTimeANX", jsonFromXmlTmp.getJSONObject("startTimeANX").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("relativeStopOrbit").has("content")) {
	        	metadataJSONObject.put("relativeStopOrbit", jsonFromXmlTmp.getJSONObject("relativeStopOrbit").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("relativeStartOrbit").has("content")) {
	        	metadataJSONObject.put("relativeStartOrbit", jsonFromXmlTmp.getJSONObject("relativeStartOrbit").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("startTime").has("content")) {
	        	metadataJSONObject.put("startTime", jsonFromXmlTmp.getJSONObject("startTime").getString("content"));
	        }
	        if(jsonFromXmlTmp.getJSONObject("stopTime").has("content")) {
	        	metadataJSONObject.put("stopTime", jsonFromXmlTmp.getJSONObject("stopTime").getString("content"));
	        }
	        metadataJSONObject.put("productName", descriptor.getProductName());
	        metadataJSONObject.put("productClass", descriptor.getClass());
	        metadataJSONObject.put("productType", descriptor.getProductType());
	        metadataJSONObject.put("resolution", descriptor.getResolution());
	        metadataJSONObject.put("missionId", descriptor.getMissionId());
	        metadataJSONObject.put("satelliteId", descriptor.getSatelliteId());
	        metadataJSONObject.put("swathtype", descriptor.getSwathtype());
	        metadataJSONObject.put("polarisation", descriptor.getPolarisation());
	        metadataJSONObject.put("dataTakeId", descriptor.getDataTakeId());
	        metadataJSONObject.put("url", descriptor.getKeyObjectStorage());
	        metadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
	        return metadataJSONObject;
		} catch (IOException | TransformerException | JSONException e) {
			throw new MetadataExtractionException(descriptor.getProductName(), e);
		}
	}
}
