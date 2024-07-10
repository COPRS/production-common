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

package esa.s1pdgs.cpoc.xbip.client;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public interface XbipClient extends Closeable {
	static final XbipClient NULL = new XbipClient() {
		@Override
		public final List<XbipEntry> list(final XbipEntryFilter filter) throws IOException {
			return Collections.emptyList();
		}

		@Override
		public final InputStream read(final XbipEntry entry){
			return new ByteArrayInputStream(new byte[] {});
		}		

		@Override
		public void close() throws IOException {	
			// nothing
		}
	};

	List<XbipEntry> list(XbipEntryFilter filter) throws IOException;	
	InputStream read(XbipEntry entry);
}
