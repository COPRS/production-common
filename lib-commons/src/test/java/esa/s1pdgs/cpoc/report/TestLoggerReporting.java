package esa.s1pdgs.cpoc.report;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestLoggerReporting {

	@Test
	public final void testDuration()
	{
		assertEquals("0.999 s", LoggerReporting.duration(999));
		assertEquals("1.001 s", LoggerReporting.duration(1001));
		assertEquals("0.001 s", LoggerReporting.duration(1));
	}
	
	@Test
	public final void testSize()
	{
		assertEquals("1.000 MiB", LoggerReporting.size(1048576));
		assertEquals("1.001 MiB", LoggerReporting.size(1048576 + 1024));
		assertEquals("0.001 MiB", LoggerReporting.size(1024));
	}
	
	@Test
	public final void testRate()
	{
		assertEquals("4.000 MiB/s", LoggerReporting.rate(4*1048576, 1000));
	}
}
