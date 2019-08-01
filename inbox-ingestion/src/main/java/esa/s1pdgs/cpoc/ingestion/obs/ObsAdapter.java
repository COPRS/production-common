package esa.s1pdgs.cpoc.ingestion.obs;

import java.io.File;
import java.nio.file.Path;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ingestion.product.ProductException;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

public class ObsAdapter {
    private final ObsClient obsClient;
    private final Path inboxPath;
    
	public ObsAdapter(
			final ObsClient obsClient, 
			final Path inboxPath
	) {
		this.obsClient 	= obsClient;
		this.inboxPath 	= inboxPath;
	}
	
	public final void upload(final ProductFamily family, final File file) throws ProductException {
		final String obsKey = toObsKey(file);
		try {
			if (!obsClient.exist(family, obsKey)) {
				obsClient.uploadFile(family, obsKey, file);
			}
		} catch (ObsException e) {
			throw new ProductException(
					String.format("Error uploading file %s (%s): %s", file, family, LogUtils.toString(e))
			);
		}
	}
	
	public final void move(final ProductFamily from,final ProductFamily to, final File file) throws ProductException {
		final String obsKey = toObsKey(file);
		try {
			if (!obsClient.exist(from, obsKey)) {
				throw new ProductException(
						String.format("File %s (%s) to move does not exist", obsKey, from)
				);
			}
			if (!obsClient.exist(to, obsKey)) {
				throw new ProductException(
						String.format("File %s (%s) to already exist", obsKey, to)
				);
			}
			obsClient.moveFile(from, to, file);
		} catch (ObsException e) {
			throw new ProductException(
					String.format("Error moving file %s from %s to %s: %s", obsKey, from, to, LogUtils.toString(e))
			);
		}
	}
	
	public String toObsKey(final File file) {
		return inboxPath.relativize(file.toPath()).toString();
	}
	
}
