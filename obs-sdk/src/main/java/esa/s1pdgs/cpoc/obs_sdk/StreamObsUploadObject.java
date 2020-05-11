package esa.s1pdgs.cpoc.obs_sdk;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class StreamObsUploadObject extends ObsUploadObject implements Closeable {

	private static final Logger LOGGER = LogManager.getLogger(StreamObsUploadObject.class);
	
	private final InputStream input;
	private final long contentLength;

	public StreamObsUploadObject(final ProductFamily family, final String key, final InputStream input, final long contentLength) {
		super(family, key);
		this.input = input;
		this.contentLength = contentLength;

		LOGGER.debug("created StreamObsUploadObject for {}, contentLength {} with inputStream markSupported {}", key, contentLength, input.markSupported());
	}

	public InputStream getInput() {
		return input;
	}

	public long getContentLength() {
		return contentLength;
	}

	@Override
	public void close() throws IOException {
		input.close();
	}

	public static final class TracingInputStream extends FilterInputStream {

		private final String obsKey;

		protected TracingInputStream(final String obsKey, final InputStream in) {
			super(in);
			this.obsKey = obsKey;
		}

		@Override
		public int read() throws IOException {
			final int read = super.read();
			LOGGER.trace("key {} read {}", obsKey, read);
			return read;
		}

		@Override
		public int read(byte[] b) throws IOException {
			final int read = super.read(b);
			LOGGER.trace("key {} read {} into byte array length {}", obsKey, read, b.length);
			return read;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			final int read = super.read(b, off, len);
			LOGGER.trace("key {} read {} into byte array length {} off {} len {}", obsKey, read, b.length, off, len);
			return read;
		}

		@Override
		public long skip(long n) throws IOException {
			final long skip = super.skip(n);
			LOGGER.trace("key {} skip {} returned {}", obsKey, n, skip);
			return skip;
		}

		@Override
		public int available() throws IOException {
			final int available = super.available();
			LOGGER.trace("key {} available {}", obsKey, available);
			return available;
		}

		@Override
		public void close() throws IOException {
			LOGGER.trace("key {} close", obsKey);
			super.close();
		}

		@Override
		public synchronized void mark(int readLimit) {
			LOGGER.trace("key {} mark {}", obsKey, readLimit);
			super.mark(readLimit);
		}

		@Override
		public synchronized void reset() throws IOException {
			LOGGER.trace("key {} reset", obsKey);
			super.reset();
		}

		@Override
		public boolean markSupported() {
			final boolean b = super.markSupported();
			LOGGER.trace("key {} markSupported {}", obsKey, b);
			return b;
		}
	}
}
