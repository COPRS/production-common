package fr.viveris.s1pdgs.jobgenerator.tasks.consumer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.InvalidFormatProduct;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMaxNumberCachedJobsReachException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMaxNumberCachedSessionsReachException;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSession;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSessionFile;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.model.product.EdrsSessionProduct;
import fr.viveris.s1pdgs.jobgenerator.service.EdrsSessionFileService;
import fr.viveris.s1pdgs.jobgenerator.status.AppStatus;
import fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher.EdrsSessionJobDispatcher;

@Component
@ConditionalOnProperty(name = "process.level", havingValue = "L0")
public class EdrsSessionConsumer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(EdrsSessionConsumer.class);
	/**
	 * Jobs dispatcher
	 */
	private final EdrsSessionJobDispatcher jobDispatcher;

	/**
	 * Service for EDRS session file
	 */
	private final EdrsSessionFileService edrsService;

	/**
	 * Session waiting for being processing by a task table
	 */
	protected Map<String, EdrsSessionProduct> cachedSessions;

	/**
	 * Maximal age of cached sessions
	 */
	private final long maxAgeSession;

	/**
	 * Maximal number of cached sessions
	 */
	private final int maxNbSessions;

	private final GenericMqiService<EdrsSessionDto> mqiService;
	/**
	 * Application status
	 */
	private final AppStatus appStatus;

	@Autowired
	public EdrsSessionConsumer(
			@Qualifier("mqiServiceForEdrsSessions") final GenericMqiService<EdrsSessionDto> mqiService,
			final EdrsSessionJobDispatcher jobDispatcher, final EdrsSessionFileService edrsService,
			@Value("${level0.maxagesession}") final long maxAgeSession,
			@Value("${level0.maxnumberofsessions}") final int maxNbSessions, final AppStatus appStatus) {
		this.mqiService = mqiService;
		this.jobDispatcher = jobDispatcher;
		this.edrsService = edrsService;
		this.maxAgeSession = maxAgeSession;
		this.maxNbSessions = maxNbSessions;
		this.cachedSessions = new ConcurrentHashMap<>(this.maxNbSessions);
		this.appStatus = appStatus;

	}

	@Scheduled(fixedDelayString = "${process.fixed-delay-ms}")
	public void consumeMessages() {
		// First, consume message
		GenericMessageDto<EdrsSessionDto> message = null;
		try {
			message = mqiService.next();
		} catch (AbstractCodedException ace) {
			LOGGER.error("[MONITOR] [code {}] {}", ace.getCode().getCode(), ace.getLogMessage());
			message = null;
		}
		if (message == null || message.getBody() == null) {
			LOGGER.trace("[MONITOR] [step 0] No message received: continue");
            appStatus.setError("NEXT_MESSAGE");
			return;
		}
		appStatus.setProcessing(message.getIdentifier());
		LOGGER.info("Initializing job processing {}", message);

		// Second process message
		EdrsSessionDto leveldto = message.getBody();

		if (leveldto.getProductType() == EdrsSessionFileType.SESSION) {

			int step = 0;
			LOGGER.info("[MONITOR] [step {}] [productName {}] Starting job generation", step,
					leveldto.getObjectStorageKey());

			try {
				// Clean sessions for whom the last message has been consumed for too long
				// TODO set in a scheduled function, warning about concurrency
				step = 999;
				LOGGER.info("[MONITOR] [step {}] Removing old sessions", step);
				cachedSessions.entrySet().stream().filter(entry -> entry.getValue() != null && entry.getValue()
						.getObject().getLastTsMsg() < System.currentTimeMillis() - this.maxAgeSession)
						.forEach(entry -> {
							EdrsSessionProduct removedSession = cachedSessions.remove(entry.getKey());
							if (removedSession != null) {
								LOGGER.error("[MONITOR] [step 999] [productName {}] [code {}] [msg {}]",
										removedSession.getIdentifier(), ErrorCode.MAX_AGE_CACHED_JOB_REACH.getCode(),
										"Removed from cached because no message received for too long");
							}
						});

				// Create the EdrsSessionFile object from the consumed message
				step = 1;
				LOGGER.info("[MONITOR] [step {}] [productName {}] Building product", step,
						leveldto.getObjectStorageKey());
				if (leveldto.getChannelId() != 1 && leveldto.getChannelId() != 2) {
					throw new InvalidFormatProduct("Invalid channel identifier " + leveldto.getChannelId());
				}
				EdrsSessionFile file = edrsService.createSessionFile(leveldto.getObjectStorageKey());

				// If session exist and raws of each channel are available => send the session
				// to the job dispatcher
				// Else set in cached sessions
				step = 2;
				LOGGER.info("[MONITOR] [step {}] [productName {}] Treating session", step, file.getSessionId());
				EdrsSessionProduct session = null;
				if (cachedSessions.containsKey(file.getSessionId())) {
					session = cachedSessions.get(file.getSessionId());
					session.getObject().setChannel(file, leveldto.getChannelId());
					if (session.getObject().getChannel1() != null && session.getObject().getChannel2() != null) {
						step = 2;
						this.cachedSessions.remove(file.getSessionId());
						LOGGER.info("[MONITOR] [step {}] [productName {}] Dispatching session", step,
								file.getSessionId());
						this.jobDispatcher.dispatch(new Job<EdrsSession>(session, message));
					} else {
						session.getObject().setLastTsMsg(System.currentTimeMillis());
					}
				} else {
					// Check mx nb session not reached
					if (this.cachedSessions.size() < this.maxNbSessions) {
						session = new EdrsSessionProduct(file.getSessionId(), leveldto.getSatelliteId(),
								leveldto.getMissionId(), file.getStartTime(), file.getStopTime(), new EdrsSession());
						session.getObject().setChannel(file, leveldto.getChannelId());
						cachedSessions.put(file.getSessionId(), session);
					} else {
						throw new JobGenMaxNumberCachedSessionsReachException(
								"Maximal number of cached sessions reached");
					}
				}

			} catch (JobGenMaxNumberCachedSessionsReachException | JobGenMaxNumberCachedJobsReachException
					| ObsException mnce) {
				LOGGER.error("[MONITOR] [step {}] [productName {}] [resuming {}] [code {}] {} ", step,
						leveldto.getObjectStorageKey(), mnce.getCode().getCode(), mnce.getLogMessage());
			} catch (AbstractCodedException e) {
				LOGGER.error("[MONITOR] [step {}] [productName {}] [code {}] {} ", step, leveldto.getObjectStorageKey(),
						e.getCode().getCode(), e.getLogMessage());
			}

			step = 0;
			LOGGER.info("[MONITOR] [step {}] [productName {}] End", step, leveldto.getObjectStorageKey());

		}

		// Ack message
		// TODO ack KO if exception occured
		try {
			mqiService.ack(new AckMessageDto(message.getIdentifier(), Ack.OK, "OK", false));
			appStatus.setWaiting();
		} catch (AbstractCodedException ace) {
			LOGGER.error("[MONITOR] [step {} [code {}] {}", 0, ace.getCode(), ace.getLogMessage());
			appStatus.setError("NEXT_MESSAGE");
		}

	}
}
