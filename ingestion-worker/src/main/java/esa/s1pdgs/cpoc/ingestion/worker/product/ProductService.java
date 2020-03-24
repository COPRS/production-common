package esa.s1pdgs.cpoc.ingestion.worker.product;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public interface ProductService {
	IngestionResult ingest(ProductFamily family, IngestionJob ingestion, final ReportingFactory reportingFactory)
			throws ProductException, InternalErrorException, ObsEmptyFileException;

	void markInvalid(IngestionJob ingestion, final ReportingFactory reportingFactory) throws ObsEmptyFileException, InternalErrorException;

	void assertFileIsNotEmpty(IngestionJob ingestion) throws ObsEmptyFileException, InternalErrorException;
}