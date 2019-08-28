package esa.s1pdgs.cpoc.report;

import static org.junit.Assert.assertEquals;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.junit.Test;

public class TestLoggerReporting {	
	private static final DecimalFormat format= (DecimalFormat) DecimalFormat.getInstance();
	private static final DecimalFormatSymbols symbols=format.getDecimalFormatSymbols();
	private static final char sep=symbols.getDecimalSeparator();

	@Test
	public final void testDuration()
	{
		assertEquals("0" +sep + "999000", LoggerReporting.duration(999));
		assertEquals("1" +sep + "001000", LoggerReporting.duration(1001));
		assertEquals("0" +sep + "001000", LoggerReporting.duration(1));
	}
	
	@Test
	public final void testSize()
	{
		assertEquals("1" +sep + "000", LoggerReporting.size(1048576));
		assertEquals("1" +sep + "001", LoggerReporting.size(1048576 + 1024));
		assertEquals("0" +sep + "001", LoggerReporting.size(1024));
	}
	
	@Test
	public final void testRate()
	{
		assertEquals("4" +sep + "000", LoggerReporting.rate(4*1048576, 1000));
	}
}
