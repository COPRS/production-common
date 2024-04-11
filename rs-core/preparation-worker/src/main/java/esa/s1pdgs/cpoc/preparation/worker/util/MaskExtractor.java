/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.preparation.worker.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			strippedDownFeature.put("name", ((Map<String, Object>) feature.get("properties")).get("name"));
			result.add(strippedDownFeature);
		}

		return result;

	}
}
