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

package esa.s1pdgs.cpoc.preparation.worker.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.AppCatJobUpdateFailed;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.AppCatalogJobNotFoundException;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.DiscardedException;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.JobStateTransistionFailed;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.TimedOutException;
import esa.s1pdgs.cpoc.preparation.worker.model.generator.ThrowingRunnable;
import esa.s1pdgs.cpoc.preparation.worker.query.AuxQuery;
import esa.s1pdgs.cpoc.preparation.worker.query.AuxQueryHandler;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;

public class InputSearchService {

	static final Logger LOGGER = LogManager.getLogger(InputSearchService.class);

	private ProductTypeAdapter typeAdapter;

	private AuxQueryHandler auxQueryHandler;

	private Map<String, TaskTableAdapter> taskTableAdapters;

	private AppCatJobService appCatJobService;

	private JobCreationService jobCreationService;

	public InputSearchService(final ProductTypeAdapter typeAdapter, final AuxQueryHandler auxQueryHandler,
			final Map<String, TaskTableAdapter> taskTableAdapters, final AppCatJobService appCatJobService,
			final JobCreationService jobCreationService) {
		this.typeAdapter = typeAdapter;
		this.auxQueryHandler = auxQueryHandler;
		this.taskTableAdapters = taskTableAdapters;
		this.appCatJobService = appCatJobService;
		this.jobCreationService = jobCreationService;
	}

	public synchronized List<IpfExecutionJob> checkIfJobsAreReady(List<AppDataJob> appDataJobs) {

		List<IpfExecutionJob> executionJobs = new ArrayList<>();

		for (AppDataJob job : appDataJobs) {
			try {
				if (job.getGeneration().getState() == AppDataJobGenerationState.INITIAL) {
					try {
						LOGGER.info("Start main input search for AppDataJob {}", job.getId());
						job = mainInputSearch(job, taskTableAdapters.get(job.getTaskTableName()));
					} catch (JobStateTransistionFailed e) {
						LOGGER.info("Main input search did not complete successfully: {}", e.getMessage());
					} catch (DiscardedException e) {
						// Terminate Job
						LOGGER.info("Received signal to discard job {}: {}", job.getId(), e.getMessage());
						job.setState(AppDataJobState.TERMINATED);
						job.setTimeoutDate(null);
					}
				}
	
				if (job.getGeneration().getState() == AppDataJobGenerationState.PRIMARY_CHECK) {
					try {
						LOGGER.info("Start aux input search for AppDataJob {}", job.getId());
						job = auxInputSearch(job, taskTableAdapters.get(job.getTaskTableName()));
					} catch (JobStateTransistionFailed e) {
						LOGGER.info("Aux input search did not complete successfully: {}", e.getMessage());
					}
				}
	
				if (job.getGeneration().getState() == AppDataJobGenerationState.READY) {
					try {
						LOGGER.info("Start generating IpfExecutionJob for AppDataJob {}", job.getId());
	
						IpfExecutionJob executionJob = jobCreationService.createExecutionJob(job,
								taskTableAdapters.get(job.getTaskTableName()));
						if (executionJob != null) {
							executionJobs.add(executionJob);
						} else {
							// TODO: Improve Error Handling
							LOGGER.error("Could not generate ExecutionJob for AppDataJob {}", job.getId());
						}
					} catch (JobStateTransistionFailed e) {
						LOGGER.info("Generation of IpfExecutionJob did not complete successfully: {}", e.getMessage());
					}
				}

				// Update Job in Mongo
				appCatJobService.updateJob(job);
			} catch (AppCatalogJobNotFoundException e) {
				LOGGER.error("Error while saving new state of AppDataJob {}: {}", job.getId(), e.getMessage());
			} catch (Exception e) {
				LOGGER.error("An unexpected exception occured while processing AppDataJob {}: {}", job.getId(), e.getMessage());
			}
		}

		return executionJobs;
	}

	public AppDataJob mainInputSearch(AppDataJob job, TaskTableAdapter taskTableAdapter)
			throws JobStateTransistionFailed {
		AppDataJobGenerationState newState = job.getGeneration().getState();

		final AtomicBoolean timeout = new AtomicBoolean(false);
		Product queried = null;
		try {
			queried = perform(() -> typeAdapter.mainInputSearch(job, taskTableAdapter),
					"querying input " + job.getProductName());
			job.setProduct(queried.toProduct());
			job.setAdditionalInputs(queried.overridingInputs());
			// FIXME dirty workaround warning, the product above is still altered in
			// validate by modifying
			// the start stop time for segments
			performVoid(() -> {
				// When job is already timed out -> skip validation
				if (!job.getTimedOut()) {
					try {
						typeAdapter.validateInputSearch(job, taskTableAdapter);
					} catch (final TimedOutException e) {
						timeout.set(true);
					}
				}
			}, "validating availability of input products for " + job.getProductName());
			newState = AppDataJobGenerationState.PRIMARY_CHECK;
		} finally {
			// The mainInputSearch may change the timeout value -> update it here for future
			// house keeping
			if (newState != AppDataJobGenerationState.PRIMARY_CHECK) {
				typeAdapter.updateTimeout(job, taskTableAdapter);
			} else {
				// Set timeoutDate to null -> new timeout (if necessary for aux)
				job.setTimeoutDate(null);
				job.setTimedOut(false);
			}
			updateJobMainInputSearch(job, queried, newState);
		}

		return job;
	}

