package esa.s1pdgs.cpoc.ipf.preparation.worker.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.metadata.SearchMetadataResult;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTablePool;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums.TaskTableMandatoryEnum;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;
import esa.s1pdgs.cpoc.metadata.model.AbstractMetadata;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;

public class AuxQuery implements Callable<JobGen> {
	private static final Logger LOGGER = LogManager.getLogger(AuxQuery.class);
	
	private final MetadataClient metadataClient;
	private final JobGen jobGen;
	private final ProductMode mode;
	private final InputTimeoutChecker timeoutChecker;
	
	public AuxQuery(
			final MetadataClient metadataClient, 
			final JobGen jobGen, 
			final ProductMode mode, 
			final InputTimeoutChecker timeoutChecker
	) {
		this.metadataClient = metadataClient;
		this.jobGen = jobGen;
		this.mode = mode;
		this.timeoutChecker = timeoutChecker;
	}

	@Override
	public final JobGen call() throws Exception {	
		LOGGER.debug("Searching required AUX for job {} (product: {})", jobGen.id(), jobGen.productName());
		performAuxQueries();
		LOGGER.info("Distributing required AUX for job {} (product: {})", jobGen.id(), jobGen.productName());
		distributeResults();	
		return jobGen;
	}

	private final void performAuxQueries() {	
		for (final SearchMetadataResult result : jobGen.queries()) {		
			// TODO make this prettier
			if (result == null || result.getResult() != null) {
				continue;
			}
			final SearchMetadataQuery query = result.getQuery();
			try {				
				LOGGER.debug("Querying input product of type {}, AppJobId {}: {}", 
						query.getProductType(), jobGen.id(), query);

				final List<SearchMetadata> results = queryAux(query);
				// save query results
				if (!results.isEmpty()) {
					result.setResult(results);
				}
			} catch (final MetadataQueryException me) {
				LOGGER.warn("Exception occurred when searching alternative {} for job {} with product {}: {}",
						query.toLogMessage(),
						jobGen.id(), 
						jobGen.productName(), 
						me.getMessage()
				);
			}
		}
	}
	
	private void distributeResults() throws IpfPrepWorkerInputsMissingException {
		int counterProc = 0;
		final Map<String, JobOrderInput> referenceInputs = new HashMap<>();
		for (final TaskTablePool pool : jobGen.taskTableAdapter().pools()) {
			for (final TaskTableTask task : pool.getTasks()) {
				final Map<String, String> missingMetadata = new HashMap<>();
				final List<JobOrderInput> futureInputs = new ArrayList<>();
				for (final TaskTableInput input : task.getInputs()) {					
					// If it is NOT a reference
					if (StringUtils.isEmpty(input.getReference())) {
						if (ProductMode.isCompatibleWithTaskTableMode(mode, input.getMode())) {			
							// returns null, if not found
							final JobOrderInput foundInput = jobGen.taskTableAdapter().findInput(jobGen, input);
														
							if (foundInput != null) {
								futureInputs.add(foundInput);
								if (!StringUtils.isEmpty(input.getId())) {
									referenceInputs.put(input.getId(), foundInput);
								}
							} else {
								// nothing found in none of the alternatives
								if (input.getMandatory() == TaskTableMandatoryEnum.YES) {
									missingMetadata.put(input.toLogMessage(), "");
								} else {			
									// optional input
									
									// if the timeout is not expired, we want to continue waiting. To do that, 
									// a IpfPrepWorkerInputsMissingException needs to be thrown. Otherwise,
									// we log that timeout is expired and we continue anyway behaving as if 
									// the input was there
									if (timeoutChecker.isTimeoutExpiredFor(jobGen.job(), input)) {
										LOGGER.info("Non-Mandatory Input {} is not available. Continue without it...", 
												input.toLogMessage());
									}
									else {
										throw new IpfPrepWorkerInputsMissingException(missingMetadata); 
									}
								}
							}
						}
					// handle Input 'references'
					} else {
						// We shall add inputs of the reference
						if (referenceInputs.containsKey(input.getReference())) {
							futureInputs.add(new JobOrderInput(referenceInputs.get(input.getReference())));
						}
					}
				}
				counterProc++;
				if (missingMetadata.isEmpty()) {
					jobGen.jobOrder().getProcs().get(counterProc - 1).setInputs(futureInputs);
				} else {
					throw new IpfPrepWorkerInputsMissingException(missingMetadata);
				}
			}
		}
	}
	
	private final List<SearchMetadata> queryAux(final SearchMetadataQuery query) throws MetadataQueryException {		
		final AppDataJob job = jobGen.job();
		return metadataClient.search(
				query,
				sanitizeDateString(job.getProduct().getStartTime()),
				sanitizeDateString(job.getProduct().getStopTime()),
				job.getProduct().getSatelliteId(),
				job.getProduct().getInsConfId(),
				job.getProduct().getProcessMode(), 
				polarisationFor(query.getProductType())
		);
	}

	
	// S1PRO-707: only "AUX_ECE" requires to query polarisation
	private final String polarisationFor(final String productType) {
		if ("AUX_ECE".equals(productType.toUpperCase())) {
			final String polarisation = jobGen.job().getProduct().getPolarisation().toUpperCase();
			if (polarisation.equals("SV") || polarisation.equals("DV")) {
				return "V";
			} else if (polarisation.equals("SH") || polarisation.equals("DH")) {
				return "H";
			}
			return "NONE";
		}
		return null;
	}
		
	private final String sanitizeDateString(final String metadataFormat) {
		return DateUtils.convertToAnotherFormat(
				metadataFormat,
				AppDataJobProduct.TIME_FORMATTER,
				AbstractMetadata.METADATA_DATE_FORMATTER
		);
	}
}
