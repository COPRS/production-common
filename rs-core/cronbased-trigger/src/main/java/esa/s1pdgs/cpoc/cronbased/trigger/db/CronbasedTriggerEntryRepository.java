package esa.s1pdgs.cpoc.cronbased.trigger.db;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductFamily;

@Component
public interface CronbasedTriggerEntryRepository extends MongoRepository<CronbasedTriggerEntry, String> {

	public List<CronbasedTriggerEntry> findByProductTypeAndProductFamilyAndPod(String productType,
			ProductFamily productFamily, String pod);
}
