/**
 * 
 */
package fr.viveris.s1pdgs.ingestor.services.file;

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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import fr.viveris.s1pdgs.ingestor.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.exception.MetadataExtractionException;

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
			JSONObject jsonFromXmlTmp = XML.toJSONObject(readFile("tmp/output.xml", Charset.defaultCharset()), true);
			JSONObject metadataJSONObject = new JSONObject();
			if (jsonFromXmlTmp.getJSONObject("validityStopTime").getString("content")
					.equals("UTC=9999-99-99T99:99:99")) {
				metadataJSONObject.put("validityStopTime", "9999-12-31T23:59:59");
			} else {
				metadataJSONObject.put("validityStopTime",
						jsonFromXmlTmp.getJSONObject("validityStopTime").get("content").toString().substring(4,
								jsonFromXmlTmp.getJSONObject("validityStopTime").get("content").toString().length()));
			}
			metadataJSONObject.put("creationTime",
					jsonFromXmlTmp.getJSONObject("creationTime").get("content").toString().substring(4,
							jsonFromXmlTmp.getJSONObject("creationTime").get("content").toString().length()));
			metadataJSONObject.put("validityStartTime",
					jsonFromXmlTmp.getJSONObject("validityStartTime").get("content").toString().substring(4,
							jsonFromXmlTmp.getJSONObject("validityStartTime").get("content").toString().length()));
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
			JSONObject metadataJSONObject = XML.toJSONObject(readFile("tmp/output.xml", Charset.defaultCharset()),
					true);
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
			JSONObject metadataJSONObject = XML.toJSONObject(readFile("tmp/output.xml", Charset.defaultCharset()),
					true);
			if (metadataJSONObject.getString("validityStopTime").equals("UTC=9999-99-99T99:99:99")) {
				metadataJSONObject.put("validityStopTime", "9999-12-31T23:59:59");
			} else {
				metadataJSONObject.put("validityStopTime", metadataJSONObject.getString("validityStopTime").toString()
						.substring(4, metadataJSONObject.getString("validityStopTime").toString().length()));
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
			JSONObject jsonFromXmlTmp = XML.toJSONObject(readFile("tmp/output.xml", Charset.defaultCharset()), true);
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
	public JSONObject processRAWFile(ErdsSessionFileDescriptor descriptor) throws MetadataExtractionException {
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
	public JSONObject processSESSIONFile(ErdsSessionFileDescriptor descriptor) throws MetadataExtractionException {
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
}
