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

package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactoryImpl;

public class TestInboxEntry {
	private InboxEntryFactory factory;

	@Before
	public void setup() {
		final ProcessConfiguration processConfiguration = new ProcessConfiguration();
		processConfiguration.setHostname("ingestor-01");
		factory = new InboxEntryFactoryImpl(processConfiguration);
	}

	@Test
	public final void testGetName_OnValidName_ShallReturnName() throws URISyntaxException {
		final InboxEntry uut = newInboxEntry("/tmp/fooBar");

		assertEquals("fooBar", uut.getName());
	}

	@Test
	public final void testHashCode_OnSameObject_ShallReturnSameHashCode() throws URISyntaxException {
		final InboxEntry uut1 = newInboxEntry("/tmp/fooBar");
		final InboxEntry uut2 = newInboxEntry("/tmp/fooBar");
		assertEquals(uut1.hashCode(), uut2.hashCode());
	}

	@Test
	public final void testHashCode_OnDifferentObject_ShallReturnDifferentHashCode() throws URISyntaxException {
		final InboxEntry uut1 = newInboxEntry("/tmp/foo");
		final InboxEntry uut2 = newInboxEntry("/tmp/bar");
		assertNotEquals(uut1.hashCode(), uut2.hashCode());
	}

	@Test
	public final void testEquals_OnSameObject_ShallReturnTrue() throws URISyntaxException {
		final InboxEntry uut1 = newInboxEntry("/tmp/foo");
		final InboxEntry uut2 = newInboxEntry("/tmp/foo");
		assertEquals(uut1, uut2);
	}

	@Test
	public final void testEquals_OnNull_ShallReturnFalse() throws URISyntaxException {
		final InboxEntry uut = newInboxEntry("/tmp/fooBar");
		assertNotEquals(null, uut);
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public final void testEquals_OnDifferentClass_ShallReturnFalse() throws URISyntaxException {
		final InboxEntry uut = newInboxEntry("/tmp/fooBar2");
		assertNotEquals(uut, new File("/tmp/fooBar2"));
	}

	private InboxEntry newInboxEntry(final String path) throws URISyntaxException {
		return factory.newInboxEntry(
				new URI("/tmp"),
				Paths.get(path),
				new Date(),
				0,
				null,
				null,
				null,
				ProductFamily.EDRS_SESSION
		);
	}
}
