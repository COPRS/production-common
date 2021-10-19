package esa.s1pdgs.cpoc.ingestion.trigger.name;

import java.nio.file.Path;
import java.nio.file.Paths;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public class FlatProductNameEvaluator implements ProductNameEvaluator {
	@Override
	public String evaluateFrom(InboxEntry entry) {
		Path relativePath = Paths.get(entry.getRelativePath());
		final Path filename = relativePath.getFileName();
		if (filename == null) {
			throw new RuntimeException(
					String.format("Filename of %s is null", relativePath)
			);
		}		
		return filename.toString();
	}
}
