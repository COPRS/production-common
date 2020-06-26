package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.metadata.SearchMetadataResult;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TasktableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.publish.Publisher;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.AuxQueryHandler;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;

public abstract class AbstractProductTypeAdapter implements ProductTypeAdapter {
	private final ApplicationLevel level;
	
	public AbstractProductTypeAdapter(final ApplicationLevel level) {
		this.level = level;
	}

	@Override
	public final ProductFamily jobFamily() {
		return level.toFamily();
	}

	@Override
	public JobGen newJobGenFor(
			final AppDataJob job, 
			final TasktableAdapter taskTableAdapter,
			final Publisher publisher,
			final JobOrder jobOrderTemplate,
			final Map<Integer, SearchMetadataQuery> metadataQueriesTemplate,
			final List<List<String>> tasks,
			final AuxQueryHandler auxQueryHandler
			
	) {
		final Map<Integer, SearchMetadataResult> queries = new HashMap<>(metadataQueriesTemplate.size());
		
		for (final Map.Entry<Integer, SearchMetadataQuery> entry : metadataQueriesTemplate.entrySet() ) {
			queries.put(entry.getKey(), new SearchMetadataResult(new SearchMetadataQuery(entry.getValue())));
		}
		return new JobGen(
				job, 
				this, 
				queries, 
				tasks, 
				taskTableAdapter, 
				auxQueryHandler, 
				new JobOrder(jobOrderTemplate, level), 
				publisher
		);
	}
}
