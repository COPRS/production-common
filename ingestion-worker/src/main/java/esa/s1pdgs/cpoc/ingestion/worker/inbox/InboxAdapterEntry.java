package esa.s1pdgs.cpoc.ingestion.worker.inbox;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public final class InboxAdapterEntry implements Closeable {
	private final String key;
	private final InputStream in;
	private final long size;

	public InboxAdapterEntry(final String key, final InputStream in, final long size) {
		this.key = key;
		this.in = in;
		this.size = size;
	}

	public final String key() {
		return key;
	}

	public final InputStream inputStream() {
		return in;
	}
	
	public final long size() {
		return size;
	}

	@Override
	public final void close() throws IOException {
		in.close();		
	}

	@Override
	public String toString() {
		return "InboxAdapterEntry [key=" + key + ", in=" + in + ", size=" + size + "]";
	}
}
