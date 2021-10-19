package esa.s1pdgs.cpoc.mdc.timer.db;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductFamily;

@Component
public interface CatalogEventTimerEntryRepository extends MongoRepository<CatalogEventTimerEntry, String> {

	public List<CatalogEventTimerEntry> findByProductTypeAndProductFamily(String productType,
			ProductFamily productFamily);
}
