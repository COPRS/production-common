package esa.s1pdgs.cpoc.ingestion.worker.obs;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ingestion.worker.product.ProductException;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class ObsAdapter {
    private final ObsClient obsClient;
    private final Path inboxPath;
    private final ReportingFactory reportingFactory;
    
	public ObsAdapter(
			final ObsClient obsClient, 
			final Path inboxPath,
			final ReportingFactory reportingFactory
	) {
		this.obsClient 	= obsClient;
		this.inboxPath 	= inboxPath;
		this.reportingFactory = reportingFactory;
	}
	
	public final void upload(final ProductFamily family, final File file, final String obsKey) throws ObsEmptyFileException {
		try {
			if (!obsClient.exists(new ObsObject(family, obsKey))) {
				obsClient.upload(Arrays.asList(new ObsUploadObject(family, obsKey, file)), reportingFactory);
			}
		} catch (AbstractCodedException | SdkClientException e) {
			throw new RuntimeException(
					String.format("Error uploading file %s (%s): %s", file, family, LogUtils.toString(e))
			);
		}
	}
	
	public final void move(final ProductFamily from,final ProductFamily to, final File file, final String obsKey) throws ProductException {
		try {
			if (!obsClient.exists(new ObsObject(from, obsKey))) {
				throw new ProductException(
						String.format("File %s (%s) to move does not exist", obsKey, from)
				);
			}
			if (obsClient.exists(new ObsObject(to, obsKey))) {
				throw new ProductException(
						String.format("File %s (%s) to already exist", obsKey, to)
				);
			}
			obsClient.move(new ObsObject(from, obsKey), to);
		} catch (ObsException | SdkClientException e) {
			throw new ProductException(
					String.format("Error moving file %s from %s to %s: %s", obsKey, from, to, LogUtils.toString(e))
			);
		}
	}
	
	public String toObsKey(final File file) {
		return inboxPath.relativize(file.toPath()).toString();
	}
	
}
