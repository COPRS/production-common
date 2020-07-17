package esa.s1pdgs.cpoc.ipf.preparation.worker.type.edrs;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.AppCatJobService;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.CatalogEventAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;
import esa.s1pdgs.cpoc.xml.model.joborder.AbstractJobOrderConf;

public final class EdrsSession extends AbstractProductTypeAdapter implements ProductTypeAdapter {		
	private final MetadataClient metadataClient;
    private final AiopPropertiesAdapter aiopAdapter;
      
	public EdrsSession(
			final MetadataClient metadataClient, 
			final AiopPropertiesAdapter aiopAdapter
	) {
		this.metadataClient = metadataClient;
		this.aiopAdapter = aiopAdapter;
	}
	
	@Override
	public final Optional<AppDataJob> findAssociatedJobFor(final AppCatJobService appCat, final CatalogEventAdapter catEvent) 
			throws AbstractCodedException {
		return appCat.findJobForSession(catEvent.sessionId());
	}

	@Override
	public final Callable<JobGen> mainInputSearch(final JobGen job) {
		return new EdrsRawQuery(job, metadataClient, aiopAdapter);
	}
	
	@Override
	public final void customAppDataJob(final AppDataJob job) {			
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(job);		
		// IMPORTANT workaround!!! Allows to get the session identifier in exec-worker
		job.getProduct().getMetadata().put("productName", eventAdapter.sessionId());	
	}
	
	@Override
	public final void customJobOrder(final JobGen job) {
    	final AbstractJobOrderConf conf = job.jobOrder().getConf();    	
    	
    	final Map<String,String> aiopParams = aiopAdapter.aiopPropsFor(job.job());    	
    	LOGGER.trace("Existing parameters: {}", conf.getProcParams());
    	LOGGER.trace("New AIOP parameters: {}", aiopParams);
    	
    	for (final Entry<String, String> newParam : aiopParams.entrySet()) {
    		updateProcParam(job.jobOrder(), newParam.getKey(), newParam.getValue());
		}    	
    	LOGGER.debug("Configured AIOP for product {} of job {} with configuration {}", 
    			job.productName(), job.id(), conf);		
	}

	@Override
	public final void customJobDto(final JobGen job, final IpfExecutionJob dto) {
        // Add input relative to the channels
        if (job.job().getProduct() != null) {
        	final EdrsSessionProduct product = EdrsSessionProduct.of(job.job());
        	
        	final List<AppDataJobFile> raws1 = product.getRawsForChannel(1);
        	final List<AppDataJobFile> raws2 = product.getRawsForChannel(2);

            // Add raw to the job order, one file per channel, alterating and in alphabetic order
            for (int i = 0; i < Math.max(raws1.size(), raws2.size()); i++) {
                if (i < raws1.size()) {
                    final AppDataJobFile raw = raws1.get(i);
                    dto.addInput(
                            new LevelJobInputDto(
                                    ProductFamily.EDRS_SESSION.name(),
                                    dto.getWorkDirectory() + "ch01/" + raw.getFilename(),
                                    raw.getKeyObs()));
                }
                if (i < raws2.size()) {
                    final AppDataJobFile raw = raws2.get(i);
                    dto.addInput(
                            new LevelJobInputDto(
                                    ProductFamily.EDRS_SESSION.name(),
                                    dto.getWorkDirectory() + "ch02/" + raw.getFilename(),
                                    raw.getKeyObs()));
                }
            }
        }		
	}
}
