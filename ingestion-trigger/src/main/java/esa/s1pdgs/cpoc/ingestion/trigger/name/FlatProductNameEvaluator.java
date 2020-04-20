package esa.s1pdgs.cpoc.ingestion.trigger.name;

import java.nio.file.Path;

public class FlatProductNameEvaluator implements ProductNameEvaluator {
	@Override
	public String evaluateFrom(final Path relativePath) {
		final Path filename = relativePath.getFileName();
		if (filename == null) {
			throw new RuntimeException(
					String.format("Filename of %s is null", relativePath)
			);
		}		
		return filename.toString();
	}
}
