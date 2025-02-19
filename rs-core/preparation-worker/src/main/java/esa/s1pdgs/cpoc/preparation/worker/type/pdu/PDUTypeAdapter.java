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

package esa.s1pdgs.cpoc.preparation.worker.type.pdu;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.time.TimeInterval;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.S3Metadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.preparation.worker.config.PreparationWorkerProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.type.PDUProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.type.PDUProperties.PDUTypeProperties;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.DiscardedException;
import esa.s1pdgs.cpoc.preparation.worker.model.pdu.PDUReferencePoint;
import esa.s1pdgs.cpoc.preparation.worker.model.pdu.PDUType;
import esa.s1pdgs.cpoc.preparation.worker.service.AppCatJobService;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.ElementMapper;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.preparation.worker.type.pdu.generator.PDUGenerator;
import esa.s1pdgs.cpoc.preparation.worker.type.s3.MultipleProductCoverSearch;
import esa.s1pdgs.cpoc.preparation.worker.type.s3.gap.ThresholdGapHandler;
import esa.s1pdgs.cpoc.preparation.worker.util.QueryUtils;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableMandatoryEnum;

public class PDUTypeAdapter extends AbstractProductTypeAdapter {

	private static final Logger LOGGER = LogManager.getLogger(PDUTypeAdapter.class);

	private MetadataClient metadataClient;
	private ElementMapper elementMapper;
	private PreparationWorkerProperties workerSettings;
	private ProcessProperties processSettings;
	private PDUProperties settings;

	public PDUTypeAdapter(final MetadataClient metadataClient, final ElementMapper elementMapper,
			final PreparationWorkerProperties workerSettings, final ProcessProperties processSettings,
			final PDUProperties settings) {
		this.metadataClient = metadataClient;
		this.elementMapper = elementMapper;
		this.workerSettings = workerSettings;
		this.processSettings = processSettings;
		this.settings = settings;
	}

	@Override
	public List<AppDataJob> createAppDataJobs(IpfPreparationJob job) throws Exception {
		PDUTypeProperties typeSettings = settings.getConfig().get(job.getCatalogEvent().getMetadataProductType());

		if (typeSettings != null) {
			PDUGenerator jobGenerator = PDUGenerator.getPDUGenerator(processSettings, typeSettings, metadataClient);
			if (jobGenerator != null) {
				return jobGenerator.generateAppDataJobs(job, workerSettings.getPrimaryCheckMaxTimelifeS());
			}
		}

		return Collections.emptyList();
	}

	@Override
	public void customJobOrder(AppDataJob job, JobOrder jobOrder) {
		PDUTypeProperties typeSettings = settings.getConfig()
				.get(job.getPrepJob().getCatalogEvent().getMetadataProductType());
		// Add PDUTimeIntervals for FRAME and STRIPE PDUs
		if (typeSettings != null && typeSettings.getType() != PDUType.TILE) {
			String timeIntervals = (String) job.getProduct().getMetadata().get(PDUProduct.PDU_TIME_INTERVALS);
			if (timeIntervals == null) {
				timeIntervals = "[" + DateUtils.convertToPDUDateTimeFormat(job.getStartTime()) + ","
						+ DateUtils.convertToPDUDateTimeFormat(job.getStopTime()) + "]";
			}

			// Add FrameNumber if it exists in metadata
			String frameNumber = (String) job.getProduct().getMetadata().get(PDUProduct.FRAME_NUMBER);
			if (frameNumber != null) {
				// Count number of time intervals
				int intervalCount = StringUtils.countMatches(timeIntervals, "];[") + 1;

				updateProcParam(jobOrder, "MtdPDUFrameNumbers", StringUtils.repeat(frameNumber, ";", intervalCount));
			}

			updateProcParam(jobOrder, "PDUTimeIntervals", timeIntervals);
		}

		// Add other dynamic processing parameters
		if (typeSettings != null && !typeSettings.getDynProcParams().isEmpty()) {
			for (Entry<String, String> entry : typeSettings.getDynProcParams().entrySet()) {
				updateProcParam(jobOrder, entry.getKey(), entry.getValue());
			}
		}

		// RS-981: PUG update introduced new process parameter "pduType"
		updateProcParam(jobOrder, "pduType", typeSettings.getType().toString().toLowerCase());

		// Update timeliness
		updateProcParam(jobOrder, "orderType", workerSettings.getProductMode().toString());
	}

