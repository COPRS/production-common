package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.mapper;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;

public final class SingleTasktableMapper implements TasktableMapper {	
	private final String tasktableFilename;
	
	public SingleTasktableMapper(final String tasktableFilename) {
		this.tasktableFilename = tasktableFilename;
	}

	@Override
	public String tasktableFor(final AppDataJob job) {
		return tasktableFilename;
	}

}
