package esa.s1pdgs.cpoc.obs_sdk;

import java.io.InputStream;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class StreamObsUploadObject extends ObsUploadObject implements AutoCloseable {
	
	private final InputStream input;
	private final long contentLength;

	public StreamObsUploadObject(final ProductFamily family, final String key, final InputStream input, long contentLength) {
		super(family, key);
		this.input = input;
		this.contentLength = contentLength;
	}

	public InputStream getInput() {
		return input;
	}

	public long getContentLength() {
		return contentLength;
	}

	@Override
	public void close() throws Exception {
		input.close();
	}
}
