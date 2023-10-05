package esa.s1pdgs.cpoc.ingestion.worker.product;

import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.CadipInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapterEntry;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapterResponse;
import esa.s1pdgs.cpoc.ingestion.worker.obs.ObsAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class ProductServiceImpl implements ProductService {

	private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

	private final ObsClient obsClient;
	private final boolean bufferInput;
	private final AppStatus appStatus;

	public ProductServiceImpl(final ObsClient obsClient, final boolean bufferInput, AppStatus appStatus) {
		this.obsClient = obsClient;
		this.bufferInput = bufferInput;
		this.appStatus = appStatus;
	}

	@Override
	public List<Product<CatalogJob>> ingest(
			final ProductFamily family, 
			final InboxAdapter inboxAdapter,
			final IngestionJob ingestion,
			final ReportingFactory reportingFactory
	) throws Exception {		
		final URI uri = IngestionJobs.toUri(ingestion);
		final ObsAdapter obsAdapter = newObsAdapterFor(reportingFactory);
		final String obsKey = obsKeyFor(ingestion);
		final String storagePath = storagePathFor(obsAdapter, family, obsKey);
		
		final CatalogJob dto = new CatalogJob(
				family, 
				obsKey,
				storagePath,
				ingestion.getRelativePath(),
				ingestion.getProductSizeByte(),
				ingestion.getMissionId(),
				ingestion.getStationName(),
				ingestion.getMode(),
				ingestion.getTimeliness()
		);
		
		// RS-248: We are using the t0PdgsDate from the ingestion event
		dto.getAdditionalFields().put("t0PdgsDate", (String) ingestion.getAdditionalFields().get("t0PdgsDate"));

		// S1OPS-971: This is a workaround for MDC to allow access of additional metadata
		if (null != ingestion.getAdditionalMetadata() && !ingestion.getAdditionalMetadata().isEmpty()) {
			dto.getMetadata().putAll(ingestion.getAdditionalMetadata());
			dto.getMetadata().put(CatalogJob.ADDITIONAL_METADATA_FLAG_KEY, true);
		}
		
		checkExistingInObs(obsAdapter, ingestion);
		List<InboxAdapterEntry> entries = upload(obsAdapter, ingestion, family, inboxAdapter, uri, obsKey);

		// Special case CADIP: We sometimes create a DSIB as well and have to create two messages
		if (inboxAdapter instanceof CadipInboxAdapter && entries.size() > 1) {
			final CatalogJob dtoDSIB = new CatalogJob(
					family, 
					entries.get(1).key(),
					storagePathFor(obsAdapter, family, entries.get(1).key()),
					entries.get(1).key(),
					entries.get(1).size(),
					ingestion.getMissionId(),
					ingestion.getStationName(),
					ingestion.getMode(),
					ingestion.getTimeliness()
			);
			
			return Arrays.asList(new Product<>(family, uri, dto), new Product<>(family, uri, dtoDSIB));
		}
		
		return Collections.singletonList(new Product<>(family, uri, dto));
	}

	private List<InboxAdapterEntry> upload(ObsAdapter obsAdapter, IngestionJob ingestion, ProductFamily family, InboxAdapter inboxAdapter, URI uri, String obsKey) throws Exception {
		try (final InboxAdapterResponse response = inboxAdapter.read(
				uri, ingestion.getProductName(), ingestion.getRelativePath(), ingestion.getProductSizeByte())) {
			obsAdapter.upload(
					family,
					response.getResult(),
					obsKey
			);
			
			return response.getResult();
		}
	}

	private void checkExistingInObs(final ObsAdapter obsAdapter, final IngestionJob ingestion) {
		final String obsKey = obsKeyFor(ingestion);

		//returns -1 if it is not in OBS
		long obsSize = obsAdapter.sizeOf(ingestion.getProductFamily(), obsKey);

		if(obsSize < 0) {
			LOG.debug("File {} is not in obs and will be ingested", obsKey);
			return;
		}

		LOG.debug("checking obsSize of {} in obs: {} against new size {}", obsKey, obsSize, ingestion.getProductSizeByte());

		if (obsSize == ingestion.getProductSizeByte()) {
			throw new RuntimeException(
					String.format("File %s is already in obs and has the same size, aborting ingestion", obsKey));
		}

		LOG.info("File {} has new size {}, will overwrite existing one with size {}", obsKey, ingestion.getProductSizeByte(), obsSize);
	}

	private String obsKeyFor(final IngestionJob ingestion) {
		if("auxip".equals(ingestion.getInboxType())) {
			return ingestion.getRelativePath();
		}

		return ingestion.getProductName();
	}
	
	private String storagePathFor(final ObsAdapter adapter, final ProductFamily family, final String keyObs) {
		return adapter.getAbsoluteStoragePath(family, keyObs);
	}

	final String toObsKey(final Path relPath) {
		return relPath.toString();
	}

	private ObsAdapter newObsAdapterFor(final ReportingFactory reportingFactory) {
		return new ObsAdapter(obsClient, reportingFactory, bufferInput, appStatus);
	}

}
