package fr.viveris.s1pdgs.jobgenerator.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestDateUtils {

	public static Date convertDateIso(String dateStr) throws ParseException {
		return convertDate(dateStr, "yyyyMMdd'T'HHmmss");
	}

	public static Date convertDate(String dateStr, String dateFormat) throws ParseException {
		DateFormat format = new SimpleDateFormat(dateFormat);
		return format.parse(dateStr);
	}
}
