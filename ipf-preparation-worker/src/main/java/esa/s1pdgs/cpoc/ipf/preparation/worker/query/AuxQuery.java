package esa.s1pdgs.cpoc.ipf.preparation.worker.query;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.metadata.SearchMetadataResult;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;
import esa.s1pdgs.cpoc.metadata.model.AbstractMetadata;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderTimeInterval;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTablePool;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableMandatoryEnum;

public class AuxQuery implements Callable<JobGen> {
	private static final Logger LOGGER = LogManager.getLogger(AuxQuery.class);
	
	private final MetadataClient metadataClient;
	private final JobGen jobGen;
	private final ProductMode mode;
	private final InputTimeoutChecker timeoutChecker;
	private final TaskTableAdapter taskTableAdapter;
	private final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataQuery> queryTemplates;

	public AuxQuery(
			final MetadataClient metadataClient,
			final JobGen jobGen,
			final ProductMode mode,
			final InputTimeoutChecker timeoutChecker,
			final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataQuery> queryTemplates) {
		this.metadataClient = metadataClient;
		this.jobGen = jobGen;
		this.mode = mode;
		this.timeoutChecker = timeoutChecker;
		this.taskTableAdapter = jobGen.taskTableAdapter();
		this.queryTemplates = queryTemplates;
	}

	@Override
	public final JobGen call() throws Exception {	
		LOGGER.debug("Searching required AUX for job {} (product: {})", jobGen.id(), jobGen.productName());
		final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataResult> results = performAuxQueries();
		LOGGER.info("Distributing required AUX for job {} (product: {})", jobGen.id(), jobGen.productName());
		jobGen.job().setAdditionalInputs(distributeResults(results));
		return jobGen;
	}

	private Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataResult> toQueries(final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataQuery> metadataQueriesTemplate) {
		return metadataQueriesTemplate.entrySet().stream().collect(
				toMap(
						Map.Entry::getKey,
						e -> new SearchMetadataResult(new SearchMetadataQuery(e.getValue())))
		);
	}

	private Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataResult> performAuxQueries() {
		final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataResult> queries = toQueries(queryTemplates);

		for (final SearchMetadataResult result : queries.values()) {
			if (result.hasResult()) {
				continue;
			}

			final SearchMetadataQuery query = result.getQuery();
			try {				
				LOGGER.debug("Querying input product of type {}, AppJobId {}: {}", 
						query.getProductType(), jobGen.id(), query);

				final List<SearchMetadata> results = queryAux(query);
				// save query results
				// this means, only if query has found something, the result is set
				// otherwise query again later
				// so the same behaviour can be achieved by simply passing result and change getResult()
				// to check null and isEmpty()
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
	
	private List<AppDataJobTaskInputs> distributeResults(final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataResult> metadataQueries) throws IpfPrepWorkerInputsMissingException {
		final Map<String, AppDataJobInput> referenceInputs = new HashMap<>();
		final List<AppDataJobTaskInputs> result = new ArrayList<>();
		//for each pool
		for (final TaskTablePool pool : jobGen.taskTableAdapter().pools()) {
			// and each task in this pool
			for (final TaskTableTask task : pool.getTasks()) {
				final Map<String, String> missingMetadata = new HashMap<>();
				final List<AppDataJobInput> futureInputs = new ArrayList<>();
				//take each input
				for (final TaskTableInput input : task.getInputs()) {					
					// If it is NOT a reference
					if (StringUtils.isEmpty(input.getReference())) {
						//check if input mode matches the product mode
						if (mode.isCompatibleWithTaskTableMode(input.getMode())) {
							// returns null, if not found
							final AppDataJobInput foundInput = convert(taskTableAdapter.findInput(jobGen, input, metadataQueries));
														
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
							futureInputs.add(new AppDataJobInput(referenceInputs.get(input.getReference())));
						}
					}
				}
				if (missingMetadata.isEmpty()) {
					result.add(new AppDataJobTaskInputs(task.getName(), task.getVersion(), futureInputs));
				} else {
					throw new IpfPrepWorkerInputsMissingException(missingMetadata);
				}
			}
		}
		return result;
	}

	// TODO TaskTableAdapter by itself should not return a JobOrderInput but a more generic
	// structure which solely holds the reference between task table input and search meta data result
	// as long as this is not changed the JobOrderInput has to be converted to AppDataJobInput here
	private AppDataJobInput convert(final JobOrderInput input) {
		if(input == null) {
			return null;
		}

		//TODO there is not check if fileNames and intervals are consistent
		final Map<String, JobOrderInputFile> fileNames = input.getFilenames().stream().collect(toMap(JobOrderInputFile::getFilename, fn -> fn));
		return new AppDataJobInput(input.getFileType(), input.getFileNameType().toString(),
				input.getTimeIntervals().stream().map(ti -> merge(fileNames.get(ti.getFileName()), ti)).collect(toList()));
	}

	private AppDataJobFile merge(final JobOrderInputFile file, final JobOrderTimeInterval interval) {
		return new AppDataJobFile(
				file.getFilename(), file.getKeyObjectStorage(),
				interval.getStart(), interval.getStop());
	}

	private List<SearchMetadata> queryAux(final SearchMetadataQuery query) throws MetadataQueryException {
		final AppDataJob job = jobGen.job();
		final AppDataJobProductAdapter productAdapter = new AppDataJobProductAdapter(job.getProduct());
		
		return metadataClient.search(
				query,
				sanitizeDateString(job.getStartTime()),
				sanitizeDateString(job.getStopTime()),
				productAdapter.getSatelliteId(),
				productAdapter.getInsConfId(),
				productAdapter.getProcessMode(), 
				polarisationFor(query.getProductType())
		);
	}

	
	// S1PRO-707: only "AUX_ECE" requires to query polarisation
	private String polarisationFor(final String productType) {
		if ("AUX_ECE".equals(productType.toUpperCase())) {
			final AppDataJobProductAdapter productAdapter = new AppDataJobProductAdapter(jobGen.job().getProduct());		
			
			final String polarisation = productAdapter.getStringValue("polarisation", "NONE").toUpperCase();
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
