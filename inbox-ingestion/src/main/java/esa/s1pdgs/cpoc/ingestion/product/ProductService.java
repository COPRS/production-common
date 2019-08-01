package esa.s1pdgs.cpoc.ingestion.product;

import java.util.List;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;

public interface ProductService {

	<E extends AbstractDto> List<Product<E>> ingest(ProductFamily family, IngestionDto ingestion)
			throws ProductException, InternalErrorException;

	void markInvalid(IngestionDto ingestion);

}