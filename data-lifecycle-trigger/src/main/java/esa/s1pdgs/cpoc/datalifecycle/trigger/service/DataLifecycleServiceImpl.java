package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.AVAILABLE_IN_LTA;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.EVICTION_DATE_IN_COMPRESSED_STORAGE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.EVICTION_DATE_IN_UNCOMPRESSED_STORAGE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.LAST_MODIFIED;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.PERSISTENT_IN_COMPRESSED_STORAGE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.PERSISTENT_IN_UNCOMPRESSED_STORAGE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.PRODUCT_NAME;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleBooleanFilter.Function.EQUALS;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleRangeValueFilter.Operator.GE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleRangeValueFilter.Operator.LE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleTextFilter.Function.MATCHES_REGEX;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.datalifecycle.trigger.config.DataLifecycleTriggerConfigurationProperties;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleSortTerm;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleSortTerm.DataLifecycleSortOrder;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleBooleanFilter;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleDateTimeFilter;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleQueryFilter;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleTextFilter;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepositoryException;
import esa.s1pdgs.cpoc.datalifecycle.trigger.service.error.DataLifecycleMetadataNotFoundException;
import esa.s1pdgs.cpoc.datalifecycle.trigger.service.error.DataLifecycleTriggerBadRequestException;
import esa.s1pdgs.cpoc.datalifecycle.client.error.DataLifecycleTriggerInternalServerErrorException;
import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.mqi.model.queue.DataRequestJob;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionManagementJob;

@Service
public class DataLifecycleServiceImpl implements DataLifecycleService {
	private static final Logger LOG = LogManager.getLogger(DataLifecycleServiceImpl.class);

	private static final LocalDateTime FOREVER = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

	private final DataLifecycleMetadataRepository lifecycleMetadataRepo;
	private final MessageProducer<EvictionManagementJob> evictionJobMessageProducer;
	private final String evictionTopic;
	private final MessageProducer<DataRequestJob> dataRequestJobMessageProducer;
	private final String dataRequestTopic;
	private final long dataRequestCooldown;

	// --------------------------------------------------------------------------

	@Autowired
	public DataLifecycleServiceImpl(final DataLifecycleTriggerConfigurationProperties configurationProperties,
			final DataLifecycleMetadataRepository lifecycleMetadataRepo, final MessageProducer<EvictionManagementJob> evictionJobMessageProducer,
			final MessageProducer<DataRequestJob> dataRequestJobMessageProducer) {
		this.lifecycleMetadataRepo = lifecycleMetadataRepo;
		this.evictionJobMessageProducer = evictionJobMessageProducer;
		this.evictionTopic = configurationProperties.getEvictionTopic();
		this.dataRequestJobMessageProducer = dataRequestJobMessageProducer;
		this.dataRequestTopic = configurationProperties.getDataRequestTopic();
		this.dataRequestCooldown = configurationProperties.getDataRequestCooldownInSec();
	}

	// --------------------------------------------------------------------------

