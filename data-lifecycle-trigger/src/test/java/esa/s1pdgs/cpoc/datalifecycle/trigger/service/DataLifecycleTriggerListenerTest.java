package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.datalifecycle.trigger.config.DataLifecycleTriggerConfigurationProperties.RetentionPolicy;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionManagementJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;

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
		
		final DataLifecycleTriggerListener<IngestionEvent> dtl = new DataLifecycleTriggerListener<>(null, null, null);
		final Date evictionDate = dtl.calculateEvictionDate(retentionPolicies, creationDate, ProductFamily.EDRS_SESSION,
				obsKey);
		Assert.assertEquals(Instant.parse("2000-01-05T00:00:00.00z"), evictionDate.toInstant());
	}
	
	@Test
	public void toEvictionManagementJob_EDRS_SESSSION() {
		
		final Date creationDate = Date.from(Instant.parse("2000-01-01T00:00:00.00z"));
		final String obsKey = "L20191204153633245000201/DCS_02_L20191204153633245000201_ch2_DSDB_00027.raw";
		final ProductFamily productFamily = ProductFamily.EDRS_SESSION;
		
		final IngestionEvent inputEvent = toInputEvent(creationDate, obsKey, productFamily);
		
		final DataLifecycleTriggerListener<IngestionEvent> dtl = new DataLifecycleTriggerListener<>(null, null, null);
		final EvictionManagementJob evictionManagementJob = dtl.toEvictionManagementJob(inputEvent, retentionPolicies, UUID.randomUUID());
		Assert.assertEquals(productFamily, evictionManagementJob.getProductFamily());
		Assert.assertEquals(Date.from(Instant.parse("2000-01-05T00:00:00.00z")), evictionManagementJob.getEvictionDate());
		Assert.assertEquals(false, evictionManagementJob.isUnlimited());
		Assert.assertEquals(obsKey, evictionManagementJob.getKeyObjectStorage());
	}
	
	@Test
	public void toEvictionManagementJob_AUX_PP1() {
		
		final Date creationDate = Date.from(Instant.parse("2000-01-01T00:00:00.00z"));
		final String obsKey = "S1A_AUX_PP1";
		final ProductFamily productFamily = ProductFamily.AUXILIARY_FILE;
		
		final IngestionEvent inputEvent = toInputEvent(creationDate, obsKey, productFamily);
		
		final DataLifecycleTriggerListener<IngestionEvent> dtl = new DataLifecycleTriggerListener<>(null, null, null);
		final EvictionManagementJob evictionManagementJob = dtl.toEvictionManagementJob(inputEvent, retentionPolicies, UUID.randomUUID());
		Assert.assertEquals(productFamily, evictionManagementJob.getProductFamily());
		Assert.assertEquals(null, evictionManagementJob.getEvictionDate());
		Assert.assertEquals(true, evictionManagementJob.isUnlimited());
		Assert.assertEquals(obsKey, evictionManagementJob.getKeyObjectStorage());
	}
	
	@Test
	public void toEvictionManagementJob_L2_ACN_ZIP() {
		
		final Date creationDate = Date.from(Instant.parse("2000-01-01T00:00:00.00z"));
		final String obsKey = "S1B_S1_OCN__2SS.zip";
		final ProductFamily productFamily = ProductFamily.L2_ACN_ZIP;
		
		final IngestionEvent inputEvent = toInputEvent(creationDate, obsKey, productFamily);
		
		final DataLifecycleTriggerListener<IngestionEvent> dtl = new DataLifecycleTriggerListener<>(null, null, null);
		final EvictionManagementJob evictionManagementJob = dtl.toEvictionManagementJob(inputEvent, retentionPolicies, UUID.randomUUID());
		Assert.assertEquals(productFamily, evictionManagementJob.getProductFamily());
		Assert.assertEquals(Date.from(Instant.parse("2000-01-08T00:00:00.00z")), evictionManagementJob.getEvictionDate());
		Assert.assertEquals(false, evictionManagementJob.isUnlimited());
		Assert.assertEquals(obsKey, evictionManagementJob.getKeyObjectStorage());
		
	}
	
	@Test
	public void toEvictionManagementJob_NOT_CONFIGURED() {
		
		final Date creationDate = Date.from(Instant.parse("2000-01-01T00:00:00.00z"));
		final String obsKey = "S1A_S1_RAW__0SD";
		final ProductFamily productFamily = ProductFamily.L0_SLICE;
		
		final IngestionEvent inputEvent = toInputEvent(creationDate, obsKey, productFamily);
		
		final DataLifecycleTriggerListener<IngestionEvent> dtl = new DataLifecycleTriggerListener<>(null, null, null);
		final EvictionManagementJob evictionManagementJob = dtl.toEvictionManagementJob(inputEvent, retentionPolicies, UUID.randomUUID());
		Assert.assertEquals(productFamily, evictionManagementJob.getProductFamily());
		Assert.assertEquals(null, evictionManagementJob.getEvictionDate());
		Assert.assertEquals(true, evictionManagementJob.isUnlimited());
		Assert.assertEquals(obsKey, evictionManagementJob.getKeyObjectStorage());
		
	}

	private IngestionEvent toInputEvent(final Date creationDate, final String obsKey, final ProductFamily productFamily) {
		final IngestionEvent inputEvent = new IngestionEvent();
		inputEvent.setCreationDate(creationDate);
		inputEvent.setKeyObjectStorage(obsKey);
		inputEvent.setProductFamily(productFamily);
		return inputEvent;
	}

}