	@Override
	public void customJobDto(AppDataJob job, IpfExecutionJob dto) {
		// Nothing to do currently
	}

	@Override
	public Optional<AppDataJob> findAssociatedJobFor(AppCatJobService appCat, CatalogEventAdapter catEvent,
			AppDataJob job) throws AbstractCodedException {
		PDUTypeProperties typeSettings = settings.getConfig().get(catEvent.productType());

		if (typeSettings != null) {

			// For DUMP based STRIPE PDUs check if there already is a job for this interval
			if (typeSettings.getType() == PDUType.STRIPE && typeSettings.getReference() == PDUReferencePoint.DUMP) {
				List<AppDataJob> productTypeJobs = appCat.findByProductType(catEvent.productType());

				if (productTypeJobs != null && !productTypeJobs.isEmpty()) {
					for (AppDataJob databaseJob : productTypeJobs) {
						// Only check start time, because the stop time may have been adjusted already
						if (databaseJob.getTaskTableName().equals(job.getTaskTableName())
								&& databaseJob.getStartTime().equals(job.getStartTime())) {
							return Optional.of(databaseJob);
						}
					}
				}
			}
		}

		return Optional.empty();
	}

	@Override
	public Product mainInputSearch(AppDataJob job, TaskTableAdapter tasktableAdapter)
			throws IpfPrepWorkerInputsMissingException, DiscardedException {
		final PDUProduct returnValue = PDUProduct.of(job);

		// Get inputs of tasks
		List<AppDataJobTaskInputs> tasks;
		if (isEmpty(job.getAdditionalInputs())) {
			tasks = QueryUtils.buildInitialInputs(tasktableAdapter);
		} else {
			tasks = job.getAdditionalInputs();
		}

		// Extract a list of all inputs without complete result sets
		final List<AppDataJobInput> inputsWithNoResults = tasks.stream()
				.flatMap(taskInputs -> taskInputs.getInputs().stream().filter(input -> !input.getHasResults()))
				.collect(toList());

		// Create list of alternatives of those inputs
		final List<TaskTableInputAlternative> alternatives = QueryUtils.alternativesOf(inputsWithNoResults,
				tasktableAdapter, workerSettings.getProductMode());

		// Loop over alternatives to execute additional logic per product type
		for (final TaskTableInputAlternative alternative : alternatives) {
			try {
				PDUTypeProperties typeSettings = settings.getConfig().get(alternative.getFileType());

				// Handle mainInputSearch for FRAME and STRIPE PDUs
				if (typeSettings != null && typeSettings.getType() != PDUType.TILE) {
					LOGGER.debug("Use additional logic 'MultipleProductCoverSearch (PDU)' for product type {}",
							alternative.getFileType());
					final MultipleProductCoverSearch mpcSearch = new MultipleProductCoverSearch(tasktableAdapter,
							elementMapper, metadataClient, workerSettings);
					tasks = mpcSearch.updateTaskInputs(tasks, alternative, returnValue.getSatelliteId(),
							job.getStartTime(), job.getStopTime());

					/*
					 * In a following step the start and stop time of the job will be set to the
					 * start and stop time of the product. That step is ruining the
					 * RangeSearch-logic so we anticipate that update and preemptively set the
					 * products start and stop time to the ones from the job
					 * 
					 * @see AppDataJobService#updateProduct()
					 */
					returnValue.setStartTime(job.getStartTime());
					returnValue.setStopTime(job.getStopTime());

					// For dump based STRIPEs we have to adjust the stop time to the validityStop of
					// the LAST granule (if that one is inside this interval)
					if (typeSettings.getType() == PDUType.STRIPE
							&& typeSettings.getReference() == PDUReferencePoint.DUMP) {
						// Check if products contain Granule with position LAST, if so update stop time
						// of interval
						List<S3Metadata> products = metadataClient.getProductsInRange(alternative.getFileType(),
								elementMapper.inputFamilyOf(alternative.getFileType()), returnValue.getSatelliteId(),
								job.getStartTime(), job.getStopTime(), 0.0, 0.0);

						for (S3Metadata product : products) {
							if (product.getGranulePosition().equals("LAST")) {
								LOGGER.debug("Update job stop time to {}", product.getValidityStop());
								returnValue.setStopTime(product.getValidityStop());

								// Update tasks again
								tasks = mpcSearch.updateTaskInputs(tasks, alternative, returnValue.getSatelliteId(),
										job.getStartTime(), product.getValidityStop());
								break;
							}
						}
					}
				}

				// Handle mainInputSearch for TILE PDUs
				if (typeSettings != null && typeSettings.getType() == PDUType.TILE) {
					S3Metadata s3Metadata = metadataClient.getS3MetadataForProduct(
							elementMapper.inputFamilyOf(alternative.getFileType()), job.getProductName());

					final Map<String, TaskTableInput> taskTableInputs = QueryUtils
							.taskTableTasksAndInputsMappedTo((list, task) -> list, Collections::singletonMap,
									tasktableAdapter)
							.stream().flatMap(Collection::stream).flatMap(map -> map.entrySet().stream())
							.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

					for (final AppDataJobTaskInputs task : tasks) {
						for (final AppDataJobInput input : task.getInputs()) {
							final TaskTableInput ttInput = taskTableInputs.get(input.getTaskTableInputReference());
							if (ttInput.getAlternatives().contains(alternative)) {
								input.setHasResults(true);
								input.setFileNameType(alternative.getFileNameType().toString());
								input.setFileType(alternative.getFileType());
								input.setMandatory(TaskTableMandatoryEnum.YES.equals(ttInput.getMandatory()));

								// Extract t0PdgsDate if possible to determine when all inputs where ready
								Date t0 = null;
								if (s3Metadata.getAdditionalProperties().containsKey("t0PdgsDate")) {
									t0 = DateUtils.toDate(s3Metadata.getAdditionalProperties().get("t0PdgsDate"));
								}

								AppDataJobFile file = new AppDataJobFile(s3Metadata.getProductName(),
										s3Metadata.getKeyObjectStorage(), s3Metadata.getValidityStart(),
										s3Metadata.getValidityStop(), t0);

								input.setFiles(Collections.singletonList(file));
							}
						}
					}
				}

			} catch (final MetadataQueryException me) {
				LOGGER.error("Error on query execution, retrying next time", me);
			}
		}

		returnValue.setAdditionalInputs(tasks);
		return returnValue;
	}

