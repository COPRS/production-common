package esa.s1pdgs.cpoc.ingestion.product;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.obs.ObsAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;

public class AuxiliaryProductFactory implements ProductFactory<ProductionEvent> {	

	private final String hostname;
	
	public AuxiliaryProductFactory(final String hostname) {
		this.hostname = hostname;
	}
	
	@Override
	public List<Product<ProductionEvent>> newProducts(final File file, final IngestionJob ingestionDto, final ObsAdapter obsAdapter) throws ProductException {			
		return Collections.singletonList(newProduct(file, obsAdapter));
	}
	
	@Override
	public String toString() {
		return "AuxiliaryProductFactory";
	}

	private final Product<ProductionEvent> newProduct(final File file, final ObsAdapter obsAdapter) {
		final Product<ProductionEvent> prod = new Product<>();
		prod.setFamily(ProductFamily.AUXILIARY_FILE);
		prod.setFile(file);	
		
		final ProductionEvent dto = new ProductionEvent(
				file.getName(), 
				obsAdapter.toObsKey(file), 
				ProductFamily.AUXILIARY_FILE
		);
		dto.setCreationDate(new Date());
		dto.setHostname(hostname);
		prod.setDto(dto);
		return prod;
	}


}
