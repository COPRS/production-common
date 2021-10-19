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
