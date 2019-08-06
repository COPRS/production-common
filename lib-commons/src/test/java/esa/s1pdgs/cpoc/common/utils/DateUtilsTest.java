package esa.s1pdgs.cpoc.common.utils;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

public class DateUtilsTest {
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
    
    @Test
    public final void testParse_FullLength() {
    	final LocalDateTime time = DateUtils.parse("2017-12-24T14:22:15.123456Z");    	
    	assertEquals("2017-12-24T14:22:15.123456Z", DateUtils.formatToMetadataDateTimeFormat(time));    	
    }
    
    @Test(expected=IllegalArgumentException.class)
    public final void testParse_FullLengthButInvalidLetter_ShallThrowException() {
    	final LocalDateTime time = DateUtils.parse("2017-12-24T14:22:15.123456X");    	
    	assertEquals("2017-12-24T14:22:15.123456Z", DateUtils.formatToMetadataDateTimeFormat(time));    	
    }
    
    @Test
    public final void testParse_NoTrailingUtcZ() {
    	final LocalDateTime time = DateUtils.parse("2017-12-24T14:22:15.123456");    	
    	assertEquals("2017-12-24T14:22:15.123456Z", DateUtils.formatToMetadataDateTimeFormat(time));    	
    }
    
    @Test
    public final void testParse_UtcPrefix() {
    	final LocalDateTime time = DateUtils.parse("UTC=2017-12-24T14:22:15");    	
    	assertEquals("2017-12-24T14:22:15.000000Z", DateUtils.formatToMetadataDateTimeFormat(time));    	
    }
    
    @Test(expected=IllegalArgumentException.class)
    public final void testParse_UtcPrefixInvalidPrefix_ShallThrowException() {
    	final LocalDateTime time = DateUtils.parse("WTF=2017-12-24T14:22:15");    	
    	assertEquals("2017-12-24T14:22:15.000000Z", DateUtils.formatToMetadataDateTimeFormat(time));    	
    }
    
    @Test
    public final void testParse_Short() {
    	final LocalDateTime time = DateUtils.parse("2017-12-24T14:22:15");    	
    	assertEquals("2017-12-24T14:22:15.000000Z", DateUtils.formatToMetadataDateTimeFormat(time));    	
    }
    
    @Test
    public final void testConvertToMetadataDateTimeFormat()
    {
    	assertEquals("2000-01-01T00:00:00.123456Z", DateUtils.convertToMetadataDateTimeFormat("2000-01-01T00:00:00.123456Z"));
    	assertEquals("2000-01-01T00:00:00.123456Z", DateUtils.convertToMetadataDateTimeFormat("2000-01-01T00:00:00.123456"));
    	assertEquals("2000-01-01T00:00:00.000000Z", DateUtils.convertToMetadataDateTimeFormat("2000-01-01T00:00:00"));
    	assertEquals("2000-01-01T00:00:00.000000Z", DateUtils.convertToMetadataDateTimeFormat("UTC=2000-01-01T00:00:00"));
    }
}