	@Override
	public void evict(String operatorName) throws DataLifecycleTriggerInternalServerErrorException {
		LOG.debug("starting general eviction request on behalf of " + (StringUtil.isNotBlank(operatorName) ? operatorName : "[NOT SPECIFIED]"));

		final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
		final DataLifecycleSortTerm sortTerm = new DataLifecycleSortTerm(LAST_MODIFIED, DataLifecycleSortOrder.ASCENDING);

		final int pageSize = 100;
		int offset = 0;
		List<DataLifecycleMetadata> productsToDelete;
		final List<DataLifecycleTriggerInternalServerErrorException> errors = new ArrayList<>();
		long evictionJobsSend = 0;

		try {
			do {
				// get result page
				productsToDelete = CollectionUtil.nullToEmptyList(this.lifecycleMetadataRepo.findByEvictionDateBefore(now, Optional.of(pageSize),
						Optional.of(offset), Collections.singletonList(sortTerm)));
				LOG.debug("found " + productsToDelete.size() + " products (page size: " + pageSize + ") to evict on behalt of "
						+ (StringUtil.isNotBlank(operatorName) ? operatorName : "[NOT SPECIFIED]"));

				// process result page
				for (final DataLifecycleMetadata metadata : productsToDelete) {
					try {
						evictionJobsSend += this.evict(metadata, false, false, operatorName);
					} catch (final DataLifecycleTriggerInternalServerErrorException e) {
						LOG.error("error on evicting product (operator: " + operatorName + "), will skip this one: " + e.getMessage());
						errors.add(e);
						continue;
					}
				}
				// calculate offset for next page
				if (((long) offset + pageSize) > Integer.MAX_VALUE) {
					throw new DataLifecycleTriggerInternalServerErrorException("paging offset exceeds limit of " + Integer.MAX_VALUE);
				}
				offset += pageSize;
			} while (CollectionUtil.isNotEmpty(productsToDelete));
		} catch (final DataLifecycleTriggerInternalServerErrorException e) {
			LOG.error("error searching for products to evict on behalf of "
					+ (StringUtil.isNotBlank(operatorName) ? operatorName : "[NOT SPECIFIED]: " + e.getMessage()));
			throw e;
		}

		if (!errors.isEmpty()) {
			if (errors.size() == 1) {
				throw new DataLifecycleTriggerInternalServerErrorException("an error ocurred on general eviction request from "
						+ (StringUtil.isNotBlank(operatorName) ? operatorName : "[NOT SPECIFIED]") + ", " + evictionJobsSend
						+ " EvictionManagementJobs where sent though: " + Exceptions.toString(errors.get(0)));
			} else {
				throw new DataLifecycleTriggerInternalServerErrorException(errors.size() + " errors ocurred on general eviction request from "
						+ (StringUtil.isNotBlank(operatorName) ? operatorName : "[NOT SPECIFIED]") + ", " + evictionJobsSend
						+ " EvictionManagementJobs where sent though.");
			}
		}

		LOG.info(evictionJobsSend + " EvictionManagementJobs where sent on behalf of "
				+ (StringUtil.isNotBlank(operatorName) ? operatorName : "[NOT SPECIFIED]"));
	}

	@Override
	public void evict(String productname, boolean forceCompressed, boolean forceUncompressed, String operatorName)
			throws DataLifecycleTriggerInternalServerErrorException, DataLifecycleMetadataNotFoundException {
		LOG.debug("starting eviction request for '" + productname + "' (forceCompressed=" + forceCompressed + ", forceUncompressed=" + forceUncompressed
				+ ", operatorName=" + operatorName + ")");

		final int evictionJobsSend;
		try {
			final DataLifecycleMetadata lifecycleMetadata = this.getOrThrow(productname);
			evictionJobsSend = this.evict(lifecycleMetadata, forceCompressed, forceUncompressed, operatorName);
		} catch (final DataLifecycleMetadataNotFoundException e) {
			LOG.info("cannot evict product (operator: " + operatorName + "): " + e.getMessage());
			throw e;
		} catch (final DataLifecycleTriggerInternalServerErrorException e) {
			LOG.error("error on evicting product (operator: " + operatorName + "): " + e.getMessage());
			throw e;
		}

		LOG.info(evictionJobsSend + " EvictionManagementJobs where sent for '" + productname + "' on behalf of "
				+ (StringUtil.isNotBlank(operatorName) ? operatorName : "[NOT SPECIFIED]"));
	}

	@Override
	public DataLifecycleMetadata updateRetention(String productname, LocalDateTime evictionTimeInCompressedStorage,
			LocalDateTime evictionTimeInUncompressedStorage, String operatorName)
					throws DataLifecycleTriggerInternalServerErrorException, DataLifecycleMetadataNotFoundException {
		LOG.debug("starting retention update request for '" + productname + "' (evictionTimeInCompressedStorage="
				+ (null != evictionTimeInCompressedStorage ? DateUtils.formatToMetadataDateTimeFormat(evictionTimeInCompressedStorage) : "FREEZE")
				+ ", evictionTimeInUncompressedStorage="
				+ (null != evictionTimeInUncompressedStorage ? DateUtils.formatToMetadataDateTimeFormat(evictionTimeInUncompressedStorage) : "FREEZE")
				+ ", operatorName=" + operatorName + ")");

		final DataLifecycleMetadata updatedMetadata;
		try {
			final DataLifecycleMetadata metadataToUpdate = this.getAndRequest(productname, operatorName);
			updatedMetadata = this.updateRetention(metadataToUpdate, evictionTimeInCompressedStorage, evictionTimeInUncompressedStorage);
		} catch (final DataLifecycleMetadataNotFoundException e) {
			LOG.info("cannot update retention of product (operator: " + operatorName + "): " + e.getMessage());
			throw e;
		} catch (final DataLifecycleTriggerInternalServerErrorException e) {
			LOG.error("error on updating retention of product (operator: " + operatorName + "): " + e.getMessage());
			throw e;
		}

		LOG.info("updated retention on behalf of " + operatorName + ": " + updatedMetadata);
		return updatedMetadata;
	}

