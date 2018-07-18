package esa.s1pdgs.cpoc.mqi.server.converter;

import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

import org.springframework.oxm.Unmarshaller;

/**
 * XML converter
 * 
 * @author Viveris Technologies
 */
public class XmlConverter {

    /**
     * Unmarshaller
     */
    private final Unmarshaller unmarshaller;
    
    /**
     * 
     * @param unmarshaller
     */
    public XmlConverter(final Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    /**
     * Convert an XML file into an object
     * 
     * @param xmlfile
     * @return
     * @throws IOException
     * @throws JAXBException
     */
    public Object convertFromXMLToObject(final String xmlfile)
            throws IOException, JAXBException {
        FileInputStream inputS = new FileInputStream(xmlfile);
        return unmarshaller.unmarshal(new StreamSource(inputS));
    }
}