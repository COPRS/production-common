package esa.s1pdgs.cpoc.mdc.timer.db;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import esa.s1pdgs.cpoc.common.ProductFamily;

public interface CatalogEventTimerEntryRepository extends MongoRepository<CatalogEventTimerEntry, String> {

	public List<CatalogEventTimerEntry> findByProductTypeAndProductFamily(String productType,
			ProductFamily productFamily);
}
