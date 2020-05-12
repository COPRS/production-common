package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.datalifecycle.trigger.config.DataLifecycleTriggerConfigurationProperties.RetentionPolicy;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;

public class DataLifecycleTriggerListenerTest {

	@Test
	public void calculateEvictionDate() {

		DataLifecycleTriggerListener<IngestionEvent> dtl = new DataLifecycleTriggerListener<>(null, null, null, null);

		List<RetentionPolicy> retentionPolicies = new ArrayList<>();

		RetentionPolicy rp = new RetentionPolicy();
		rp.setFilePattern("DCS_[0-9][0-9]_[a-zA-Z0-9-]+_ch[0-9]_DSDB_[0-9]{5}\\.raw");
		rp.setProductFamily("EDRS_SESSION");
		rp.setRetentionTimeDays(7);

		retentionPolicies.add(rp);

		Date creationDate = Date.from(Instant.parse("2000-01-01T00:00:00.00z"));

		String obsKey = "L20191204153633245000201/DCS_02_L20191204153633245000201_ch2_DSDB_00027.raw";
		Date evictionDate = dtl.calculateEvictionDate(retentionPolicies, creationDate, ProductFamily.EDRS_SESSION,
				obsKey);
		Assert.assertEquals(Instant.parse("2000-01-08T00:00:00.00z"), evictionDate.toInstant());
	}

}
