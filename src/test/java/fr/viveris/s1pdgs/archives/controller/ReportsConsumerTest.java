package fr.viveris.s1pdgs.archives.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.viveris.s1pdgs.archives.controller.dto.ReportDto;

public class ReportsConsumerTest {

	private File resultExists;
	private File resultNotExists;
	
	@Before
	public void init() {
		resultExists = new File("test/data/reports/l0_report/productName");
		resultNotExists = new File("test/data/reports/l0_reports/productName");
	}
	
	@After
	public void clean() {
		if(resultExists.exists()) {
			resultExists.delete();
		}
		if(resultNotExists.exists()) {
			resultNotExists.delete();
		}
	}
	
	@Test
	public void testReceive() {
		ReportsConsumer consumer = new ReportsConsumer("test/data/reports");
		consumer.receive(new ReportDto("productName",  "content", "L0_REPORT"));
		assertTrue("File exist", resultExists.exists());

	}
	
	@Test
	public void testReceiveNoDirectory() {
		ReportsConsumer consumer = new ReportsConsumer("test/data/reports");
		consumer.receive(new ReportDto("productName",  "content", "L0_REPORTS"));
		assertFalse("File exist", resultNotExists.exists());
	}

}
