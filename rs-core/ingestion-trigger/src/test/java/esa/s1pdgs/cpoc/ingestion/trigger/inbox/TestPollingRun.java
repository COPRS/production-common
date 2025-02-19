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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public class TestPollingRun {

	@SuppressWarnings("deprecation")
	private static final Date LAST_MODIFIED_1 = new Date(2021, 2, 1, 10, 22, 22);
	@SuppressWarnings("deprecation")
	private static final Date LAST_MODIFIED_2 = new Date(2021, 2, 2, 11, 33, 33);
	private static final LocalDateTime KNOWN_SINCE_1 = LocalDateTime.now(ZoneOffset.UTC);
	private static final String FILE_PATH_1 = "S1B/DCS_04_S1B_20210208090050025513_dat/ch_1/DCS_04_S1B_20210208090050025513_ch1_DSDB_00020.raw";
	private static final String PICKUP_URL_1 = "https://esa-copernicus.ksat.no/SVL/SENTINEL1/NOMINAL";
	private static final String PROCESSING_POD_1 = "s1pro-ingestion-xbip-cgs02-trigger-0";
	private static final String INBOX_TYPE_1 = "xbip";
	private static final String PRODUCT_FAMILY_1 = "EDRS_SESSION";
	private static final String STATION_NAME_1 = "SGS_";
	private static final String MISSION_ID = "S1";
	private static final int STATION_RETENTION_TIME_DEFAULT = 0;
	private static final long SIZE_1 = 667788;

	// --------------------------------------------------------------------------

	@Test
	public void test_inboxEntryComparison_withoutProductFamily_expectAllEmpty() {
		// creating a polling run with method newInstanceWithoutProductFamily will compare inbox entries
		// from persistence and pickup ignoring the product family so that legacy data from persistence
		// does not lead to re-ingestion

		final PollingRun pollingRunExpectAllEmpty = PollingRun.newInstanceWithoutProductFamily(
				Collections.emptySet(), Collections.emptyList(), STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectAllEmpty.finishedElements()),
				"expected finished elements to be empty!");
		assertTrue(CollectionUtil.isEmpty(pollingRunExpectAllEmpty.newElements()),
				"expected new elements to be empty!");

		final InboxEntry inboxEntry = createInboxEntry();
		final InboxEntry inboxEntryWithoutProductFamily = createInboxEntryWithoutProductFamily();

		final PollingRun pollingRunExpectAllEmpty2 = PollingRun.newInstanceWithoutProductFamily(
				Collections.singleton(inboxEntryWithoutProductFamily), Collections.singletonList(inboxEntry),
				STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectAllEmpty2.finishedElements()),
				"expected finished elements to be empty!");
		assertTrue(CollectionUtil.isEmpty(pollingRunExpectAllEmpty2.newElements()),
				"expected new elements to be empty!");

		final PollingRun pollingRunExpectAllEmpty3 = PollingRun.newInstanceWithoutProductFamily(
				Collections.singleton(inboxEntry), Collections.singletonList(inboxEntryWithoutProductFamily),
				STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectAllEmpty3.finishedElements()),
				"expected finished elements to be empty!");
		assertTrue(CollectionUtil.isEmpty(pollingRunExpectAllEmpty3.newElements()),
				"expected new elements to be empty!");

		final PollingRun pollingRunExpectAllEmpty4 = PollingRun.newInstanceWithoutProductFamily(
				Collections.singleton(inboxEntryWithoutProductFamily),
				Collections.singletonList(inboxEntryWithoutProductFamily), STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectAllEmpty4.finishedElements()),
				"expected finished elements to be empty!");
		assertTrue(CollectionUtil.isEmpty(pollingRunExpectAllEmpty4.newElements()),
				"expected new elements to be empty!");
	}

	@Test
	public void test_inboxEntryComparison_withoutProductFamily_expectNew() {
		// creating a polling run with method newInstanceWithoutProductFamily will compare inbox entries
		// from persistence and pickup ignoring the product family so that legacy data from persistence
		// does not lead to re-ingestion

		final InboxEntry inboxEntryWithoutProductFamily = createInboxEntryWithoutProductFamily();

		final PollingRun pollingRunExpectNew = PollingRun.newInstanceWithoutProductFamily(Collections.emptySet(),
				Collections.singletonList(inboxEntryWithoutProductFamily), STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectNew.finishedElements()),
				"expected finished elements to be empty!");
		assertTrue(CollectionUtil.isNotEmpty(pollingRunExpectNew.newElements())
				&& pollingRunExpectNew.newElements().size() == 1
				&& inboxEntryWithoutProductFamily.equals(pollingRunExpectNew.newElements().get(0)),
				"expected new elements return 1 inbox entry!");

		final InboxEntry inboxEntry = createInboxEntry();

		final PollingRun pollingRunExpectNew2 = PollingRun.newInstanceWithoutProductFamily(Collections.emptySet(),
				Collections.singletonList(inboxEntry), STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectNew2.finishedElements()),
				"expected finished elements to be empty!");
		assertTrue(CollectionUtil.isNotEmpty(pollingRunExpectNew2.newElements())
				&& pollingRunExpectNew2.newElements().size() == 1
				&& inboxEntry.equals(pollingRunExpectNew2.newElements().get(0)),
				"expected new elements return 1 inbox entry!");
	}

	@Test
	public void test_inboxEntryComparison_withoutProductFamily_expectFinished() {
		// creating a polling run with method newInstanceWithoutProductFamily will compare inbox entries
		// from persistence and pickup ignoring the product family so that legacy data from persistence
		// does not lead to re-ingestion

		final InboxEntry inboxEntryWithoutProductFamily = createInboxEntryWithoutProductFamily();

		final PollingRun pollingRunExpectFinished = PollingRun.newInstanceWithoutProductFamily(
				Collections.singleton(inboxEntryWithoutProductFamily), Collections.emptyList(),
				STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectFinished.newElements()),
				"expected new elements to be empty!");
		assertTrue(CollectionUtil.isNotEmpty(pollingRunExpectFinished.finishedElements())
				&& pollingRunExpectFinished.finishedElements().size() == 1,
				"expected finished elements return 1 inbox entry!");
		assertTrue(inboxEntryWithoutProductFamily.equals(pollingRunExpectFinished.finishedElements().iterator().next()),
				"expected finished element to equal persisted element");

		final InboxEntry inboxEntry = createInboxEntry();

		final PollingRun pollingRunExpectFinished2 = PollingRun.newInstanceWithoutProductFamily(
				Collections.singleton(inboxEntry), Collections.emptyList(), STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectFinished2.newElements()),
				"expected new elements to be empty!");
		assertTrue(CollectionUtil.isNotEmpty(pollingRunExpectFinished2.finishedElements())
				&& pollingRunExpectFinished2.finishedElements().size() == 1,
				"expected finished elements return 1 inbox entry!");
		assertTrue(inboxEntry.equals(pollingRunExpectFinished2.finishedElements().iterator().next()),
				"expected finished element to equal persisted element");
	}

	@Test
	public void test_stationRetentionTime_withoutProductFamily_expectFinished() {
		// creating a polling run with method newInstanceWithoutProductFamily will compare inbox entries
		// from persistence and pickup ignoring the product family so that legacy data from persistence
		// does not lead to re-ingestion

		final int stationRetentionTimeInDays = 7;
		final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

		final InboxEntry inboxEntryExpectFinished = createInboxEntryWithoutProductFamily();
		inboxEntryExpectFinished.setKnownSince(now.minusDays(stationRetentionTimeInDays).minusSeconds(1));

		final PollingRun pollingRunExpectFinished = PollingRun.newInstanceWithoutProductFamily(
				Collections.singleton(inboxEntryExpectFinished), Collections.emptyList(), stationRetentionTimeInDays);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectFinished.newElements()),
				"expected new elements to be empty!");
		assertTrue(CollectionUtil.isNotEmpty(pollingRunExpectFinished.finishedElements())
				&& pollingRunExpectFinished.finishedElements().size() == 1
				&& inboxEntryExpectFinished.equals(pollingRunExpectFinished.finishedElements().iterator().next()),
				"expected finished elements return 1 inbox entry!");
	}

	@Test
	public void test_stationRetentionTime_withoutProductFamily_expectNotFinished() {
		// creating a polling run with method newInstanceWithoutProductFamily will compare inbox entries
		// from persistence and pickup ignoring the product family so that legacy data from persistence
		// does not lead to re-ingestion

		final int stationRetentionTimeInDays = 7;
		final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

		final InboxEntry inboxEntryExpectNotFinished = createInboxEntryWithoutProductFamily();
		inboxEntryExpectNotFinished.setKnownSince(now.minusDays(stationRetentionTimeInDays).plusSeconds(1));

		final PollingRun pollingRunExpectFinished = PollingRun.newInstanceWithoutProductFamily(
				Collections.singleton(inboxEntryExpectNotFinished), Collections.emptyList(), stationRetentionTimeInDays);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectFinished.newElements()),
				"expected new elements to be empty!");
		assertTrue(CollectionUtil.isEmpty(pollingRunExpectFinished.finishedElements()),
				"expected finished elements to be empty!");
	}

	@Test
	public void test_inboxEntryComparison_withProductFamily_expectAllEmpty() {
		// creating a polling run with method newInstance will take product family into account
		// when comparing inbox entries from persistence with inbox entries from pickup

		final PollingRun pollingRunExpectAllEmpty = PollingRun.newInstance(Collections.emptySet(),
				Collections.emptyList(), STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectAllEmpty.finishedElements()),
				"expected finished elements to be empty!");
		assertTrue(CollectionUtil.isEmpty(pollingRunExpectAllEmpty.newElements()),
				"expected new elements to be empty!");

		final InboxEntry inboxEntry = createInboxEntry();

		final PollingRun pollingRunExpectAllEmpty2 = PollingRun.newInstance(
				Collections.singleton(inboxEntry), Collections.singletonList(inboxEntry), STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectAllEmpty2.finishedElements()),
				"expected finished elements to be empty!");
		assertTrue(CollectionUtil.isEmpty(pollingRunExpectAllEmpty2.newElements()),
				"expected new elements to be empty!");
	}

	@Test
	public void test_inboxEntryComparison_withProductFamily_expectNew() {
		// creating a polling run with method newInstance will take product family into account
		// when comparing inbox entries from persistence with inbox entries from pickup

		final InboxEntry inboxEntry = createInboxEntry();

		final PollingRun pollingRunExpectNew = PollingRun.newInstance(Collections.emptySet(),
				Collections.singletonList(inboxEntry), STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectNew.finishedElements()),
				"expected finished elements to be empty!");
		assertTrue(	CollectionUtil.isNotEmpty(pollingRunExpectNew.newElements())
				&& pollingRunExpectNew.newElements().size() == 1
				&& inboxEntry.equals(pollingRunExpectNew.newElements().get(0)),
				"expected new elements return 1 inbox entry!");

		final InboxEntry inboxEntryWithoutProductFamily = createInboxEntryWithoutProductFamily();

		final PollingRun pollingRunExpectNew2 = PollingRun.newInstance(Collections.emptySet(),
				Collections.singletonList(inboxEntryWithoutProductFamily), STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectNew2.finishedElements()),
				"expected finished elements to be empty!");
		assertTrue(CollectionUtil.isNotEmpty(pollingRunExpectNew2.newElements())
				&& pollingRunExpectNew2.newElements().size() == 1
				&& inboxEntryWithoutProductFamily.equals(pollingRunExpectNew2.newElements().get(0)),
				"expected new elements return 1 inbox entry!");

	}

	@Test
	public void test_inboxEntryComparison_withProductFamily_expectFinished() {
		// creating a polling run with method newInstance will take product family into account
		// when comparing inbox entries from persistence with inbox entries from pickup

		final InboxEntry inboxEntry = createInboxEntry();

		final PollingRun pollingRunExpectFinished = PollingRun.newInstance(
				Collections.singleton(inboxEntry), Collections.emptyList(), STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectFinished.newElements()),
				"expected new elements to be empty!");
		assertTrue(CollectionUtil.isNotEmpty(pollingRunExpectFinished.finishedElements())
				&& pollingRunExpectFinished.finishedElements().size() == 1
				&& inboxEntry.equals(pollingRunExpectFinished.finishedElements().iterator().next()),
				"expected finished elements return 1 inbox entry!");

		final InboxEntry inboxEntryWithoutProductFamily = createInboxEntryWithoutProductFamily();

		final PollingRun pollingRunExpectFinished2 = PollingRun.newInstance(
				Collections.singleton(inboxEntryWithoutProductFamily), Collections.emptyList(),
				STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectFinished2.newElements()),
				"expected new elements to be empty!");
		assertTrue(CollectionUtil.isNotEmpty(pollingRunExpectFinished2.finishedElements())
				&& pollingRunExpectFinished2.finishedElements().size() == 1
				&& inboxEntryWithoutProductFamily.equals(pollingRunExpectFinished2.finishedElements().iterator().next()),
				"expected finished elements return 1 inbox entry!");
	}

	@Test
	@Ignore // FIXME This test runs non-deterministic. Disable until behaviour is fixed
	public void test_inboxEntryComparison_withProductFamily_expectFinishedAndNew() {
		// creating a polling run with method newInstance will take product family into account
		// when comparing inbox entries from persistence with inbox entries from pickup

		final InboxEntry inboxEntry = createInboxEntry();
		final InboxEntry inboxEntryWithoutProductFamily = createInboxEntryWithoutProductFamily();

		final PollingRun pollingRunExpectFinishedAndNew = PollingRun.newInstance(
				Collections.singleton(inboxEntry), Collections.singletonList(inboxEntryWithoutProductFamily),
				STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isNotEmpty(pollingRunExpectFinishedAndNew.newElements())
				&& pollingRunExpectFinishedAndNew.newElements().size() == 1
				&& inboxEntryWithoutProductFamily.equals(pollingRunExpectFinishedAndNew.newElements().iterator().next()),
				"expected new elements return 1 inbox entry!");
		assertTrue(CollectionUtil.isNotEmpty(pollingRunExpectFinishedAndNew.finishedElements())
				&& pollingRunExpectFinishedAndNew.finishedElements().size() == 1
				&& inboxEntry.equals(pollingRunExpectFinishedAndNew.finishedElements().iterator().next()),
				"expected finished elements return 1 inbox entry!");

		final PollingRun pollingRunExpectFinishedAndNew2 = PollingRun.newInstance(
				Collections.singleton(inboxEntryWithoutProductFamily), Collections.singletonList(inboxEntry),
				STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isNotEmpty(pollingRunExpectFinishedAndNew2.newElements())
				&& pollingRunExpectFinishedAndNew2.newElements().size() == 1
				&& inboxEntry.equals(pollingRunExpectFinishedAndNew2.newElements().iterator().next()),
				"expected new elements return 1 inbox entry!");
		assertTrue(CollectionUtil.isNotEmpty(pollingRunExpectFinishedAndNew2.finishedElements())
				&& pollingRunExpectFinishedAndNew2.finishedElements().size() == 1
				&& inboxEntryWithoutProductFamily.equals(pollingRunExpectFinishedAndNew2.finishedElements().iterator().next()),
				"expected finished elements return 1 inbox entry!");
	}

	@Test
	public void test_inboxEntryComparison_withProductFamily_differentModificationDate_expectAllEmpty() {

		final InboxEntry inboxEntry = createInboxEntry(LAST_MODIFIED_1);
		final InboxEntry inboxEntry2 = createInboxEntry(LAST_MODIFIED_2);

		final PollingRun pollingRunExpectAllEmpty = PollingRun.newInstance(
				Collections.singleton(inboxEntry), Collections.singletonList(inboxEntry2), STATION_RETENTION_TIME_DEFAULT);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectAllEmpty.finishedElements()),
				"expected finished elements to be empty!");
		assertTrue(CollectionUtil.isEmpty(pollingRunExpectAllEmpty.newElements()),
				"expected new elements to be empty!");
	}

	@Test
	public void test_stationRetentionTime_withProductFamily_expectFinished() {
		final int stationRetentionTimeInDays = 7;
		final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

		final InboxEntry inboxEntryExpectFinished = createInboxEntryWithoutProductFamily();
		inboxEntryExpectFinished.setKnownSince(now.minusDays(stationRetentionTimeInDays).minusSeconds(1));

		final PollingRun pollingRunExpectFinished = PollingRun.newInstance(
				Collections.singleton(inboxEntryExpectFinished), Collections.emptyList(), stationRetentionTimeInDays);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectFinished.newElements()),
				"expected new elements to be empty!");
		assertTrue(CollectionUtil.isNotEmpty(pollingRunExpectFinished.finishedElements())
				&& pollingRunExpectFinished.finishedElements().size() == 1
				&& inboxEntryExpectFinished.equals(pollingRunExpectFinished.finishedElements().iterator().next()),
				"expected finished elements return 1 inbox entry!");
	}

	@Test
	public void test_stationRetentionTime_withProductFamily_expectNotFinished() {
		final int stationRetentionTimeInDays = 7;
		final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

		final InboxEntry inboxEntryExpectNotFinished = createInboxEntryWithoutProductFamily();
		inboxEntryExpectNotFinished.setKnownSince(now.minusDays(stationRetentionTimeInDays).plusSeconds(1));

		final PollingRun pollingRunExpectFinished = PollingRun.newInstance(
				Collections.singleton(inboxEntryExpectNotFinished), Collections.emptyList(), stationRetentionTimeInDays);

		assertTrue(CollectionUtil.isEmpty(pollingRunExpectFinished.newElements()),
				"expected new elements to be empty!");
		assertTrue(CollectionUtil.isEmpty(pollingRunExpectFinished.finishedElements()),
				"expected finished elements to be empty!");
	}

	// --------------------------------------------------------------------------

	private static InboxEntry createInboxEntry() {
		final InboxEntry inboxEntry = new InboxEntry(FILE_PATH_1, FILE_PATH_1, PICKUP_URL_1, LAST_MODIFIED_1, SIZE_1,
				PROCESSING_POD_1, INBOX_TYPE_1, PRODUCT_FAMILY_1, STATION_NAME_1, MISSION_ID);
		inboxEntry.setKnownSince(KNOWN_SINCE_1);

		return inboxEntry;
	}

	private static InboxEntry createInboxEntry(final Date lastModified) {
		final InboxEntry inboxEntry = new InboxEntry(FILE_PATH_1, FILE_PATH_1, PICKUP_URL_1, lastModified, SIZE_1,
				PROCESSING_POD_1, INBOX_TYPE_1, PRODUCT_FAMILY_1, STATION_NAME_1, MISSION_ID);
		inboxEntry.setKnownSince(KNOWN_SINCE_1);

		return inboxEntry;
	}

	private static InboxEntry createInboxEntryWithoutProductFamily() {
		final InboxEntry inboxEntry = new InboxEntry(FILE_PATH_1, FILE_PATH_1, PICKUP_URL_1, LAST_MODIFIED_1, SIZE_1,
				PROCESSING_POD_1, INBOX_TYPE_1, null, STATION_NAME_1, MISSION_ID);
		inboxEntry.setKnownSince(KNOWN_SINCE_1);

		return inboxEntry;
	}

}


