package esa.s1pdgs.cpoc.ipf.preparation.worker.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;

public class AuxQueryHandler {	
    private final MetadataClient metadataClient;
	private final ProductMode mode;
	private final InputTimeoutChecker timeoutChecker;
	private final ElementMapper elementMapper;
	private final TaskTableAdapter taskTableAdapter;

	public AuxQueryHandler(
			final MetadataClient metadataClient,
			final ProductMode mode,
			final InputTimeoutChecker timeoutChecker,
			final ElementMapper elementMapper,
			final TaskTableAdapter taskTableAdapter
	) {
		this.metadataClient = metadataClient;
		this.mode = mode;
		this.timeoutChecker = timeoutChecker;
		this.elementMapper = elementMapper;
		this.taskTableAdapter = taskTableAdapter;
	}
	
	public List<AppDataJobTaskInputs> queryFor(final AppDataJob job) throws IpfPrepWorkerInputsMissingException {
		final AuxQuery query = new AuxQuery(
				metadataClient,
				job,
				mode,
				timeoutChecker,
				taskTableAdapter,
				buildMetadataSearchQuery(taskTableAdapter));
		
		return query.queryAux();
	}

	private Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataQuery> buildMetadataSearchQuery(final TaskTableAdapter taskTableAdapter) {
		final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataQuery> metadataQueryTemplate =  new HashMap<>();

		taskTableAdapter.allTaskTableInputAlternatives()
				.forEach((inputAltKey, alternatives) -> {
					final String fileType = elementMapper.mappedFileType(inputAltKey.getFileType());
					final ProductFamily family = elementMapper.inputFamilyOf(fileType);
					final SearchMetadataQuery query = new SearchMetadataQuery(
							0,
							inputAltKey.getRetrievalMode(),
							inputAltKey.getDeltaTime0(),
							inputAltKey.getDeltaTime1(),
							fileType,
							family
					);
					metadataQueryTemplate.put(inputAltKey, query);
				});
		return metadataQueryTemplate;
	}
}
