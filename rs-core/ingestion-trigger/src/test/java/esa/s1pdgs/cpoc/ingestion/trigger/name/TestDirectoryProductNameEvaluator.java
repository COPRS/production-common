package esa.s1pdgs.cpoc.ingestion.trigger.name;

import org.junit.Assert;
import org.junit.Test;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public class TestDirectoryProductNameEvaluator {
	
	
	
	@Test
	public void testEvaluateFrom() {
		
		DirectoryProductNameEvaluator uut = new DirectoryProductNameEvaluator();
		
		InboxEntry entry = new InboxEntry();
		entry.setRelativePath("main/test.txt");
		String result = uut.evaluateFrom(entry);
		Assert.assertEquals("main", result);
		
		
		InboxEntry entry2 = new InboxEntry();
		entry2.setRelativePath("main");
		String result2 = uut.evaluateFrom(entry2);
		Assert.assertEquals("main", result2);
	}

}
