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

package esa.s1pdgs.cpoc.xml;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
	private final Marshaller marshaller;
	private final Unmarshaller unmarshaller;
	
	public XmlConverter(final Marshaller marshaller, final Unmarshaller unmarshaller) {
		this.marshaller = marshaller;
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
		final FileOutputStream os = new FileOutputStream(filepath);
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
		final StringResult ret = new StringResult();
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
	public Object convertFromXMLToObject(final String xmlfile) throws IOException, JAXBException {
		final FileInputStream is = new FileInputStream(xmlfile);
		return unmarshaller.unmarshal(new StreamSource(is));
	}
	
	/**
	 * Convert an InputStream into an object
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 */
	public Object convertFromStreamToObject(final InputStream inputStream) throws IOException, JAXBException {
		return unmarshaller.unmarshal(new StreamSource(inputStream));
	}
}