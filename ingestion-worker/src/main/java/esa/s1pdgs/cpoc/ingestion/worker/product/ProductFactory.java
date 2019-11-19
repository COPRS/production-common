package esa.s1pdgs.cpoc.ingestion.worker.product;

import java.io.File;
import java.util.List;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.worker.obs.ObsAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

public interface ProductFactory<E extends AbstractDto> {
	
    @SuppressWarnings("unchecked")
	public static <E extends AbstractDto> ProductFactory<E> newProductFactoryFor(final ProductFamily family, String hostname)
    {
    	if (family == ProductFamily.AUXILIARY_FILE) {
    		return (ProductFactory<E>) new AuxiliaryProductFactory(hostname);
    	} else if (family == ProductFamily.EDRS_SESSION) {
    		return (ProductFactory<E>) new EdrsSessionFactory(hostname);
    	}    	
    	throw new UnsupportedOperationException(String.format("Not yet supported for %s", family)); 
    }
    
    public List<Product<E>> newProducts(final File file, final IngestionJob ingestionJob, final ObsAdapter obsAdapter) throws ProductException; 
}
