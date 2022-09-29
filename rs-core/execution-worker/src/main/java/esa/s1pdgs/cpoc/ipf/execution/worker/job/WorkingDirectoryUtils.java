package esa.s1pdgs.cpoc.ipf.execution.worker.job;

import java.io.File;
import java.util.Collections;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.obs_sdk.FileObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class WorkingDirectoryUtils {
	
	private static final Logger LOG = LogManager.getLogger(WorkingDirectoryUtils.class);

	private final ObsClient obsClient;
	private final String hostname;

	public WorkingDirectoryUtils(ObsClient obsClient, String hostname) {
		this.obsClient = obsClient;
		this.hostname = hostname;
	}

	public void copyWorkingDirectory(ReportingFactory reporting, UUID uid, IpfExecutionJob job, ProductFamily family)
			throws AbstractCodedException, ObsEmptyFileException {
		final String debugPrefix = ipfExecutionErrorPrefix(hostname, uid, job);

		LOG.info("{} Copying working directory to '{}'", MonitorLogUtils.getPrefixMonitorLog(MonitorLogUtils.LOG_ERROR, job), debugPrefix);
		
		final FileObsUploadObject upload = new FileObsUploadObject(family, debugPrefix,
				new File(job.getWorkDirectory()));
		obsClient.upload(Collections.singletonList(upload), reporting);
	}

	private String ipfExecutionErrorPrefix(final String hostname, final UUID uuid, final IpfExecutionJob job) {
		return hostname + "_" + job.getKeyObjectStorage() + "_" + uuid.toString() + "_" + job.getRetryCounter();
	}

}
