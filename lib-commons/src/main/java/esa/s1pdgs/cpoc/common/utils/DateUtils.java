package esa.s1pdgs.cpoc.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class DateUtils {
	public final static String METADATA_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
	public final static DateTimeFormatter METADATA_DATE_FORMATTER = DateTimeFormatter.ofPattern(METADATA_DATE_FORMAT);
	
	private static final Map<Predicate<String>,DateTimeFormatter> FORMATS = new HashMap<>();	
	static {
		FORMATS.put(s -> s.length() == 27 && s.endsWith("Z"), METADATA_DATE_FORMATTER);
		FORMATS.put(s -> s.length() == 26, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"));
		FORMATS.put(s -> s.length() == 23 && s.startsWith("UTC="), DateTimeFormatter.ofPattern("'UTC='yyyy-MM-dd'T'HH:mm:ss"));
		FORMATS.put(s -> s.length() == 19, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
	}
	
    /**
     * Convert a date
     * 
     * @param dateStr
     * @param inFormatter
     * @param outFormatter
     * @return
     */
    public static String convertToAnotherFormat(
    		String dateStr,
            DateTimeFormatter inFormatter, 
            DateTimeFormatter outFormatter
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
    
    public static String convertToMetadataDateTimeFormat(String dateString) {
    	return formatToMetadataDateTimeFormat(parse(dateString));
    }
    
    private static final DateTimeFormatter formatterFor(final String dateString)
    {
    	for (final Map.Entry<Predicate<String>,DateTimeFormatter> entry : FORMATS.entrySet())
    	{
    		if (entry.getKey().test(dateString))
    		{
    			return entry.getValue();
    		}    		
    	}
    	throw new IllegalArgumentException(
    			String.format("Invalid date string '%s'. Available formats are: %s", dateString, FORMATS.values())
    	);
    }
}
