package esa.s1pdgs.cpoc.mdc.timer.dispatcher;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.mdc.timer.db.CatalogEventTimerEntry;
import esa.s1pdgs.cpoc.mdc.timer.db.CatalogEventTimerEntryRepository;
import esa.s1pdgs.cpoc.mdc.timer.publish.Publisher;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

public class CatalogEventDispatcherImpl implements CatalogEventDispatcher {
	
	private static final LocalDateTime MIN_DATE = LocalDateTime.of(2000, 1, 1, 0, 0);
	
	private MetadataClient metadataClient;
	private CatalogEventTimerEntryRepository repository;
	private Publisher publisher;
	private String productType;
	private ProductFamily productFamily;
	private String satelliteId;

	public CatalogEventDispatcherImpl(final MetadataClient metadataClient,
			final CatalogEventTimerEntryRepository repository, final Publisher publisher, final String productType,
			final ProductFamily productFamily, final String satelliteId) {
		this.metadataClient = metadataClient;
		this.repository = repository;
		this.productType = productType;
		this.productFamily = productFamily;
		this.publisher = publisher;
		this.satelliteId = satelliteId;
	}

	@Override
	public void run() {
		LocalDateTime intervalStart;
		CatalogEventTimerEntry entry = getCatalogEventTimerEntry();

		// Get the intervalStart from the database entry
		if (entry == null) {
			intervalStart = MIN_DATE;
		} else {
			intervalStart = LocalDateTime.ofInstant(entry.getLastCheckDate().toInstant(), ZoneId.systemDefault());
		}
		
		LOGGER.debug("Retrieved last timestamp {}", intervalStart.toString());
		LocalDateTime intervalStop = LocalDateTime.now();

		try {
			LOGGER.debug("Retrieve new products from database");
			List<SearchMetadata> products = this.metadataClient.searchInterval(this.productFamily, this.productType,
					intervalStart, intervalStop, this.satelliteId);

			String lastInsertionTime = null;
			
			for (SearchMetadata product : products) {
				LOGGER.info("Publish CatalogEvent for product {}", product.getProductName());
				CatalogEvent event = toCatalogEvent(product);
				this.publisher.publish(event);
				
				lastInsertionTime = product.getInsertionTime();
			}
			
			// Update database entry, if we produced new events
			if (lastInsertionTime != null) {
				if (entry == null) {
					entry = new CatalogEventTimerEntry();
					entry.setProductFamily(this.productFamily);
					entry.setProductType(this.productType);
				}
				
				LOGGER.info("Update database entry for this dispatcher");
				updateCatalogEventTimerEntry(entry, DateUtils.parse(lastInsertionTime));
			}
		} catch (MetadataQueryException e) {
			LOGGER.warn("An exception occured while fetching new products: ", e);
		} catch (Exception e) {
			LOGGER.warn("An exception occured while publishing a CatalogEvent: ", e);
		}
	}

	/**
	 * Retrieve the corresponding entry from the database, to determine the last
	 * time this dispatcher ran
	 * 
	 * @return CatalogEventTimerEntry corresponding to this dispatcher
	 */
	private CatalogEventTimerEntry getCatalogEventTimerEntry() {
		List<CatalogEventTimerEntry> entries = this.repository.findByProductTypeAndProductFamily(this.productType,
				this.productFamily);

		if (entries.isEmpty()) {
			return null;
		}

		return entries.get(0);
	}

	/**
	 * Convert a product from the elastic search into an catalog event
	 */
	private CatalogEvent toCatalogEvent(SearchMetadata metadata) {
		CatalogEvent event = new CatalogEvent();
		event.setProductFamily(this.productFamily);
		event.setMetadataProductName(metadata.getProductName());
		event.setKeyObjectStorage(metadata.getKeyObjectStorage());
		event.setMetadataProductType(this.productType);
		event.setUid(UUID.randomUUID());
		
		event.getMetadata().put("satelliteId", this.satelliteId);
		event.getMetadata().put(MissionId.FIELD_NAME, metadata.getMissionId());
		event.getMetadata().put("startTime", metadata.getValidityStart());
		event.getMetadata().put("stopTime", metadata.getValidityStop());
		
		return event;
	}

	/**
	 * Update entry in database to prohibit creation of the same CatalogEvent
	 * multiple times
	 */
	private void updateCatalogEventTimerEntry(CatalogEventTimerEntry currentEntry, LocalDateTime newTime) {
		currentEntry.setLastCheckDate(Date.from(newTime.atZone(ZoneId.systemDefault()).toInstant()));

		this.repository.save(currentEntry);
	}
}
