package esa.s1pdgs.cpoc.ebip.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public interface EdipClient {	
	static final EdipClient NULL = new EdipClient() {
		@Override
		public final List<EdipEntry> list(final EdipEntryFilter filter) throws IOException {
			return Collections.emptyList();
		}

		@Override
		public final InputStream read(final EdipEntry entry){
			return new ByteArrayInputStream(new byte[] {});
		}		
	};

	List<EdipEntry> list(EdipEntryFilter filter) throws IOException;	
	InputStream read(EdipEntry entry);
}
