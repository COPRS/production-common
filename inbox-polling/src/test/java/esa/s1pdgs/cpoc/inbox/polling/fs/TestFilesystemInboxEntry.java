package esa.s1pdgs.cpoc.inbox.polling.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;

import org.junit.Test;

import esa.s1pdgs.cpoc.inbox.polling.fs.FilesystemInboxEntry;

public class TestFilesystemInboxEntry {

	@Test
	public final void testGetName_OnValidName_ShallReturnName()
	{
		final FilesystemInboxEntry uut = new FilesystemInboxEntry(new File("/tmp/fooBar"));
		assertEquals("fooBar", uut.getName());		
	}
	
	@Test
	public final void testHashCode_OnSameObject_ShallReturnSameHashCode()
	{
		final FilesystemInboxEntry uut1 = new FilesystemInboxEntry(new File("/tmp/fooBar"));	
		final FilesystemInboxEntry uut2 = new FilesystemInboxEntry(new File("/tmp/fooBar"));	
		assertEquals(uut1.hashCode(), uut2.hashCode());
	}
	
	@Test
	public final void testHashCode_OnDifferentObject_ShallReturnDifferentHashCode()
	{
		final FilesystemInboxEntry uut1 = new FilesystemInboxEntry(new File("/tmp/foo"));	
		final FilesystemInboxEntry uut2 = new FilesystemInboxEntry(new File("/tmp/bar"));	
		assertNotEquals(uut1.hashCode(), uut2.hashCode());
	}
	
	@Test
	public final void testEquals_OnSameObject_ShallReturnTrue()
	{
		final FilesystemInboxEntry uut1 = new FilesystemInboxEntry(new File("/tmp/fooBar"));	
		final FilesystemInboxEntry uut2 = new FilesystemInboxEntry(new File("/tmp/fooBar"));	
		assertEquals(true, uut1.equals(uut2));
	}
	
	@Test
	public final void testEquals_OnNull_ShallReturnFalse()
	{
		final FilesystemInboxEntry uut = new FilesystemInboxEntry(new File("/tmp/fooBar"));	
		assertEquals(false, uut.equals(null));
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public final void testEquals_OnDifferntClass_ShallReturnFalse()
	{
		final FilesystemInboxEntry uut = new FilesystemInboxEntry(new File("/tmp/fooBar"));	
		assertEquals(false, uut.equals(new File("/tmp/fooBar2")));
	}
}
