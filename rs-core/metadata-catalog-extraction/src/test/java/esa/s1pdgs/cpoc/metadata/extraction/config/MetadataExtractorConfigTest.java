package esa.s1pdgs.cpoc.metadata.extraction.config;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.metadata.extraction.config.MetadataExtractorConfig;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class MetadataExtractorConfigTest {

    @Autowired
    private MetadataExtractorConfig extractorConfig;

    @Test
    public void testSettings() {
        assertEquals(
                "config/xsltDir/",
                extractorConfig.getXsltDirectory());
    }
}
