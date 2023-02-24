package esa.s1pdgs.cpoc.ingestion.worker.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.common.errors.SardineRuntimeException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapterManager;
import esa.s1pdgs.cpoc.ingestion.worker.product.IngestionJobs;
import esa.s1pdgs.cpoc.ingestion.worker.product.Product;
import esa.s1pdgs.cpoc.ingestion.worker.product.ProductService;
import esa.s1pdgs.cpoc.ingestion.worker.product.report.IngestionWorkerReportingOutput;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFactory;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.input.InboxReportingInput;

public class IngestionWorkerService implements Function<IngestionJob, List<Message<CatalogJob>>> {
	static final Logger LOG = LogManager.getLogger(IngestionWorkerService.class);

	private final CommonConfigurationProperties commonProperties;
	private final ProductService productService;
	private final InboxAdapterManager inboxAdapterManager;

	@Autowired
	public IngestionWorkerService(final CommonConfigurationProperties commonProperties, final ProductService productService, final InboxAdapterManager inboxAdapterManager) {
		this.commonProperties = commonProperties;
		this.productService = productService;
		this.inboxAdapterManager = inboxAdapterManager;
	}

	@Override
	public List<Message<CatalogJob>> apply(IngestionJob ingestion) {
		final String productName;
		if ("auxip".equalsIgnoreCase(ingestion.getInboxType())) {
			productName = ingestion.getRelativePath();
		} else {
			productName = ingestion.getProductName();
		}

		MissionId mission = MissionId.valueOf(ingestion.getMissionId());

		final Reporting reporting = ReportingUtils.newReportingBuilder(mission)
				.rsChainName(commonProperties.getRsChainName())
				.rsChainVersion(commonProperties.getRsChainVersion())
				.predecessor(ingestion.getUid())
				.newReporting("IngestionWorker");

		LOG.debug("received Ingestion: {}", productName);
		final URI productUri = IngestionJobs.toUri(ingestion);
		final InboxAdapter inboxAdapter = inboxAdapterManager.getInboxAdapterFor(productUri);

		reporting.begin(new InboxReportingInput(productName, ingestion.getRelativePath(), ingestion.getPickupBaseURL()),
				new ReportingMessage("Start processing of %s", productName));

		List<Message<CatalogJob>> events;
		try {
			final List<Product<CatalogJob>> result = identifyAndUpload(inboxAdapter, ingestion, reporting);
			events = publish(result, reporting.getUid());
		} catch (Exception e) {
			reporting.error(new ReportingMessage("Error processing of %s: %s", ingestion.getKeyObjectStorage(),
					LogUtils.toString(e)));
			throw new RuntimeException(e);
		}

		inboxAdapter.delete(productUri);
		reporting.end(IngestionWorkerReportingOutput.newInstance(ingestion, new Date()), new ReportingMessage(
				ingestion.getProductSizeByte(), "End processing of %s", ingestion.getKeyObjectStorage()));
		return events;
	}

	final List<Product<CatalogJob>> identifyAndUpload(final InboxAdapter inboxAdapter, final IngestionJob ingestion,
			final ReportingFactory reportingFactory) throws Exception {
		try {
			return productService.ingest(ingestion.getProductFamily(), inboxAdapter, ingestion, reportingFactory);
		} catch (final Exception e) {
			LOG.error(e);
			// Only throw if it is NOT an Internal Server Error (500)
			if (e instanceof SardineRuntimeException) {
				return Collections.emptyList();
			} else {
				throw e;
			}
		}
	}

	final List<Message<CatalogJob>> publish(final List<Product<CatalogJob>> products, final UUID reportingId) {
		final List<Message<CatalogJob>> result = new ArrayList<>();
		for (final Product<CatalogJob> product : products) {
			final CatalogJob event = product.getDto();
			event.setUid(reportingId);
			
			// RS-536: Add RS Chain version to message
			event.setRsChainVersion(commonProperties.getRsChainVersion());

			LOG.info("publishing : {}", event);
			result.add(MessageBuilder.withPayload(event).build());
		}
		return result;
	}
}
