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
    public final void testParse_OdataReturnValues() {
    	LocalDateTime time = DateUtils.parse("2017-12-24T14:22:15.12345Z");    	
    	assertEquals("2017-12-24T14:22:15.123450Z", DateUtils.formatToMetadataDateTimeFormat(time));  
    	time = DateUtils.parse("2017-12-24T14:22:15.1234Z");    	
    	assertEquals("2017-12-24T14:22:15.123400Z", DateUtils.formatToMetadataDateTimeFormat(time));  
    	time = DateUtils.parse("2017-12-24T14:22:15.12Z");    	
    	assertEquals("2017-12-24T14:22:15.120000Z", DateUtils.formatToMetadataDateTimeFormat(time));  
    	time = DateUtils.parse("2017-12-24T14:22:15.1Z");    	
    	assertEquals("2017-12-24T14:22:15.100000Z", DateUtils.formatToMetadataDateTimeFormat(time));  
    }
    
    @Test
    public final void testConvertToMetadataDateTimeFormat()
    {
    	assertEquals("2000-01-01T00:00:00.123456Z", DateUtils.convertToMetadataDateTimeFormat("2000-01-01T00:00:00.123456Z"));
    	assertEquals("2000-01-01T00:00:00.123456Z", DateUtils.convertToMetadataDateTimeFormat("2000-01-01T00:00:00.123456"));
    	assertEquals("2000-01-01T00:00:00.000000Z", DateUtils.convertToMetadataDateTimeFormat("2000-01-01T00:00:00"));
    	assertEquals("2000-01-01T00:00:00.000000Z", DateUtils.convertToMetadataDateTimeFormat("UTC=2000-01-01T00:00:00"));
    	
    	assertEquals("2020-09-10T23:37:12.123573Z", DateUtils.convertToMetadataDateTimeFormat("UTC=2020-09-10T23:37:12.123573"));
    }
}
