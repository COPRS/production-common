package esa.s1pdgs.cpoc.ingestion.product;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.ingestion.obs.ObsAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@Service
public class ProductServiceImpl implements ProductService {	
    private final ObsClient obsClient;
	
	@Autowired
	public ProductServiceImpl(ObsClient obsClient) {
		this.obsClient = obsClient;
	}
	
	@Override
	public <E extends AbstractDto> List<Product<E>> ingest(final ProductFamily family, final IngestionDto ingestion) 
			throws ProductException, InternalErrorException {
		final File file = toFile(ingestion);		
		assertPermissions(ingestion, file);
		final ProductFactory<E> productFactory = ProductFactory.newProductFactoryFor(
				family, 
				ProductCategory.fromProductFamily(family).getDtoClass()
		);
		
		final ObsAdapter obsAdapter = newObsAdapterFor(file);
		final List<Product<E>> result = productFactory.newProducts(file, obsAdapter);					

		// is restart scenario?
		if (ingestion.getFamily() == ProductFamily.INVALID) {
			obsAdapter.move(ProductFamily.INVALID, family, file);			
		} else {
			obsAdapter.upload(family, file);
		}		
		return result;	
	}

	@Override
	public void markInvalid(IngestionDto ingestion) {	
		final File file = toFile(ingestion);
		newObsAdapterFor(file).upload(ProductFamily.INVALID, file);		
	}
	
	final String toObsKey(final Path relPath) {
		return relPath.toString();
	}
	
	final File toFile(final IngestionDto ingestion) {
		return new File(ingestion.getProductUrl().replace("file://", ""));
	}
	
	private final ObsAdapter newObsAdapterFor(final File file) {
		final Path inboxPath = file.toPath().getParent();	
		return new ObsAdapter(obsClient, inboxPath);		
	}
	
	static void assertPermissions(final IngestionDto ingestion, final File file) {
		if (!file.exists()) {
			throw new ProductException(String.format("File %s of %s does not exist", file, ingestion));
		}
		if (!file.canRead()) {
			throw new ProductException(String.format("File %s of %s is nor readable", file, ingestion));
		}
		if (!file.canWrite()) {
			throw new ProductException(String.format("File %s of %s is nor writeable", file, ingestion));
		}
	}
}
