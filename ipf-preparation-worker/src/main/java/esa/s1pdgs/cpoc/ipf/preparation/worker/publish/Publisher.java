package esa.s1pdgs.cpoc.ipf.preparation.worker.publish;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobPoolDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobTaskDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.output.JobOrderReportingOutput;
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

@Component
public class Publisher {	
	private static final Logger LOGGER = LogManager.getLogger(Publisher.class);
	
    private final IpfPreparationWorkerSettings prepperSettings;
	private final ProcessSettings settings;
	private final ElementMapper elementMapper;
	private final XmlConverter xmlConverter;
	private final MqiClient mqiClient;
	
	@Autowired
	public Publisher(
			final IpfPreparationWorkerSettings prepperSettings,
			final ProcessSettings settings,
			final ElementMapper elementMapper,
			final XmlConverter xmlConverter,
			final MqiClient mqiClient
	) {
		this.prepperSettings = prepperSettings;
		this.settings = settings;
		this.elementMapper = elementMapper;
		this.xmlConverter = xmlConverter;
		this.mqiClient = mqiClient;
	}

	public Callable<JobGen> send(final JobGen job)  {
		return () -> {
			doSend(job);
			return job;
		};
	}
	
	// TODO this needs further cleanup but I'm running out of time
	private void doSend(final JobGen job) {
		final long inc = job.job().getId();
		final String workingDir = "/data/localWD/" + inc + "/";
		final String jobOrderName = "JobOrder." + inc + ".xml";
		final String jobOrder = workingDir + jobOrderName;

		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(job.job().getReportingId())
				.newReporting("JobGenerator");

		reporting.begin(new ReportingMessage("Start job generation"));
		
		try {
			// Second, build the DTO
			//TODO check if it's possible to create the JobOrder here instead of changing contents inside
			buildJobOrder(job, workingDir);

			publishJob(job, createIpfExecutionJob(job, workingDir, jobOrder, reporting));

			reporting.end(new JobOrderReportingOutput(jobOrderName, toProcParamMap(job)),
					new ReportingMessage("End job generation"));
		} catch (final AbstractCodedException e) {
			// TODO cause is not contained in reporting message
			reporting.error(new ReportingMessage("Error on job generation"));
		}
	}

	private IpfExecutionJob createIpfExecutionJob(final JobGen job, final String workingDir, final String jobOrder, final Reporting reporting) throws InternalErrorException {
		final IpfExecutionJob execJob = new IpfExecutionJob(
				settings.getLevel().toFamily(),
				job.productName(),
				job.processMode(),
				workingDir,
				jobOrder,
				timeliness(job.job()),
				reporting.getUid()
		);
		execJob.setCreationDate(new Date());
		execJob.setHostname(settings.getHostname());

		try {
			// Add jobOrder inputs to the DTO
			final List<JobOrderInput> distinctInputJobOrder = jobOrderInputs(job.jobOrder()).distinct().collect(toList());

			distinctInputJobOrder.forEach(input -> {
				for (final JobOrderInputFile file : input.getFilenames()) {
					execJob.addInput(new LevelJobInputDto(input.getFamily().name(), file.getFilename(),
							file.getKeyObjectStorage()));
				}
			});

			final String jobOrderXml = xmlConverter.convertFromObjectToXMLString(job.jobOrder());
			LOGGER.trace("Adding input JobOrderXml '{}' for product '{}'", jobOrderXml, job.productName());

			// Add the jobOrder itself in inputs
			execJob.addInput(new LevelJobInputDto(ProductFamily.JOB_ORDER.name(), jobOrder, jobOrderXml));

			// Add jobOrder outputs to the DTO
			execJob.addOutputs(regexpOutputs(job));
			execJob.addOutputs(directoryOutputs(job));

			addOqcFlags(execJob);

			// Add the tasks
			for (final List<String> pool : job.tasks()) {
				final LevelJobPoolDto poolDto = new LevelJobPoolDto();
				for (final String task : pool) {
					poolDto.addTask(new LevelJobTaskDto(task));
				}
				execJob.addPool(poolDto);
			}

			job.typeAdapter().customJobDto(job, execJob);
			return execJob;
		}
		catch (IOException | JAXBException e) {
			throw new InternalErrorException("Cannot send the job", e);
		}
	}

	private List<LevelJobOutputDto> directoryOutputs(final JobGen job) {
		return jobOrderOutputs(job.jobOrder())
				.filter(output -> output.getFileNameType() == JobOrderFileNameType.DIRECTORY
						&& output.getDestination() == JobOrderDestination.DB)
				.distinct()
				.map(output -> new LevelJobOutputDto(output.getFamily().name(),
						output.getFileName() + "^.*" + output.getFileType() + ".*$"))
				.collect(toList());
	}

	private List<LevelJobOutputDto> regexpOutputs(final JobGen job) {
		return jobOrderOutputs(job.jobOrder())
				.filter(output -> output.getFileNameType() == JobOrderFileNameType.REGEXP
						&& output.getDestination() == JobOrderDestination.DB)
				.distinct()
				.map(output -> new LevelJobOutputDto(output.getFamily().name(), output.getFileName()))
				.collect(toList());
	}

