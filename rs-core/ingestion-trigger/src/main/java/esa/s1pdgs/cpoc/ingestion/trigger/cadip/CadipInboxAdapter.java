package esa.s1pdgs.cpoc.ingestion.trigger.cadip;

import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

import de.werum.coprs.cadip.client.CadipClient;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.AbstractInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.SupportsProductFamily;

public class CadipInboxAdapter extends AbstractInboxAdapter implements SupportsProductFamily {

	private ProcessConfiguration processConfiguration;
	private CadipStateRepository stateRepository;
	private CadipSessionStateRepository sessionRepository;
	private CadipClient cadipClient;

	public CadipInboxAdapter(InboxEntryFactory inboxEntryFactory, ProcessConfiguration processConfiguration,
			CadipStateRepository stateRepository, CadipSessionStateRepository sessionRepository, CadipClient client,
			URI inboxURL, String stationName, String missionId, ProductFamily productFamily) {
		super(inboxEntryFactory, inboxURL, stationName, missionId, productFamily);
		
		this.processConfiguration = processConfiguration;
		this.stateRepository = stateRepository;
		this.sessionRepository = sessionRepository;
		this.cadipClient = client;
	}

	@Override
	protected Stream<EntrySupplier> list() throws IOException {
		return null;
	}
}
