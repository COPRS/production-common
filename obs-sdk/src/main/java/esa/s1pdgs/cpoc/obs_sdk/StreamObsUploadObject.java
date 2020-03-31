package esa.s1pdgs.cpoc.obs_sdk;

import java.io.InputStream;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class StreamObsUploadObject extends ObsUploadObject {
	
	private final InputStream input;

	public StreamObsUploadObject(final ProductFamily family, final String key, final InputStream input) {
		super(family, key);
		this.input = input;
	}

	public InputStream getInput() {
		// Remark: it's the responsibility of the caller to close the stream after upload
		return input;
	}
}
