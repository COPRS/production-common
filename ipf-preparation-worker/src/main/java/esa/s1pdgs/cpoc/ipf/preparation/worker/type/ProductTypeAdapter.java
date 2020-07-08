package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.AppCatAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.CatalogEventAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.mapper.TasktableMapper;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;

public interface ProductTypeAdapter {	
	static final Logger LOGGER = LogManager.getLogger(ProductTypeAdapter.class); 
	
	TasktableMapper taskTableMapper();
	
	Callable<JobGen> mainInputSearch(JobGen job);
	
    void customJobOrder(JobGen job);
	
    void customJobDto(final JobGen job, final IpfExecutionJob dto);	  
    
	Optional<AppDataJob> findAssociatedJobFor(
			final AppCatAdapter appCat, 
			final CatalogEventAdapter catEvent
    ) throws AbstractCodedException;
}
