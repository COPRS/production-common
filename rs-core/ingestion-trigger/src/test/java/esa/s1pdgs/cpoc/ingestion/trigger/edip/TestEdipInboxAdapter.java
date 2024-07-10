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

package esa.s1pdgs.cpoc.ingestion.trigger.edip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ebip.client.EdipEntry;
import esa.s1pdgs.cpoc.ebip.client.EdipEntryImpl;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactoryImpl;

public class TestEdipInboxAdapter {		

	@Test
	public void testNewInboxEntryFor_OnValidEdipEntryProvision_ShallNotReturnDotDotPrefix() {
		// Entry as observed in the TRACE logs
		final EdipEntry e1 = new EdipEntryImpl(
				"S1A_OPER_AMV_ERRMAT_MPC__20200120T040010_V20000101T000000_20200119T201427.EOF", 
				Paths.get("/out/S1A_OPER_AMV_ERRMAT_MPC__20200120T040010_V20000101T000000_20200119T201427.EOF"),
				URI.create("ftps://s1pro-mock-edip-pedc-svc.processing.svc.cluster.local:21/out/S1A_OPER_AMV_ERRMAT_MPC__20200120T040010_V20000101T000000_20200119T201427.EOF"),
				Date.from(Instant.parse("2020-01-02T00:00:00Z")), 
				123
		);		
		final InboxEntryFactoryImpl factory = new InboxEntryFactoryImpl(new ProcessConfiguration() {
			@Override
			public String getHostname() {
				return "fooBar-HOST";
			}		
		});
		
		// problem can be triggered by e.g. using '[...]/out/.'
		final EdipInboxAdapter uut = new EdipInboxAdapter(
				URI.create("ftps://s1pro-mock-edip-pedc-svc.processing.svc.cluster.local:21/out/"), 
				null, 
				factory, 
				"WILE", 
				null,
				ProductFamily.AUXILIARY_FILE
		);		
		final InboxEntry actual = uut.newInboxEntryFor(e1);
		
		// check that there is no leading '../' in the name	and relative path	
		assertEquals("S1A_OPER_AMV_ERRMAT_MPC__20200120T040010_V20000101T000000_20200119T201427.EOF", actual.getName());
		assertEquals("S1A_OPER_AMV_ERRMAT_MPC__20200120T040010_V20000101T000000_20200119T201427.EOF", actual.getRelativePath());
	}
}
