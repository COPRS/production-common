/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
