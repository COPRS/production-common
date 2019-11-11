package esa.s1pdgs.cpoc.archives.controller;

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

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;

public class ReportsConsumerTest {

	/**
     * Acknowledgement
     */
    @Mock
    private Acknowledgment ack;
    
    @Mock
    private AppStatus appStatus;
	
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
		ReportsConsumer consumer = new ReportsConsumer("test/data/reports",appStatus);
		doNothing().when(ack).acknowledge();
		consumer.receive(new LevelReportDto("productName", "content", ProductFamily.L0_REPORT), ack, "topic");
		assertTrue("File exist", resultExists.exists());
		verify(ack, times(1)).acknowledge();

	}
	
	@Test
	public void testReceiveNoDirectory() {
		ReportsConsumer consumer = new ReportsConsumer("test/data/reports",appStatus);
		consumer.receive(new LevelReportDto("productName",  "content", ProductFamily.BLANK), ack, "topic");
		assertFalse("File exist", resultNotExists.exists());
	}
	
	@Test
	public void testReceiveAckException() {
		ReportsConsumer consumer = new ReportsConsumer("test/data/reports",appStatus);
		doThrow(new IllegalArgumentException("error message")).when(ack)
        .acknowledge();
		consumer.receive(new LevelReportDto("productName", "content", ProductFamily.L0_REPORT), ack, "topic");
		verify(ack, times(1)).acknowledge();
	}

}
