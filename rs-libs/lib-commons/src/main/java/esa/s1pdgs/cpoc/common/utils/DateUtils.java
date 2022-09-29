package esa.s1pdgs.cpoc.common.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public class DateUtils {
	public final static String METADATA_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
	public final static DateTimeFormatter METADATA_DATE_FORMATTER = DateTimeFormatter.ofPattern(METADATA_DATE_FORMAT);
	public final static String ODATA_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	public final static DateTimeFormatter ODATA_DATE_FORMATTER = DateTimeFormatter.ofPattern(ODATA_DATE_FORMAT);
	public final static String FILENAME_DATE_FORMAT = "yyyyMMdd'T'HHmmss";
	public final static DateTimeFormatter FILENAME_DATE_FORMATTER = DateTimeFormatter.ofPattern(FILENAME_DATE_FORMAT);
	public final static String PDU_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS";
	public final static DateTimeFormatter PDU_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
	
	private static final Map<Predicate<String>,DateTimeFormatter> FORMATS = new LinkedHashMap<>();	
	static {
		FORMATS.put(s -> s.length() == 27 && s.endsWith("Z"), METADATA_DATE_FORMATTER);
		FORMATS.put(s -> s.length() == 26, PDU_DATE_FORMATTER);
		FORMATS.put(s -> s.length() == 24 && s.endsWith("Z"), ODATA_DATE_FORMATTER);
		FORMATS.put(s -> s.length() == 23 && s.startsWith("UTC="), DateTimeFormatter.ofPattern("'UTC='yyyy-MM-dd'T'HH:mm:ss"));
		FORMATS.put(s -> s.length() == 30 && s.startsWith("UTC="), DateTimeFormatter.ofPattern("'UTC='yyyy-MM-dd'T'HH:mm:ss.SSSSSS"));
		FORMATS.put(s -> s.length() == 19, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
		FORMATS.put(s -> s.length() == 15, FILENAME_DATE_FORMATTER);
	}

	public static String convertToAnotherFormat(
    		final String dateStr,
            final DateTimeFormatter inFormatter, 
            final DateTimeFormatter outFormatter
    ) {
        final LocalDateTime dateToConvert = LocalDateTime.parse(dateStr, inFormatter);
        return dateToConvert.format(outFormatter);
    }
    
    public static final LocalDateTime parse(final String dateString) {
	    return LocalDateTime.parse(dateString, formatterFor(dateString));
    }
    
    public static final String formatToMetadataDateTimeFormat(final LocalDateTime _dateTime) {
    	return _dateTime.format(METADATA_DATE_FORMATTER);
    }
    
    public static String convertToMetadataDateTimeFormat(final String dateString) {
    	return formatToMetadataDateTimeFormat(parse(dateString));
    }
    
    public static final String formatToOdataDateTimeFormat(final LocalDateTime _dateTime) {
    	return _dateTime.format(ODATA_DATE_FORMATTER);
    }
    
    public static final String formatToPDUDateTimeFormat(final LocalDateTime _dateTime) {
    	return _dateTime.format(PDU_DATE_FORMATTER);
    }
    
    public static String convertToPDUDateTimeFormat(final String dateString) {
    	return formatToPDUDateTimeFormat(parse(dateString));
    }

    private static final DateTimeFormatter formatterFor(final String dateString) 
    		throws IllegalArgumentException {
    	// find a fitting formatter for the provided dateString or fail, if no fitting formatter is 
    	// available
    	for (final Map.Entry<Predicate<String>,DateTimeFormatter> entry : FORMATS.entrySet()) {
    		if (entry.getKey().test(dateString)) {
    			return entry.getValue();
    		}    		
    	}
    	throw new IllegalArgumentException(
    			String.format("Invalid date string '%s'. Available formats are: %s", dateString, FORMATS.values())
    	);
    }
    
	public static final Instant toInstant(final String dateString) {
		return DateUtils.parse(dateString)
				.atZone(ZoneId.systemDefault())
				.toInstant();
	}
	
	public static final Date toDate(final String dateString) {
		if (dateString == null) {
			return null;
		}		
		return Date.from(toInstant(dateString));
	}
}
