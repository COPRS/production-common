package esa.s1pdgs.cpoc.prip.worker.service.metadata;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestPripPublishingJobListener {

	@Test
	public final void testRemoveZipSuffix() {
		assertEquals("foo.bar", PripPublishingJobListener.removeZipSuffix("foo.bar.Zip"));
		assertEquals("foo.bar", PripPublishingJobListener.removeZipSuffix("foo.bar.ZIP"));
		assertEquals("foo.bar", PripPublishingJobListener.removeZipSuffix("foo.bar.zip"));
		assertEquals("foo.bar", PripPublishingJobListener.removeZipSuffix("foo.bar"));
	}
}
