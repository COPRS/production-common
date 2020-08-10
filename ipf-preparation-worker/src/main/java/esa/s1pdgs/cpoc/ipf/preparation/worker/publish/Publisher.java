package esa.s1pdgs.cpoc.ipf.preparation.worker.publish;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
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
import esa.s1pdgs.cpoc.report.ReportingOutput;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.output.JobOrderReportingOutput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInputFile;

public class Publisher {	
	private static final Logger LOGGER = LogManager.getLogger(Publisher.class);
	
	private final IpfPreparationWorkerSettings prepperSettings;
	private final ProcessSettings settings;
	private final MqiClient mqiClient;
	private final TaskTableAdapter tasktableAdapter;
	private final JobOrderAdapter.Factory jobOrderFactory;
	private final ProductTypeAdapter typeAdapter;

	public Publisher(
			final IpfPreparationWorkerSettings prepperSettings,
			final ProcessSettings settings,
			final MqiClient mqiClient,
			final TaskTableAdapter tasktableAdapter,
			final JobOrderAdapter.Factory jobOrderFactory,
			final ProductTypeAdapter typeAdapter
	) {
		this.prepperSettings = prepperSettings;
		this.settings = settings;
		this.mqiClient = mqiClient;
		this.tasktableAdapter = tasktableAdapter;
		this.jobOrderFactory = jobOrderFactory;
		this.typeAdapter = typeAdapter;
	}

	public void send(final AppDataJob job)  {
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(job.getReportingId())
				.newReporting("JobGenerator");

		reporting.begin(new ReportingMessage("Start job generation"));
		
		try {
			final JobOrderAdapter jobOrderAdapter = jobOrderFactory.newJobOrderFor(job);
			
			// Second, build the DTO
			publishJob(job, createIpfExecutionJob(job, jobOrderAdapter, reporting));
			
			final ReportingOutput reportOut = new JobOrderReportingOutput(
					jobOrderAdapter.getJobOrderName(), 
					jobOrderAdapter.toProcParamMap(tasktableAdapter)
			);
			reporting.end(reportOut,new ReportingMessage("End job generation"));
		} catch (final AbstractCodedException e) {
			// TODO cause is not contained in reporting message
			reporting.error(new ReportingMessage("Error on job generation"));
		}
	}

	private IpfExecutionJob createIpfExecutionJob(
			final AppDataJob job, 
			final JobOrderAdapter jobOrderAdapter,
			final Reporting reporting
	) throws InternalErrorException {
		final AppDataJobProductAdapter product = new AppDataJobProductAdapter(job.getProduct());
		
		final IpfExecutionJob execJob = new IpfExecutionJob(
				settings.getLevel().toFamily(),
				product.getProductName(),
				product.getProcessMode(),
				jobOrderAdapter.getWorkdir().getPath() + "/",
				jobOrderAdapter.getJobOrderName(),
				product.getStringValue("timeliness", ""),
				reporting.getUid()
		);
		execJob.setCreationDate(new Date());
		execJob.setHostname(settings.getHostname());

		try {
			// Add jobOrder inputs to the DTO
			jobOrderAdapter.distinctInputs().forEach(input -> {
				for (final JobOrderInputFile file : input.getFilenames()) {
					execJob.addInput(new LevelJobInputDto(input.getFamily().name(), file.getFilename(),
							file.getKeyObjectStorage()));
				}
			});

			final String jobOrderXml = jobOrderAdapter.toXml();
			LOGGER.trace("Adding input JobOrderXml '{}' for product '{}'", jobOrderXml, product.getProductName());

			// Add the jobOrder itself in inputs
			execJob.addInput(new LevelJobInputDto(
					ProductFamily.JOB_ORDER.name(), 
					new File(jobOrderAdapter.getWorkdir(), jobOrderAdapter.getJobOrderName()).getPath(), 
					jobOrderXml
			));

			// Add jobOrder outputs to the DTO
			execJob.addOutputs(jobOrderAdapter.physicalOutputs());
			execJob.addOutputs(jobOrderAdapter.regexpOutputs());
			execJob.addOutputs(jobOrderAdapter.directoryOutputs());
			addOqcFlags(execJob);

			// Add the tasks
			for (final List<String> pool : tasktableAdapter.buildTasks()) {
				final LevelJobPoolDto poolDto = new LevelJobPoolDto();
				for (final String task : pool) {
					poolDto.addTask(new LevelJobTaskDto(task));
				}
				execJob.addPool(poolDto);
			}

			typeAdapter.customJobDto(job, execJob);
			return execJob;
		}
		catch (IOException | JAXBException e) {
			throw new InternalErrorException("Cannot send the job", e);
		}
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

	private void publishJob(final AppDataJob job, final IpfExecutionJob execJob) throws AbstractCodedException {
		LOGGER.info("Publishing job {} (product {})", job.getId(), job.getProductName());
		final GenericPublicationMessageDto<IpfExecutionJob> messageToPublish =
				new GenericPublicationMessageDto<>(
						job.getPrepJobMessageId(),
						execJob.getProductFamily(),
						execJob
				);
		messageToPublish.setInputKey(job.getPrepJobInputQueue());
		messageToPublish.setOutputKey(execJob.getProductFamily().name());
		mqiClient.publish(messageToPublish, ProductCategory.LEVEL_JOBS);
	}
}
