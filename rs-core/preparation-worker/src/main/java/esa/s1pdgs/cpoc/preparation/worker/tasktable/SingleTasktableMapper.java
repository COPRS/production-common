package esa.s1pdgs.cpoc.preparation.worker.tasktable;

import java.util.ArrayList;
import java.util.List;

import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

public final class SingleTasktableMapper implements TasktableMapper {	
	private final String tasktableFilename;
	
	public SingleTasktableMapper(final String tasktableFilename) {
		this.tasktableFilename = tasktableFilename;
	}

	@Override
	public List<String> tasktableFor(final CatalogEvent job) {
		final List<String> taskTableNames = new ArrayList<String>();
		taskTableNames.add(tasktableFilename);
		return taskTableNames;
	}

}
