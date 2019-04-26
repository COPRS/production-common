package esa.s1pdgs.cpoc.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;

public class DateUtils {

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
}
