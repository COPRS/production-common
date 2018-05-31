package fr.viveris.s1pdgs.jobgenerator.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.xml.transform.StringResult;

/**
 * XML converter
 * @author Cyrielle Gailliard
 *
 */
public class XmlConverter {
	
	/**
	 * Marshaller
	 */
	private Marshaller marshaller;
	
	/**
	 * Unmarshaller
	 */
	private Unmarshaller unmarshaller;

	/**
	 * @param marshaller the marshaller to set
	 */
	public void setMarshaller(final Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	/**
	 * @param unmarshaller the unmarshaller to set
	 */
	public void setUnmarshaller(final Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}

	/**
	 * Convert an object into an XML file
	 * @param object
	 * @param filepath
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void convertFromObjectToXML(Object object, String filepath) throws IOException, JAXBException {
		try (FileOutputStream os = new FileOutputStream(filepath)) {
			marshaller.marshal(object, new StreamResult(os));
		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * Convert an object into an string XML format
	 * @param object
	 * @param filepath
	 * @throws IOException
	 * @throws JAXBException
	 */
	public String convertFromObjectToXMLString(Object object) throws IOException, JAXBException {
		try {
			StringResult r = new StringResult();
			marshaller.marshal(object, r);
			return r.toString();
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Convert an XML file into an object
	 * @param xmlfile
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 */
	public Object convertFromXMLToObject(String xmlfile) throws IOException, JAXBException {
		try (FileInputStream is = new FileInputStream(xmlfile)) {
			return unmarshaller.unmarshal(new StreamSource(is));
		} catch (Exception e) {
			throw e;
		}
	}
}