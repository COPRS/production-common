package esa.s1pdgs.cpoc.production.trigger.taskTableMapping;

import java.util.ArrayList;
import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;

public final class SingleTasktableMapper implements TasktableMapper {	
	private final String tasktableFilename;
	
	public SingleTasktableMapper(final String tasktableFilename) {
		this.tasktableFilename = tasktableFilename;
	}

	@Override
	public List<String> tasktableFor(final AppDataJobProduct job) {
		List<String> taskTableNames = new ArrayList<String>();
		taskTableNames.add(tasktableFilename);
		return taskTableNames;
	}

}
