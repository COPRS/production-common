package esa.s1pdgs.cpoc.jobgenerator.model;

import java.util.Objects;

import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.enums.TaskTableInputMode;

/**
 * Product mode (available in task table)
 * @author Cyrielle Gailliard
 *
 */
public enum ProductMode {
	ALWAYS, SLICING, NON_SLICING, BLANK;

	/**
	 * Check if the mode in task table in compatible with the product mode
	 * @param m
	 * @param i
	 * @return
	 */
	public static boolean isCompatibleWithTaskTableMode(final ProductMode pMode, final TaskTableInputMode tMode) {
		boolean ret;
		if (pMode == null || tMode == null) {
			ret = false;
		} else if (pMode == ProductMode.ALWAYS) {
			ret = true;
		} else if (tMode == TaskTableInputMode.ALWAYS) {
			ret = true;
		} else if (Objects.equals(pMode.name(), tMode.name())) {
			ret = true;
		} else {
			ret = false;
		}
		return ret;
	}
}
