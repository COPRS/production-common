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

package esa.s1pdgs.cpoc.preparation.worker.model.joborder;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.preparation.worker.config.PreparationWorkerProperties;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.ElementMapper;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.s3.DuplicateProductFilter;
import esa.s1pdgs.cpoc.xml.XmlConverter;
import esa.s1pdgs.cpoc.xml.model.joborder.AbstractJobOrderProc;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderOutput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderSensingTime;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderTimeInterval;
import esa.s1pdgs.cpoc.xml.model.joborder.enums.JobOrderDestination;
import esa.s1pdgs.cpoc.xml.model.joborder.enums.JobOrderFileNameType;

public final class JobOrderAdapter
{
	public static final class Factory {	
		private final Function<TaskTableAdapter, JobOrder> jobOrderSupplier;
		private final ProductTypeAdapter typeAdapter;
		private final ElementMapper elementMapper;
		private final XmlConverter xmlConverter;
		private final PreparationWorkerProperties prepperSettings;

		public Factory(
				final Function<TaskTableAdapter, JobOrder> jobOrderSupplier, 
				final ProductTypeAdapter typeAdapter,
				final ElementMapper elementMapper,
				final XmlConverter xmlConverter,
				final PreparationWorkerProperties prepperSettings
		) {
			this.jobOrderSupplier = jobOrderSupplier;
			this.typeAdapter = typeAdapter;
			this.elementMapper = elementMapper;
			this.xmlConverter = xmlConverter;
			this.prepperSettings = prepperSettings;
		}

		public final JobOrderAdapter newJobOrderFor(final AppDataJob job, final TaskTableAdapter taskTableAdapter) {
			final long inc = job.getId();
			
			final String workingDir = "/data/localWD/" + inc + "/";
			final File jobOrderFile = new File(
					workingDir,
					"JobOrder." + inc + ".xml"
			);
			final JobOrder jobOrder = jobOrderSupplier.apply(taskTableAdapter);
			
			// collect all additional inputs
			final Map<String, AppDataJobInput> inputsByReference =
					job.getAdditionalInputs().stream().flatMap(taskInputs -> taskInputs.getInputs().stream())
							.collect(toMap(AppDataJobInput::getTaskTableInputReference, i -> i));

			//set these inputs in corresponding job order processors
			jobOrder.getProcs().forEach(
					p -> replacePlaceHolderWithResults(p, inputsByReference));

			inputsOf(jobOrder).forEach(input -> {
				input.getFilenames().forEach(filename -> filename.setFilename(workingDir + filename.getFilename()));
				input.getTimeIntervals().forEach(interval -> interval.setFileName(workingDir + interval.getFileName()));
			});

			outputsOf(jobOrder).forEach(output -> output.setFileName(workingDir + output.getFileName()));

			// Apply implementation build job
			jobOrder.getConf().setSensingTime(new JobOrderSensingTime(
					DateUtils.convertToAnotherFormat(job.getStartTime(),
							AppDataJobProduct.TIME_FORMATTER, JobOrderSensingTime.DATETIME_FORMATTER),
					DateUtils.convertToAnotherFormat(job.getStopTime(),
							AppDataJobProduct.TIME_FORMATTER, JobOrderSensingTime.DATETIME_FORMATTER)));
			
			typeAdapter.customJobOrder(job, jobOrder);
			
			if (prepperSettings.isUseLatestOnly()) {
				inputsOf(jobOrder).forEach(input -> {
					JobOrderInput newInput = DuplicateProductFilter.filterJobOrderInput(input);
					input.setFilenames(newInput.getFilenames());
					input.setTimeIntervals(newInput.getTimeIntervals());
				});
			}
			
			return new JobOrderAdapter(xmlConverter, jobOrderFile, jobOrder);
		}

		private void replacePlaceHolderWithResults(final AbstractJobOrderProc p, final Map<String, AppDataJobInput> inputsByReference) {
			final List<JobOrderInput> jobOrderInputs = new ArrayList<>();

			p.getInputs().forEach(placeHolder -> {
				final AppDataJobInput appDataJobInput = inputsByReference.get(placeHolder.getFileType());

				if(appDataJobInput == null) {
					LOGGER.info("removing JobOrder placeholder without result (probably timeout): {}", placeHolder.getFileType());
				} else {
					jobOrderInputs.add(toJobOrderInput(appDataJobInput));
				}
			});

			p.setInputs(jobOrderInputs);
		}

		private JobOrderInput toJobOrderInput(final AppDataJobInput input) {
			return new JobOrderInput(
					input.getFileType(),
					filenameTypeOf(input.getFileNameType()),
					toFileNames(input.getFiles()),
					toIntervals(input.getFiles()),
					elementMapper.inputFamilyOf(input.getFileType()));
		}
		
		//this is a copy from TaskTableAdapter but there it should be removed soon
		private JobOrderFileNameType filenameTypeOf(final String fileNameType) {
			if ("".equals(fileNameType)) {
				return JobOrderFileNameType.BLANK;
			}
			return JobOrderFileNameType.valueOf(fileNameType);	
		}		

		private List<JobOrderTimeInterval> toIntervals(final List<AppDataJobFile> files) {
			return files.stream().map(
					f -> new JobOrderTimeInterval(
							f.getStartDate(), 
							f.getEndDate(), 
							f.getFilename())
					).collect(toList());
		}

