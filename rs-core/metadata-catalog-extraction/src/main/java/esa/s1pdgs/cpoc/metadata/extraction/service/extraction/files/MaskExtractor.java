package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MaskExtractor {
	
	public List<Map<String, Object>> extract(File eofFile)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		List<Map<String, Object>> result = new ArrayList<>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document xml = db.parse(eofFile);

		XPathFactory xpf = XPathFactory.newInstance();
		XPath xpath = xpf.newXPath();

		String json = (String) xpath.evaluate("/Earth_Explorer_File/Data_Block", xml, XPathConstants.STRING);

		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		Map<String, Object> root = new Gson().newBuilder().create().fromJson(json, type);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> features = (List<Map<String, Object>>)root.get("features");
		for (Map<String, Object> feature : features) {
			@SuppressWarnings("unchecked")
			Map<String, Object> geometry = (Map<String, Object>)feature.get("geometry");
			Map<String, Object> strippedDownFeature = new HashMap<>();
			strippedDownFeature.put("geometry", geometry);
			result.add(strippedDownFeature);
		}

		return result;

	}
}
