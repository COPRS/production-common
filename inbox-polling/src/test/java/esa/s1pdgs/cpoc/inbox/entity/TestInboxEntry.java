package esa.s1pdgs.cpoc.inbox.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;

import org.junit.Test;

import esa.s1pdgs.cpoc.inbox.fs.FilesystemInboxEntryFactory;

public class TestInboxEntry {	
	private final FilesystemInboxEntryFactory factory = new FilesystemInboxEntryFactory();

	@Test
	public final void testGetName_OnValidName_ShallReturnName()
	{
		final InboxEntry uut = factory.newInboxEntry("/tmp/fooBar");
		assertEquals("fooBar", uut.getName());		
	}
	
	@Test
	public final void testHashCode_OnSameObject_ShallReturnSameHashCode()
	{
		final InboxEntry uut1 = factory.newInboxEntry("/tmp/fooBar");	
		final InboxEntry uut2 = factory.newInboxEntry("/tmp/fooBar");	
		assertEquals(uut1.hashCode(), uut2.hashCode());
	}
	
	@Test
	public final void testHashCode_OnDifferentObject_ShallReturnDifferentHashCode()
	{
		final InboxEntry uut1 = factory.newInboxEntry("/tmp/foo");	
		final InboxEntry uut2 = factory.newInboxEntry("/tmp/bar");	
		assertNotEquals(uut1.hashCode(), uut2.hashCode());
	}
	
	@Test
	public final void testEquals_OnSameObject_ShallReturnTrue()
	{
		final InboxEntry uut1 = factory.newInboxEntry("/tmp/fooBar");	
		final InboxEntry uut2 = factory.newInboxEntry("/tmp/fooBar");	
		assertEquals(true, uut1.equals(uut2));
	}
	
	@Test
	public final void testEquals_OnNull_ShallReturnFalse()
	{
		final InboxEntry uut = factory.newInboxEntry("/tmp/fooBar");		
		assertEquals(false, uut.equals(null));
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public final void testEquals_OnDifferntClass_ShallReturnFalse()
	{
		final InboxEntry uut = factory.newInboxEntry("/tmp/fooBar");
		assertEquals(false, uut.equals(new File("/tmp/fooBar2")));
	}
}
