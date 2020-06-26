package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TasktableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.publish.Publisher;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.AuxQueryHandler;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;

public interface ProductTypeAdapter {
	
	JobGen newJobGenFor(
			AppDataJob job,
			TasktableAdapter taskTableAdapter,
			Publisher publisher,
			JobOrder jobOrderTemplate,
			Map<Integer, SearchMetadataQuery> metadataQueries,
			List<List<String>> tasks,
			AuxQueryHandler auxQueryHandler
	);
	
	Callable<Void> mainInputSearch(JobGen job);
	
	ProductFamily jobFamily();
	
    void customJobOrder(JobGen job);
	
    void customJobDto(final JobGen job, final IpfExecutionJob dto);	
    
    
}
