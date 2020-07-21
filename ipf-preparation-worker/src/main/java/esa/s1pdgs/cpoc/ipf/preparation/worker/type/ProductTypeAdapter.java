package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.AppCatJobService;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;

public interface ProductTypeAdapter {	
	Logger LOGGER = LogManager.getLogger(ProductTypeAdapter.class);
	
	Product mainInputSearch(final AppDataJob job) throws IpfPrepWorkerInputsMissingException;
	
	default void validateInputSearch(final AppDataJob job) throws IpfPrepWorkerInputsMissingException {
		// default implementation: don't validate
	}
	
	void customAppDataJob(final AppDataJob job);
	
    void customJobOrder(final AppDataJob job, final JobOrder jobOrder);
	
    void customJobDto(final AppDataJob job, final IpfExecutionJob dto);	
    
	// default implementation. Only required for S1 special scenarios (session, segments)
	default Optional<AppDataJob> findAssociatedJobFor(
			final AppCatJobService appCat, 
			final CatalogEventAdapter catEvent
    ) throws AbstractCodedException {
		return Optional.empty();
	}
}
