package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.datalifecycle.trigger.config.DataLifecycleTriggerConfigurationProperties.RetentionPolicy;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleSortTerm;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleQueryFilter;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepositoryException;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;

public class DataLifecycleTriggerListenerTest {
	
	List<RetentionPolicy> retentionPolicies = new ArrayList<>();
	
	private DataLifecycleMetadataRepository metadataRepoMock = new DataLifecycleMetadataRepository() {
		@Override
		public void save(DataLifecycleMetadata metadata)
				throws DataLifecycleMetadataRepositoryException {
			// nothing
		}
		@Override
		public Optional<DataLifecycleMetadata> findByProductName(String productName)
				throws DataLifecycleMetadataRepositoryException {
			return Optional.empty();
		}
		@Override
		public List<DataLifecycleMetadata> findByEvictionDateBefore(LocalDateTime timestamp)
				throws DataLifecycleMetadataRepositoryException {
			return Collections.emptyList();
		}
		@Override
		public List<DataLifecycleMetadata> findByProductNames(List<String> productNames)
				throws DataLifecycleMetadataRepositoryException {
			return Collections.emptyList();
		}
		@Override
		public List<DataLifecycleMetadata> findWithFilters(List<DataLifecycleQueryFilter> filters,
				Optional<Integer> top, Optional<Integer> skip,	List<DataLifecycleSortTerm> sortTerms)
						throws DataLifecycleMetadataRepositoryException {
			return Collections.emptyList();
		}
	};
	
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
		
		final DataLifecycleTriggerListener<IngestionEvent> dtl = new DataLifecycleTriggerListener<>(null, null, null, this.metadataRepoMock, null, null, null);
		final Date evictionDate = dtl.calculateEvictionDate(retentionPolicies, creationDate, ProductFamily.EDRS_SESSION,
				dtl.getFileName(obsKey));
		Assert.assertEquals(Instant.parse("2000-01-05T00:00:00.00z"), evictionDate.toInstant());
	}
	
}
