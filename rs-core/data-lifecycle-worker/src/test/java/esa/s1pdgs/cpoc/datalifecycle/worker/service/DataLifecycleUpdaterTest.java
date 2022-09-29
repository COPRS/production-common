package esa.s1pdgs.cpoc.datalifecycle.worker.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.datalifecycle.client.DataLifecycleClientUtil;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.RetentionPolicy;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepositoryException;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.LtaDownloadEvent;

public class DataLifecycleUpdaterTest {

	private DataLifecycleUpdater uut;

	private List<RetentionPolicy> retentionPolicies;

	@Mock
	private DataLifecycleMetadataRepository repo;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		retentionPolicies = new ArrayList<>();

		RetentionPolicy r1 = new RetentionPolicy();
		r1.setProductFamily(ProductFamily.S3_GRANULES);
		r1.setFilePattern("S3.*");
		r1.setRetentionTimeDays(10);
		retentionPolicies.add(r1);

		RetentionPolicy r2 = new RetentionPolicy();
		r2.setProductFamily(ProductFamily.S2_AUX);
		r2.setFilePattern("S2.*AUX.*");
		r2.setRetentionTimeDays(30);
		retentionPolicies.add(r2);

		RetentionPolicy r3 = new RetentionPolicy();
		r3.setProductFamily(ProductFamily.S2_AUX_ZIP);
		r3.setFilePattern("S2.*AUX.*\\.zip");
		r3.setRetentionTimeDays(7);
		retentionPolicies.add(r3);

		final RetentionPolicy r4 = new RetentionPolicy();
		r4.setFilePattern("DCS_[0-9][0-9]_[a-zA-Z0-9-]+_ch[0-9]_DSDB_[0-9]{5}\\.raw");
		r4.setProductFamily(ProductFamily.EDRS_SESSION);
		r4.setRetentionTimeDays(4);
		retentionPolicies.add(r4);

		final RetentionPolicy r5 = new RetentionPolicy();
		r5.setFilePattern("S1[ABCD]_AUX_PP1");
		r5.setProductFamily(ProductFamily.AUXILIARY_FILE);
		r5.setRetentionTimeDays(-1);
		retentionPolicies.add(r5);

		final RetentionPolicy r6 = new RetentionPolicy();
		r6.setFilePattern("S1[ABCD]_(S[1-6]|IW|EW|WV)_OCN__2S[SD].*\\.zip");
		r6.setProductFamily(ProductFamily.L2_ACN_ZIP);
		r6.setRetentionTimeDays(7);
		retentionPolicies.add(r6);

		final RetentionPolicy r7 = new RetentionPolicy();
		r7.setFilePattern("S1[ABCD]_WV_RAW__0S[SD]");
		r7.setProductFamily(ProductFamily.L0_SLICE);
		r7.setRetentionTimeDays(4);
		retentionPolicies.add(r7);

		final RetentionPolicy r8 = new RetentionPolicy();
		r8.setFilePattern("S1[ABCD]_GP_RAW__0_");
		r8.setProductFamily(ProductFamily.L0_BLANK);
		r8.setRetentionTimeDays(2);
		retentionPolicies.add(r8);

