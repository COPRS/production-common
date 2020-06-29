package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.AbstractJobOrderConf;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.mapper.TasktableMapper;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;

public final class EdrsSession extends AbstractProductTypeAdapter implements ProductTypeAdapter {		
	private final MetadataClient metadataClient;
    private final AiopPropertiesAdapter aiopAdapter;
      
	public EdrsSession(final TasktableMapper taskTableMapper, final MetadataClient metadataClient, final AiopPropertiesAdapter aiopAdapter) {
		super(taskTableMapper);
		this.metadataClient = metadataClient;
		this.aiopAdapter = aiopAdapter;
	}

	@Override
	public final Callable<JobGen> mainInputSearch(final JobGen job) {
		return new EdrsRawQuery(job, metadataClient, aiopAdapter);
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
            final int nb1 = job.job().getProduct().getRaws1().size();
            final int nb2 = job.job().getProduct().getRaws2().size();

            // Add raw to the job order, one file per channel, alterating and in alphabetic order
            for (int i = 0; i < Math.max(nb1, nb2); i++) {
                if (i < nb1) {
                    final AppDataJobFile raw = job.job().getProduct().getRaws1().get(i);
                    dto.addInput(
                            new LevelJobInputDto(
                                    ProductFamily.EDRS_SESSION.name(),
                                    dto.getWorkDirectory() + "ch01/" + raw.getFilename(),
                                    raw.getKeyObs()));
                }
                if (i < nb2) {
                    final AppDataJobFile raw = job.job().getProduct().getRaws2().get(i);
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
