package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable;

import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;

public final class TaskTableInputAdapter {
	private final String reference;
	private final TaskTableInput input;
	
	public TaskTableInputAdapter(final String reference, final TaskTableInput input) {
		this.reference = reference;
		this.input = input;
	}

	public String getReference() {
		return reference;
	}

	public TaskTableInput getInput() {
		return input;
	}
	
}