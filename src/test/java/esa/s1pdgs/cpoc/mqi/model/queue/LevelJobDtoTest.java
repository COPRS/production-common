package esa.s1pdgs.cpoc.mqi.model.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobPoolDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobTaskDto;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the DTO object KafkaJobDto
 * 
 * @author Cyrielle Gailliard
 *
 */
public class LevelJobDtoTest {

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

	private void checkDto(LevelJobDto dto1) {
	    assertEquals(ProductFamily.L0_JOB, dto1.getFamily());
		assertTrue("testEqualsFunction".equals(dto1.getProductIdentifier()));
		assertTrue("/data/localWD/123456".equals(dto1.getWorkDirectory()));
		assertTrue("/data/localWD/123456/JobOrder.xml".equals(dto1.getJobOrder()));
		assertTrue(2 == dto1.getInputs().size());
		assertEquals(dto1.getInputs().get(1), input2);
		assertTrue(3 == dto1.getOutputs().size());
		assertEquals(dto1.getOutputs().get(0), output1);
		assertEquals(dto1.getOutputs().get(2), output3);
		assertTrue(2 == dto1.getPools().size());
		assertEquals(dto1.getPools().get(0), pool1);
	}

	/**
	 * Test the constructors
	 */
	@Test
	public void testConstructorAndGetters() {
		LevelJobDto dto1 = new LevelJobDto(ProductFamily.L0_JOB, "testEqualsFunction", "/data/localWD/123456", "/data/localWD/123456/JobOrder.xml");
		dto1.addInput(input1);
		dto1.addInput(input2);
		dto1.addOutput(output1);
		dto1.addOutput(output2);
		dto1.addOutput(output3);
		dto1.addPool(pool1);
		dto1.addPool(pool2);

		checkDto(dto1);
	}

	/**
	 * Test to string and setters
	 */
	@Test
	public void testToStringAndSetters() {
		LevelJobDto dto1 = new LevelJobDto();
		dto1.setFamily(ProductFamily.L0_JOB);
		dto1.setProductIdentifier("testEqualsFunction");
		dto1.setWorkDirectory("/data/localWD/123456");
		dto1.setJobOrder("/data/localWD/123456/JobOrder.xml");
		dto1.setInputs(Arrays.asList(input1, input2));
		dto1.setOutputs(Arrays.asList(output1, output2, output3));
		dto1.setPools(Arrays.asList(pool1, pool2));

		checkDto(dto1);

		String str = dto1.toString();
        assertTrue(str.contains("family: L0_JOB"));
		assertTrue(str.contains("productIdentifier: testEqualsFunction"));
		assertTrue(str.contains("workDirectory: /data/localWD/123456"));
		assertTrue(str.contains("jobOrder: /data/localWD/123456/JobOrder.xml"));
		assertTrue(str.contains("inputs:"));
		assertTrue(str.contains(input2.toString()));
		assertTrue(str.contains("outputs:"));
		assertTrue(str.contains(output1.toString()));
		assertTrue(str.contains("pools:"));
		assertTrue(str.contains(pool2.toString()));

	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(LevelJobDto.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
