package esa.s1pdgs.cpoc.ingestion.worker.product;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;

public interface ProductService {
	IngestionResult ingest(ProductFamily family, IngestionJob ingestion)
			throws ProductException, InternalErrorException, ObsEmptyFileException;

	void markInvalid(IngestionJob ingestion) throws ObsEmptyFileException;

}