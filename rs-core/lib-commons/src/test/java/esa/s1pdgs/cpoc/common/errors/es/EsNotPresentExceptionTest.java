package esa.s1pdgs.cpoc.common.errors.es;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.es.EsNotPresentException;

/**
 * Test the exception EsNotPresentException
 */
public class EsNotPresentExceptionTest {

    /**
     * Test getters and constructors
     */
    @Test
    public void testGettersConstructors() {
        EsNotPresentException exception = new EsNotPresentException();

        assertEquals(ErrorCode.ES_NOT_PRESENT_ERROR, exception.getCode());
    }

    /**
     * Test get log message
     */
    @Test
    public void testLogMessage() {
        EsNotPresentException exception = new EsNotPresentException();

        String log = exception.getLogMessage();
        assertTrue(log.contains("[msg"));
    }

}
