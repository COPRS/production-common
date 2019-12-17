package esa.s1pdgs.cpoc.mqi.client;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

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
	private final List<MqiMessageFilter> mqiMessageFilter;
	private final long pollingIntervalMillis;
	private final long initialDelay;
	private final AppStatus appStatus;
	
	public MqiConsumer(
			final MqiClient client,
			final ProductCategory category,
			final MqiListener<E> mqiListener,
			final List<MqiMessageFilter> mqiMessageFilter,
			final long pollingIntervalMillis,
			final long initialDelay,
			final AppStatus appStatus) {
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
		this(client, category, mqiListener, Collections.EMPTY_LIST, pollingIntervalMillis, initialDelay, appStatus);
	}

	public MqiConsumer(
			final MqiClient client,
			final ProductCategory category,
			final MqiListener<E> mqiListener,
			final long pollingIntervalMillis) {
		this(client, category, mqiListener, Collections.EMPTY_LIST, pollingIntervalMillis, 0L, AppStatus.NULL);
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
			try {
				if (appStatus.isShallBeStopped()) {
					LOG.info("MQI has been commanded to be stopped");
					appStatus.forceStopping();
					return;
				}
				LOG.trace("{} polls MQI", this);
				final GenericMessageDto<E> message = client.next(category);	
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
				AckMessageDto ackMess;			
				try {
					mqiListener.onMessage(message);
					ackMess = new AckMessageDto(message.getId(), Ack.OK, null, false);
				// any other error --> dump prominently into log file but continue	
				} catch (final Exception e) {
					LOG.error(String.format("Error handling message %s", message), e);
					ackMess = new AckMessageDto(message.getId(), Ack.ERROR, LogUtils.toString(e), false);
				}			
				client.ack(ackMess, category);
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

	boolean allowConsumption(GenericMessageDto<E> message) {

		boolean allowConsumption = true;

		for (MqiMessageFilter f : mqiMessageFilter) {

			if (f.getProductFamily().equals(message.getBody().getProductFamily())) {
				if (!Pattern.matches(f.getMatchRegex(), message.getBody().getKeyObjectStorage())) {
					allowConsumption = false;
				}
				break;
			}
		}

		return allowConsumption;
	}

	@Override
	public final String toString() {
		return "MqiConsumer [category=" + category + "]";
	}
}
