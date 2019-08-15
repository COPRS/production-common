package esa.s1pdgs.cpoc.mdcatalog.extraction.xml;

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
 * 
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
	 * @param marshaller
	 *            the marshaller to set
	 */
	public void setMarshaller(final Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	/**
	 * @param unmarshaller
	 *            the unmarshaller to set
	 */
	public void setUnmarshaller(final Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}

	/**
	 * Convert an object into an XML file
	 * 
	 * @param object
	 * @param filepath
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void convertFromObjectToXML(final Object object, final String filepath) throws IOException, JAXBException {
		FileOutputStream os = new FileOutputStream(filepath);
		marshaller.marshal(object, new StreamResult(os));
	}

	/**
	 * Convert an object into an string XML format
	 * 
	 * @param object
	 * @param filepath
	 * @throws IOException
	 * @throws JAXBException
	 */
	public String convertFromObjectToXMLString(final Object object) throws IOException, JAXBException {
		StringResult ret = new StringResult();
		marshaller.marshal(object, ret);
		return ret.toString();
	}

	/**
	 * Convert an XML file into an object
	 * 
	 * @param xmlfile
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 */
	public Object convertFromXMLToObject(String xmlfile) throws IOException, JAXBException {
		FileInputStream is = new FileInputStream(xmlfile);
		return unmarshaller.unmarshal(new StreamSource(is));
	}
}