		private List<JobOrderInputFile> toFileNames(final List<AppDataJobFile> files) {
			return files.stream().map(
					f -> new JobOrderInputFile(f.getFilename(), f.getKeyObs())).collect(toList());
		}
	}
	
	private static final Logger LOGGER = LogManager.getLogger(JobOrderAdapter.class);
	
	private final XmlConverter xmlConverter;
	private final File fileInWorkdir;
	private final JobOrder jobOrder;
	
	public JobOrderAdapter(final XmlConverter xmlConverter, final File fileInWorkdir, final JobOrder jobOrder) {
		this.xmlConverter = xmlConverter;
		this.fileInWorkdir = fileInWorkdir;
		this.jobOrder = jobOrder;
	}

	public final File getWorkdir() {
		return fileInWorkdir.getParentFile();
	}
	
	public final String getJobOrderName() {
		return fileInWorkdir.getName();
	}
	
	public final List<JobOrderInput> distinctInputs() {
		return inputsOf(jobOrder)
			.distinct()
			.collect(toList());
	}
	
	@SafeVarargs
	private final Predicate<JobOrderOutput> filenameTypeFilter(final JobOrderFileNameType fnt, final String ... outputTypes) {	
		final List<String> allowedTypes = CollectionUtil.toList(outputTypes); 
		
		// empty or null means: all are allowed
		if (allowedTypes.isEmpty() || StringUtil.isEmpty(allowedTypes.get(0))) {
			LOGGER.debug("Using all {} outputs from tasktable", fnt);
			return output -> true;
		}		
		LOGGER.debug("Using '{}' {} outputs from tasktable", allowedTypes, fnt);
		return output -> allowedTypes.contains(output.getFileType());	
	}
	
	@SafeVarargs
	public final List<LevelJobOutputDto> physicalOutputs(final String ... outputTypes) {
		return outputsOfFilenameType(
				JobOrderFileNameType.PHYSICAL, 
				output ->  new LevelJobOutputDto(
						output.getFamily().name(), 
						output.getFileName() + "^.*" + output.getFileType() + ".*$"
				),
				outputTypes
		);
	}
	
	@SafeVarargs
	public final List<LevelJobOutputDto> directoryOutputs(final String ... outputTypes) {
		return outputsOfFilenameType(
				JobOrderFileNameType.DIRECTORY, 
				output ->  new LevelJobOutputDto(
						output.getFamily().name(), 
						output.getFileName() + "^.*" + output.getFileType() + ".*$"
				),
				outputTypes
		);
	}

	@SafeVarargs
	public final List<LevelJobOutputDto> regexpOutputs(final String ... outputTypes) {		
		return outputsOfFilenameType(
				JobOrderFileNameType.REGEXP, 
				output -> new LevelJobOutputDto(output.getFamily().name(), output.getFileName()),
				outputTypes
		);
	}
	
	final List<LevelJobOutputDto> outputsOfFilenameType(
			final JobOrderFileNameType filenameType, 
			final Function<JobOrderOutput,LevelJobOutputDto> levelJobOutputProvider,
			final String ... outputTypes
			
	) {
		final Predicate<JobOrderOutput> outputFilter = output -> 
			output.getFileNameType() == filenameType &&
			output.getDestination() == JobOrderDestination.DB;
		
		return outputsOf(jobOrder)
				.filter(filenameTypeFilter(filenameType, outputTypes))
				.filter(outputFilter)
				.distinct()
				.map(levelJobOutputProvider)
				.collect(toList());
	}
	
	public final String toXml() throws IOException, JAXBException {
		return xmlConverter.convertFromObjectToXMLString(jobOrder);
	}
	
	public final Map<String, String> toProcParamMap(final TaskTableAdapter tasktableAdapter) {
		try {
			final Map<String, String> result = new HashMap<>();

			for (final JobOrderProcParam param : jobOrder.getConf().getProcParams()) {
				// S1PRO-699: Determine type of parameter to use the appropriate suffix in
				// reporting
				final String reportingType = mapTaskTableTypeToReportingType(
						tasktableAdapter.getTypeForParameterName(param.getName())
				);
				result.put(param.getName() + reportingType, param.getValue());
			}
			return result;
		} catch (final Exception e) {
			// this is only used for reporting so don't break anything if this goes wrong here and provide the error message
			LOGGER.error(e);
			return Collections.singletonMap("error", LogUtils.toString(e));
		}
	}
	
	
	private String mapTaskTableTypeToReportingType(final String type) {
		if ("number".equalsIgnoreCase(type)) {
			return "_double";
		} else if ("datenumber".equalsIgnoreCase(type)) {
			return "_date";
		}
		return "_string";
	}
	
	private static Stream<JobOrderInput> inputsOf(final JobOrder jobOrder) {
		return jobOrder.getProcs().stream()
				.filter(proc -> proc != null && !CollectionUtils.isEmpty(proc.getInputs()))
				.flatMap(proc -> proc.getInputs().stream());
	}

	private static Stream<JobOrderOutput> outputsOf(final JobOrder jobOrder) {
		return jobOrder.getProcs().stream()
				.filter(proc -> proc != null && !CollectionUtils.isEmpty(proc.getOutputs()))
				.flatMap(proc -> proc.getOutputs().stream());
	}
	
}