		uut = new DataLifecycleUpdater(retentionPolicies, null, repo);

	}

	@Test
	public void updateMetadata_uncompressed() throws DataLifecycleMetadataRepositoryException, InterruptedException {

		LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));

		CatalogEvent event = new CatalogEvent();
		event.setProductFamily(ProductFamily.S3_GRANULES);
		event.setKeyObjectStorage("S3file");

		ArgumentCaptor<DataLifecycleMetadata> usedMetadata = ArgumentCaptor.forClass(DataLifecycleMetadata.class);

		uut.updateMetadata(event, now);
		verify(repo).upsert(usedMetadata.capture(), anyMap());

		assertEquals("S3file", usedMetadata.getValue().getPathInUncompressedStorage());
		assertEquals(ProductFamily.S3_GRANULES, usedMetadata.getValue().getProductFamilyInUncompressedStorage());
		assertEquals(
				LocalDateTime.ofInstant(event.getCreationDate().toInstant().plus(Period.ofDays(10)), ZoneId.of("UTC")),
				usedMetadata.getValue().getEvictionDateInUncompressedStorage());
		assertEquals(now, usedMetadata.getValue().getLastInsertionInUncompressedStorage());

		assertEquals(null, usedMetadata.getValue().getPathInCompressedStorage());
		assertEquals(null, usedMetadata.getValue().getProductFamilyInCompressedStorage());
		assertEquals(null, usedMetadata.getValue().getEvictionDateInCompressedStorage());
		assertEquals(null, usedMetadata.getValue().getLastInsertionInCompressedStorage());
	}

	@Test
	public void updateMetadata_compressed() throws DataLifecycleMetadataRepositoryException, InterruptedException {

		LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));

		CatalogEvent event = new CatalogEvent();
		event.setProductFamily(ProductFamily.S2_AUX_ZIP);
		event.setKeyObjectStorage("S2fileAUX.zip");

		ArgumentCaptor<DataLifecycleMetadata> usedMetadata = ArgumentCaptor.forClass(DataLifecycleMetadata.class);

		uut.updateMetadata(event, now);
		verify(repo).upsert(usedMetadata.capture(), anyMap());

		assertEquals(null, usedMetadata.getValue().getPathInUncompressedStorage());
		assertEquals(null, usedMetadata.getValue().getProductFamilyInUncompressedStorage());
		assertEquals(null, usedMetadata.getValue().getEvictionDateInUncompressedStorage());
		assertEquals(null, usedMetadata.getValue().getLastInsertionInUncompressedStorage());

		assertEquals("S2fileAUX.zip", usedMetadata.getValue().getPathInCompressedStorage());
		assertEquals(ProductFamily.S2_AUX_ZIP, usedMetadata.getValue().getProductFamilyInCompressedStorage());
		assertEquals(
				LocalDateTime.ofInstant(event.getCreationDate().toInstant().plus(Period.ofDays(7)), ZoneId.of("UTC")),
				usedMetadata.getValue().getEvictionDateInCompressedStorage());
		assertEquals(now, usedMetadata.getValue().getLastInsertionInCompressedStorage());
	}

	@Test
	public void updateMetadata_not_match() throws DataLifecycleMetadataRepositoryException, InterruptedException {

		LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));

		CatalogEvent event = new CatalogEvent();
		event.setProductFamily(ProductFamily.S2_AUX_ZIP);
		event.setKeyObjectStorage("S2file.zip");

		ArgumentCaptor<DataLifecycleMetadata> usedMetadata = ArgumentCaptor.forClass(DataLifecycleMetadata.class);

		uut.updateMetadata(event, now);
		verify(repo).upsert(usedMetadata.capture(), anyMap());

		assertEquals(null, usedMetadata.getValue().getPathInUncompressedStorage());
		assertEquals(null, usedMetadata.getValue().getProductFamilyInUncompressedStorage());
		assertEquals(null, usedMetadata.getValue().getEvictionDateInUncompressedStorage());
		assertEquals(null, usedMetadata.getValue().getLastInsertionInUncompressedStorage());

		assertEquals("S2file.zip", usedMetadata.getValue().getPathInCompressedStorage());
		assertEquals(ProductFamily.S2_AUX_ZIP, usedMetadata.getValue().getProductFamilyInCompressedStorage());
		assertEquals(null, usedMetadata.getValue().getEvictionDateInCompressedStorage());
		assertEquals(now, usedMetadata.getValue().getLastInsertionInCompressedStorage());
	}

	@Test
	public void calculateEvictionDate() {

		final Date creationDate = Date.from(Instant.parse("2000-01-01T00:00:00.00z"));
		final String obsKey = "L20191204153633245000201/DCS_02_L20191204153633245000201_ch2_DSDB_00027.raw";

		final Date evictionDate = DataLifecycleClientUtil.calculateEvictionDate(retentionPolicies, creationDate,
				ProductFamily.EDRS_SESSION, DataLifecycleClientUtil.getFileName(obsKey));
		assertEquals(Instant.parse("2000-01-05T00:00:00.00z"), evictionDate.toInstant());
	}

	@Test
	public void needsInsertionTimeUpdate() {
		final AbstractMessage yes[] = { new CompressionEvent(), new CatalogEvent(),
				new LtaDownloadEvent() };
		final AbstractMessage no[] = { new EvictionEvent() /* and all the others */ };

		for (final AbstractMessage event : yes) {
			assertTrue("expected " + event.getClass().getSimpleName() + " to need insertion time update",
					DataLifecycleUpdater.needsInsertionTimeUpdate(event));
		}

		for (final AbstractMessage event : no) {
			assertFalse("expected " + event.getClass().getSimpleName() + " to NOT need insertion time update",
					DataLifecycleUpdater.needsInsertionTimeUpdate(event));
		}
	}

}
