package esa.s1pdgs.cpoc.ipf.preparation.worker.publish;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.converter.XmlConverter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderOutput;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.enums.JobOrderDestination;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.enums.JobOrderFileNameType;
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

@Component
public class Publisher {	
	private static final Logger LOGGER = LogManager.getLogger(Publisher.class);
	
    private final IpfPreparationWorkerSettings prepperSettings;
	private final ProcessSettings settings;
	private final XmlConverter xmlConverter;
	private final MqiClient mqiClient;
	
	@Autowired
	public Publisher(
			final IpfPreparationWorkerSettings prepperSettings, 
			final ProcessSettings settings,
			final XmlConverter xmlConverter,
			final MqiClient mqiClient
	) {
		this.prepperSettings = prepperSettings;
		this.settings = settings;
		this.xmlConverter = xmlConverter;
		this.mqiClient = mqiClient;
	}

	public Callable<JobGen> send(final JobGen job)  {
		return new Callable<JobGen>() {
			@Override
			public JobGen call() throws Exception {
				doSend(job);
				return job;
			}
		};		
	}
	
	// TODO this needs further cleanup but I'm running out of time
	private final void doSend(final JobGen job) {
		final long inc = job.job().getId();
		final String workingDir = "/data/localWD/" + inc + "/";
		final String joborderName = "JobOrder." + inc + ".xml";
		final String jobOrder = workingDir + joborderName;

		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(job.job().getReportingId())
				.newReporting("JobGenerator");

		reporting.begin(new ReportingMessage("Start job generation"));
		
		try {
			// Second, build the DTO
			job.buildJobOrder(workingDir);
			
			final IpfExecutionJob execJob = new IpfExecutionJob(
					settings.getLevel().toFamily(),
					job.productName(),
					job.processMode(), 
					workingDir, 
					jobOrder, 
					job.timeliness(),
					reporting.getUid()
			);
			execJob.setCreationDate(new Date());
			execJob.setHostname(settings.getHostname());

			try {
				// Add jobOrder inputs to the DTO
				final List<JobOrderInput> distinctInputJobOrder = job.jobOrder().getProcs().stream()
						.filter(proc -> proc != null && !CollectionUtils.isEmpty(proc.getInputs()))
						.flatMap(proc -> proc.getInputs().stream()).distinct().collect(Collectors.toList());

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

				// Add joborder output to the DTO
				final List<JobOrderOutput> distinctOutputJobOrder = job.jobOrder().getProcs().stream()
						.filter(proc -> proc != null && !CollectionUtils.isEmpty(proc.getOutputs()))
						.flatMap(proc -> proc.getOutputs().stream())
						.filter(output -> output.getFileNameType() == JobOrderFileNameType.REGEXP
								&& output.getDestination() == JobOrderDestination.DB)
						.distinct().collect(Collectors.toList());

				execJob.addOutputs(distinctOutputJobOrder.stream()
						.map(output -> new LevelJobOutputDto(output.getFamily().name(), output.getFileName()))
						.collect(Collectors.toList()));

				final List<JobOrderOutput> distinctOutputJobOrderNotRegexp = job.jobOrder().getProcs().stream()
						.filter(proc -> proc != null && !CollectionUtils.isEmpty(proc.getOutputs()))
						.flatMap(proc -> proc.getOutputs().stream())
						.filter(output -> output.getFileNameType() == JobOrderFileNameType.DIRECTORY
								&& output.getDestination() == JobOrderDestination.DB)
						.distinct().collect(Collectors.toList());

				execJob.addOutputs(distinctOutputJobOrderNotRegexp.stream()
						.map(output -> new LevelJobOutputDto(output.getFamily().name(),
								output.getFileName() + "^.*" + output.getFileType() + ".*$"))
						.collect(Collectors.toList()));

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

				// Add the tasks
				for (final List<String> pool : job.tasks()) {
					final LevelJobPoolDto poolDto = new LevelJobPoolDto();
					for (final String task : pool) {
						poolDto.addTask(new LevelJobTaskDto(task));
					}
					execJob.addPool(poolDto);
				}
				job.customJobDto(execJob);
			} 
			catch (IOException | JAXBException e) {
				throw new InternalErrorException("Cannot send the job", e);
			}

			LOGGER.info("Publishing job {} (product {})", job.id(), job.productName());
			final AppDataJob dto = job.job();

			final GenericPublicationMessageDto<IpfExecutionJob> messageToPublish = 
					new GenericPublicationMessageDto<IpfExecutionJob>(
					dto.getPrepJobMessageId(), 
					execJob.getProductFamily(), 
					execJob
			);
			messageToPublish.setInputKey(dto.getPrepJobInputQueue());
			messageToPublish.setOutputKey(execJob.getProductFamily().name());
			mqiClient.publish(messageToPublish, ProductCategory.LEVEL_JOBS);

			reporting.end(new JobOrderReportingOutput(joborderName, toProcParamMap(job)),
					new ReportingMessage("End job generation"));
		} catch (final AbstractCodedException e) {
			reporting.error(new ReportingMessage("Error on job generation"));
		}
	}
	

	private final Map<String, String> toProcParamMap(final JobGen jobGen) {
		try {
			final Map<String, String> result = new HashMap<>();

			for (final JobOrderProcParam param : jobGen.jobOrder().getConf().getProcParams()) {
				// S1PRO-699: Determine type of parameter to use the appropriate suffix in
				// reporting
				final String reportingType = mapTasktableTypeToReportingType(
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
	
	private final String mapTasktableTypeToReportingType(final String type) {
		if ("number".equalsIgnoreCase(type)) {
			return "_double";
		} else if ("datenumber".equalsIgnoreCase(type)) {
			return "_date";
		}
		return "_string";
	}

}
