package esa.s1pdgs.cpoc.dissemination.trigger.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.DisseminationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil;

public class MyOceanDisseminationJobCreator implements DisseminationJobCreator {
	
	private static final Logger LOGGER = LogManager.getLogger(MyOceanDisseminationJobCreator.class);
	
	public static final String TYPE = DisseminationTriggerType.MYOCEAN.name().toLowerCase();
	
	private static final String MANIFEST_SAFE_FILE = "manifest.safe";
	
	private final MetadataClient metadataClient;
	
	public MyOceanDisseminationJobCreator(final MetadataClient metadataClient) {
		this.metadataClient = metadataClient;
	}

	@Override
	public DisseminationJob createJob(AbstractMessage productionEvent) throws AbstractCodedException {
		
		if (metadataClient.isIntersectingOceanMask(productionEvent.getProductFamily(), productionEvent.getKeyObjectStorage())) {
			LOGGER.info("intersects ocean mask: {}", productionEvent.getKeyObjectStorage());
			final DisseminationJob disseminationJob = new DisseminationJob();
			disseminationJob.setKeyObjectStorage(productionEvent.getKeyObjectStorage());
			disseminationJob.setProductFamily(productionEvent.getProductFamily());
			disseminationJob.addDisseminationSource(
					CompressionEventUtil.composeCompressedProductFamily(productionEvent.getProductFamily()),
					CompressionEventUtil.composeCompressedKeyObjectStorage(productionEvent.getKeyObjectStorage()));
			disseminationJob.addDisseminationSource(productionEvent.getProductFamily(),
					productionEvent.getKeyObjectStorage() + "/" + MANIFEST_SAFE_FILE);
			return disseminationJob;
		} else {
			LOGGER.info("skipping job, product does not intersect ocean mask: {}",
					productionEvent.getKeyObjectStorage());
			return null;
		}
	}

}