	public AppDataJob auxInputSearch(AppDataJob job, TaskTableAdapter taskTableAdapter)
			throws JobStateTransistionFailed {
		AppDataJobGenerationState newState = job.getGeneration().getState();
		final AuxQuery auxQuery = auxQueryHandler.queryFor(job, taskTableAdapter);

		List<AppDataJobTaskInputs> queried = Collections.emptyList();

		try {
			queried = perform(() -> auxQuery.queryAux(), "querying required AUX");
			job.setAdditionalInputs(queried);
			performVoid(() -> auxQuery.validate(job), "validating availability of AUX for " + job.getProductName());
			newState = AppDataJobGenerationState.READY;
		} finally {
			if (newState != AppDataJobGenerationState.READY) {
				auxQuery.updateTimeout(job, taskTableAdapter);
			}

			updateJobAuxSearch(job, queried, newState);
		}

		return job;
	}

	private final void performVoid(final ThrowingRunnable command, final String name) throws JobStateTransistionFailed {
		perform(() -> {
			command.run();
			return null;
		}, name);
	}

	private final <E> E perform(final Callable<E> command, final String name) throws JobStateTransistionFailed {
		try {
			return command.call();
		}
		// expected on failed transition
		catch (final IpfPrepWorkerInputsMissingException e) {
			// TODO once there is some time for refactoring, cleanup the created error
			// message of
			// IpfPrepWorkerInputsMissingException to be more descriptive
			throw new JobStateTransistionFailed(e.getLogMessage());
		}
		// expected on updating AppDataJob in persistence -> simply retry next time
		catch (final AppCatJobUpdateFailed e) {
			throw new JobStateTransistionFailed(
					String.format("Error on persisting change of '%s': %s", name, Exceptions.messageOf(e)), e);
		}
		// expected on discard scenarios -> terminate job
		catch (final DiscardedException e) {
			throw e;
		} catch (final Exception e) {
			throw new RuntimeException(String.format("Fatal error on %s: %s", name, Exceptions.messageOf(e)), e);
		}
	}

	private void updateJobMainInputSearch(AppDataJob job, Product queried, AppDataJobGenerationState newState) {
		if (queried != null) {
			final AppDataJobProduct prod = queried.toProduct();
			job.setProduct(prod);
			job.setAdditionalInputs(queried.overridingInputs());
			job.setPreselectedInputs(queried.preselectedInputs());

			// dirty workaround for segment and session scenario
			final AppDataJobProductAdapter productAdapter = new AppDataJobProductAdapter(prod);
			job.setStartTime(productAdapter.getStartTime());
			job.setStopTime(productAdapter.getStopTime());
		}

		// Before updating the state -> save last state
		job.getGeneration().setPreviousState(job.getGeneration().getState());

		// no transition?
		if (job.getGeneration().getState() == newState) {
			// don't update jobs last modified date here to enable timeout, just update the
			// generations
			// last update time
			job.getGeneration().setLastUpdateDate(new Date());
			job.getGeneration().setNbErrors(job.getGeneration().getNbErrors() + 1);
		} else {
			job.getGeneration().setState(newState);
			job.setLastUpdateDate(new Date());
		}
	}

	private void updateJobAuxSearch(AppDataJob job, List<AppDataJobTaskInputs> queried,
			AppDataJobGenerationState newState) {
		if (!queried.isEmpty()) {
			job.setAdditionalInputs(queried);
		}

		// Before updating the state -> save last state
		job.getGeneration().setPreviousState(job.getGeneration().getState());

		// no transition?
		if (job.getGeneration().getState() == newState) {
			// don't update jobs last modified date here to enable timeout, just update the
			// generation time
			job.getGeneration().setLastUpdateDate(new Date());
			job.getGeneration().setNbErrors(job.getGeneration().getNbErrors() + 1);
		} else {
			job.getGeneration().setState(newState);
			job.setLastUpdateDate(new Date());
		}
	}

}
