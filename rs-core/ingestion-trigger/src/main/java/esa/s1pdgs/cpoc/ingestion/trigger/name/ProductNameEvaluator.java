package esa.s1pdgs.cpoc.ingestion.trigger.name;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public interface ProductNameEvaluator {
	String evaluateFrom(final InboxEntry relativePath);
}
