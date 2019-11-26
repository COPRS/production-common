package esa.s1pdgs.cpoc.mdc.worker.extraction.xml;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import esa.s1pdgs.cpoc.mdc.worker.extraction.model.EdrsSessionFile;
import esa.s1pdgs.cpoc.mdc.worker.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.mdc.worker.extraction.xml.XmlConverterConfig;

public class XmlConverterTest {
	
	/**
     * Spring annotation context
     */
    private AnnotationConfigApplicationContext ctx;

    /**
     * XML converter
     */
    private XmlConverter xmlConverter;

    /**
     * Initialize the spring context
     */
    @Before
    public void init() {
        ctx = new AnnotationConfigApplicationContext();
        ctx.register(XmlConverterConfig.class);
        ctx.refresh();
        xmlConverter = ctx.getBean(XmlConverter.class);

    }

    /**
     * Close the spring context
     */
    @After
    public void close() {
        ctx.close();
    }
    
	/**
     * Test the conversion XML file => EDRS session object
     */
    @Test
    public void testUnmarshalingEdrsSessionFiles() {
        try {
            EdrsSessionFile fileChannel1 =
                    (EdrsSessionFile) xmlConverter.convertFromXMLToObject(
                            "./src/test/resources/data/DCS_02_L20171109175634707000125_ch1_DSIB.xml");
            assertEquals("Session identifiers not equaled",
                    "L20171109175634707000125", fileChannel1.getSessionId());
            assertEquals("Start times not equaled", "2017-12-13T14:59:48Z",
                    fileChannel1.getStartTime());
            assertEquals("End times not equaled", "2017-12-13T15:17:25Z",
                    fileChannel1.getStopTime());
            assertEquals("Invalid number of raws", 35,
                    fileChannel1.getRawNames().size());
        } catch (IOException | JAXBException e) {
            fail("Exception raised", e);
        }
    }
	
}
