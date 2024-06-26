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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.StringUtil;

/**
 * A type wrapping the response of an inbox read and the client that was used for the read to allow for proper freeing of resources.<br>
 * The calling code is responsible for closing the underlying resources.<br>
 * As the type implements {@link java.io.Closeable} it can conveniently be used with the try-with-resources construct.
 */
public class InboxAdapterResponse implements Closeable {

	private static final Logger LOG = LoggerFactory.getLogger(InboxAdapterResponse.class);

	private final Object inboxClient;
	private final List<InboxAdapterEntry> result;

	// --------------------------------------------------------------------------

	public InboxAdapterResponse(final List<InboxAdapterEntry> result, final Object inboxClient) {
		this.inboxClient = inboxClient;
		this.result = CollectionUtil.nullToEmptyList(result);
	}

	// --------------------------------------------------------------------------

	@Override
	public void close() throws IOException {
		// try to close streams
		CollectionUtil.nullToEmpty(this.result).forEach(entry -> {
			try {
				entry.close();
			} catch (final IOException e) {
				// ¯\_(ツ)_/¯
				LOG.warn(String.format("error closing input stream of %s: %s", entry, StringUtil.stackTraceToString(e)));
			}
		});

		// try to close client
		if (this.inboxClient instanceof Closeable) {
			try {
				((Closeable) this.inboxClient).close();
			} catch (final IOException e) {
				// ¯\_(ツ)_/¯
				LOG.warn(String.format("error closing inbox client %s: %s", this.inboxClient, StringUtil.stackTraceToString(e)));
			}
		}
	}

	public List<InboxAdapterEntry> getResult() {
		return this.result;
	}

}
