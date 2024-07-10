/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.preparation.worker.model.tasktable;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.xml.model.joborder.AbstractJobOrderConf;
import esa.s1pdgs.cpoc.xml.model.joborder.AbstractJobOrderProc;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderOutput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.xml.model.joborder.L0JobOrderConf;
import esa.s1pdgs.cpoc.xml.model.joborder.L1JobOrderConf;
import esa.s1pdgs.cpoc.xml.model.joborder.L2JobOrderConf;
import esa.s1pdgs.cpoc.xml.model.joborder.SppMbuJobOrderBreakpoint;
import esa.s1pdgs.cpoc.xml.model.joborder.SppMbuJobOrderConf;
import esa.s1pdgs.cpoc.xml.model.joborder.SppMbuJobOrderProc;
import esa.s1pdgs.cpoc.xml.model.joborder.SppObsJobOrderBreakpoint;
import esa.s1pdgs.cpoc.xml.model.joborder.SppObsJobOrderConf;
import esa.s1pdgs.cpoc.xml.model.joborder.SppObsJobOrderProc;
import esa.s1pdgs.cpoc.xml.model.joborder.StandardJobOrderBreakpoint;
import esa.s1pdgs.cpoc.xml.model.joborder.StandardJobOrderProc;
import esa.s1pdgs.cpoc.xml.model.joborder.enums.JobOrderDestination;
import esa.s1pdgs.cpoc.xml.model.joborder.enums.JobOrderFileNameType;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableCfgFile;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableDynProcParam;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableOuput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTablePool;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableFileNameType;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableInputMode;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableMandatoryEnum;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableOutputDestination;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableTestEnum;

/**
 * Convert TaskTable objects into JobOrder objects
 * 
 * @author Cyrielle Gailliard
 *
 */
public class TaskTableToJobOrderConverter implements SuperConverter<TaskTable, JobOrder> {

	private final ProductMode productMode;

	public TaskTableToJobOrderConverter(ProductMode productMode) {
		this.productMode = productMode;
	}

	/**
	 * Conversion function
	 */
	@Override
	public JobOrder apply(final TaskTable tObj) {
		final TaskTableDynProcParamToJobOrderProcParamConverter procParamConv =
				new TaskTableDynProcParamToJobOrderProcParamConverter();
		final TaskTableCfgFilesToString confFilesConv = new TaskTableCfgFilesToString();
		final TaskTableTaskToJobOrderProc procConv =
				new TaskTableTaskToJobOrderProc(allInputsWithIdOf(tObj), productMode, tObj.getLevel());

		final JobOrder order = new JobOrder();

		final AbstractJobOrderConf conf;
		switch(tObj.getLevel()) {
			case L0: conf = new L0JobOrderConf(); break;
			case L2: conf = new L2JobOrderConf(); break;
			case SPP_MBU: conf = new SppMbuJobOrderConf(); break;
			case SPP_OBS: conf = new SppObsJobOrderConf(); break;
			default: conf = new L1JobOrderConf();
		}

		conf.setProcessorName(tObj.getProcessorName());
		conf.setVersion(tObj.getVersion());
		if (tObj.getTest() == TaskTableTestEnum.YES) {
			conf.setTest(true);
		}
		conf.addConfigFiles(confFilesConv.convertToList(tObj.getCfgFiles()));
		conf.setProcParams(procParamConv.convertToList(tObj.getDynProcParams()));
		order.setConf(conf);

		int poolNumber = 0;
		for (TaskTablePool pool : tObj.getPools()) {
			order.addProcs(procConv.convertToList(toIndexedTasks(poolNumber++, pool.getTasks())));
		}

		return order;
	}

	private Map<String, TaskTableInput> allInputsWithIdOf(TaskTable tObj) {
		return tObj.getPools().stream().flatMap(pool -> pool.getTasks().stream()).flatMap(task -> task.getInputs().stream())
				.filter(input -> !StringUtils.isEmpty(input.getId())).collect(toMap(TaskTableInput::getId, i -> i));
	}

