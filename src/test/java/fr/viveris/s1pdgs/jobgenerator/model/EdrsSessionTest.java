package fr.viveris.s1pdgs.jobgenerator.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the class EDRS session
 * 
 * @author Cyrielle Gailliard
 *
 */
public class EdrsSessionTest {

	/**
	 * Check set channel
	 */
	@Test
	public void testSetChannel() {
		EdrsSession session = new EdrsSession();
		EdrsSessionFile ch1 = new EdrsSessionFile();
		ch1.setSessionId("tutu");
		EdrsSessionFile ch2 = new EdrsSessionFile();
		ch2.setSessionId("toto");

		session.setChannel(ch1, 3);
		assertNull(session.getChannel1());
		assertNull(session.getChannel2());
		
		session.setChannel(ch1, 1);
		assertEquals(ch1, session.getChannel1());
		assertNull(session.getChannel2());
		
		session.setChannel(ch2, 2);
		assertEquals(ch1, session.getChannel1());
		assertEquals(ch2, session.getChannel2());
	}

	/**
	 * Check toString
	 */
	@Test
	public void testToString() {
		EdrsSession session = new EdrsSession();
		EdrsSessionFile ch1 = new EdrsSessionFile();
		ch1.setSessionId("tutu");
		EdrsSessionFile ch2 = new EdrsSessionFile();
		ch2.setSessionId("toto");
		session.setChannel1(ch1);
		session.setChannel2(ch2);

		String str = session.toString();
		assertTrue(str.contains("channel1: " + ch1.toString()));
		assertTrue(str.contains("channel2: " + ch2.toString()));
		assertTrue(str.contains("lastTsMsg:"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(EdrsSession.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
