package esa.s1pdgs.cpoc.jobgenerator.model.tasktable;

import org.junit.Test;

import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableCfgFile;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableDynProcParam;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableOuput;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTablePool;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableInputAlternative.TaskTableInputAltKey;
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
	public void equalsDtoTaskTableCfgFile() {
		EqualsVerifier.forClass(TaskTableCfgFile.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDtoTaskTableDynProcParam() {
		EqualsVerifier.forClass(TaskTableDynProcParam.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDtoTaskTableInput() {
		EqualsVerifier.forClass(TaskTableInput.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDtoTaskTableInputAlternative() {
		EqualsVerifier.forClass(TaskTableInputAlternative.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS)
				.verify();
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
	public void equalsDtoTaskTableOuput() {
		EqualsVerifier.forClass(TaskTableOuput.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
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
