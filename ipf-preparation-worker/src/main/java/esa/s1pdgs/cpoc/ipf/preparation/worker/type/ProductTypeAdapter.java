package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.AppCatJobService;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;

public interface ProductTypeAdapter {	
	Logger LOGGER = LogManager.getLogger(ProductTypeAdapter.class);
	
	Callable<Void> mainInputSearch(final AppDataJob job);
	
	void customAppDataJob(final AppDataJob job);
	
    void customJobOrder(JobGen job);
	
    void customJobDto(final JobGen job, final IpfExecutionJob dto);	
    
	// default implementation. Only required for S1 special scenarios (session, segments)
	default Optional<AppDataJob> findAssociatedJobFor(
			final AppCatJobService appCat, 
			final CatalogEventAdapter catEvent
    ) throws AbstractCodedException {
		return Optional.empty();
	}
}
