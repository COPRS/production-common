package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.mapper.TasktableMapper;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;

public final class L0Segment extends AbstractProductTypeAdapter implements ProductTypeAdapter {
	private final MetadataClient metadataClient;
	private final long timeoutInputSearchMs;

	public L0Segment(
			final TasktableMapper taskTableMapper, 
			final MetadataClient metadataClient,
			final long timeoutInputSearchMs
	) {
		super(taskTableMapper);
		this.metadataClient = metadataClient;
		this.timeoutInputSearchMs = timeoutInputSearchMs;
	}

	@Override
	public final Callable<JobGen> mainInputSearch(final JobGen job) {
		return new L0SegmentPolarisationQuery(job, metadataClient, timeoutInputSearchMs);
	}

	@Override
	public final void customJobOrder(final JobGen job) {
        updateProcParam(
        		job.jobOrder(), 
        		"Mission_Id",
                job.job().getProduct().getMissionId() + job.job().getProduct().getSatelliteId()
        );		
	}

	@Override
	public final void customJobDto(final JobGen job, final IpfExecutionJob dto) {
        // NOTHING TO DO		
	}



}
