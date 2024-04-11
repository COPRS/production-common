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

package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.config.InboxConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.name.ProductNameEvaluator;

public class TestInboxFactory {
	@Test
	public final void testNewProductNameEvaluatorFor_DefaultConfig() {
		final ProductFamily productFamily = ProductFamily.EDRS_SESSION;

		final InboxConfiguration defConfig = new InboxConfiguration();
		defConfig.setFamily(productFamily);

		final InboxFactory uut = new InboxFactory(null, null, null, null, null, null, null);

		final ProductNameEvaluator eval = uut.newProductNameEvaluatorFor(defConfig);
		final InboxEntry entry = new InboxEntry();
		entry.setRelativePath("S1B/L20180724144436762001030/ch02/DCS_02_L20180724144436762001030_ch2_DSDB_00025.raw");
		entry.setProductFamily(productFamily.name());
		assertEquals(
				"L20180724144436762001030/ch02/DCS_02_L20180724144436762001030_ch2_DSDB_00025.raw",
				eval.evaluateFrom(entry)
				);
	}

	@Test
	public final void testIgnoreFilesBeforeDateFor_GivenDateIsInFuture_ShallReturnCurrentDate() {
		final Date now = new Date();

		final long oneDayMillis = 24 * 3600 * 1000;
		final Date futureDate = new Date(now.getTime()+oneDayMillis);

		final InboxConfiguration config = new InboxConfiguration();
		config.setIgnoreFilesBeforeDate(futureDate);

		assertEquals(now, InboxFactory.ignoreFilesBeforeDateFor(config, now));
	}

	@Test
	public final void testIgnoreFilesBeforeDateFor_GivenDateIsInPast_ShallReturnConfiguredDate() {
		final Date now = new Date();

		final long oneDayMillis = 24 * 3600 * 1000;
		final Date pastDate = new Date(now.getTime()-oneDayMillis);

		final InboxConfiguration config = new InboxConfiguration();
		config.setIgnoreFilesBeforeDate(pastDate);

		assertEquals(pastDate, InboxFactory.ignoreFilesBeforeDateFor(config, now));
	}
}