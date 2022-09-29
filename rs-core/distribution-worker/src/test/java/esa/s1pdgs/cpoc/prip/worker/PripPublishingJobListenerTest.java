package esa.s1pdgs.cpoc.prip.worker;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PripPublishingJobListenerTest {

	@Test
    public void test() {
        final boolean isLineString = "S1A_WV_RAW__0NSV_20200120T114913_20200120T121043_030883_038B58_E294.SAFE"
                .matches("(RF|WV)_RAW__0(A|C|N|S)");
        System.out.println("isLineString: " + isLineString);
        assertFalse(isLineString);
    }
	
	@Test
    public void test2() {
        final boolean isLineString = "S1A_WV_RAW__0NSV_20200120T114913_20200120T121043_030883_038B58_E294.SAFE"
                .matches("S1.*(RF|WV)_RAW__0(A|C|N|S).*");
        System.out.println("isLineString: " + isLineString);
        assertTrue(isLineString);
    }

}
