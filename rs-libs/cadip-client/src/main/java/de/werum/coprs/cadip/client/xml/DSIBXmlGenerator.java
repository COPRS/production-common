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

	public static String generate(String sessionId, List<String> filenames) {
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
			Element e_timestop = doc.createElement("time_stop");
			Element e_timecreate = doc.createElement("time_stop");
			Element e_timefinished = doc.createElement("time_finished");
			Element e_datasize = doc.createElement("data_size");

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
