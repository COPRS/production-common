package esa.s1pdgs.cpoc.ingestion.trigger.name;

import java.nio.file.Path;

public interface ProductNameEvaluator {
	String evaluateFrom(final Path relativePath);
}
