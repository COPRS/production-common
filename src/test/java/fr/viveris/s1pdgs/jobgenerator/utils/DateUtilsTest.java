package fr.viveris.s1pdgs.jobgenerator.utils;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import fr.viveris.s1pdgs.jobgenerator.exception.InternalErrorException;

/**
 * 
 */
public class DateUtilsTest {

	@Test
	public void testconvertWithSimpleDateFormat() throws InternalErrorException {
		Date date1 = DateUtils.convertWithSimpleDateFormat("20180125T125633", "yyyyMMdd'T'HHmmss");
		assertTrue(date1.getTime() == 1516881393000L);
	}

	@Test(expected = InternalErrorException.class)
	public void testconvertWithSimpleDateFormatInvalidFormat() throws InternalErrorException {
		DateUtils.convertWithSimpleDateFormat("2018012512633", "yyyyMMdd'T'HHmmss");
	}

	@Test
	public void testconvertIso() throws InternalErrorException {
		Date date1 = DateUtils.convertDateIso("20180125T125633");
		assertTrue(date1.getTime() == 1516881393000L);
	}
}
