package esa.s1pdgs.cpoc.cronbased.trigger.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.support.CronExpression;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.cronbased.trigger.config.CronbasedTriggerProperties;
import esa.s1pdgs.cpoc.cronbased.trigger.config.CronbasedTriggerProperties.TimerProperties;
import esa.s1pdgs.cpoc.cronbased.trigger.db.CronbasedTriggerEntry;
import esa.s1pdgs.cpoc.cronbased.trigger.db.CronbasedTriggerEntryRepository;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

public class CronbasedTriggerService implements Function<Message<?>, List<Message<CatalogEvent>>> {

	private static final Logger LOGGER = LogManager.getLogger(CronbasedTriggerService.class);
	private static final LocalDateTime MIN_DATE = LocalDateTime.of(2000, 1, 1, 0, 0);

	private CronbasedTriggerProperties properties;
	private MetadataClient metadataClient;
	private CronbasedTriggerEntryRepository repository;
	private ObsClient obsClient;

	public CronbasedTriggerService(final CronbasedTriggerProperties properties, final MetadataClient metadataClient,
			final CronbasedTriggerEntryRepository repository, final ObsClient obsClient) {
		this.properties = properties;
		this.metadataClient = metadataClient;
		this.repository = repository;
		this.obsClient = obsClient;
	}

	@Override
	public List<Message<CatalogEvent>> apply(Message<?> t) {
		LOGGER.debug("Received message. Start checking if configured productTypes are ready.");
		List<Message<CatalogEvent>> result = new ArrayList<>();

		for (Entry<String, TimerProperties> entry : properties.getConfig().entrySet()) {
			CronbasedTriggerEntry triggerEntry = getCatalogEventTimerEntry(entry.getValue().getFamily(),
					entry.getKey());

			if (groupShallBeChecked(triggerEntry, new Date(), entry.getValue().getCron())) {
				LOGGER.info("Start checking for new products of productType {} and productFamily {}", entry.getKey(),
						entry.getValue().getFamily());

				result.addAll(checkProductGroupForNewMessages(entry.getKey(), entry.getValue(), triggerEntry));
			}
		}

		return result;
	}

	/*
	 * Determine if the productGroup shall be checked. Evaluates the cron expression
	 * of the config and checks if the current time is the first time a new trigger
	 * interval is reached.
	 * 
	 * ex.: triggerEntry states 00:15 was the last execution and the cron expression
	 * states, the productGroup shall be checked every 15 minutes. The first event
	 * that is received after 00:30 will trigger the routine.
	 */
	boolean groupShallBeChecked(CronbasedTriggerEntry triggerEntry, Date now, String cronExpression) {
		// When no execution happened til now - trigger.
		if (triggerEntry == null) {
			return true;
		}

		Date lastCheck = triggerEntry.getLastCheckDate();
		LocalDateTime lastCheckTemporal = lastCheck.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

		CronExpression cron = CronExpression.parse(cronExpression);
		LocalDateTime nextExecution = cron.next(lastCheckTemporal);

		// When now is after or equal to next execution, group shall be checked.
		return !nextExecution.atZone(ZoneId.systemDefault()).toInstant().isAfter(now.toInstant());
	}