	private List<NamedEntry<TaskTableTask>> toIndexedTasks(int poolNumber, List<TaskTableTask> tasks) {
		int taskNumber = 0;
		List<NamedEntry<TaskTableTask>> result = new ArrayList<>();
		for (TaskTableTask task : tasks) {
			result.add(new NamedEntry<>(String.format("P%sT%s:%s-%s", poolNumber, taskNumber++, task.getName(), task.getVersion()), task));
		}
		return result;
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
		final JobOrderProcParam rObj = new JobOrderProcParam();
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
 * Class to convert TaskTableTask into JobOrderProc
 * 
 * @author Cyrielle Gailliard
 *
 */
class TaskTableTaskToJobOrderProc implements SuperConverter<NamedEntry<TaskTableTask>, AbstractJobOrderProc> {
	final TaskTableInputToJobOrderInputPlaceholderProc inputProc = new TaskTableInputToJobOrderInputPlaceholderProc();

	private final Map<String, TaskTableInput> inputsWithId;
	private final ProductMode productMode;
	private final ApplicationLevel applicationLevel;

	TaskTableTaskToJobOrderProc(Map<String, TaskTableInput> inputsWithId, ProductMode productMode, ApplicationLevel applicationLevel) {
		this.inputsWithId = inputsWithId;
		this.productMode = productMode;
		this.applicationLevel = applicationLevel;
	}

	/**
	 * Conversion function
	 */
	@Override
	public AbstractJobOrderProc apply(final NamedEntry<TaskTableTask> tObj) {
		final TaskTableOuputToJobOrderOutput outputConverter = new TaskTableOuputToJobOrderOutput();
		final AbstractJobOrderProc rObj;

		switch (applicationLevel) {
			case SPP_MBU:
				rObj = new SppMbuJobOrderProc();
				rObj.setBreakpoint(new SppMbuJobOrderBreakpoint());
				break;
			case SPP_OBS:
				rObj = new SppObsJobOrderProc();
				rObj.setBreakpoint(new SppObsJobOrderBreakpoint());
				break;
			default:
				rObj = new StandardJobOrderProc();
				rObj.setBreakpoint(new StandardJobOrderBreakpoint());
		}

		rObj.setTaskName(tObj.getEntry().getName());
		rObj.setTaskVersion(tObj.getEntry().getVersion());

		rObj.addOutputs(outputConverter.convertToList(tObj.getEntry().getOutputs()));

		rObj.setInputs(inputProc.convertToList(
				toIndexedInputs(tObj.getName(), tObj.getEntry().getInputs())
						.stream().filter(this::withMatchingMode).collect(toList())));

		return rObj;
	}

	private boolean withMatchingMode(NamedEntry<TaskTableInput> input) {
		return productMode.isCompatibleWithTaskTableMode(modeOfInputOrReference(input.getEntry(), inputsWithId));
	}

	private TaskTableInputMode modeOfInputOrReference(TaskTableInput input, Map<String, TaskTableInput> inputsWithId) {
		if(StringUtils.isEmpty(input.getReference())) {
			return input.getMode();
		}

		final TaskTableInput reference = inputsWithId.get(input.getReference());

		if(reference == null) {
			throw new RuntimeException("no input in taskTable with id " + input.getReference());
		}

		return reference.getMode();
	}

	private List<NamedEntry<TaskTableInput>> toIndexedInputs(String taskIndex, List<TaskTableInput> inputs) {
		int inputNumber = 0;
		List<NamedEntry<TaskTableInput>> result = new ArrayList<>();
		for (TaskTableInput input : inputs) {
			result.add(new NamedEntry<>(String.format("%sI%s", taskIndex, inputNumber++), input));
		}
		return result;
	}
}

class TaskTableInputToJobOrderInputPlaceholderProc implements SuperConverter<NamedEntry<TaskTableInput>, JobOrderInput> {
	@Override
	public JobOrderInput apply(NamedEntry<TaskTableInput> tIObj) {
		return  new JobOrderInput(tIObj.getName(), JobOrderFileNameType.BLANK, Collections.emptyList(), Collections.emptyList(), ProductFamily.BLANK);
	}
}

/**
 * Class to convert TaskTableOutput into JobOrderOutput
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

		final JobOrderOutput rObj = new JobOrderOutput();
		if (tObj.getMandatory() == TaskTableMandatoryEnum.YES) {
			rObj.setMandatory(true);
		}
		rObj.setFileType(tObj.getType());
		rObj.setFileNameType(fileNameTypeConverter.apply(tObj.getFileNameType()));
		if (EnumSet.of(TaskTableOutputDestination.DB, TaskTableOutputDestination.DBPROC).contains(tObj.getDestination())) {
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

class NamedEntry<U> {
	private final String name;
	private final U entry;

	NamedEntry(String name, U entry) {
		this.name = name;
		this.entry = entry;
	}

	public String getName() {
		return name;
	}

	public U getEntry() {
		return entry;
	}
}