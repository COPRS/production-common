package esa.s1pdgs.cpoc.ingestion.worker.config;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapterManager;
import esa.s1pdgs.cpoc.ingestion.worker.product.ProductService;
import esa.s1pdgs.cpoc.ingestion.worker.service.IngestionWorkerService;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

@Configuration
public class IngestionWorkerServiceConfiguration {

	@Autowired
	private ProductService productService;
	
	@Autowired
	private InboxAdapterManager inboxAdapterManager;
	
	@Bean
	public Function<IngestionJob, List<Message<CatalogJob>>> ingest() {
		return new IngestionWorkerService(productService, inboxAdapterManager);
	}
}
