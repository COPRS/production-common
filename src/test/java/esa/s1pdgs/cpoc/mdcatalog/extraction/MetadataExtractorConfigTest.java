package esa.s1pdgs.cpoc.mdcatalog.extraction;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class MetadataExtractorConfigTest {

    @Autowired
    private MetadataExtractorConfig extractorConfig;

    @Test
    public void testSettings() {
        assertEquals("Type Overlap string equals",
                "EW:8.2F||IW:7.4F||SM:7.7F||WM:0.0F",
                extractorConfig.getTypeoverlapstr());
        assertEquals("Type Slice length string equals",
                "EW:60.0F||IW:25.0F||SM:25.0F||WM:0.0F",
                extractorConfig.getTypeslicelengthstr());
        assertEquals("Metadata extractor config objects equals",
                "MetadataExtractorConfig [typeoverlapstr=EW:8.2F||IW:7.4F||SM:7.7F||WM:0.0F, typeOverlap={EW=8.2, WM=0.0, SM=7.7, IW=7.4}, typeslicelengthstr=EW:60.0F||IW:25.0F||SM:25.0F||WM:0.0F, typeSliceLength={EW=60.0, WM=0.0, SM=25.0, IW=25.0}]",
                extractorConfig.toString());
    }
}
