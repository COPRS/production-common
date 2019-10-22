package esa.s1pdgs.cpoc.mqi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.status.AppStatus;

public final class MqiConsumer<E extends AbstractDto> implements Runnable {
	private static final Logger LOG = LogManager.getLogger(MqiConsumer.class);
	
	private final MqiClient client;
	private final ProductCategory category;
	private final MqiListener<E> mqiListener;
	private final long pollingIntervalMillis;
	private final long initialDelay;
	private final AppStatus appStatus;
	
	public MqiConsumer(
			final MqiClient client, 
			final ProductCategory category, 
			final MqiListener<E> mqiListener,
			final long pollingIntervalMillis,
			final long initialDelay,
			final AppStatus appStatus
	) {
		this.client = client;
		this.category = category;
		this.mqiListener = mqiListener;
		this.pollingIntervalMillis = pollingIntervalMillis;
		this.initialDelay = initialDelay;
		this.appStatus = appStatus;
	}
	
	public MqiConsumer(
			final MqiClient client, 
			final ProductCategory category, 
			final MqiListener<E> mqiListener,
			final long pollingIntervalMillis
	) {
		this(client, category, mqiListener, pollingIntervalMillis, 0L, AppStatus.NULL);
	}

	@Override
	public final void run() {		
		// handle initial delay
		if (initialDelay > 0L) {
			LOG.debug("Start MQI polling in {}ms", initialDelay);
			try {
				appStatus.sleep(pollingIntervalMillis);
			} catch (InterruptedException e) {
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
				appStatus.setProcessing(message.getId());
				LOG.debug("{} received {} from MQI", this, message);
				AckMessageDto ackMess;			
				try {
					mqiListener.onMessage(message);
					ackMess = new AckMessageDto(message.getId(), Ack.OK, null, false);
				// any other error --> dump prominently into log file but continue	
				} catch (Exception e) {
					LOG.error(String.format("Unexpected Error handling message %s", message), e);
					ackMess = new AckMessageDto(message.getId(), Ack.ERROR, LogUtils.toString(e), false);
				}			
				client.ack(ackMess, category);
			// on communication errors with Mqi --> just dump warning and retry on next polling attempt
			} catch (AbstractCodedException ace) {
				LOG.warn("Error Code: {}, Message: {}", ace.getCode().getCode(), ace.getLogMessage());
				appStatus.setError("NEXT_MESSAGE");
			}
			try {
				appStatus.sleep(pollingIntervalMillis);
			} catch (InterruptedException e) {
				LOG.debug("{} has been cancelled", this);
				break;
			}
		} while (!appStatus.isInterrupted());
		LOG.info("Exiting {}", this);
	}

	@Override
	public final String toString() {
		return "MqiConsumer [category=" + category + "]";
	}
}
