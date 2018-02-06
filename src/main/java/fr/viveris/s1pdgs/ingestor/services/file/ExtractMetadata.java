/**
 * 
 */
package fr.viveris.s1pdgs.ingestor.services.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.ingestor.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileDescriptor;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class to extract the metadata from various types of files
 * 
 * @author Olivier Bex-Chauvet
 * 
 */
@Service
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
	private String readFile(String FileName, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(FileName));
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
	public String processMPLEOFFile(ConfigFileDescriptor descriptor, File file) throws IOException, URISyntaxException, TransformerException, JSONException {
		//XSLT Transformation
		Source XSLTMPLEOF = new StreamSource(new File("xsltDir/XSLT_MPL_EOF.xslt"));
        Transformer transformerMPL = transFactory.newTransformer(XSLTMPLEOF);
        Source MPLMetadataFile = new StreamSource(file);
        transformerMPL.transform(MPLMetadataFile, new StreamResult(new File("tmp/output.xml")));
        //JSON creation
        JSONObject jsonFromXmlTmp = XML.toJSONObject(readFile("tmp/output.xml", Charset.defaultCharset()), true);
        JSONObject MetadataJSONObject = new JSONObject();
        if(jsonFromXmlTmp.getJSONObject("validityStopTime").getString("content").equals("UTC=9999-99-99T99:99:99")) {
        	MetadataJSONObject.put("validityStopTime", "9999-12-31T23:59:59");
        }
        else {
        	MetadataJSONObject.put("validityStopTime", jsonFromXmlTmp.getJSONObject("validityStopTime").get("content").toString().substring(4, jsonFromXmlTmp.getJSONObject("validityStopTime").get("content").toString().length()));
        }
        MetadataJSONObject.put("creationTime", jsonFromXmlTmp.getJSONObject("creationTime").get("content").toString().substring(4, jsonFromXmlTmp.getJSONObject("creationTime").get("content").toString().length()));
        MetadataJSONObject.put("validityStartTime", jsonFromXmlTmp.getJSONObject("validityStartTime").get("content").toString().substring(4, jsonFromXmlTmp.getJSONObject("validityStartTime").get("content").toString().length()));
        MetadataJSONObject.put("version", jsonFromXmlTmp.getJSONObject("version").get("content"));
        MetadataJSONObject.put("productName", descriptor.getProductName());
        MetadataJSONObject.put("productClass", descriptor.getProductClass());
        MetadataJSONObject.put("productType", descriptor.getProductType());	        
        MetadataJSONObject.put("missionid", descriptor.getMissionId());
        MetadataJSONObject.put("satelliteid", descriptor.getSatelliteId());
        MetadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
        return MetadataJSONObject.toString();
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
	public String processAUXEOFFile(ConfigFileDescriptor descriptor, File file) throws IOException, URISyntaxException, TransformerException, JSONException {
		//XSLT Transformation
		Source XSLTAUXEOF = new StreamSource(new File("xsltDir/XSLT_AUX_EOF.xslt"));
        Transformer transformerMPL = transFactory.newTransformer(XSLTAUXEOF);
        Source MPLMetadataFile = new StreamSource(file);
        transformerMPL.transform(MPLMetadataFile, new StreamResult(new File("tmp/output.xml")));
        //JSON creation
        JSONObject MetadataJSONObject = XML.toJSONObject(readFile("tmp/output.xml", Charset.defaultCharset()), true);
        MetadataJSONObject.put("validityStopTime", MetadataJSONObject.getString("validityStopTime").toString().substring(4, MetadataJSONObject.getString("validityStopTime").toString().length()));
        MetadataJSONObject.put("creationTime", MetadataJSONObject.getString("creationTime").toString().substring(4, MetadataJSONObject.getString("creationTime").toString().length()));
        MetadataJSONObject.put("validityStartTime", MetadataJSONObject.getString("validityStartTime").toString().substring(4, MetadataJSONObject.getString("validityStartTime").toString().length()));
        MetadataJSONObject.put("productName", descriptor.getProductName());
        MetadataJSONObject.put("productClass", descriptor.getProductClass());
        MetadataJSONObject.put("productType", descriptor.getProductType());	        
        MetadataJSONObject.put("missionid", descriptor.getMissionId());
        MetadataJSONObject.put("satelliteid", descriptor.getSatelliteId());
        MetadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
        return MetadataJSONObject.toString();
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
	public String processAUXXMLFile(ConfigFileDescriptor descriptor, File file) throws IOException, URISyntaxException, TransformerException, JSONException {
		//XSLT Transformation
		Source XSLTAUXXML = new StreamSource(new File("xsltDir/XSLT_AUX_XML.xslt"));
        Transformer transformerAUX = transFactory.newTransformer(XSLTAUXXML);
        Source AUXMetadataFile = new StreamSource(file);
        transformerAUX.transform(AUXMetadataFile, new StreamResult(new File("tmp/output.xml")));
        //JSON creation
        JSONObject MetadataJSONObject = XML.toJSONObject(readFile("tmp/output.xml", Charset.defaultCharset()), true);
        if(MetadataJSONObject.getString("validityStopTime").equals("UTC=9999-99-99T99:99:99")) {
        	MetadataJSONObject.put("validityStopTime", "9999-12-31T23:59:59");
        }
        else {
        	MetadataJSONObject.put("validityStopTime", MetadataJSONObject.getString("validityStopTime").toString().substring(4, MetadataJSONObject.getString("validityStopTime").toString().length()));
        }
        MetadataJSONObject.put("productName", descriptor.getProductName());
        MetadataJSONObject.put("productClass", descriptor.getProductClass());
        MetadataJSONObject.put("productType", descriptor.getProductType());	        
        MetadataJSONObject.put("missionid", descriptor.getMissionId());
        MetadataJSONObject.put("satelliteid", descriptor.getSatelliteId());
        MetadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
        return MetadataJSONObject.toString();
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
	public String processAUXMANIFESTFile(ConfigFileDescriptor descriptor, File file) throws IOException, URISyntaxException, TransformerException, JSONException {
		//XSLT Transformation
		Source XSLTAUXMANIFEST = new StreamSource(new File("xsltDir/XSLT_AUX_MANIFEST.xslt"));
        Transformer transformerAUX = transFactory.newTransformer(XSLTAUXMANIFEST);
        Source AUXMetadataFile = new StreamSource(file);
        transformerAUX.transform(AUXMetadataFile, new StreamResult(new File("tmp/output.xml")));
        //JSON creation
        JSONObject jsonFromXmlTmp = XML.toJSONObject(readFile("tmp/output.xml", Charset.defaultCharset()), true);
        JSONObject MetadataJSONObject = new JSONObject();
        MetadataJSONObject.put("site", jsonFromXmlTmp.getJSONObject("site").getString("site"));
        MetadataJSONObject.put("instrumentConfigurationId", jsonFromXmlTmp.getJSONObject("instrumentConfigurationId").getString("content"));
        MetadataJSONObject.put("creationTime", jsonFromXmlTmp.getJSONObject("creationTime").getString("content"));
        MetadataJSONObject.put("validityStartTime", jsonFromXmlTmp.getJSONObject("validityStartTime").getString("content"));
        MetadataJSONObject.put("productName", descriptor.getProductName());
        MetadataJSONObject.put("productType", descriptor.getProductType());	        
        MetadataJSONObject.put("missionid", descriptor.getMissionId());
        MetadataJSONObject.put("satelliteid", descriptor.getSatelliteId());
        MetadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
        return MetadataJSONObject.toString();
	}
	
	/**
	 * Function which extracts metadata from RAW file
	 * 
	 * @param MD_FileName
	 * 
	 * @return the json object with extracted metadata
	 * @throws JSONException 
	 */
	public String processRAWFile(ErdsSessionFileDescriptor descriptor) throws JSONException {
		JSONObject MetadataJSONObject = new JSONObject();
		MetadataJSONObject.put("productName", descriptor.getProductName());
		MetadataJSONObject.put("productType", descriptor.getProductType());
		MetadataJSONObject.put("sessionid", descriptor.getSessionIdentifier());
		MetadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
        return MetadataJSONObject.toString();
	}
	
	/**
	 * Function which extracts metadata from SESSION file
	 * 
	 * @param MD_FileName
	 * 
	 * @return the json object with extracted metadata
	 * @throws JSONException 
	 */
	public String processSESSIONFile(ErdsSessionFileDescriptor descriptor) throws JSONException {
		JSONObject MetadataJSONObject = new JSONObject();
		MetadataJSONObject.put("productName", descriptor.getProductName());
		MetadataJSONObject.put("productType", descriptor.getProductType());
		MetadataJSONObject.put("sessionid", descriptor.getSessionIdentifier());
		MetadataJSONObject.put("insertionTime", dateFormat.format(new Date()));
        return MetadataJSONObject.toString();
	}
}
