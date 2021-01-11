package esa.s1pdgs.cpoc.ingestion.worker.product;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.obs.ObsAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class ProductServiceImpl implements ProductService {

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
		final Product<IngestionEvent> prod = new Product<>(family, uri, dto);
		obsAdapter.upload(
				family, 
				inboxAdapter.read(uri, ingestion.getProductName(), ingestion.getRelativePath(), ingestion.getProductSizeByte()),
				obsKey
		);
		return Collections.singletonList(prod);
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
