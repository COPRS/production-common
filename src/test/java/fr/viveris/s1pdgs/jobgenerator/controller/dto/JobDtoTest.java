package fr.viveris.s1pdgs.jobgenerator.controller.dto;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobDto;

/**
 * Test the DTO object KafkaJobDto
 * @author Cyrielle Gailliard
 *
 */
public class JobDtoTest {

	/**
	 * Test the "equals" definition
	 */
	@Test
	public void testEqualsFunction() {
		JobDto dto1 = new JobDto();
		dto1.setProductIdentifier("testEqualsFunction");

		JobDto dto2 = new JobDto();
		dto2.setProductIdentifier("testEqualsFunction");

		assertTrue(String.format("%s shall equal %s", dto1, dto2), dto1.equals(dto2));
		assertTrue(String.format("%s shall equal %s", dto2, dto1), dto2.equals(dto1));
	}

	/**
	 * Test the constructors
	 */
	@Test
	public void testConstructors() {
		JobDto dto1 = new JobDto("testEqualsFunction", "/data/localWD/123456", "/data/localWD/123456/JobOrder.xml");
		assertTrue("testEqualsFunction".equals(dto1.getProductIdentifier()));
		assertTrue("/data/localWD/123456".equals(dto1.getWorkDirectory()));
		assertTrue("/data/localWD/123456/JobOrder.xml".equals(dto1.getJobOrder()));
	}

}
