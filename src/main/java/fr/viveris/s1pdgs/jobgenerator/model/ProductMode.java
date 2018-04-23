package fr.viveris.s1pdgs.jobgenerator.model;

import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableInputMode;

public enum ProductMode {
	ALWAYS, SLICING, NON_SLICING, BLANK;

	public static boolean isCompatibleWithTaskTableMode(ProductMode m, TaskTableInputMode i) {
		if (m == ProductMode.ALWAYS) {
			return true;
		} else if (i == TaskTableInputMode.ALWAYS) {
			return true;
		} else if (m.name().equals(i.name())) {
			return true;
		}
		return false;
	}

	public static ProductMode convertTaskTableMode(TaskTableInputMode i) {
		switch (i) {
		case ALWAYS:
			return ProductMode.ALWAYS;
		case SLICING:
			return ProductMode.SLICING;
		case NON_SLICING:
			return ProductMode.NON_SLICING;
		default:
			return ProductMode.BLANK;
		}
	}

	public static boolean isCompatible(ProductMode m, ProductMode i) {
		if (m == ProductMode.ALWAYS) {
			return true;
		} else if (i == ProductMode.ALWAYS) {
			return true;
		} 
		return m == i;
	}
}
