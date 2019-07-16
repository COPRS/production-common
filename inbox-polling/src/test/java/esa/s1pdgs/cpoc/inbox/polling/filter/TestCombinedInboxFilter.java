package esa.s1pdgs.cpoc.inbox.polling.filter;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class TestCombinedInboxFilter {
	
	private final FakeInboxEntry entry = new FakeInboxEntry("fakeEntry");
	
	@Test
	public final void testAccept_OnAllAccept_ShallReturnTrue() {
		final CombinedInboxFilter uut = new CombinedInboxFilter(Arrays.asList(InboxFilter.ALLOW_ALL,InboxFilter.ALLOW_ALL));
		assertEquals(true, uut.accept(entry));
	}
	
	@Test
	public final void testAccept_OnSingleNonAccept_ShallReturnFalse() {
		final CombinedInboxFilter uut = new CombinedInboxFilter(Arrays.asList(InboxFilter.ALLOW_ALL,InboxFilter.ALLOW_NONE));
		assertEquals(false, uut.accept(entry));
	}

}
