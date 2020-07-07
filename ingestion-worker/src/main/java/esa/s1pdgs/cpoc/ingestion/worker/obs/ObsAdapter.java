package esa.s1pdgs.cpoc.ingestion.worker.obs;

import java.util.List;
import java.util.stream.Collectors;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapterEntry;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.obs_sdk.StreamObsUploadObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class ObsAdapter {
    private final ObsClient obsClient;
    private final ReportingFactory reportingFactory;
    
	public ObsAdapter(
			final ObsClient obsClient,
			final ReportingFactory reportingFactory
	) {
		this.obsClient 	= obsClient;
		this.reportingFactory = reportingFactory;
	}
	
	public final void upload(final ProductFamily family, final List<InboxAdapterEntry> entries, final String obsKey) throws ObsEmptyFileException {
		try {
			obsClient.uploadStreams(toUploadObjects(family, entries), reportingFactory);
		} catch (final AbstractCodedException e) {
			throw new RuntimeException(
					String.format("Error uploading %s (%s): %s", obsKey, family, LogUtils.toString(e))
			);
		}
	}
	
	private final List<StreamObsUploadObject> toUploadObjects(final ProductFamily family, final List<InboxAdapterEntry> entries) {
		return entries.stream()
			.map(e -> new StreamObsUploadObject(family, e.key(), e.inputStream(), e.size()))
			.collect(Collectors.toList());
	}
}
