package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable;

import java.util.Optional;

import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;

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
	
	public final Optional<TaskTableInputAlternative> getAlternativeForType(final String filetype) {
		for (final TaskTableInputAlternative alt : input.getAlternatives()) {
			if (filetype.equals(alt.getFileType())) {
				return Optional.of(alt);
			}
		}
		return Optional.empty();
	}
	
}