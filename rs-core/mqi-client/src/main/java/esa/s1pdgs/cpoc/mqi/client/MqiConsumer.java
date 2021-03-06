package esa.s1pdgs.cpoc.mqi.client;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiAckApiError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiNextApiError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublishApiError;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public final class MqiConsumer<E extends AbstractMessage> implements Runnable {
	static final Logger LOG = LogManager.getLogger(MqiConsumer.class);
	
	private final MqiClient client;
	private final ProductCategory category;
	private final MqiListener<E> mqiListener;
	private final List<? extends MessageFilter> mqiMessageFilter;
	private final long pollingIntervalMillis;
	private final long initialDelay;
	private final AppStatus appStatus;
	
	public static <E extends AbstractMessage> MqiConsumer<E> valueOf(
			final Class<E> dtoClass,
			final MqiClient client,
			final ProductCategory category,
			final MqiListener<E> mqiListener,
			final List<? extends MessageFilter> mqiMessageFilter,
			final long pollingIntervalMillis,
			final long initialDelay,
			final AppStatus appStatus) {
		return new MqiConsumer<E>(client, category, mqiListener, mqiMessageFilter, pollingIntervalMillis, initialDelay, appStatus);
	}

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

	@Override
	public final void run() {		
		// handle initial delay
		if (initialDelay > 0L) {
			LOG.debug("Start MQI polling in {}ms", initialDelay);
			try {
				appStatus.sleep(initialDelay);
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
					LOG.trace("Filter does not allow consumption of message {}: sending ack and continue", message);
					client.ack(new AckMessageDto(message.getId(), Ack.OK, null, false), category);
					continue;
				}
				appStatus.setProcessing(message.getId());
				LOG.debug("{} received {} from MQI", this, message);
				try {
					final MqiMessageEventHandler handler = mqiListener.onMessage(message);
					if (handler == null) {
						throw new RuntimeException(
								String.format("MqiListener implementation %s returned null", mqiListener.getClass())
						);
					}	
					
					String warning = handler.processMessages(client);
					if (!"".equals(warning)) {
						mqiListener.onWarning(message, warning);
						client.ack(new AckMessageDto(message.getId(), Ack.WARN, null, false), category);
						LOG.info("{} handled {} with warning, done!", this, message.getId());
					} else {
						client.ack(new AckMessageDto(message.getId(), Ack.OK, null, false), category);
						LOG.info("{} handled {} successfully, done!", this, message.getId());
					}
				// should be thrown if publish() or ack() is failing	
				} catch (final MqiAckApiError | MqiPublishApiError ace) {
					// S1PRO-1406: simply propagate exception to initiate shutdown in case publish or ack fails
					// since input message is considered as not handled, no error request shall be created
					throw ace;								
				// any other error --> dump prominently into log file but continue	
				} catch (final Exception e) {
					LOG.error(String.format("Error handling message %s", message), e);
					appStatus.setError("NEXT_MESSAGE");
					client.ack(new AckMessageDto(message.getId(), Ack.ERROR, LogUtils.toString(e), false), category);				
					// S1PRO-1045: as this implementation is used for e.g. appending something to the ErrorQueue, 
					// it must only be called on errors if ack call was successful (otherwise, the message may be 
					// distributed to another pod)
					mqiListener.onTerminalError(message, e);
				}
			// on communication errors with Mqi --> just dump warning and retry on next polling attempt
			} catch (final MqiAckApiError | MqiNextApiError | MqiPublishApiError ace) {
				/*
				 * S1PRO-1370: It was requested that if an communication with the MQI server fails for the max amount of retries,
				 * the service is going into fail and being restarted by Kubernetes. All these exceptions are likely raised in
				 * this scenario and shall be handled before the AbstractCodedException in general is handled!
				 */
				LOG.error("Unable to reach the MQI Server for the maximum of retries. Terminating this service now. Error Code: {}, Message: {}", ace.getCode().getCode(), ace.getLogMessage());
				appStatus.setShallBeStopped(true);
				appStatus.forceStopping();
			} catch (final AbstractCodedException ace) {
				LOG.warn("Error Code: {}, Message: {}", ace.getCode().getCode(), ace.getLogMessage());
				appStatus.setError("NEXT_MESSAGE");
			} catch (final Throwable e) {
				// S1PRO-2431: Exceptions and Errors like OutOfMemoryError not handled shall lead to a restart by Kubernetes
				LOG.error("Unexpected Error: {},  Terminating this service now! ", e.getMessage());
				e.printStackTrace();
				appStatus.setShallBeStopped(true);
				appStatus.forceStopping();
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
			if (!filter.accept(message.getBody())) {
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
