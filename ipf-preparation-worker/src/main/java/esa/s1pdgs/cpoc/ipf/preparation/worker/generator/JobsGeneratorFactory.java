package esa.s1pdgs.cpoc.ipf.preparation.worker.generator;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TasktableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.publish.Publisher;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.AuxQueryHandler;
import esa.s1pdgs.cpoc.ipf.preparation.worker.service.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeFactory;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;

@Service
public class JobsGeneratorFactory {
	private final ProcessSettings settings;
	private final MetadataClient metadataClient;
	private final TaskTableFactory taskTableFactory;
	private final Function<TaskTable, InputTimeoutChecker> timeoutCheckerFactory;
	private final ElementMapper elementMapper;
    private final GracePeriodHandler gracePeriodHandler;
    private final Publisher publisher;
    private final ErrorRepoAppender errorAppender;
	private final AppCatalogJobClient appCatClient;
    
	@Autowired
	public JobsGeneratorFactory(
			final ProcessSettings settings,
			final MetadataClient metadataClient,
			final TaskTableFactory taskTableFactory,
			final Function<TaskTable, InputTimeoutChecker> timeoutCheckerFactory,
			final ElementMapper elementMapper,
			final GracePeriodHandler gracePeriodHandler,
		    final Publisher publisher,
		    final ErrorRepoAppender errorAppender,
			final AppCatalogJobClient appCatClient
	) {
		this.settings = settings;
		this.metadataClient = metadataClient;
		this.taskTableFactory = taskTableFactory;
		this.timeoutCheckerFactory = timeoutCheckerFactory;
		this.elementMapper = elementMapper;
		this.gracePeriodHandler = gracePeriodHandler;
		this.publisher = publisher;
		this.errorAppender = errorAppender;
		this.appCatClient = appCatClient;
	}
	
	public final JobGenerator newJobGenerator(final File taskTableFile, final ProductTypeFactory typeFactory) {		
		final TasktableAdapter tasktableAdapter = new TasktableAdapter(
				taskTableFile, 
				taskTableFactory.buildTaskTable(taskTableFile, settings.getLevel()), 
				elementMapper
		);		
	    final JobOrder jobOrderTemplate = tasktableAdapter.newJobOrderTemplate(settings);	    
	    final Map<Integer, SearchMetadataQuery> metadataQueryTemplate = tasktableAdapter.buildMetadataSearchQuery();	    		
	    final List<List<String>> tasks = tasktableAdapter.buildTasks();	    
		final AuxQueryHandler auxQueryHandler = new AuxQueryHandler(
				metadataClient, 
				ProductMode.SLICING, 
				timeoutCheckerFactory.apply(tasktableAdapter.taskTable())
		);
		return new JobGeneratorImpl(
				tasktableAdapter, 
				typeFactory.typeAdapter(), 
				appCatClient, 
				gracePeriodHandler, 
				settings, 
				errorAppender, 
				publisher, 
				jobOrderTemplate, 
				metadataQueryTemplate, 
				tasks, 
				auxQueryHandler
		);
	}
}
