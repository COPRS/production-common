package esa.s1pdgs.cpoc.compression.trigger.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class ProductionEventListener implements MqiListener<ProductionEvent> {

	private static final Logger LOGGER = LogManager.getLogger(ProductionEventListener.class);

	private final GenericMqiClient mqiClient;
	private final long pollingIntervalMs;
	private final long pollingInitialDelayMs;
	
	private final CompressionTrigger compressionTrigger;
	
	private final AppStatus appStatus;

	@Autowired
	public ProductionEventListener(final GenericMqiClient mqiClient,
			@Value("${compression-trigger.fixed-delay-ms}") final long pollingIntervalMs,
			@Value("${compression-trigger.init-delay-poll-ms}") final long pollingInitialDelayMs,
			final CompressionTrigger compressionTrigger,
			final AppStatus appStatus) {
		this.mqiClient = mqiClient;
		this.pollingIntervalMs = pollingIntervalMs;
		this.pollingInitialDelayMs = pollingInitialDelayMs;
		this.compressionTrigger = compressionTrigger;
		this.appStatus = appStatus;
	}
	
	@PostConstruct
	public void initService() {
		if (pollingIntervalMs > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(new MqiConsumer<ProductionEvent>(mqiClient, ProductCategory.COMPRESSED_PRODUCTS, this,
					pollingIntervalMs, pollingInitialDelayMs, appStatus));
		}
	}

	@Override
	public void onMessage(GenericMessageDto<ProductionEvent> productionEventMessage) throws AbstractCodedException {
		compressionTrigger.trigger(productionEventMessage);
	}

}
