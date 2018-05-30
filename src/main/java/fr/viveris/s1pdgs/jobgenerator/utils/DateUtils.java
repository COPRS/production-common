package fr.viveris.s1pdgs.jobgenerator.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fr.viveris.s1pdgs.jobgenerator.exception.InternalErrorException;

public class DateUtils {

	public static Date convertDateIso(String dateStr) throws InternalErrorException {
		return convertWithSimpleDateFormat(dateStr, "yyyyMMdd'T'HHmmss");
	}

	public static Date convertWithSimpleDateFormat(String dateStr, String dateFormat) throws InternalErrorException {
		try {
			DateFormat format = new SimpleDateFormat(dateFormat);
			return format.parse(dateStr);
		} catch (ParseException pe) {
			throw new InternalErrorException("Cannot convert date " + dateStr, pe);
		}
	}
}
