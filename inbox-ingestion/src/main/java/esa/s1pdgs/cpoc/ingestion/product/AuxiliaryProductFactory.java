package esa.s1pdgs.cpoc.ingestion.product;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.obs.ObsAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

public class AuxiliaryProductFactory implements ProductFactory<ProductDto> {	
    /**
     * Pattern for configuration files to extract data
     */

	@Override
	public List<Product<ProductDto>> newProducts(final File file, final IngestionDto ingestionDto, final ObsAdapter obsAdapter) throws ProductException {			
		return Collections.singletonList(newProduct(file, obsAdapter));
	}
	
	@Override
	public String toString() {
		return "AuxiliaryProductFactory";
	}

	private final Product<ProductDto> newProduct(final File file, final ObsAdapter obsAdapter) {
		final Product<ProductDto> prod = new Product<>();
		prod.setFamily(ProductFamily.AUXILIARY_FILE);
		prod.setFile(file);	
		
		final ProductDto dto = new ProductDto(
				file.getName(), 
				obsAdapter.toObsKey(file), 
				ProductFamily.AUXILIARY_FILE
		);
		prod.setDto(dto);
		return prod;
	}


}