	private Stream<JobOrderInput> jobOrderInputs(final JobOrder jobOrder) {
		return jobOrder.getProcs().stream()
				.filter(proc -> proc != null && !CollectionUtils.isEmpty(proc.getInputs()))
				.flatMap(proc -> proc.getInputs().stream());
	}

	private Stream<JobOrderOutput> jobOrderOutputs(final JobOrder jobOrder) {
		return jobOrder.getProcs().stream()
				.filter(proc -> proc != null && !CollectionUtils.isEmpty(proc.getOutputs()))
				.flatMap(proc -> proc.getOutputs().stream());
	}

	private IpfExecutionJob addOqcFlags(final IpfExecutionJob execJob) {
		for (final LevelJobOutputDto output : execJob.getOutputs()) {
			// Iterate over the outputs and identify if an OQC check is required
			final ProductFamily outputFamily = ProductFamily.valueOf(output.getFamily());
			if (prepperSettings.getOqcCheck().contains(outputFamily)) {
				// Hit, we found a product family that had been configured as oqc check. Flag
				// it.
				LOGGER.info("Found output of family {}, flagging it as oqcCheck", outputFamily);
				output.setOqcCheck(true);
			} else {
				// No hit
				LOGGER.debug("Found output of family {}, no oqcCheck required", outputFamily);
			}
		}
		return execJob;
	}

	private void publishJob(final JobGen job, final IpfExecutionJob execJob) throws AbstractCodedException {
		LOGGER.info("Publishing job {} (product {})", job.id(), job.productName());
		final AppDataJob dto = job.job();

		final GenericPublicationMessageDto<IpfExecutionJob> messageToPublish =
				new GenericPublicationMessageDto<>(
						dto.getPrepJobMessageId(),
						execJob.getProductFamily(),
						execJob
				);
		messageToPublish.setInputKey(dto.getPrepJobInputQueue());
		messageToPublish.setOutputKey(execJob.getProductFamily().name());
		mqiClient.publish(messageToPublish, ProductCategory.LEVEL_JOBS);
	}

	public final String timeliness(final AppDataJob job) {
		try {
			return String.valueOf(job.getMessages().get(0).getBody().getMetadata().getOrDefault("timeliness", ""));
		} catch (final Exception e) {
			// fall through: just don't care if the mess above fails and return an empty value
		}
		return "";
	}

	// it is not a good idea to change the job order; instead it should be generated in the last step
	public final void buildJobOrder(final JobGen jobGen, final String workingDir) {
		//FIXME find better way to create proper paths than just modifying fileNames, intervals and outputs

		final JobOrder jobOrder = jobGen.jobOrder();
		final AppDataJob job = jobGen.job();
		final ProductTypeAdapter typeAdapter = jobGen.typeAdapter();

		jobOrderInputs(jobOrder).forEach(input -> {
			input.getFilenames().forEach(filename -> filename.setFilename(workingDir + filename.getFilename()));
			input.getTimeIntervals().forEach(interval -> interval.setFileName(workingDir + interval.getFileName()));
		});

		jobOrderOutputs(jobOrder).forEach(output -> output.setFileName(workingDir + output.getFileName()));

		// Apply implementation build job
		jobOrder.getConf().setSensingTime(new JobOrderSensingTime(
				DateUtils.convertToAnotherFormat(job.getStartTime(),
						AppDataJobProduct.TIME_FORMATTER, JobOrderSensingTime.DATETIME_FORMATTER),
				DateUtils.convertToAnotherFormat(job.getStopTime(),
						AppDataJobProduct.TIME_FORMATTER, JobOrderSensingTime.DATETIME_FORMATTER)));

		// collect all additional inputs
		final Map<String, AppDataJobTaskInputs> inputs
				= job.getAdditionalInputs().stream().collect(toMap(i -> i.getTaskName() + ":" + i.getTaskVersion(), i -> i));

		//set these inputs in corresponding job order processors
		jobOrder.getProcs().forEach(
				p -> p.setInputs(toJobOrderInputs(inputs.get(p.getTaskName() + ":" + p.getTaskVersion()))));

		typeAdapter.customJobOrder(jobGen);
	}

	private List<JobOrderInput> toJobOrderInputs(final AppDataJobTaskInputs appDataJobTaskInputs) {
		return appDataJobTaskInputs.getInputs().stream().map(this::toJobOrderInputs).collect(toList());
	}

	private JobOrderInput toJobOrderInputs(final AppDataJobInput input) {
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

	private Map<String, String> toProcParamMap(final JobGen jobGen) {
		try {
			final Map<String, String> result = new HashMap<>();

			for (final JobOrderProcParam param : jobGen.jobOrder().getConf().getProcParams()) {
				// S1PRO-699: Determine type of parameter to use the appropriate suffix in
				// reporting
				final String reportingType = mapTaskTableTypeToReportingType(
						jobGen.taskTableAdapter().getTypeForParameterName(param.getName())
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

}
