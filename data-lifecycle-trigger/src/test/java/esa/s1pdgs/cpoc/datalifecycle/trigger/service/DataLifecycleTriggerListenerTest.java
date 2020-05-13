package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
		RetentionPolicy rp1 = new RetentionPolicy();
		rp1.setFilePattern("DCS_[0-9][0-9]_[a-zA-Z0-9-]+_ch[0-9]_DSDB_[0-9]{5}\\.raw");
		rp1.setProductFamily("EDRS_SESSION");
		rp1.setRetentionTimeDays(4);
		retentionPolicies.add(rp1);
		
		RetentionPolicy rp2 = new RetentionPolicy();
		rp2.setFilePattern("S1[ABCD]_AUX_PP1");
		rp2.setProductFamily("AUXILIARY_FILE");
		rp2.setRetentionTimeDays(-1);
		retentionPolicies.add(rp2);
		
		RetentionPolicy rp3 = new RetentionPolicy();
		rp3.setFilePattern("S1[ABCD]_(S[1-6]|IW|EW|WV)_OCN__2S[SD].*\\.zip");
		rp3.setProductFamily("L2_ACN_ZIP");
		rp3.setRetentionTimeDays(7);
		retentionPolicies.add(rp3);
		
		RetentionPolicy rp4 = new RetentionPolicy();
		rp4.setFilePattern("S1[ABCD]_WV_RAW__0S[SD]");
		rp4.setProductFamily("L0_SLICE");
		rp4.setRetentionTimeDays(4);
		retentionPolicies.add(rp4);
		
		RetentionPolicy rp5 = new RetentionPolicy();
		rp5.setFilePattern("S1[ABCD]_GP_RAW__0_");
		rp5.setProductFamily("L0_BLANK");
		rp5.setRetentionTimeDays(2);
		retentionPolicies.add(rp5);
	}

	@Test
	public void calculateEvictionDate() {

		Date creationDate = Date.from(Instant.parse("2000-01-01T00:00:00.00z"));
		String obsKey = "L20191204153633245000201/DCS_02_L20191204153633245000201_ch2_DSDB_00027.raw";
		
		DataLifecycleTriggerListener<IngestionEvent> dtl = new DataLifecycleTriggerListener<>(null, null, null, null);
		Date evictionDate = dtl.calculateEvictionDate(retentionPolicies, creationDate, ProductFamily.EDRS_SESSION,
				obsKey);
		Assert.assertEquals(Instant.parse("2000-01-05T00:00:00.00z"), evictionDate.toInstant());
	}
	
	@Test
	public void toEvictionManagementJob_EDRS_SESSSION() {
		
		Date creationDate = Date.from(Instant.parse("2000-01-01T00:00:00.00z"));
		String obsKey = "L20191204153633245000201/DCS_02_L20191204153633245000201_ch2_DSDB_00027.raw";
		ProductFamily productFamily = ProductFamily.EDRS_SESSION;
		
		IngestionEvent inputEvent = toInputEvent(creationDate, obsKey, productFamily);
		
		DataLifecycleTriggerListener<IngestionEvent> dtl = new DataLifecycleTriggerListener<>(null, null, null, null);
		EvictionManagementJob evictionManagementJob = dtl.toEvictionManagementJob(inputEvent, retentionPolicies);
		Assert.assertEquals(productFamily, evictionManagementJob.getProductFamily());
		Assert.assertEquals(Date.from(Instant.parse("2000-01-05T00:00:00.00z")), evictionManagementJob.getEvictionDate());
		Assert.assertEquals(false, evictionManagementJob.isUnlimited());
		Assert.assertEquals(obsKey, evictionManagementJob.getKeyObjectStorage());
	}
	
	@Test
	public void toEvictionManagementJob_AUX_PP1() {
		
		Date creationDate = Date.from(Instant.parse("2000-01-01T00:00:00.00z"));
		String obsKey = "S1A_AUX_PP1";
		ProductFamily productFamily = ProductFamily.AUXILIARY_FILE;
		
		IngestionEvent inputEvent = toInputEvent(creationDate, obsKey, productFamily);
		
		DataLifecycleTriggerListener<IngestionEvent> dtl = new DataLifecycleTriggerListener<>(null, null, null, null);
		EvictionManagementJob evictionManagementJob = dtl.toEvictionManagementJob(inputEvent, retentionPolicies);
		Assert.assertEquals(productFamily, evictionManagementJob.getProductFamily());
		Assert.assertEquals(null, evictionManagementJob.getEvictionDate());
		Assert.assertEquals(true, evictionManagementJob.isUnlimited());
		Assert.assertEquals(obsKey, evictionManagementJob.getKeyObjectStorage());
	}
	
	@Test
	public void toEvictionManagementJob_L2_ACN_ZIP() {
		
		Date creationDate = Date.from(Instant.parse("2000-01-01T00:00:00.00z"));
		String obsKey = "S1B_S1_OCN__2SS.zip";
		ProductFamily productFamily = ProductFamily.L2_ACN_ZIP;
		
		IngestionEvent inputEvent = toInputEvent(creationDate, obsKey, productFamily);
		
		DataLifecycleTriggerListener<IngestionEvent> dtl = new DataLifecycleTriggerListener<>(null, null, null, null);
		EvictionManagementJob evictionManagementJob = dtl.toEvictionManagementJob(inputEvent, retentionPolicies);
		Assert.assertEquals(productFamily, evictionManagementJob.getProductFamily());
		Assert.assertEquals(Date.from(Instant.parse("2000-01-08T00:00:00.00z")), evictionManagementJob.getEvictionDate());
		Assert.assertEquals(false, evictionManagementJob.isUnlimited());
		Assert.assertEquals(obsKey, evictionManagementJob.getKeyObjectStorage());
		
	}
	
	@Test
	public void toEvictionManagementJob_NOT_CONFIGURED() {
		
		Date creationDate = Date.from(Instant.parse("2000-01-01T00:00:00.00z"));
		String obsKey = "S1A_S1_RAW__0SD";
		ProductFamily productFamily = ProductFamily.L0_SLICE;
		
		IngestionEvent inputEvent = toInputEvent(creationDate, obsKey, productFamily);
		
		DataLifecycleTriggerListener<IngestionEvent> dtl = new DataLifecycleTriggerListener<>(null, null, null, null);
		EvictionManagementJob evictionManagementJob = dtl.toEvictionManagementJob(inputEvent, retentionPolicies);
		Assert.assertEquals(productFamily, evictionManagementJob.getProductFamily());
		Assert.assertEquals(null, evictionManagementJob.getEvictionDate());
		Assert.assertEquals(true, evictionManagementJob.isUnlimited());
		Assert.assertEquals(obsKey, evictionManagementJob.getKeyObjectStorage());
		
	}

	private IngestionEvent toInputEvent(Date creationDate, String obsKey, ProductFamily productFamily) {
		IngestionEvent inputEvent = new IngestionEvent();
		inputEvent.setCreationDate(creationDate);
		inputEvent.setKeyObjectStorage(obsKey);
		inputEvent.setProductFamily(productFamily);
		return inputEvent;
	}

}