	@Override
	public void validateInputSearch(final AppDataJob job, final TaskTableAdapter taskTableAdapter)
			throws IpfPrepWorkerInputsMissingException {
		// Check if timeout is reached -> start job with current input
		if (workerSettings.getPrimaryCheckMaxTimelifeS() != 0) {
			final long startTime = job.getGeneration().getCreationDate().toInstant().toEpochMilli();
			final long timeoutTime = startTime + (workerSettings.getPrimaryCheckMaxTimelifeS() * 1000);

			if (Instant.now().toEpochMilli() > timeoutTime) {
				// Timeout reached
				LOGGER.info("Timeout reached for job {}. Continue without missing products...", job.getId());
				handleTimeout(job, taskTableAdapter);
				setHasResultsToTrueForTimeoutReached(job);
				return;
			}
		}

		// Extract a list of all inputs from the tasks
		final List<AppDataJobInput> inputsWithNoResults = job.getAdditionalInputs().stream()
				.flatMap(taskInputs -> taskInputs.getInputs().stream().filter(input -> !input.getHasResults()))
				.collect(toList());

		// Create list of alternatives
		final List<TaskTableInputAlternative> alternatives = QueryUtils.alternativesOf(inputsWithNoResults,
				taskTableAdapter, workerSettings.getProductMode());

		// Check if there is an alternative which should have been filled by additional
		// logic
		final Map<String, String> missingAlternatives = new HashMap<>();
		for (final TaskTableInputAlternative alternative : alternatives) {
			PDUTypeProperties typeSettings = settings.getConfig().get(alternative.getFileType());

			if (typeSettings != null) {
				missingAlternatives.put(alternative.getFileType(),
						"Incomplete result set for " + alternative.getFileType());
			}
		}

		// If there is at least one input incomplete, try again
		if (!missingAlternatives.isEmpty()) {
			throw new IpfPrepWorkerInputsMissingException(missingAlternatives);
		}
	}

