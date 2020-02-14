package esa.s1pdgs.cpoc.mqi.client;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public final class MqiConsumer<E extends AbstractMessage> implements Runnable {
	private static final Logger LOG = LogManager.getLogger(MqiConsumer.class);
	
	private final MqiClient client;
	private final ProductCategory category;
	private final MqiListener<E> mqiListener;
	private final List<? extends MessageFilter> mqiMessageFilter;
	private final long pollingIntervalMillis;
	private final long initialDelay;
	private final AppStatus appStatus;
	
	public MqiConsumer(
			final MqiClient client,
			final ProductCategory category,
			final MqiListener<E> mqiListener,
			final List<? extends MessageFilter> mqiMessageFilter,
			final long pollingIntervalMillis,
			final long initialDelay,
			final AppStatus appStatus
	) {
		this.client = client;
		this.category = category;
		this.mqiListener = mqiListener;
		this.mqiMessageFilter = mqiMessageFilter;
		this.pollingIntervalMillis = pollingIntervalMillis;
		this.initialDelay = initialDelay;
		this.appStatus = appStatus;
	}

	public MqiConsumer(
			final MqiClient client,
			final ProductCategory category,
			final MqiListener<E> mqiListener,
			final long pollingIntervalMillis,
			final long initialDelay,
			final AppStatus appStatus) {
		this(client, category, mqiListener, Collections.emptyList(), pollingIntervalMillis, initialDelay, appStatus);
	}

	@Override
	public final void run() {		
		// handle initial delay
		if (initialDelay > 0L) {
			LOG.debug("Start MQI polling in {}ms", initialDelay);
			try {
				appStatus.sleep(pollingIntervalMillis);
			} catch (final InterruptedException e) {
				LOG.debug("{} has been cancelled", this);
				LOG.info("Exiting {}", this);
				return;
			}
		}		
		// generic polling loop
		LOG.info("Starting {}", this);
		do {
			GenericMessageDto<E> message = null;
			try {
				if (appStatus.isShallBeStopped()) {
					LOG.info("MQI has been commanded to be stopped");
					appStatus.forceStopping();
					return;
				}
				LOG.trace("{} polls MQI", this);
				message = client.next(category);	
				appStatus.setWaiting();
				if (message == null || message.getBody() == null) {
					LOG.trace("No message received: continue");
					continue;
				}
				if (!allowConsumption(message)) {					
					LOG.trace("Filter does not allow consumption: continue");
					continue;
				}
				appStatus.setProcessing(message.getId());
				LOG.debug("{} received {} from MQI", this, message);
				try {
					mqiListener.onMessage(message);
					client.ack(new AckMessageDto(message.getId(), Ack.OK, null, false), category);
				// any other error --> dump prominently into log file but continue	
				} catch (final Exception e) {
					LOG.error(String.format("Error handling message %s", message), e);
					client.ack(new AckMessageDto(message.getId(), Ack.ERROR, LogUtils.toString(e), false), category);				
					// S1PRO-1045: as this implementation is used for e.g. appending something to the ErrorQueue, it must only be called 
					// on errors if ack call was successful
					mqiListener.onTerminalError(message, e);					
				}
			// on communication errors with Mqi --> just dump warning and retry on next polling attempt
			} catch (final AbstractCodedException ace) {
				LOG.warn("Error Code: {}, Message: {}", ace.getCode().getCode(), ace.getLogMessage());
				appStatus.setError("NEXT_MESSAGE");
			}
			try {
				appStatus.sleep(pollingIntervalMillis);
			} catch (final InterruptedException e) {
				LOG.debug("{} has been cancelled", this);
				break;
			}
		} while (!appStatus.isInterrupted());
		LOG.info("Exiting {}", this);
	}

	final boolean allowConsumption(final GenericMessageDto<E> message) {
		for (final MessageFilter filter : mqiMessageFilter) {
			if (filter.accept(message.getBody())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public final String toString() {
		return "MqiConsumer [category=" + category + "]";
	}
}
