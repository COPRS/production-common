package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTablePool;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative.TaskTableInputAltKey;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class TaskTableObjectsTest {

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDtoTaskTable() {
		EqualsVerifier.forClass(TaskTable.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDtoTaskTableInputAltKey() {
		EqualsVerifier.forClass(TaskTableInputAltKey.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDtoTaskTablePool() {
		EqualsVerifier.forClass(TaskTablePool.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDtoTaskTableTask() {
		EqualsVerifier.forClass(TaskTableTask.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
