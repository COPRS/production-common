package esa.s1pdgs.cpoc.ipf.preparation.worker.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.metadata.SearchMetadataResult;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
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
	private final TaskTableAdapter taskTableAdapter;
	//TODO clarify if elementMapper or parts of it are mission specific
	private final ElementMapper elementMapper;
	private final Map<Integer, SearchMetadataQuery> queryTemplates;

	public AuxQuery(
			final MetadataClient metadataClient,
			final JobGen jobGen,
			final ProductMode mode,
			final InputTimeoutChecker timeoutChecker, ElementMapper elementMapper) {
		this.metadataClient = metadataClient;
		this.jobGen = jobGen;
		this.mode = mode;
		this.timeoutChecker = timeoutChecker;
		this.taskTableAdapter = jobGen.taskTableAdapter();
		this.elementMapper = elementMapper;
		queryTemplates = buildMetadataSearchQuery();
	}

	@Override
	public final JobGen call() throws Exception {	
		LOGGER.debug("Searching required AUX for job {} (product: {})", jobGen.id(), jobGen.productName());
		final Map<Integer, SearchMetadataResult> results = performAuxQueries();
		LOGGER.info("Distributing required AUX for job {} (product: {})", jobGen.id(), jobGen.productName());
		distributeResults(results);
		return jobGen;
	}

	//TODO check if some stuff from here can be put back again to TaskTableAdapter
	private Map<Integer, SearchMetadataQuery> buildMetadataSearchQuery() {
		final AtomicInteger counter = new AtomicInteger(0);
		final Map<Integer, SearchMetadataQuery> metadataQueryTemplate =  new HashMap<>();

		taskTableAdapter.allTaskTableInputs()
				.forEach((k, v) -> {
					final int queryId = counter.incrementAndGet();
					final String fileType = elementMapper.mappedFileType(k.getFileType());
					final ProductFamily family = elementMapper.inputFamilyOf(fileType);
					final SearchMetadataQuery query = new SearchMetadataQuery(
							queryId,
							k.getRetrievalMode(),
							k.getDeltaTime0(),
							k.getDeltaTime1(),
							fileType,
							family
					);
					metadataQueryTemplate.put(queryId, query);
					// FIXME: It's a very bad idea to rely on getting the original objects here provided and everything
					// to be written through properly. Furthermore, altering the taskTable after having it read is also
					// pretty error-prone and will eventually break at some point in the future (apart from all the WTFs
					// this will cause on encountering this logic)
					// Hence, this should be changed in order to create the queryId uniquely on the alternative once when
					// reading the taskTable and here, this value can be used as the query id (if no better structure for
					// storing the queries is found).
					// Apart from that, it's sad to see how functional programming is used to mutate the state of a
					// data structure that doesn't change but here... :(
					v.forEach(alt -> alt.setIdSearchMetadataQuery(queryId));
				});
		return metadataQueryTemplate;
	}

	private Map<Integer, SearchMetadataResult> toQueries(Map<Integer, SearchMetadataQuery> metadataQueriesTemplate) {
		final Map<Integer, SearchMetadataResult> queries = new HashMap<>(metadataQueriesTemplate.size());

		for (final Map.Entry<Integer, SearchMetadataQuery> entry : metadataQueriesTemplate.entrySet() ) {
			queries.put(entry.getKey(), new SearchMetadataResult(new SearchMetadataQuery(entry.getValue())));
		}

		return queries;
	}

	private Map<Integer, SearchMetadataResult> performAuxQueries() {
		final Map<Integer, SearchMetadataResult> queries = toQueries(queryTemplates);

		for (final SearchMetadataResult result : queries.values()) {
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

		return queries;
	}
	
	private void distributeResults(Map<Integer, SearchMetadataResult> metadataQueries) throws IpfPrepWorkerInputsMissingException {
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
							final JobOrderInput foundInput = taskTableAdapter.findInput(jobGen, input, metadataQueries);
														
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
	
	private List<SearchMetadata> queryAux(final SearchMetadataQuery query) throws MetadataQueryException {
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
	private String polarisationFor(final String productType) {
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
		
	private String sanitizeDateString(final String metadataFormat) {
		return DateUtils.convertToAnotherFormat(
				metadataFormat,
				AppDataJobProduct.TIME_FORMATTER,
				AbstractMetadata.METADATA_DATE_FORMATTER
		);
	}
}