	@Override
	public List<DataLifecycleMetadata> getProducts(String namePattern, Boolean persistentInUncompressedStorage,
			LocalDateTime minimalEvictionTimeInUncompressedStorage, LocalDateTime maximalEvictionTimeInUncompressedStorage,
			Boolean persistentIncompressedStorage, LocalDateTime minimalEvictionTimeInCompressedStorage, LocalDateTime maximalEvictionTimeInCompressedStorage,
			Boolean availableInLta, Integer pageSize, Integer pageNumber)
					throws DataLifecycleTriggerInternalServerErrorException, DataLifecycleTriggerBadRequestException {
		LOG.debug(String.format("incoming query for data lifecycle metadata of products with attributes:"
				+ " namePattern=%s, persistentInUncompressedStorage=%b, minimalEvictionTimeInUncompressedStorage=%s, maximalEvictionTimeInUncompressedStorage=%s,"
				+ " persistentIncompressedStorage=%b, minimalEvictionTimeInCompressedStorage=%s, maximalEvictionTimeInCompressedStorage=%s,"
				+ " availableInLta=%b, pageSize=%d, pageNumber=%d", namePattern, persistentInUncompressedStorage,
				(null != minimalEvictionTimeInUncompressedStorage ? DateUtils.formatToMetadataDateTimeFormat(minimalEvictionTimeInUncompressedStorage)
						: "null"),
				(null != maximalEvictionTimeInUncompressedStorage ? DateUtils.formatToMetadataDateTimeFormat(maximalEvictionTimeInUncompressedStorage)
						: "null"),
				persistentIncompressedStorage,
				(null != minimalEvictionTimeInCompressedStorage ? DateUtils.formatToMetadataDateTimeFormat(minimalEvictionTimeInCompressedStorage) : "null"),
				(null != maximalEvictionTimeInCompressedStorage ? DateUtils.formatToMetadataDateTimeFormat(maximalEvictionTimeInCompressedStorage) : "null"),
				availableInLta, pageSize, pageNumber));

		final ArrayList<DataLifecycleQueryFilter> filters = new ArrayList<>();

		if (StringUtil.isNotBlank(namePattern)) {
			filters.add(new DataLifecycleTextFilter(PRODUCT_NAME, MATCHES_REGEX, namePattern));
		}

		if (null != persistentInUncompressedStorage) {
			filters.add(new DataLifecycleBooleanFilter(PERSISTENT_IN_UNCOMPRESSED_STORAGE, EQUALS, persistentInUncompressedStorage));
		}
		if (null != persistentIncompressedStorage) {
			filters.add(new DataLifecycleBooleanFilter(PERSISTENT_IN_COMPRESSED_STORAGE, EQUALS, persistentIncompressedStorage));
		}
		if (null != availableInLta) {
			filters.add(new DataLifecycleBooleanFilter(AVAILABLE_IN_LTA, EQUALS, availableInLta));
		}

		if (null != minimalEvictionTimeInUncompressedStorage) {
			filters.add(new DataLifecycleDateTimeFilter(EVICTION_DATE_IN_UNCOMPRESSED_STORAGE, GE, minimalEvictionTimeInUncompressedStorage));
		}
		if (null != maximalEvictionTimeInUncompressedStorage) {
			filters.add(new DataLifecycleDateTimeFilter(EVICTION_DATE_IN_UNCOMPRESSED_STORAGE, LE, maximalEvictionTimeInUncompressedStorage));
		}

		if (null != minimalEvictionTimeInCompressedStorage) {
			filters.add(new DataLifecycleDateTimeFilter(EVICTION_DATE_IN_COMPRESSED_STORAGE, GE, minimalEvictionTimeInCompressedStorage));
		}
		if (null != maximalEvictionTimeInCompressedStorage) {
			filters.add(new DataLifecycleDateTimeFilter(EVICTION_DATE_IN_COMPRESSED_STORAGE, LE, maximalEvictionTimeInCompressedStorage));
		}

		// paging
		Optional<Integer> oTop = Optional.empty();
		Optional<Integer> oSkip = Optional.empty();

		if (null != pageSize || null != pageNumber) {
			if (null != pageSize && pageSize > 0 && null != pageNumber && pageNumber >= 0) {
				oTop = Optional.of(pageSize);
				oSkip = Optional.of(pageNumber * pageSize);
			} else {
				throw new DataLifecycleTriggerBadRequestException(
						"error querying for products: for paging both arguments (page size > 0 and page number >= 0) must be provided");
			}
		}

		return this.lifecycleMetadataRepo.findWithFilters(filters, oTop, oSkip, Collections.emptyList());
	}

