package esa.s1pdgs.cpoc.ingestion.trigger.name;

import java.nio.file.Path;

public class SessionProductNameEvaluator implements ProductNameEvaluator {
	
	public SessionProductNameEvaluator() {
	}

	@Override
	public String evaluateFrom(final Path relativePath) {
		return relativePath.toString();
	}

}
