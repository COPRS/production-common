package esa.s1pdgs.cpoc.preparation.worker.service;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobPoolDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobTaskDto;
import esa.s1pdgs.cpoc.preparation.worker.config.PreparationWorkerProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.DiscardedException;
import esa.s1pdgs.cpoc.preparation.worker.model.joborder.JobOrderAdapter;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingOutput;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.output.JobOrderReportingOutput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableInputOrigin;

public class JobCreationService {
	private static final Logger LOGGER = LogManager.getLogger(JobCreationService.class);

	private final CommonConfigurationProperties commonProperties;
	private final PreparationWorkerProperties prepperSettings;
	private final ProcessProperties settings;
	private final JobOrderAdapter.Factory jobOrderFactory;
	private final ProductTypeAdapter typeAdapter;

	public JobCreationService(final CommonConfigurationProperties commonProperties,
			final PreparationWorkerProperties prepperSettings, final ProcessProperties settings,
			final JobOrderAdapter.Factory jobOrderFactory, final ProductTypeAdapter typeAdapter) {
		this.commonProperties = commonProperties;
		this.prepperSettings = prepperSettings;
		this.settings = settings;
		this.jobOrderFactory = jobOrderFactory;
		this.typeAdapter = typeAdapter;
	}

	public IpfExecutionJob createExecutionJob(final AppDataJob job, final TaskTableAdapter tasktableAdapter) throws DiscardedException {

		MissionId mission = MissionId.valueOf((String) job.getProduct().getMetadata().get(MissionId.FIELD_NAME));

		final Reporting reporting = ReportingUtils.newReportingBuilder(mission)
				.rsChainName(commonProperties.getRsChainName())
				.rsChainVersion(commonProperties.getRsChainVersion())
				.predecessor(job.getReportingId())
				.newReporting("JobGenerator");

		reporting.begin(new ReportingMessage("Start job generation"));

		IpfExecutionJob executionJob = null;
		AppDataJobGenerationState newState = job.getGeneration().getState();
		
		try {
			final JobOrderAdapter jobOrderAdapter = jobOrderFactory.newJobOrderFor(job, tasktableAdapter);

			// Create the ExecutionJob as output of the PreparationWorker
			executionJob = createIpfExecutionJob(job, jobOrderAdapter, tasktableAdapter, reporting);
			newState = AppDataJobGenerationState.SENT;
			
			final ReportingOutput reportOut = new JobOrderReportingOutput(jobOrderAdapter.getJobOrderName(),
					jobOrderAdapter.toProcParamMap(tasktableAdapter));
			reporting.end(reportOut, new ReportingMessage("End job generation"));
		} catch (final AbstractCodedException e) {
			// TODO cause is not contained in reporting message
			reporting.error(new ReportingMessage("Error on job generation"));
		} finally {
			updateSend(job, newState);
		}
		
		LOGGER.info("Publishing job {} (product {})", job.getId(), job.getProductName());
		return executionJob;
	}

	private IpfExecutionJob createIpfExecutionJob(final AppDataJob job, final JobOrderAdapter jobOrderAdapter,
			final TaskTableAdapter tasktableAdapter, final Reporting reporting) throws InternalErrorException {
		final AppDataJobProductAdapter product = new AppDataJobProductAdapter(job.getProduct());

		final File joborder = new File(jobOrderAdapter.getWorkdir(), jobOrderAdapter.getJobOrderName());

		final IpfExecutionJob execJob = new IpfExecutionJob(settings.getLevel().toFamily(), product.getProductName(),
				product.getProcessMode(), jobOrderAdapter.getWorkdir().getPath() + "/", joborder.getPath(),
				product.getStringValue("timeliness", ""), reporting.getUid());
		execJob.setCreationDate(new Date());
		execJob.setPodName(settings.getHostname());

		final IpfPreparationJob prepJob = job.getPrepJob();
		execJob.setPreparationJob(prepJob);
		execJob.setDebug(prepJob.isDebug());
		execJob.setTimedOut(job.getTimedOut());

		try {
			// Add jobOrder inputs to ExecJob (except PROC inputs)
			addInputsToExecJob(jobOrderAdapter, tasktableAdapter, execJob);

			final String jobOrderXml = jobOrderAdapter.toXml();
			LOGGER.trace("Adding input JobOrderXml '{}' for product '{}'", jobOrderXml, product.getProductName());

			// Add the jobOrder itself in inputs
			execJob.addInput(new LevelJobInputDto(ProductFamily.JOB_ORDER.name(), joborder.getPath(), jobOrderXml));

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
			
			// Determine t0PdgsDate
			Date t0 = null;
			for (AppDataJobTaskInputs inputs : job.getAdditionalInputs()) {
				for (AppDataJobInput input : inputs.getInputs()) {
					for (AppDataJobFile file : input.getFiles()) {
						if (file.getT0_pdgs_date() != null && (t0 == null || t0.before(file.getT0_pdgs_date()))) {
							t0 = file.getT0_pdgs_date();
						}
					}
				}
			}
			execJob.setT0PdgsDate(t0);

			typeAdapter.customJobDto(job, execJob);
			return execJob;
		} catch (IOException | JAXBException e) {
			throw new InternalErrorException("Cannot send the job", e);
		}
	}

	/**
	 * Add inputs from job order to execution job. Ignore PROC inputs, as they
	 * should not be downloaded from the OBS
	 */
	private void addInputsToExecJob(final JobOrderAdapter jobOrderAdapter, final TaskTableAdapter tasktableAdapter,
			final IpfExecutionJob execJob) {
		// Create list of all FileTypes of TaskTableInputAlternatives of Origin PROC
		final List<String> procAlternatives = tasktableAdapter.getAllAlternatives().stream()
				.filter(alternative -> alternative.getOrigin() == TaskTableInputOrigin.PROC)
				.map(alternative -> alternative.getFileType()).collect(toList());

		jobOrderAdapter.distinctInputs().forEach(input -> {
			for (final JobOrderInputFile inputFile : input.getFilenames()) {
				final File file = new File(inputFile.getFilename());
				if (!procAlternatives.contains(file.getName())) {
					execJob.addInput(new LevelJobInputDto(input.getFamily().name(), inputFile.getFilename(),
							inputFile.getKeyObjectStorage()));
				}
			}
		});
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
	
	private void updateSend(AppDataJob job, AppDataJobGenerationState newState) {
		if (job.getGeneration().getState() == newState) {
			// Before updating the state -> save last state
			job.getGeneration().setPreviousState(job.getGeneration().getState());
			
			// don't update jobs last modified date here to enable timeout, just update the generation time
			job.getGeneration().setLastUpdateDate(new Date());
			job.getGeneration().setNbErrors(job.getGeneration().getNbErrors()+1);
		}
		else {		
			// set the previous state to output state in order to wait before termination
			job.getGeneration().setPreviousState(newState);
			job.getGeneration().setState(newState);
			job.setLastUpdateDate(new Date());
		}
	}
}
