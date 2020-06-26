package esa.s1pdgs.cpoc.ipf.preparation.worker.dispatch;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.JobGenerator;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.mapper.TasktableMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.report.TaskTableLookupReportingOutput;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

/**
 * Job dispatcher<br/>
 * 
 * When a message is read and can be processing, it will be dispatch to one or
 * several task tables according the product category.
 * 
 * @param <T>
 */
public class JobDispatcherImpl implements JobDispatcher {
    private final AppCatalogJobClient appCatClient;
    private final TasktableMapper tasktableMapper;
    private final ProcessSettings settings;
    private final Map<String, JobGenerator> generators; 

    public JobDispatcherImpl(
    		final TasktableMapper tasktableMapper,
    		final ProcessSettings settings,
            final AppCatalogJobClient appCatClient,
            final Map<String, JobGenerator> generators
    ) {
        this.tasktableMapper = tasktableMapper;
        this.appCatClient = appCatClient;
        this.generators = generators;
        this.settings = settings;
    }

	@Override
	public final void dispatch(final GenericMessageDto<IpfPreparationJob> message) throws Exception {
    	final IpfPreparationJob prepJob = message.getBody();
    	final AppDataJob job = prepJob.getAppDataJob();    	
    	LOGGER.trace("== dispatch job {}", job.toString());
        
        final Reporting reporting = ReportingUtils.newReportingBuilder()
        		.predecessor(prepJob.getUid())
        		.newReporting("TaskTableLookup");
        
    	reporting.begin(
    			ReportingUtils.newFilenameReportingInputFor(prepJob.getProductFamily(), job.getProduct().getProductName()),
    			new ReportingMessage("Start associating TaskTables to AppDataJob %s", job.getId())
    	);    	
        try {
            final String tasktableFilename = tasktableMapper.tasktableFor(job);
            LOGGER.trace("Got TaskTable {}", tasktableFilename);
            
            // assert that there is a job generator for the assigned tasktable
            if (!generators.containsKey(tasktableFilename)) {
            	throw new IllegalStateException(
            			String.format(
            					"No job generator found for tasktable %s. Available are: %s", 
            					tasktableFilename,
            					generators.keySet()
            			)
            	);
            }
            
            final Date now = new Date();            
            job.getGeneration().setState(AppDataJobGenerationState.INITIAL);
            job.getGeneration().setTaskTable(tasktableFilename);
            job.getGeneration().setNbErrors(0);
            job.getGeneration().setCreationDate(now);
            job.getGeneration().setLastUpdateDate(now);

            job.setPrepJobMessageId(message.getId());
            job.setPrepJobInputQueue(message.getInputKey());
            job.setReportingId(reporting.getUid());
            job.setState(AppDataJobState.GENERATING); // will activate that this request can be polled
            job.setPod(settings.getHostname()); // will ensure that this pod will handled the job
            
            appCatClient.updateJob(job);
            LOGGER.debug ("== dispatched job {}", job.toString());
            reporting.end(
            		new TaskTableLookupReportingOutput(Collections.singletonList(tasktableFilename)),
            		new ReportingMessage("End associating TaskTables to AppDataJob %s", job.getId())
            );
        } catch (final Exception e) {        	
        	reporting.error(new ReportingMessage(
        			"Error associating TaskTables to AppDataJob %s: %s", 
        			job.getId(),
        			LogUtils.toString(e)
        	));
            throw e;
        }
    }

}
