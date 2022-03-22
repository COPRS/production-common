package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.datalifecycle.client.DataLifecycleClientUtil;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.RetentionPolicy;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.LtaDownloadEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;

public class DataLifecycleTriggerListenerTest {
	
	List<RetentionPolicy> retentionPolicies = new ArrayList<>();
	
	@Before
	public void init() {
		final RetentionPolicy rp1 = new RetentionPolicy();
		rp1.setFilePattern("DCS_[0-9][0-9]_[a-zA-Z0-9-]+_ch[0-9]_DSDB_[0-9]{5}\\.raw");
		rp1.setProductFamily("EDRS_SESSION");
		rp1.setRetentionTimeDays(4);
		retentionPolicies.add(rp1);
		
		final RetentionPolicy rp2 = new RetentionPolicy();
		rp2.setFilePattern("S1[ABCD]_AUX_PP1");
		rp2.setProductFamily("AUXILIARY_FILE");
		rp2.setRetentionTimeDays(-1);
		retentionPolicies.add(rp2);
		
		final RetentionPolicy rp3 = new RetentionPolicy();
		rp3.setFilePattern("S1[ABCD]_(S[1-6]|IW|EW|WV)_OCN__2S[SD].*\\.zip");
		rp3.setProductFamily("L2_ACN_ZIP");
		rp3.setRetentionTimeDays(7);
		retentionPolicies.add(rp3);
		
		final RetentionPolicy rp4 = new RetentionPolicy();
		rp4.setFilePattern("S1[ABCD]_WV_RAW__0S[SD]");
		rp4.setProductFamily("L0_SLICE");
		rp4.setRetentionTimeDays(4);
		retentionPolicies.add(rp4);
		
		final RetentionPolicy rp5 = new RetentionPolicy();
		rp5.setFilePattern("S1[ABCD]_GP_RAW__0_");
		rp5.setProductFamily("L0_BLANK");
		rp5.setRetentionTimeDays(2);
		retentionPolicies.add(rp5);
	}

	@Test
	public void calculateEvictionDate() {

		final Date creationDate = Date.from(Instant.parse("2000-01-01T00:00:00.00z"));
		final String obsKey = "L20191204153633245000201/DCS_02_L20191204153633245000201_ch2_DSDB_00027.raw";
		
		final Date evictionDate = DataLifecycleClientUtil.calculateEvictionDate(retentionPolicies, creationDate, ProductFamily.EDRS_SESSION,
				DataLifecycleClientUtil.getFileName(obsKey));
		Assert.assertEquals(Instant.parse("2000-01-05T00:00:00.00z"), evictionDate.toInstant());
	}

	@Test
	public void needsInsertionTimeUpdate() {
		final AbstractMessage yes[] = { new IngestionEvent(), new CompressionEvent(), new ProductionEvent(), new LtaDownloadEvent() };
		final AbstractMessage no[] = { new EvictionEvent() /* and all the others */ };

		for (final AbstractMessage event : yes) {
			Assert.assertTrue("expected " + event.getClass().getSimpleName() + " to need insertion time update",
					DataLifecycleTriggerListener.needsInsertionTimeUpdate(event));
		}

		for (final AbstractMessage event : no) {
			Assert.assertFalse("expected " + event.getClass().getSimpleName() + " to NOT need insertion time update",
					DataLifecycleTriggerListener.needsInsertionTimeUpdate(event));
		}
	}

	@Test
	public void needsEvictionTimeShorteningInUncompressedStorage() {
		final AbstractMessage yes[] = { //
				new CompressionEvent(ProductFamily.L1_SLICE_ZIP, null, null), //
				new CompressionEvent(ProductFamily.L1_ACN_ZIP, null, null) //
		};

		final EvictionEvent evictionEvent1 = new EvictionEvent();
		evictionEvent1.setProductFamily(ProductFamily.L1_SLICE);
		final EvictionEvent evictionEvent2 = new EvictionEvent();
		evictionEvent2.setProductFamily(ProductFamily.L1_SLICE_ZIP);
		final LtaDownloadEvent ltaDownloadEvent1 = new LtaDownloadEvent();
		ltaDownloadEvent1.setProductFamily(ProductFamily.L1_ACN);
		final LtaDownloadEvent ltaDownloadEvent2 = new LtaDownloadEvent();
		ltaDownloadEvent2.setProductFamily(ProductFamily.L1_ACN_ZIP);

		final AbstractMessage no[] = { //
				new CompressionEvent(ProductFamily.L1_SLICE, null, null), //
				new CompressionEvent(ProductFamily.L1_ACN, null, null), //
				new IngestionEvent(ProductFamily.L1_SLICE, null, null, 0, null, null, null, null), //
				new IngestionEvent(ProductFamily.L1_SLICE_ZIP, null, null, 0, null, null, null, null), //
				new ProductionEvent(null, null, ProductFamily.L1_ACN), //
				new ProductionEvent(null, null, ProductFamily.L1_ACN_ZIP), //
				evictionEvent1, evictionEvent2, //
				ltaDownloadEvent1, ltaDownloadEvent2 //
				/* and all the others */
		};

		final Map<ProductFamily, Integer> shortingEvictionTimeAfterCompression = new HashMap<>();
		shortingEvictionTimeAfterCompression.put(ProductFamily.L1_SLICE_ZIP, 6);
		shortingEvictionTimeAfterCompression.put(ProductFamily.L1_ACN_ZIP, 6);

		for (final AbstractMessage event : yes) {
			Assert.assertTrue("expected " + event.getClass().getSimpleName() + "[" + event.getProductFamily() + "] to need eviction time update",
					DataLifecycleTriggerListener.needsEvictionTimeShorteningInUncompressedStorage(event, shortingEvictionTimeAfterCompression));
		}

		for (final AbstractMessage event : no) {
			Assert.assertFalse("expected " + event.getClass().getSimpleName() + "[" + event.getProductFamily() + "] to NOT need eviction time update",
					DataLifecycleTriggerListener.needsEvictionTimeShorteningInUncompressedStorage(event, shortingEvictionTimeAfterCompression));
		}
	}

}
