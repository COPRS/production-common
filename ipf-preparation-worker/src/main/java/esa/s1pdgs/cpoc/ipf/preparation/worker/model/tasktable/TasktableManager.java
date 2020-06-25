package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.Assert;

public class TasktableManager {	
	private final List<File> tasktables;
		
	public TasktableManager(final List<File> tasktables) {
		this.tasktables = tasktables;
	}

	public static final TasktableManager of(final File directory) {
		Assert.isTrue(directory != null, "No tasktable directory specified");
		Assert.isTrue(directory.isDirectory(), "No tasktable directory specified");

        final File[] taskTableFiles = directory.listFiles(parameter -> parameter.isFile());
		Assert.isTrue(taskTableFiles != null, "Tasktable listing is null (IOError)");
		return new TasktableManager(Arrays.asList(taskTableFiles));
	}
	
	public final List<File> tasktables() {
		return tasktables;
	}	
	
	public final int size() {
		return tasktables.size();
	}
}
