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

package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import esa.s1pdgs.cpoc.metadata.extraction.config.XmlConverterConfig;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.EdrsSessionFile;

@Ignore
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
            fail(String.format("Exception raised: %s", e));
        }
    }
	
}
