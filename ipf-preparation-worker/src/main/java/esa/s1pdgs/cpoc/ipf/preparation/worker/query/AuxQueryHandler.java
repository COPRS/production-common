package esa.s1pdgs.cpoc.ipf.preparation.worker.query;

import java.util.HashMap;
import java.util.Map;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;

public class AuxQueryHandler {	
    private final MetadataClient metadataClient;
	private final ProductMode mode;
	private final InputTimeoutChecker timeoutChecker;
	private final ElementMapper elementMapper;

	public AuxQueryHandler(
			final MetadataClient metadataClient,
			final ProductMode mode,
			final InputTimeoutChecker timeoutChecker,
			ElementMapper elementMapper) {
		this.metadataClient = metadataClient;
		this.mode = mode;
		this.timeoutChecker = timeoutChecker;
		this.elementMapper = elementMapper;
	}
	
	public AuxQuery queryFor(final JobGen jobGen) {
		return new AuxQuery(
				metadataClient,
				jobGen,
				mode,
				timeoutChecker,
				elementMapper,
				buildMetadataSearchQuery(jobGen.taskTableAdapter()));
	}

	private Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataQuery> buildMetadataSearchQuery(TaskTableAdapter taskTableAdapter) {
		final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataQuery> metadataQueryTemplate =  new HashMap<>();

		taskTableAdapter.allTaskTableInputs()
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