	@Override
	public List<DataLifecycleMetadata> getProducts(List<String> productnames) throws DataLifecycleTriggerInternalServerErrorException {
		LOG.debug("incoming query for data lifecycle metadata of products: " + productnames);

		if (CollectionUtil.isEmpty(productnames)) {
			return Collections.emptyList();
		}

		final List<DataLifecycleMetadata> metadata = this.getAndRequest(productnames, "[NOT SPECIFIED]");

		LOG.info("answering data lifecycle metadata query for " + metadata.size() + " products with " + metadata.size() + " results");
		return metadata;
	}

	@Override
	public DataLifecycleMetadata getProduct(String productname)
			throws DataLifecycleMetadataNotFoundException, DataLifecycleTriggerInternalServerErrorException {
		LOG.debug("incoming query for data lifecycle metadata of product '" + productname + "'");

		final DataLifecycleMetadata lifecycleMetadata;
		try {
			lifecycleMetadata = this.getAndRequest(productname, "[NOT SPECIFIED]");
		} catch (DataLifecycleTriggerInternalServerErrorException | DataLifecycleMetadataNotFoundException e) {
			LOG.info("error reading data lifecycle metadata: " + e.getMessage());
			throw e;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("answering data lifecycle metadata query for product '" + productname + "' with: " + lifecycleMetadata);
		} else {
			LOG.info("answering data lifecycle metadata query for product: " + productname);
		}

		return lifecycleMetadata;
	}

	// --------------------------------------------------------------------------

	private DataLifecycleMetadata getOrThrow(final String productname) throws DataLifecycleMetadataNotFoundException, DataLifecycleMetadataRepositoryException {
		final Optional<DataLifecycleMetadata> oLifecycleMetadata = this.lifecycleMetadataRepo.findByProductName(productname);

		if (oLifecycleMetadata.isPresent()) {
			return oLifecycleMetadata.get();
		} else {
			throw new DataLifecycleMetadataNotFoundException("no data lifecycle metadata found for product: " + productname);
		}
	}

	/**
	 * @return the metadata if existing and in case of no path in uncomressed storage sends a data request
	 * @throws DataLifecycleMetadataNotFoundException           if the metadata does not exist
	 * @throws DataLifecycleTriggerInternalServerErrorException if something goes wrong with persistence or kafka
	 */
	private DataLifecycleMetadata getAndRequest(final String productname, final String operatorName)
			throws DataLifecycleMetadataNotFoundException, DataLifecycleTriggerInternalServerErrorException {
		final DataLifecycleMetadata dataLifecycleMetadata = this.getOrThrow(productname);

		// send data requests if not in uncompressed storage
		if (StringUtil.isBlank(dataLifecycleMetadata.getPathInUncompressedStorage())) {
			this.sendDataRequest(dataLifecycleMetadata, operatorName);
		}

		return dataLifecycleMetadata;
	}

