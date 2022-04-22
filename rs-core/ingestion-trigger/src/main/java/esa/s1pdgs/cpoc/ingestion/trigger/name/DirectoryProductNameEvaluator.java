package esa.s1pdgs.cpoc.ingestion.trigger.name;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public class DirectoryProductNameEvaluator implements ProductNameEvaluator {

	@Override
	public String evaluateFrom(InboxEntry entry) {
		
		if (entry.getRelativePath().indexOf("/") != -1) {
			return entry.getRelativePath().substring(0, entry.getRelativePath().indexOf("/"));
		} else return entry.getRelativePath();
	}

}
