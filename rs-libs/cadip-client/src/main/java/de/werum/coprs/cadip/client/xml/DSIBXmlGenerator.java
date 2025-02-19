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

package de.werum.coprs.cadip.client.xml;

import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DSIBXmlGenerator {

	public static String generate(String sessionId, List<String> filenames, String startTime, String stopTime, long size) {
		try {
			/*
			 * Create a new builder for a DSIB element
			 */
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();

			Comment headline = doc.createComment(
					"This DSIB was generated automatically by the COPRS CADIP ingestion system. Only required fields are filled!");
			doc.appendChild(headline);

			Element rootElement = doc.createElement("DCSU_Session_Information_Block");
			doc.appendChild(rootElement);

			Element e_sessionId = doc.createElement("session_id");
			e_sessionId.setTextContent(sessionId);
			/*
			 * Following elements are not used by COPRS and thus can be left empty
			 */
			Element e_timestart = doc.createElement("time_start");
			e_timestart.setTextContent(startTime);
			Element e_timestop = doc.createElement("time_stop");
			e_timestop.setTextContent(stopTime);
			Element e_timecreate = doc.createElement("time_created");
			e_timecreate.setTextContent(startTime);
			Element e_timefinished = doc.createElement("time_finished");
			e_timefinished.setTextContent(stopTime);
			Element e_datasize = doc.createElement("data_size");
			e_datasize.setTextContent(Long.toString(size));

			rootElement.appendChild(e_sessionId);
			rootElement.appendChild(e_timestart);
			rootElement.appendChild(e_timestop);
			rootElement.appendChild(e_timecreate);
			rootElement.appendChild(e_timefinished);
			rootElement.appendChild(e_datasize);

			Element e_dsdb_list = doc.createElement("dsdb_list");
			rootElement.appendChild(e_dsdb_list);

			/*
			 * Add all provided files to the DSIB file list
			 */
			for (String filename : filenames) {
				Element e_filename = doc.createElement("dsdb_name");
				e_dsdb_list.appendChild(e_filename);
				e_filename.setTextContent(filename);
			}

			/*
			 * Generate a pretty string containing the DSIB content 
			 */
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			StringWriter writer = new StringWriter();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(writer);
			transformer.transform(source, result);
			
			return writer.getBuffer().toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}

	public static String generateName(final String sessionId, final long channelId) {
		return "DCS_00_" + sessionId + "_ch" + channelId + "_DSIB.xml";
	}
}
