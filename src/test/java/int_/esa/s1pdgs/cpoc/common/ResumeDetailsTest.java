package int_.esa.s1pdgs.cpoc.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import int_.esa.s1pdgs.cpoc.common.ResumeDetails;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object ResumeDetails
 * 
 * @author Cyrielle Gailliard
 *
 */
public class ResumeDetailsTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {
		ResumeDetails obj = new ResumeDetails("topic-name", "dto-object");
		assertEquals("topic-name", obj.getTopicName());
		assertEquals("dto-object", obj.getDto());
	}
	
	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		ResumeDetails obj = new ResumeDetails("topic-name", "dto-object");
		String str = obj.toString();
		assertTrue(str.contains("topicName: topic-name"));
		assertTrue(str.contains("dto: dto-object"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(ResumeDetails.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
