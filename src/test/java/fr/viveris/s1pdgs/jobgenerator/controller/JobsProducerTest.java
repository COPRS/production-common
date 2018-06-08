package fr.viveris.s1pdgs.jobgenerator.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobDto;
import fr.viveris.s1pdgs.jobgenerator.exception.KafkaSendException;

/**
 * Test the class {@link JobsProducer}
 * 
 * @author Cyrielle Gailliard
 *
 */
public class JobsProducerTest {

	/**
	 * Template to publish on Kafka topics
	 */
	@Mock
	private KafkaTemplate<String, JobDto> kafkaJobsTemplate;

	/**
	 * Mock future corresponding to the return of the Kafka send
	 */
	@Mock
	private ListenableFuture<SendResult<String, JobDto>> sendResult;

	/**
	 * Our producer
	 */
	private JobsProducer producer;

	/**
	 * Init function
	 */
	@Before
	public void setUp() {
		// Mockito
		MockitoAnnotations.initMocks(this);
		// Mock sender
		doReturn(sendResult).when(kafkaJobsTemplate).send(Mockito.anyString(), Mockito.any());
		// Build producer
		producer = new JobsProducer(kafkaJobsTemplate, "t-pdgs-test");
	}

	/**
	 * Test nominal send
	 * 
	 * @throws KafkaSendException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	@Test
	public void testSend() throws KafkaSendException, InterruptedException, ExecutionException {
		Mockito.doReturn(null).when(this.sendResult).get();
		JobDto dto = new JobDto("product-id1", "work-directory", "job-order");
		producer.send(dto);
		Mockito.verify(kafkaJobsTemplate, Mockito.times(1)).send(Mockito.eq("t-pdgs-test"), Mockito.eq(dto));
		Mockito.verify(sendResult, Mockito.times(1)).get();
	}

	/**
	 * Test KafkaSendException raised when a CancellationException is raised by the
	 * Kafka template
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testCancelException() throws InterruptedException, ExecutionException {
		Mockito.doThrow(CancellationException.class).when(this.sendResult).get();
		JobDto dto = new JobDto("product-id1", "work-directory", "job-order");
		sendAndCheckKafkaExceptionRaised(dto);
	}

	/**
	 * Test KafkaSendException raised when a InterruptedException is raised by the
	 * Kafka template
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testInterruptedException() throws InterruptedException, ExecutionException {
		Mockito.doThrow(InterruptedException.class).when(this.sendResult).get();
		JobDto dto = new JobDto("product-id2", "work-directory", "job-order");
		sendAndCheckKafkaExceptionRaised(dto);
	}

	/**
	 * Test KafkaSendException raised when a ExecutionException is raised by the
	 * Kafka template
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testExecutionExceptionException() throws InterruptedException, ExecutionException {
		Mockito.doThrow(ExecutionException.class).when(this.sendResult).get();
		JobDto dto = new JobDto("product-id3", "work-directory", "job-order");
		sendAndCheckKafkaExceptionRaised(dto);
	}

	private void sendAndCheckKafkaExceptionRaised(JobDto dto) {
		try {
			producer.send(dto);
			fail("Expected KafkaSendException");
		} catch (KafkaSendException e) {
			assertEquals("Invalid topic for raised exception", "t-pdgs-test", e.getTopic());
			assertEquals("Invalid product name for raised exception", dto.getProductIdentifier(), e.getProductName());
		}
	}
}