	private List<Message<CatalogEvent>> checkProductGroupForNewMessages(final String productType,
			final TimerProperties timerProperties, CronbasedTriggerEntry triggerEntry) {
		List<Message<CatalogEvent>> result = new ArrayList<>();

		LocalDateTime intervalStart;

		// Get the intervalStart from the database entry
		if (triggerEntry == null) {
			intervalStart = MIN_DATE;
		} else {
			intervalStart = LocalDateTime.ofInstant(triggerEntry.getLastCheckDate().toInstant(),
					ZoneId.systemDefault());
		}

		LOGGER.debug("Retrieved last timestamp {}", intervalStart.toString());
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime intervalStop = now;
		
		// Handle offsets
		if (timerProperties.getQueryOffsetInS() > 0) {
			LOGGER.debug("Use offset {} to move interval back into the past", timerProperties.getQueryOffsetInS());
			intervalStart = intervalStart.minusSeconds(timerProperties.getQueryOffsetInS());
			intervalStop = intervalStop.minusSeconds(timerProperties.getQueryOffsetInS());
		}

		List<String> satelliteIds = new ArrayList<String>(Arrays.asList(timerProperties.getSatelliteIds().split(",")));

		for (String satelliteId : satelliteIds) {
			try {
				LOGGER.debug("Retrieve new products from database");
				List<SearchMetadata> products = this.metadataClient.searchInterval(timerProperties.getFamily(),
						productType, intervalStart, intervalStop, satelliteId, timerProperties.getTimeliness());

				String lastInsertionTime = null;

				for (SearchMetadata product : products) {
					LOGGER.info("Create CatalogEvent for product {}", product.getProductName());
					CatalogEvent event = toCatalogEvent(timerProperties.getFamily(), productType, product);
					result.add(MessageBuilder.withPayload(event).build());

					lastInsertionTime = product.getInsertionTime();
				}

				// Update database entry, use intervalStop minus two seconds (to account for
				// index refreshes) or lastInsertionTime, whichever is bigger.
				LocalDateTime newEntryTime = now.minusSeconds(2);

				if (lastInsertionTime != null) {
					LocalDateTime lastInsertion = DateUtils.parse(lastInsertionTime);
					if (lastInsertion.isAfter(newEntryTime)) 
						newEntryTime = lastInsertion;
				} 

				if (triggerEntry == null) {
					triggerEntry = new CronbasedTriggerEntry();
					triggerEntry.setProductFamily(timerProperties.getFamily());
					triggerEntry.setProductType(productType);
				}

				LOGGER.info("Update database entry for this dispatcher");
				updateCatalogEventTimerEntry(triggerEntry, newEntryTime);

			} catch (MetadataQueryException e) {
				LOGGER.warn("An exception occured while fetching new products: ", e);
			} catch (Exception e) {
				LOGGER.warn("An exception occured while publishing a CatalogEvent: ", e);
			}
		}

		return result;
	}

	/**
	 * Retrieve the corresponding entry from the database, to determine the last
	 * time this dispatcher ran
	 * 
	 * @return CronbasedTriggerEntry corresponding to this dispatcher
	 */
	private CronbasedTriggerEntry getCatalogEventTimerEntry(final ProductFamily productFamily,
			final String productType) {
		List<CronbasedTriggerEntry> entries = this.repository.findByProductTypeAndProductFamily(productType,
				productFamily);

		if (entries.isEmpty()) {
			return null;
		}

		return entries.get(0);
	}

	/**
	 * Convert a product from the elastic search into an catalog event
	 */
	private CatalogEvent toCatalogEvent(final ProductFamily productFamily, final String productType,
			final SearchMetadata metadata) {
		CatalogEvent event = new CatalogEvent();
		event.setUid(UUID.randomUUID());
		
		for (Entry<String, String> entry : metadata.getAdditionalProperties().entrySet()) {
			event.getMetadata().put(entry.getKey(), entry.getValue());
		}
		
		event.setMissionId(metadata.getMissionId());
		event.setSatelliteId(metadata.getSatelliteId());
		event.setMetadataProductName(metadata.getProductName());
		event.setKeyObjectStorage(metadata.getKeyObjectStorage());
		event.setStoragePath(obsClient.getAbsoluteStoragePath(productFamily, metadata.getProductName()));
		event.setProductFamily(productFamily);
		event.setMetadataProductType(productType);
		
		return event;
	}

	/**
	 * Update entry in database to prohibit creation of the same CatalogEvent
	 * multiple times
	 */
	private void updateCatalogEventTimerEntry(CronbasedTriggerEntry currentEntry, LocalDateTime newTime) {
		currentEntry.setLastCheckDate(Date.from(newTime.atZone(ZoneId.systemDefault()).toInstant()));

		this.repository.save(currentEntry);
	}
}
