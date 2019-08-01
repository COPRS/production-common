package esa.s1pdgs.cpoc.ingestion.obs;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Test;

public class TestObsAdapter {	
	@Test
	public final void testToObsKey() {
		final ObsAdapter uut = new ObsAdapter(null, Paths.get("/tmp/foo"));		
		assertEquals("bar/baaaaar", uut.toObsKey(new File("/tmp/foo/bar/baaaaar")));
	}
}
