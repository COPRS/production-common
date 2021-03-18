package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence.DataLifecycleMetadataRepositoryException;
import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.mqi.model.queue.DataRequestJob;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionManagementJob;

@Service
public class DataLifecycleServiceImpl implements DataLifecycleService {
	private static final Logger LOG = LogManager.getLogger(DataLifecycleServiceImpl.class);

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

		final List<DataLifecycleMetadata> productsToDelete;
		try {
			productsToDelete = this.lifecycleMetadataRepo.findByEvictionDateBefore(LocalDateTime.now(ZoneId.of("UTC")));
			LOG.info("found " + productsToDelete.size() + " products to evict on behalt of "
					+ (StringUtil.isNotBlank(operatorName) ? operatorName : "[NOT SPECIFIED]"));
		} catch (final DataLifecycleTriggerInternalServerErrorException e) {
			LOG.error("error searching for products to evict on behalf of "
					+ (StringUtil.isNotBlank(operatorName) ? operatorName : "[NOT SPECIFIED]: " + e.getMessage()));
			throw e;
		}

		final List<DataLifecycleTriggerInternalServerErrorException> errors = new ArrayList<>();
		int evictionJobsSend = 0;
		for (final DataLifecycleMetadata metadata : productsToDelete) {
			try {
				evictionJobsSend += this.evict(metadata, false, false, operatorName);
			} catch (final DataLifecycleTriggerInternalServerErrorException e) {
				LOG.error("error on evicting product (operator: " + operatorName + "), will skip this one: " + e.getMessage());
				errors.add(e);
			}
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
			final DataLifecycleMetadata lifecycleMetadata = this.getAndCheckPathExists(productname);
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
			final DataLifecycleMetadata metadataToUpdate = this.getOrRequest(productname, operatorName);
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
	public List<DataLifecycleMetadata> getProducts(String namePattern, boolean persistentInUncompressedStorage,
			LocalDateTime minimalEvictionTimeInUncompressedStorage, LocalDateTime maximalEvictionTimeInUncompressedStorage,
			boolean persistentIncompressedStorage, LocalDateTime minimalEvictionTimeInCompressedStorage, LocalDateTime maximalEvictionTimeInCompressedStorage,
			boolean availableInLta, Integer pageSize, Integer pageNumber) {
		// TODO @MSc: impl
		return null;
	}

	@Override
	public List<DataLifecycleMetadata> getProducts(List<String> productnames) throws DataLifecycleMetadataRepositoryException {
		LOG.debug("incoming query for data lifecycle metadata of products: " + productnames);

		if (CollectionUtil.isEmpty(productnames)) {
			return Collections.emptyList();
		}

		final List<DataLifecycleMetadata> metadata = this.lifecycleMetadataRepo.findByProductNames(productnames);

		LOG.debug("answering data lifecycle metadata query for " + metadata.size() + " products with " + metadata.size() + " results");
		return metadata;
	}

	@Override
	public DataLifecycleMetadata getProduct(String productname)
			throws DataLifecycleMetadataNotFoundException, DataLifecycleTriggerInternalServerErrorException {
		LOG.debug("incoming query for data lifecycle metadata of product '" + productname + "'");

		final DataLifecycleMetadata lifecycleMetadata;

		try {
			lifecycleMetadata = this.get(productname);
		} catch (DataLifecycleMetadataRepositoryException | DataLifecycleMetadataNotFoundException e) {
			LOG.info("error reading data lifecycle metadata: " + e.getMessage());
			throw e;
		}

		LOG.debug("answering data lifecycle metadata query for product '" + productname + "' with: " + lifecycleMetadata);
		return lifecycleMetadata;
	}

	// --------------------------------------------------------------------------

	private DataLifecycleMetadata get(final String productname) throws DataLifecycleMetadataNotFoundException, DataLifecycleMetadataRepositoryException {
		final Optional<DataLifecycleMetadata> oLifecycleMetadata = this.lifecycleMetadataRepo.findByProductName(productname);

		if (oLifecycleMetadata.isPresent()) {
			return oLifecycleMetadata.get();
		} else {
			throw new DataLifecycleMetadataNotFoundException("no data lifecycle metadata found for product: " + productname);
		}
	}

	private DataLifecycleMetadata getAndCheckPathExists(final String productname)
			throws DataLifecycleMetadataNotFoundException, DataLifecycleMetadataRepositoryException {
		final DataLifecycleMetadata dataLifecycleMetadata = this.get(productname);

		if (StringUtil.isNotBlank(dataLifecycleMetadata.getPathInUncompressedStorage())
				|| StringUtil.isNotBlank(dataLifecycleMetadata.getPathInCompressedStorage())) {
			return dataLifecycleMetadata;
		} else {
			throw new DataLifecycleMetadataNotFoundException("data lifecycle metadata contains no storage paths for product: " + productname);
		}
	}

	private DataLifecycleMetadata getOrRequest(final String productname, final String operatorName)
			throws DataLifecycleMetadataNotFoundException, DataLifecycleTriggerInternalServerErrorException {
		final Optional<DataLifecycleMetadata> oLifecycleMetadata = this.lifecycleMetadataRepo.findByProductName(productname);

		if (oLifecycleMetadata.isPresent()) {
			final DataLifecycleMetadata dataLifecycleMetadata = oLifecycleMetadata.get();

			if (StringUtil.isNotBlank(dataLifecycleMetadata.getPathInUncompressedStorage())) {
				return dataLifecycleMetadata;
			} else {
				this.sendDataRequest(dataLifecycleMetadata, operatorName);
				throw new DataLifecycleMetadataNotFoundException(
						"data lifecycle metadata contains no path in uncompressed storage for product '" + productname
						+ "', data request sent, try again later");
			}
		} else {
			throw new DataLifecycleMetadataNotFoundException("no data lifecycle metadata found for product '" + productname + "', unable to send data request");
		}
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

			final DataRequestJob dataRequestJob = new DataRequestJob();
			dataRequestJob.setKeyObjectStorage(dataLifecycleMetadata.getProductName());
			dataRequestJob.setProductFamily(productFamily);
			dataRequestJob.setOperatorName(operatorName);

			this.publish(dataRequestJob);
			dataLifecycleMetadata.setLastDataRequest(now);
			this.lifecycleMetadataRepo.save(dataLifecycleMetadata);
			return true;
		}

		return false;
	}

	private DataLifecycleMetadata updateRetention(@NonNull DataLifecycleMetadata dataLifecycleMetadata, LocalDateTime evictionTimeInCompressedStorage,
			LocalDateTime evictionTimeInUncompressedStorage)
					throws DataLifecycleMetadataRepositoryException, DataLifecycleMetadataNotFoundException {
		// uncompressed
		if (null != evictionTimeInUncompressedStorage) {
			dataLifecycleMetadata.setEvictionDateInUncompressedStorage(evictionTimeInUncompressedStorage);
		} else { // freeze
			dataLifecycleMetadata.setEvictionDateInUncompressedStorage(LocalDateTime.MAX);
		}

		// compressed
		if (null != evictionTimeInCompressedStorage) {
			dataLifecycleMetadata.setEvictionDateInCompressedStorage(evictionTimeInCompressedStorage);
		} else { // freeze
			dataLifecycleMetadata.setEvictionDateInCompressedStorage(LocalDateTime.MAX);
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
