package esa.s1pdgs.cpoc.ipf.preparation.worker.publish;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.xml.XmlConverter;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderOutput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderSensingTime;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderTimeInterval;
import esa.s1pdgs.cpoc.xml.model.joborder.enums.JobOrderDestination;
import esa.s1pdgs.cpoc.xml.model.joborder.enums.JobOrderFileNameType;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableFileNameType;

public final class JobOrderAdapter
{
	public static final class Factory {	
		private final Supplier<JobOrder> jobOrderSupplier;
		private final ProductTypeAdapter typeAdapter;
		private final ElementMapper elementMapper;
		private final XmlConverter xmlConverter;

		public Factory(
				final Supplier<JobOrder> jobOrderSupplier, 
				final ProductTypeAdapter typeAdapter,
				final ElementMapper elementMapper,
				final XmlConverter xmlConverter
		) {
			this.jobOrderSupplier = jobOrderSupplier;
			this.typeAdapter = typeAdapter;
			this.elementMapper = elementMapper;
			this.xmlConverter = xmlConverter;
		}

		final JobOrderAdapter newJobOrderFor(final AppDataJob job) {
			final long inc = job.getId();
			
			final String workingDir = "/data/localWD/" + inc + "/";
			final File jobOrderFile = new File(
					workingDir,
					"JobOrder." + inc + ".xml"
			);
			final JobOrder jobOrder = jobOrderSupplier.get();
			
			// collect all additional inputs
			final Map<String, AppDataJobTaskInputs> inputs
					= job.getAdditionalInputs().stream().collect(toMap(i -> i.getTaskName() + ":" + i.getTaskVersion(), i -> i));

			//set these inputs in corresponding job order processors
			jobOrder.getProcs().forEach(
					p -> p.setInputs(toJobOrderInputs(inputs.get(p.getTaskName() + ":" + p.getTaskVersion()))));

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
			
			return new JobOrderAdapter(xmlConverter, jobOrderFile, jobOrder);
		}
		
		private List<JobOrderInput> toJobOrderInputs(final AppDataJobTaskInputs appDataJobTaskInputs) {
			if(appDataJobTaskInputs == null) {
				return emptyList();
			}
			return appDataJobTaskInputs.getInputs().stream()
					.map(this::toJobOrderInput)
					.collect(toList());
		}
		
		private JobOrderInput toJobOrderInput(final AppDataJobInput input) {
			return new JobOrderInput(
					input.getFileType(),
					getFileNameTypeFor(input.getFileNameType()),
					toFileNames(input.getFiles()),
					toIntervals(input.getFiles()),
					elementMapper.inputFamilyOf(input.getFileType()));
		}
		
		//this is a copy from TaskTableAdapter but there it should be removed soon
		private JobOrderFileNameType getFileNameTypeFor(final String fileNameType) {
			final TaskTableFileNameType type = TaskTableFileNameType.valueOf(fileNameType);
			switch (type) {
				case PHYSICAL:
					return JobOrderFileNameType.PHYSICAL;
				case DIRECTORY:
					return JobOrderFileNameType.DIRECTORY;
				case REGEXP:
					return JobOrderFileNameType.REGEXP;
				default:
					// fall through
			}
			return JobOrderFileNameType.BLANK;
		}
		

		private List<JobOrderTimeInterval> toIntervals(final List<AppDataJobFile> files) {
			return files.stream().map(
					f -> new JobOrderTimeInterval(f.getStartDate(), f.getEndDate(), f.getFilename())).collect(toList());
		}

		private List<JobOrderInputFile> toFileNames(final List<AppDataJobFile> files) {
			return files.stream().map(
					f -> new JobOrderInputFile(f.getFilename(), f.getKeyObs())).collect(toList());
		}
		

		
	}
	
	private static final Logger LOGGER = LogManager.getLogger(Publisher.class);
	
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
	
	
	public final List<LevelJobOutputDto> directoryOutputs() {
		return outputsOf(jobOrder)
				.filter(output -> output.getFileNameType() == JobOrderFileNameType.DIRECTORY
						&& output.getDestination() == JobOrderDestination.DB)
				.distinct()
				.map(output -> new LevelJobOutputDto(output.getFamily().name(),
						output.getFileName() + "^.*" + output.getFileType() + ".*$"))
				.collect(toList());
	}

	public final List<LevelJobOutputDto> regexpOutputs() {
		return outputsOf(jobOrder)
				.filter(output -> output.getFileNameType() == JobOrderFileNameType.REGEXP
						&& output.getDestination() == JobOrderDestination.DB)
				.distinct()
				.map(output -> new LevelJobOutputDto(output.getFamily().name(), output.getFileName()))
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