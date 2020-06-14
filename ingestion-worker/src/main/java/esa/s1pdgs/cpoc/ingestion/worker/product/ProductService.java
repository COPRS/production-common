package esa.s1pdgs.cpoc.ingestion.worker.product;

import java.util.List;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public interface ProductService {
	List<Product<IngestionEvent>> ingest(
			ProductFamily family, 
			InboxAdapter inboxAdapter,			
			IngestionJob ingestion, 
			ReportingFactory reportingFactory
	) throws Exception;
}