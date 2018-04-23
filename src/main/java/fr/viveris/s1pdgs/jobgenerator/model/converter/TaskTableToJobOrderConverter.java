package fr.viveris.s1pdgs.jobgenerator.model.converter;

import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrder;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderBreakpoint;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderConf;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderOutput;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderProc;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderProcParam;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.enums.JobOrderDestination;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.enums.JobOrderFileNameType;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTable;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTableCfgFile;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTableDynProcParam;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTableOuput;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTableTask;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableFileNameType;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableMandatoryEnum;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableTestEnum;

/**
 * Convert TaskTable objects into JobOrder objects
 * 
 * @author Cyrielle Gailliard
 *
 */
public class TaskTableToJobOrderConverter implements SuperConverter<TaskTable, JobOrder> {
	@Override
	public JobOrder apply(TaskTable t) {
		final TaskTableDynProcParamToJobOrderProcParamConverter procParamConverter = new TaskTableDynProcParamToJobOrderProcParamConverter();
		final TaskTableCfgFilesToString confFilesConverter = new TaskTableCfgFilesToString();
		final TaskTableTaskToJobOrderProc procConverter = new TaskTableTaskToJobOrderProc();

		final JobOrder order = new JobOrder();
		final JobOrderConf conf = new JobOrderConf();
		conf.setProcessorName(t.getProcessorName());
		conf.setVersion(t.getVersion());
		if (t.getTest() == TaskTableTestEnum.YES) {
			conf.setTest(true);
		}
		conf.addConfigFiles(confFilesConverter.convertToList(t.getCfgFiles()));
		conf.setProcParams(procParamConverter.convertToList(t.getDynProcParams()));
		order.setConf(conf);
		t.getPools().forEach(pool -> {
			order.addProcs(procConverter.convertToList(pool.getTasks()));
		});

		return order;
	}
}

/**
 * Class to convert TaskTableDynProcParam into JobOrderProcParam
 * 
 * @author Cyrielle Gailliard
 *
 */
class TaskTableDynProcParamToJobOrderProcParamConverter
		implements SuperConverter<TaskTableDynProcParam, JobOrderProcParam> {
	@Override
	public JobOrderProcParam apply(TaskTableDynProcParam t) {
		JobOrderProcParam r = new JobOrderProcParam();
		r.setName(t.getName());
		r.setValue(t.getDefaultValue());
		return r;
	}
}

/**
 * Class to convert TaskTableCfgFile into String
 * 
 * @author Cyrielle Gailliard
 *
 */
class TaskTableCfgFilesToString implements SuperConverter<TaskTableCfgFile, String> {
	@Override
	public String apply(TaskTableCfgFile t) {
		return t.getFileName();
	}
}

/**
 * Class to covnert TaskTableTask into JobOrderProc
 * 
 * @author Cyrielle Gailliard
 *
 */
class TaskTableTaskToJobOrderProc implements SuperConverter<TaskTableTask, JobOrderProc> {
	@Override
	public JobOrderProc apply(TaskTableTask t) {
		final TaskTableOuputToJobOrderOutput outputConverter = new TaskTableOuputToJobOrderOutput();
		JobOrderProc r = new JobOrderProc();
		r.setTaskName(t.getName());
		r.setTaskVersion(t.getVersion());
		r.setBreakpoint(new JobOrderBreakpoint());
		r.addOutputs(outputConverter.convertToList(t.getOutputs()));
		return r;
	}
}

/**
 * Class to convert TaskTableOuput into JobOrderOutput
 * 
 * @author Cyrielle Gailliard
 *
 */
class TaskTableOuputToJobOrderOutput implements SuperConverter<TaskTableOuput, JobOrderOutput> {
	@Override
	public JobOrderOutput apply(TaskTableOuput t) {
		final TaskTableFileNameTypeToJobOrderFileNameType fileNameTypeConverter = new TaskTableFileNameTypeToJobOrderFileNameType();

		JobOrderOutput r = new JobOrderOutput();
		if (t.getMandatory() == TaskTableMandatoryEnum.YES) {
			r.setMandatory(true);
		}
		r.setFileType(t.getType());
		r.setFileNameType(fileNameTypeConverter.apply(t.getFileNameType()));
		switch (t.getDestination()) {
		case DB:
			r.setDestination(JobOrderDestination.DB);
			break;
		default:
			r.setDestination(JobOrderDestination.PROC);
			break;
		}

		return r;
	}
}

/**
 * Class to convert TaskTableFileNameType into JobOrderFileNameType
 * 
 * @author Cyrielle Gailliard
 *
 */
class TaskTableFileNameTypeToJobOrderFileNameType
		implements SuperConverter<TaskTableFileNameType, JobOrderFileNameType> {
	@Override
	public JobOrderFileNameType apply(TaskTableFileNameType t) {
		JobOrderFileNameType r = JobOrderFileNameType.BLANK;
		switch (t) {
		case DIRECTORY:
			r = JobOrderFileNameType.DIRECTORY;
			break;
		case REGEXP:
			r = JobOrderFileNameType.REGEXP;
			break;
		case PHYSICAL:
			r = JobOrderFileNameType.PHYSICAL;
			break;
		default:
			r = JobOrderFileNameType.BLANK;
			break;
		}
		return r;
	}
}