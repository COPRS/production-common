package esa.s1pdgs.cpoc.ingestion.trigger.edip;

import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ebip.client.EdipClient;
import esa.s1pdgs.cpoc.ebip.client.EdipEntry;
import esa.s1pdgs.cpoc.ebip.client.EdipEntryFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.AbstractInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.SupportsProductFamily;

public class EdipInboxAdapter extends AbstractInboxAdapter implements SupportsProductFamily {

	public final static String INBOX_TYPE = "edip";
	
	private final EdipClient edipClient;

	public EdipInboxAdapter(
			final URI inboxURL,
			final EdipClient edipClient,
			final InboxEntryFactory inboxEntryFactory,
			final String stationName,
			final String missionId,
			final ProductFamily productFamily
	) {
		super(inboxEntryFactory, inboxURL, stationName, missionId, productFamily);
		this.edipClient = edipClient;
	}

	@Override
	protected Stream<EntrySupplier> list() throws IOException {
		return this.edipClient.list(EdipEntryFilter.ALLOW_ALL).stream()
				.map(p -> new EntrySupplier(p.getPath(), () -> newInboxEntryFor(p)));
	}
	
	final InboxEntry newInboxEntryFor(final EdipEntry edipEntry) {
		
		return inboxEntryFactory.newInboxEntry(
				inboxURL,
				edipEntry.getPath(),
				edipEntry.getLastModified(),
				edipEntry.getSize(),
				stationName,
				missionId,
				INBOX_TYPE,
				productFamily
				);
	}

}
