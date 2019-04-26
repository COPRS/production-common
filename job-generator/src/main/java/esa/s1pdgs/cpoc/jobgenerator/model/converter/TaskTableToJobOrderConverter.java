package esa.s1pdgs.cpoc.jobgenerator.model.converter;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.AbstractJobOrderConf;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderBreakpoint;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderOutput;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderProc;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.L0JobOrderConf;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.L1JobOrderConf;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.enums.JobOrderDestination;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.enums.JobOrderFileNameType;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableCfgFile;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableDynProcParam;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableOuput;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.enums.TaskTableFileNameType;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.enums.TaskTableMandatoryEnum;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.enums.TaskTableOutputDestination;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.enums.TaskTableTestEnum;

/**
 * Convert TaskTable objects into JobOrder objects
 * 
 * @author Cyrielle Gailliard
 *
 */
public class TaskTableToJobOrderConverter implements SuperConverter<TaskTable, JobOrder> {

	/**
	 * Conversion function
	 */
	@Override
	public JobOrder apply(final TaskTable tObj) {
		final TaskTableDynProcParamToJobOrderProcParamConverter procParamConv = new TaskTableDynProcParamToJobOrderProcParamConverter();
		final TaskTableCfgFilesToString confFilesConv = new TaskTableCfgFilesToString();
		final TaskTableTaskToJobOrderProc procConv = new TaskTableTaskToJobOrderProc();

		final JobOrder order = new JobOrder();
		AbstractJobOrderConf conf = tObj.getLevel() == ApplicationLevel.L0 ? new L0JobOrderConf() : new L1JobOrderConf();
		conf.setProcessorName(tObj.getProcessorName());
		conf.setVersion(tObj.getVersion());
		if (tObj.getTest() == TaskTableTestEnum.YES) {
			conf.setTest(true);
		}
		conf.addConfigFiles(confFilesConv.convertToList(tObj.getCfgFiles()));
		conf.setProcParams(procParamConv.convertToList(tObj.getDynProcParams()));
		order.setConf(conf);
		tObj.getPools().forEach(pool -> {
			order.addProcs(procConv.convertToList(pool.getTasks()));
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
	
	/**
	 * Conversion function
	 */
	@Override
	public JobOrderProcParam apply(final TaskTableDynProcParam tObj) {
		JobOrderProcParam rObj = new JobOrderProcParam();
		rObj.setName(tObj.getName());
		rObj.setValue(tObj.getDefaultValue());
		return rObj;
	}
}

/**
 * Class to convert TaskTableCfgFile into String
 * 
 * @author Cyrielle Gailliard
 *
 */
class TaskTableCfgFilesToString implements SuperConverter<TaskTableCfgFile, String> {
	/**
	 * Default constructor
	 */
	public TaskTableCfgFilesToString() {
		super();
	}

	/**
	 * Conversion function
	 */
	@Override
	public String apply(final TaskTableCfgFile tObj) {
		return tObj.getFileName();
	}
}

/**
 * Class to covnert TaskTableTask into JobOrderProc
 * 
 * @author Cyrielle Gailliard
 *
 */
class TaskTableTaskToJobOrderProc implements SuperConverter<TaskTableTask, JobOrderProc> {
	
	/**
	 * Conversion function
	 */
	@Override
	public JobOrderProc apply(final TaskTableTask tObj) {
		final TaskTableOuputToJobOrderOutput outputConverter = new TaskTableOuputToJobOrderOutput();
		JobOrderProc rObj = new JobOrderProc();
		rObj.setTaskName(tObj.getName());
		rObj.setTaskVersion(tObj.getVersion());
		rObj.setBreakpoint(new JobOrderBreakpoint());
		rObj.addOutputs(outputConverter.convertToList(tObj.getOutputs()));
		return rObj;
	}
}

/**
 * Class to convert TaskTableOuput into JobOrderOutput
 * 
 * @author Cyrielle Gailliard
 *
 */
class TaskTableOuputToJobOrderOutput implements SuperConverter<TaskTableOuput, JobOrderOutput> {
	
	/**
	 * Conversion function
	 */
	@Override
	public JobOrderOutput apply(final TaskTableOuput tObj) {
		final TaskTableFileNameTypeToJobOrderFileNameType fileNameTypeConverter = new TaskTableFileNameTypeToJobOrderFileNameType();

		JobOrderOutput rObj = new JobOrderOutput();
		if (tObj.getMandatory() == TaskTableMandatoryEnum.YES) {
			rObj.setMandatory(true);
		}
		rObj.setFileType(tObj.getType());
		rObj.setFileNameType(fileNameTypeConverter.apply(tObj.getFileNameType()));
		if (tObj.getDestination() == TaskTableOutputDestination.DB) {
			rObj.setDestination(JobOrderDestination.DB);
		} else {
			rObj.setDestination(JobOrderDestination.PROC);
		}

		return rObj;
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
	
	/**
	 * Conversion function
	 */
	@Override
	public JobOrderFileNameType apply(final TaskTableFileNameType tObj) {
		JobOrderFileNameType rObj;
		switch (tObj) {
		case DIRECTORY:
			rObj = JobOrderFileNameType.DIRECTORY;
			break;
		case REGEXP:
			rObj = JobOrderFileNameType.REGEXP;
			break;
		case PHYSICAL:
			rObj = JobOrderFileNameType.PHYSICAL;
			break;
		default:
			rObj = JobOrderFileNameType.BLANK;
			break;
		}
		return rObj;
	}
}