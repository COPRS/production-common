package esa.s1pdgs.cpoc.mqi.model.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the DTO object KafkaJobDto
 * 
 * @author Cyrielle Gailliard
 *
 */
public class IpfExecutionJobTest {

	private LevelJobInputDto input1;
	private LevelJobInputDto input2;
	private LevelJobOutputDto output1;
	private LevelJobOutputDto output2;
	private LevelJobOutputDto output3;
	private LevelJobPoolDto pool1;
	private LevelJobPoolDto pool2;

	@Before
	public void init() {
		input1 = new LevelJobInputDto("family1", "local-path1", "content-ref1");
		input2 = new LevelJobInputDto("family2", "local-path2", "content-ref2");
		output1 = new LevelJobOutputDto("family1", "regexp1");
		output2 = new LevelJobOutputDto("family2", "regexp2");
		output3 = new LevelJobOutputDto("family3", "regexp3");
		pool1 = new LevelJobPoolDto();
		pool1.addTask(new LevelJobTaskDto("path1"));
		pool2 = new LevelJobPoolDto();
		pool2.addTask(new LevelJobTaskDto("path2"));
	}

	private void checkDto(final IpfExecutionJob job) {
	    assertEquals(ProductFamily.L0_JOB, job.getProductFamily());
		assertTrue("testEqualsFunction".equals(job.getKeyObjectStorage()));
        assertTrue("NRT".equals(job.getProductProcessMode()));
		assertTrue("/data/localWD/123456".equals(job.getWorkDirectory()));
		assertTrue("/data/localWD/123456/JobOrder.xml".equals(job.getJobOrder()));
		assertTrue(2 == job.getInputs().size());
		assertEquals(job.getInputs().get(1), input2);
		assertTrue(3 == job.getOutputs().size());
		assertEquals(job.getOutputs().get(0), output1);
		assertEquals(job.getOutputs().get(2), output3);
		assertTrue(2 == job.getPools().size());
		assertEquals(job.getPools().get(0), pool1);
	}

	/**
	 * Test the constructors
	 */
	@Test
	public void testConstructorAndGetters() {
		final IpfExecutionJob job = new IpfExecutionJob(ProductFamily.L0_JOB, "testEqualsFunction", "NRT", "/data/localWD/123456", "/data/localWD/123456/JobOrder.xml", new UUID(23L, 42L));
		job.addInput(input1);
		job.addInput(input2);
		job.addOutput(output1);
		job.addOutput(output2);
		job.addOutput(output3);
		job.addPool(pool1);
		job.addPool(pool2);

		checkDto(job);
	}

	/**
	 * Test to string and setters
	 */
	@Test
	public void testToStringAndSetters() {
		final IpfExecutionJob job = new IpfExecutionJob();
		job.setProductFamily(ProductFamily.L0_JOB);
		job.setKeyObjectStorage("testEqualsFunction");
		job.setProductProcessMode("NRT");
		job.setWorkDirectory("/data/localWD/123456");
		job.setJobOrder("/data/localWD/123456/JobOrder.xml");
		job.setInputs(Arrays.asList(input1, input2));
		job.setOutputs(Arrays.asList(output1, output2, output3));
		job.setPools(Arrays.asList(pool1, pool2));

		checkDto(job);

		final String str = job.toString();
        assertTrue(str.contains("L0_JOB"));
		assertTrue(str.contains("testEqualsFunction"));
        assertTrue(str.contains("NRT"));
		assertTrue(str.contains("/data/localWD/123456"));
		assertTrue(str.contains("/data/localWD/123456/JobOrder.xml"));
		assertTrue(str.contains(input2.toString()));
		assertTrue(str.contains(output1.toString()));
		assertTrue(str.contains(pool2.toString()));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(IpfExecutionJob.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
