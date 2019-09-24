package esa.s1pdgs.cpoc.ingestion.product;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.ingestion.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.obs.ObsAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@Service
public class ProductServiceImpl implements ProductService {	
	static final Logger LOG = LogManager.getLogger(ProductServiceImpl.class);
	
    private final ObsClient obsClient;
    
    private final String hostname;
	
	@Autowired
	public ProductServiceImpl(final ObsClient obsClient, final ProcessConfiguration processConfiguration) {
		this.obsClient = obsClient;
		this.hostname = processConfiguration.getHostname();
	}
	
	@Override
	public IngestionResult ingest(final ProductFamily family, final IngestionDto ingestion) 
			throws ProductException, InternalErrorException {
		final File file = toFile(ingestion);		
		assertPermissions(ingestion, file);
		final ProductFactory<AbstractDto> productFactory = ProductFactory.newProductFactoryFor(family, hostname);
		LOG.debug("Using {} for {}", productFactory, family);
		
		final ObsAdapter obsAdapter = newObsAdapterFor(Paths.get(ingestion.getPickupPath()));
		final List<Product<AbstractDto>> result = productFactory.newProducts(file, ingestion, obsAdapter);					
		long transferAmount = 0L;
		// is restart scenario?
		if (ingestion.getFamily() == ProductFamily.INVALID) {
			
			// has been already restarted before?
			if (family == ProductFamily.INVALID) {
				
			} else {
				obsAdapter.move(ProductFamily.INVALID, family, file);	
			}
		} else {
			obsAdapter.upload(family, file);
			transferAmount = FileUtils.sizeOf(file);
		}		
		return new IngestionResult(result, transferAmount);	
	}

	@Override
	public void markInvalid(IngestionDto ingestion) {	
		final File file = toFile(ingestion);
		newObsAdapterFor(Paths.get(ingestion.getPickupPath())).upload(ProductFamily.INVALID, file);		
	}
	
	final String toObsKey(final Path relPath) {
		return relPath.toString();
	}
	
	final File toFile(final IngestionDto ingestion) {
		return Paths.get(ingestion.getPickupPath(), ingestion.getRelativePath()).toFile();
	}
	
	private final ObsAdapter newObsAdapterFor(final Path inboxPath) {	
		return new ObsAdapter(obsClient, inboxPath);		
	}
	
	static void assertPermissions(final IngestionDto ingestion, final File file) {
		if (!file.exists()) {
			throw new RuntimeException(String.format("File %s of %s does not exist", file, ingestion));
		}
		if (!file.canRead()) {
			throw new RuntimeException(String.format("File %s of %s is not readable", file, ingestion));
		}
		if (!file.canWrite()) {
			throw new RuntimeException(String.format("File %s of %s is not writeable", file, ingestion));
		}
	}
}
