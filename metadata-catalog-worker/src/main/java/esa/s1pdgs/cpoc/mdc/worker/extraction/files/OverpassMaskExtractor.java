package esa.s1pdgs.cpoc.mdc.worker.extraction.files;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class OverpassMaskExtractor {

	public List<JSONObject> extract(File eofFile)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		List<JSONObject> result = new ArrayList<>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document xml = db.parse(eofFile);

		XPathFactory xpf = XPathFactory.newInstance();
		XPath xpath = xpf.newXPath();

		String json = (String) xpath.evaluate("/Earth_Explorer_File/Data_Block", xml, XPathConstants.STRING);

		StringReader reader = new StringReader(json);
		JSONTokener tokener = new JSONTokener(reader);
		JSONObject root = new JSONObject(tokener);

		JSONArray jsonArray = root.getJSONArray("features");

		for (int i = 0; i < jsonArray.length(); ++i) {
			JSONObject obj = new JSONObject();
			JSONObject currentFeature = jsonArray.getJSONObject(i);
			obj.put("geometry", currentFeature.getJSONObject("geometry"));
			result.add(obj);
		}

		return result;

	}
}
