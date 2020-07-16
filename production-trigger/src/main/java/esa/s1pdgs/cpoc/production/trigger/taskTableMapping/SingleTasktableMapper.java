package esa.s1pdgs.cpoc.production.trigger.taskTableMapping;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;

public final class SingleTasktableMapper implements TasktableMapper {	
	private final String tasktableFilename;
	
	public SingleTasktableMapper(final String tasktableFilename) {
		this.tasktableFilename = tasktableFilename;
	}

	@Override
	public String tasktableFor(final AppDataJobProduct job) {
		return tasktableFilename;
	}

}
