package fr.viveris.s1pdgs.scaler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.viveris.s1pdgs.scaler.AbstractCodedException.ErrorCode;

public class AbstractCodedExceptionTest {

    @Test
    public void testEnumErrorCode() {
        assertEquals(ErrorCode.KAFKA_PAUSING_ERROR, ErrorCode.valueOf("KAFKA_PAUSING_ERROR"));
        assertEquals(ErrorCode.K8S_UNKNOWN_RESOURCE, ErrorCode.valueOf("K8S_UNKNOWN_RESOURCE"));
        assertEquals(15, ErrorCode.values().length);
    }
}
