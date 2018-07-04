package fr.viveris.s1pdgs.archives.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.Acknowledgment;

import fr.viveris.s1pdgs.archives.controller.dto.ReportDto;
import fr.viveris.s1pdgs.archives.model.ProductFamily;

public class ReportsConsumerTest {

	/**
     * Acknowledgement
     */
    @Mock
    private Acknowledgment ack;
	
	private File resultExists;
	private File resultNotExists;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
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
		doNothing().when(ack).acknowledge();
		consumer.receive(new ReportDto("productName", "content", ProductFamily.L0_REPORT), ack, "topic");
		assertTrue("File exist", resultExists.exists());
		verify(ack, times(1)).acknowledge();

	}
	
	@Test
	public void testReceiveNoDirectory() {
		ReportsConsumer consumer = new ReportsConsumer("test/data/reports");
		consumer.receive(new ReportDto("productName",  "content", ProductFamily.BLANK), ack, "topic");
		assertFalse("File exist", resultNotExists.exists());
	}
	
	@Test
	public void testReceiveAckException() {
		ReportsConsumer consumer = new ReportsConsumer("test/data/reports");
		doThrow(new IllegalArgumentException("error message")).when(ack)
        .acknowledge();
		consumer.receive(new ReportDto("productName", "content", ProductFamily.L0_REPORT), ack, "topic");
		verify(ack, times(1)).acknowledge();
	}

}
