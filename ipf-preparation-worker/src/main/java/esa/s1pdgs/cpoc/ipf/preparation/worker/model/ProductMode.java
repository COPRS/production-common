package esa.s1pdgs.cpoc.ipf.preparation.worker.model;

import java.util.Objects;

import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums.TaskTableInputMode;

/**
 * Product mode (available in task table)
 * @author Cyrielle Gailliard
 *
 */
// TODO clarify if ProcessingMode is more meaningful as my understanding is:
// there are several mode a processor can run NRT, SYSTEMATIC etc.
// when input selection is done, only inputs matching this mode are selected
public enum ProductMode {
	ALWAYS, SLICING, NON_SLICING, BLANK;

	public boolean isCompatibleWithTaskTableMode(final TaskTableInputMode tMode) {
		return isCompatibleWithTaskTableMode(this, tMode);
	}

	/**
	 * Check if the mode in task table in compatible with the product mode
	 */
	public static boolean isCompatibleWithTaskTableMode(final ProductMode pMode, final TaskTableInputMode tMode) {
		if (pMode == null || tMode == null) {
			return false;
		}

		if (pMode.equals(ProductMode.ALWAYS) || tMode.equals(TaskTableInputMode.ALWAYS)) {
			return true;
		}

		return Objects.equals(pMode.name(), tMode.name());
	}
}
