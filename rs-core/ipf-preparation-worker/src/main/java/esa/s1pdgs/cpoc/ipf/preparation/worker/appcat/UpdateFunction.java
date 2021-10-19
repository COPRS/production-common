package esa.s1pdgs.cpoc.ipf.preparation.worker.appcat;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;

@FunctionalInterface
public interface UpdateFunction {
	void applyUpdateOn(AppDataJob job);
}