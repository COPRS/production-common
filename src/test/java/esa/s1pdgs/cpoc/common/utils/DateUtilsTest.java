package esa.s1pdgs.cpoc.common.utils;

import static org.junit.Assert.assertEquals;

import java.time.format.DateTimeFormatter;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;

public class DateUtilsTest {

    /*
     * @Test public void testconvertWithSimpleDateFormat() throws
     * InternalErrorException { Date date1 =
     * DateUtils.convertWithSimpleDateFormat("20180125T125633",
     * "yyyyMMdd'T'HHmmss");
     * assertTrue("Dates aren't close enough to each other!",
     * Math.abs(date1.getTime() - 1516881393000L) < 1000); }
     */

    @Test(expected = InternalErrorException.class)
    public void testconvertWithSimpleDateFormatInvalidFormat()
            throws InternalErrorException {
        DateUtils.convertWithSimpleDateFormat("2018012512633",
                "yyyyMMdd'T'HHmmss");
    }

    /*
     * @Test public void testconvertIso() throws InternalErrorException { Date
     * date1 = DateUtils.convertDateIso("20180125T125633");
     * assertTrue("Dates aren't close enough to each other!",
     * Math.abs(date1.getTime() - 1516881393000L) < 1000); }
     */

    @Test
    public void testConvertToAnotherFormat() {
        assertEquals("2017-12-24T14:22:15.124578Z",
                DateUtils.convertToAnotherFormat("20171224_142215_124578",
                        DateTimeFormatter
                                .ofPattern("yyyyMMdd_HHmmss_SSSSSS"),
                        DateTimeFormatter
                                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")));
        assertEquals("2017-12-24T14:22:15.000000Z",
                DateUtils.convertToAnotherFormat("2017-12-24T14:22:15",
                        DateTimeFormatter
                                .ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                        DateTimeFormatter
                                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")));
    }
}
