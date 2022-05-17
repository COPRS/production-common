package esa.s1pdgs.cpoc.preparation.worker.model.tasktable;

import java.util.Optional;

import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;

public final class TaskTableInputAdapter {
	private final String reference;
	private final TaskTableInput input;
	private final ElementMapper elementMapper;
	
	public TaskTableInputAdapter(
			final String reference, 
			final TaskTableInput input,
			final ElementMapper elementMapper
	) {
		this.reference = reference;
		this.input = input;
		this.elementMapper = elementMapper;
	}

	public String getReference() {
		return reference;
	}

	public TaskTableInput getInput() {
		return input;
	}
	
	public final Optional<TaskTableInputAlternative> getAlternativeForType(final String filetype) {
		for (final TaskTableInputAlternative alt : input.getAlternatives()) {
			final String mapped = elementMapper.mappedFileType(alt.getFileType());
			
			if (filetype.matches(mapped)) {
				return Optional.of(alt);
			}
		}
		return Optional.empty();
	}
	
}