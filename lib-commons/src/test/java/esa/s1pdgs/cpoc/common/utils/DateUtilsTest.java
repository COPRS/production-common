package esa.s1pdgs.cpoc.common.utils;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

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

//	@Test
//	public void testConvert_HandlingOfMicroseconds() throws ParseException {
//
//		final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
//		format.setTimeZone(TimeZone.getTimeZone("UTC"));
//		final Date res = format.parse("2019-01-01T00:00:00.123456Z");
//		System.out.println("Lutz " + format.format(res));
//
//		final DateTimeFormatter uut = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
//		final LocalDateTime parsedDate = LocalDateTime.parse("2019-01-01T00:00:00.123456Z", uut);		
//		System.out.println("Lutz " + uut.format(parsedDate));
//
//    }
    
    

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
