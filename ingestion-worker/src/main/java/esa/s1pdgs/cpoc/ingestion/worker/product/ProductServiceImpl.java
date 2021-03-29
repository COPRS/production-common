package esa.s1pdgs.cpoc.ingestion.worker.product;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.obs.ObsAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class ProductServiceImpl implements ProductService {

	private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

	private final ObsClient obsClient;
	private final boolean bufferInput;

	public ProductServiceImpl(final ObsClient obsClient, final boolean bufferInput) {
		this.obsClient = obsClient;
		this.bufferInput = bufferInput;
	}

	@Override
	public List<Product<IngestionEvent>> ingest(
			final ProductFamily family, 
			final InboxAdapter inboxAdapter,
			final IngestionJob ingestion,
			final ReportingFactory reportingFactory
	) throws Exception {		
		final URI uri = IngestionJobs.toUri(ingestion);
		final ObsAdapter obsAdapter = newObsAdapterFor(reportingFactory);
		final String obsKey = obsKeyFor(ingestion);

		final IngestionEvent dto = new IngestionEvent(
				family, 
				obsKey,
				ingestion.getRelativePath(), 
				ingestion.getProductSizeByte(),
				ingestion.getStationName(),
				ingestion.getMode(),
				ingestion.getTimeliness()
		);

		checkExistingInObs(obsAdapter, ingestion);
		upload(obsAdapter, ingestion, family, inboxAdapter, uri, obsKey);

		return Collections.singletonList(new Product<>(family, uri, dto));
	}

	private void upload(ObsAdapter obsAdapter, IngestionJob ingestion, ProductFamily family, InboxAdapter inboxAdapter, URI uri, String obsKey) throws Exception {
		obsAdapter.upload(
				family,
				inboxAdapter.read(uri, ingestion.getProductName(), ingestion.getRelativePath(), ingestion.getProductSizeByte()),
				obsKey
		);
	}

	private void checkExistingInObs(final ObsAdapter obsAdapter, final IngestionJob ingestion) {
		final String obsKey = obsKeyFor(ingestion);

		//returns -1 if it is not in OBS
		long size = obsAdapter.sizeOf(ingestion.getProductFamily(), obsKey);

		if (ingestion.getProductSizeByte() >= 0 && size == ingestion.getProductSizeByte()) {
			throw new RuntimeException(
					String.format("File %s is already in obs and has the same size, aborting ingestion", obsKey));
		}

		if(size > 0) {
			LOG.info("File {} has new size {}, will overwrite existing one with size {}", obsKey, ingestion.getProductSizeByte(), size);
		}
	}

	private String obsKeyFor(final IngestionJob ingestion) {
		if("auxip".equals(ingestion.getInboxType())) {
			return ingestion.getRelativePath();
		}

		return ingestion.getProductName();
	}

	final String toObsKey(final Path relPath) {
		return relPath.toString();
	}

	private ObsAdapter newObsAdapterFor(final ReportingFactory reportingFactory) {
		return new ObsAdapter(obsClient, reportingFactory, bufferInput);
	}

}
