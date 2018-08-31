package esa.s1pdgs.cpoc.jobgenerator.utils;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.jobgenerator.utils.DateUtils;

/**
 * 
 */
public class DateUtilsTest {

	/*@Test
	public void testconvertWithSimpleDateFormat() throws InternalErrorException {
		Date date1 = DateUtils.convertWithSimpleDateFormat("20180125T125633", "yyyyMMdd'T'HHmmss");
		assertTrue("Dates aren't close enough to each other!", Math.abs(date1.getTime() - 1516881393000L) < 1000);
	}*/

	@Test(expected = InternalErrorException.class)
	public void testconvertWithSimpleDateFormatInvalidFormat() throws InternalErrorException {
		DateUtils.convertWithSimpleDateFormat("2018012512633", "yyyyMMdd'T'HHmmss");
	}

	/*@Test
	public void testconvertIso() throws InternalErrorException {
		Date date1 = DateUtils.convertDateIso("20180125T125633");
		assertTrue("Dates aren't close enough to each other!", Math.abs(date1.getTime() - 1516881393000L) < 1000);
	}*/
}
