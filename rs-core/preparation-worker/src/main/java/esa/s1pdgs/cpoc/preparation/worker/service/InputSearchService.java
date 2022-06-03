package esa.s1pdgs.cpoc.preparation.worker.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.AppCatJobUpdateFailed;
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

	private ProductTypeAdapter typeAdapter;
	
	private AuxQueryHandler auxQueryHandler;
	
	public InputSearchService(final ProductTypeAdapter typeAdapter, final AuxQueryHandler auxQueryHandler) {
		this.typeAdapter = typeAdapter;
		this.auxQueryHandler = auxQueryHandler;
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
				try {
					typeAdapter.validateInputSearch(job, taskTableAdapter);
				} catch (final TimedOutException e) {
					timeout.set(true);
				}
			}, "validating availability of input products for " + job.getProductName());
			newState = AppDataJobGenerationState.PRIMARY_CHECK;
		} finally {
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