	/**
	 * When the timeout was reached set the boolean "hasResults" for all input to
	 * true, that contain at least one product. The boolean "hasResults" is kept at
	 * false from the additional logic, to determine whether or not the list of
	 * products is enough to satisfy the additional constraints
	 * 
	 * @param job job to update
	 */
	private void setHasResultsToTrueForTimeoutReached(final AppDataJob job) {
		for (final AppDataJobTaskInputs task : job.getAdditionalInputs()) {
			for (final AppDataJobInput input : task.getInputs()) {
				if (!isEmpty(input.getFiles())) {
					input.setHasResults(true);
				}
			}
		}
	}

	/**
	 * Handle Timeout for PDU Generation
	 * 
	 * Determine if the PDU product has files attached. If no file is attached, drop
	 * job all together, otherwise construct correct time intervals
	 */
	private void handleTimeout(AppDataJob job, TaskTableAdapter ttAdapter) {
		// Extract a list of all inputs from the tasks
		final List<AppDataJobInput> incompleteInputs = job.getAdditionalInputs().stream()
				.flatMap(taskInputs -> taskInputs.getInputs().stream().filter(input -> !input.getHasResults()))
				.collect(toList());

		// Determine PDU Input
		AppDataJobInput pduInput = null;
		PDUTypeProperties typeSettings = null;
		for (AppDataJobInput input : incompleteInputs) {
			List<TaskTableInputAlternative> alternatives = QueryUtils.alternativesOf(Collections.singletonList(input),
					ttAdapter, workerSettings.getProductMode());

			for (TaskTableInputAlternative alternative : alternatives) {
				typeSettings = settings.getConfig().get(alternative.getFileType());
				if (typeSettings != null) {
					pduInput = input;
					break;
				}
			}
			if (pduInput != null) {
				break;
			}
		}

		// Calculate new TimeIntervals for PUG
		if (pduInput != null) {
			if (!pduInput.getFiles().isEmpty()) {
				List<TimeInterval> intervals = new ArrayList<>();
				for (AppDataJobFile file : pduInput.getFiles()) {
					intervals.add(
							new TimeInterval(DateUtils.parse(file.getStartDate()), DateUtils.parse(file.getEndDate())));
				}
				TimeInterval jobInterval = new TimeInterval(DateUtils.parse(job.getStartTime()),
						DateUtils.parse(job.getStopTime()));

				ThresholdGapHandler gapHandler = new ThresholdGapHandler(typeSettings.getGapThreshholdInS());
				intervals = gapHandler.mergeTimeIntervals(jobInterval, intervals);
				
				// Check if the timeout created malformed time intervals and drop them if necessary
				if (typeSettings.getMinPDULengthThreshold() > 0.0) {
					List<TimeInterval> correctedIntervals = new ArrayList<>();
					for (int i = 0; i < intervals.size(); i++) {
						// Too small intervals get dropped
						if (intervals.get(i).lengthInNanos() >= (long) (typeSettings.getMinPDULengthThreshold() * 1000000000L)) {
							correctedIntervals.add(intervals.get(i));
						}
					}
					intervals = correctedIntervals;
				}

				String pduTimeIntervals = intervals.stream()
						.map(i -> "[" + DateUtils.formatToPDUDateTimeFormat(i.getStart()) + ","
								+ DateUtils.formatToPDUDateTimeFormat(i.getStop()) + "]")
						.collect(Collectors.joining(";"));

				LOGGER.info("Adjust PDUTimeIntervals to {}", pduTimeIntervals);
				job.getProduct().getMetadata().put(PDUProduct.PDU_TIME_INTERVALS, pduTimeIntervals);

				String newStartTime = DateUtils.formatToMetadataDateTimeFormat(intervals.get(0).getStart());
				String newStopTime = DateUtils
						.formatToMetadataDateTimeFormat(intervals.get(intervals.size() - 1).getStop());

				LOGGER.debug("Adjust job time to [{},{}]", newStartTime, newStopTime);
				job.getProduct().getMetadata().put("startTime", newStartTime);
				job.getProduct().getMetadata().put("stopTime", newStopTime);
			} else {
				// No files at all - terminate job
				throw new DiscardedException("No files for PDU generation");
			}
		}
	}
}
