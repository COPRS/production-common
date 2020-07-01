package esa.s1pdgs.cpoc.mqi.server.converter;

import java.io.FileInputStream;
import java.io.IOException;

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
     */
    public XmlConverter(final Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    /**
     * Convert an XML file into an object
     * 
     */
    public Object convertFromXMLToObject(final String xmlfile)
            throws IOException {
        FileInputStream inputS = new FileInputStream(xmlfile);
        return unmarshaller.unmarshal(new StreamSource(inputS));
    }
}