	/**
	 * @return the metadata for the given product names and in case of no path in uncomressed storage sending a data requests
	 * @throws DataLifecycleTriggerInternalServerErrorException if something goes wrong with persistence or data request
	 */
	private List<DataLifecycleMetadata> getAndRequest(final List<String> productNames, final String operatorName)
			throws DataLifecycleTriggerInternalServerErrorException {
		final List<DataLifecycleMetadata> metadata = CollectionUtil.nullToEmptyList(this.lifecycleMetadataRepo.findByProductNames(productNames));

		// send data requests if not in uncompressed storage
		final List<DataLifecycleMetadata> notAvailableUncompressed = metadata.stream().filter(m -> StringUtil.isBlank(m.getPathInUncompressedStorage()))
				.collect(Collectors.toList());
		long requestsSent = 0;
		for (final DataLifecycleMetadata requestMe : notAvailableUncompressed) {
			try {
				if (this.sendDataRequest(requestMe, "[NOT SPECIFIED]")) {
					requestsSent++;
				}
			} catch (final DataLifecycleTriggerInternalServerErrorException e) {
				LOG.error("error sending data request for product '" + requestMe.getProductName() + "' on behalf of '" + operatorName + "': "
						+ Exceptions.toString(e));
				continue; // best effort approach
			}
		}

		LOG.debug("products queried: " + productNames.size() + " / lifecycle metadata found: " + metadata.size() + " / not available uncompressed: "
				+ notAvailableUncompressed.size() + " / data requests sent: " + requestsSent);
		return metadata;
	}

	private DataLifecycleMetadata updateRetention(@NonNull DataLifecycleMetadata dataLifecycleMetadata, LocalDateTime evictionTimeInCompressedStorage,
			LocalDateTime evictionTimeInUncompressedStorage)
					throws DataLifecycleMetadataRepositoryException, DataLifecycleMetadataNotFoundException {
		// uncompressed
		if (null != evictionTimeInUncompressedStorage) {
			dataLifecycleMetadata.setEvictionDateInUncompressedStorage(evictionTimeInUncompressedStorage);
		} else { // freeze
			dataLifecycleMetadata.setEvictionDateInUncompressedStorage(FOREVER);
		}

		// compressed
		if (null != evictionTimeInCompressedStorage) {
			dataLifecycleMetadata.setEvictionDateInCompressedStorage(evictionTimeInCompressedStorage);
		} else { // freeze
			dataLifecycleMetadata.setEvictionDateInCompressedStorage(FOREVER);
		}

		this.lifecycleMetadataRepo.save(dataLifecycleMetadata);
		return this.lifecycleMetadataRepo.findByProductName(dataLifecycleMetadata.getProductName())
				.orElseThrow(() -> new DataLifecycleMetadataNotFoundException(
						"error reading metadata for product '" + dataLifecycleMetadata.getProductName() + "' after retention update"));
	}

