package esa.s1pdgs.cpoc.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;

public class DateUtils {
	
	private final static DateTimeFormatter METADATA_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
	private final static DateTimeFormatter DATE_FORMATTER_26 =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    private final static DateTimeFormatter DATE_FORMATTER_SHORT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	private final static DateTimeFormatter DATE_FORMATTER_UTC_PREFIXED_SHORT =
            DateTimeFormatter.ofPattern("'UTC='yyyy-MM-dd'T'HH:mm:ss");
	
    /**
     * Convert a date in ISO format "yyyyMMdd'T'HHmmss"
     * 
     * @param dateStr
     * @return
     * @throws InternalErrorException
     */
    public static Date convertDateIso(final String dateStr)
            throws InternalErrorException {
        return convertWithSimpleDateFormat(dateStr, "yyyyMMdd'T'HHmmss");
    }

    /**
     * Convert a date from given format
     * 
     * @param dateStr
     * @param dateFormat
     * @return
     * @throws InternalErrorException
     */
    public static Date convertWithSimpleDateFormat(final String dateStr,
            final String dateFormat) throws InternalErrorException {
        try {
            DateFormat format = new SimpleDateFormat(dateFormat);
            return format.parse(dateStr);
        } catch (ParseException pe) {
            throw new InternalErrorException("Cannot convert date " + dateStr,
                    pe);
        }
    }

    /**
     * Convert a date
     * 
     * @param dateStr
     * @param inFormatter
     * @param outFormatter
     * @return
     */
    public static String convertToAnotherFormat(String dateStr,
            DateTimeFormatter inFormatter, DateTimeFormatter outFormatter) {
        LocalDateTime dateToConvert = LocalDateTime.parse(dateStr, inFormatter);
        return dateToConvert.format(outFormatter);
    }
    
    public static String convertToMetadataDateTimeFormat(String datetime) {
        if (datetime.length() == 27 && datetime.endsWith("Z")) {
            return convertToAnotherFormat(datetime, METADATA_DATE_FORMATTER, METADATA_DATE_FORMATTER); // "convert" to validate
        } else if (datetime.length() == 26) {
            return convertToAnotherFormat(datetime, DATE_FORMATTER_26, METADATA_DATE_FORMATTER);
        } else if (datetime.length() == 23 && datetime.startsWith("UTC=")) {
        	return convertToAnotherFormat(datetime, DATE_FORMATTER_UTC_PREFIXED_SHORT, METADATA_DATE_FORMATTER);
        } else {
            return convertToAnotherFormat(datetime, DATE_FORMATTER_SHORT, METADATA_DATE_FORMATTER);
        }
    }
}
