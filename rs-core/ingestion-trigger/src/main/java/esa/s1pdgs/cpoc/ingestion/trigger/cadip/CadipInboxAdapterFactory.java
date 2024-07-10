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

package esa.s1pdgs.cpoc.ingestion.trigger.cadip;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.werum.coprs.cadip.client.CadipClientFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.config.CadipConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.config.InboxConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

@Component
public class CadipInboxAdapterFactory implements InboxAdapterFactory {

	private final CadipConfiguration configuration;
	private final ProcessConfiguration processConfiguration;
	private final InboxEntryFactory inboxEntryFactory;
	private final CadipStateRepository cadipStateRepository;
	private final CadipSessionStateRepository cadipSessionStateRepository;
	private final CadipClientFactory clientFactory;

	@Autowired
	public CadipInboxAdapterFactory(final CadipConfiguration configuration,
			final ProcessConfiguration processConfiguration, final InboxEntryFactory inboxEntryFactory,
			final CadipStateRepository cadipStateRepository,
			final CadipSessionStateRepository cadipSessionStateRepository, final CadipClientFactory clientFactory) {
		this.configuration = configuration;
		this.processConfiguration = processConfiguration;
		this.inboxEntryFactory = inboxEntryFactory;
		this.cadipStateRepository = cadipStateRepository;
		this.cadipSessionStateRepository = cadipSessionStateRepository;
		this.clientFactory = clientFactory;
	}

	@Override
	public InboxAdapter newInboxAdapter(URI inbox, InboxConfiguration inboxConfig) {
		return new CadipInboxAdapter(inboxEntryFactory, configuration, processConfiguration, cadipStateRepository,
				cadipSessionStateRepository, clientFactory.newCadipClient(inbox), inbox, inboxConfig.getStationName(),
				inboxConfig.getMissionId(), inboxConfig.getFamily(), inboxConfig.getSatelliteId());
	}

}