	private int evict(@NonNull DataLifecycleMetadata dataLifecycleMetadata, boolean forceCompressed, boolean forceUncompressed,
			String operatorName) throws DataLifecycleTriggerInternalServerErrorException {
		int evictionJobsSend = 0;
		final List<EvictionManagementJob> evictionJobs = new ArrayList<>();
		final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));

		// uncompressed
		final String pathInUncompressedStorage = dataLifecycleMetadata.getPathInUncompressedStorage();
		final LocalDateTime evictionDateInUncompressedStorage = dataLifecycleMetadata.getEvictionDateInUncompressedStorage();

		if (StringUtil.isNotBlank(pathInUncompressedStorage)
				&& (forceUncompressed || (null != evictionDateInUncompressedStorage && now.isAfter(evictionDateInUncompressedStorage)))) {
			final ProductFamily productFamilyInUncompressedStorage = dataLifecycleMetadata.getProductFamilyInUncompressedStorage();
			if (null == productFamilyInUncompressedStorage || ProductFamily.BLANK == productFamilyInUncompressedStorage) {
				throw new DataLifecycleTriggerInternalServerErrorException(
						"error trigger eviction of product, no valid product family found for uncompressed storage for: " + dataLifecycleMetadata);
			}

			final EvictionManagementJob evictionJobUncompressed = new EvictionManagementJob();

			evictionJobUncompressed.setKeyObjectStorage(pathInUncompressedStorage);
			evictionJobUncompressed.setProductFamily(productFamilyInUncompressedStorage);
			evictionJobUncompressed.setOperatorName(operatorName);

			evictionJobs.add(evictionJobUncompressed);
			LOG.debug("will trigger eviction of product from uncompressed storage on behalf of " + operatorName + ": " + dataLifecycleMetadata);
		} else {
			LOG.debug("cannot trigger eviction of product from uncompressed storage on behalf of " + operatorName + ": " + dataLifecycleMetadata);
		}

		// compressed
		final String pathInCompressedStorage = dataLifecycleMetadata.getPathInCompressedStorage();
		final LocalDateTime evictionDateInCompressedStorage = dataLifecycleMetadata.getEvictionDateInCompressedStorage();

		if (StringUtil.isNotBlank(pathInCompressedStorage)
				&& (forceUncompressed || (null != evictionDateInCompressedStorage && now.isAfter(evictionDateInCompressedStorage)))) {
			final ProductFamily productFamilyInCompressedStorage = dataLifecycleMetadata.getProductFamilyInCompressedStorage();
			if (null == productFamilyInCompressedStorage || ProductFamily.BLANK == productFamilyInCompressedStorage) {
				throw new DataLifecycleTriggerInternalServerErrorException(
						"error trigger evictiion of product, no valid product family found for compressed storage for: " + dataLifecycleMetadata);
			}

			final EvictionManagementJob evictionJobCompressed = new EvictionManagementJob();

			evictionJobCompressed.setKeyObjectStorage(pathInCompressedStorage);
			evictionJobCompressed.setProductFamily(productFamilyInCompressedStorage);
			evictionJobCompressed.setOperatorName(operatorName);

			evictionJobs.add(evictionJobCompressed);
			LOG.debug("will trigger eviction of product from compressed storage on behalf of " + operatorName + ": " + dataLifecycleMetadata);
		} else {
			LOG.debug("cannot trigger eviction from product from compressed storage on behalf of " + operatorName + ": " + dataLifecycleMetadata);
		}

		// publish jobs
		for (final EvictionManagementJob job : evictionJobs) {
			this.publish(job);
			evictionJobsSend++;
		}

		return evictionJobsSend;
	}

	public boolean sendDataRequest(final DataLifecycleMetadata dataLifecycleMetadata, final String operatorName)
			throws DataLifecycleTriggerInternalServerErrorException {
		final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));

		if (null == dataLifecycleMetadata.getLastDataRequest()
				|| now.minusSeconds(this.dataRequestCooldown).isAfter(dataLifecycleMetadata.getLastDataRequest())) {
			final ProductFamily productFamily = dataLifecycleMetadata.getProductFamilyInUncompressedStorage();
			if (null == productFamily) {
				throw new DataLifecycleTriggerInternalServerErrorException(
						"cannot send data request for '" + dataLifecycleMetadata.getProductName() + "' as product family in uncompressed storage is unknown.");
			}

			// prevent sending data requests for session files, because they are non-recoverable
			if (ProductFamily.EDRS_SESSION == productFamily) {
				LOG.debug(String.format("sending no data request for '%s', because session files are non-recoverable",
						dataLifecycleMetadata.getProductName()));
				return false;
			}

			final DataRequestJob dataRequestJob = new DataRequestJob();
			dataRequestJob.setKeyObjectStorage(dataLifecycleMetadata.getProductName());
			dataRequestJob.setProductFamily(productFamily);
			dataRequestJob.setOperatorName(operatorName);

			LOG.debug("sending data request: " + dataRequestJob);
			this.publish(dataRequestJob);
			dataLifecycleMetadata.setLastDataRequest(now);
			this.lifecycleMetadataRepo.save(dataLifecycleMetadata);
			return true;
		} else {
			LOG.debug(String.format("ommitting sending a data request for '%s', because of active cooldown, ending %s",dataLifecycleMetadata.getProductName(),DateUtils
					.formatToMetadataDateTimeFormat(dataLifecycleMetadata.getLastDataRequest().plusSeconds(this.dataRequestCooldown))));
			return false;
		}
	}

	private void publish(final EvictionManagementJob job) throws DataLifecycleTriggerInternalServerErrorException {
		try {
			this.evictionJobMessageProducer.send(this.evictionTopic, job);
		} catch (final Exception e) {
			throw new DataLifecycleTriggerInternalServerErrorException(
					String.format("Error on publishing EvictionManagementJob for %s to %s: %s", job.getKeyObjectStorage(), this.evictionTopic,
							Exceptions.messageOf(e)), e);
		}
	}

	private void publish(final DataRequestJob job) throws DataLifecycleTriggerInternalServerErrorException {
		try {
			this.dataRequestJobMessageProducer.send(this.dataRequestTopic, job);
		} catch (final Exception e) {
			throw new DataLifecycleTriggerInternalServerErrorException(String.format("Error on publishing DataRequestJob for %s to %s: %s",
					job.getKeyObjectStorage(), this.dataRequestTopic, Exceptions.messageOf(e)), e);
		}
	}

}
