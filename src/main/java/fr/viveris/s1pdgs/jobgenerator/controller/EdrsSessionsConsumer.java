package fr.viveris.s1pdgs.jobgenerator.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.jobgenerator.controller.dto.EdrsSessionDto;
import fr.viveris.s1pdgs.jobgenerator.exception.AbstractCodedException;
import fr.viveris.s1pdgs.jobgenerator.exception.AbstractCodedException.ErrorCode;
import fr.viveris.s1pdgs.jobgenerator.exception.InvalidFormatProduct;
import fr.viveris.s1pdgs.jobgenerator.exception.MaxNumberCachedSessionsReachException;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSession;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSessionFile;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.model.product.EdrsSessionProduct;
import fr.viveris.s1pdgs.jobgenerator.service.EdrsSessionFileService;
import fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher.EdrsSessionJobDispatcher;

/**
 * KAFKA consumer for EDRS session files. Once the 2 files of the same session
 * are received, the consumer sends the session to the job dispatcher
 * 
 * @author Cyrielle Gailliard
 *
 */
@Component
@ConditionalOnProperty(prefix = "kafka.enable-consumer", name = "edrs-sessions")
public class EdrsSessionsConsumer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(EdrsSessionsConsumer.class);

	/**
	 * Jobs dispatcher
	 */
	private final EdrsSessionJobDispatcher edrsSessionJobDispatcher;

	/**
	 * Service for EDRS session file
	 */
	private final EdrsSessionFileService edrsSessionFileService;

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

	/**
	 * 
	 * @param jobsDispatcher
	 * @param edrsSessionFileService
	 */
	@Autowired
	public EdrsSessionsConsumer(final EdrsSessionJobDispatcher edrsSessionJobDispatcher,
			final EdrsSessionFileService edrsSessionFileService,
			@Value("${level0.maxagesession}") final long maxAgeSession,
			@Value("${level0.maxnumberofsessions}") final int maxNbSessions) {
		this.edrsSessionJobDispatcher = edrsSessionJobDispatcher;
		this.edrsSessionFileService = edrsSessionFileService;
		this.maxAgeSession = maxAgeSession;
		this.maxNbSessions = maxNbSessions;
		this.cachedSessions = new ConcurrentHashMap<>(this.maxNbSessions);
	}

	/**
	 * Message listener container. Read a message.<br/>
	 * <ul>
	 * <li>If we receive the 2nd channel of a cached session, we send the session to
	 * the dispatcher</li>
	 * <li>If we receive a session not cached, we put the session in the cache</li>
	 * <li>Else we ignore the message</li>
	 * </ul>
	 * Furthermore, as soon as we receive a message, we clean the cache and remove
	 * sessions cached for too long
	 * 
	 * @param payload
	 */
	@KafkaListener(topics = "${kafka.topics.edrs-sessions}", groupId = "${kafka.group-id}")
	public void receive(EdrsSessionDto dto) {

		if ("SESSION".equalsIgnoreCase(dto.getProductType())) {
			LOGGER.info("[MONITOR] [step 0] [productName {}] Starting job generation", dto.getObjectStorageKey());

			int step = 999;
			try {
				// Clean sessions for whom the last message has been consumed for too long
				// TODO set in a scheduled function, warning about concurrency
				LOGGER.info("[MONITOR] [step 999] Removing old sessions");
				cachedSessions.entrySet().stream()
						.filter(entry -> entry.getValue() != null && entry.getValue().getObject()
								.getLastTimestampMessageReception() < System.currentTimeMillis() - this.maxAgeSession)
						.forEach(entry -> {
							EdrsSessionProduct removedSession = cachedSessions.remove(entry.getKey());
							LOGGER.error("[MONITOR] [step 999] [productName {}] [code {}] [msg {}]",
									removedSession.getIdentifier(), ErrorCode.MAX_AGE_CACHED_JOB_REACH.getCode(),
									"Removed from cached because no message received for too long");
						});

				step = 1;
				// Check dto channel
				if (dto.getChannelId() != 1 && dto.getChannelId() != 2) {
					throw new InvalidFormatProduct("Invalid channel identifier " + dto.getChannelId());
				}
				// Create the EdrsSessionFile object from the consumed message
				LOGGER.info("[MONITOR] [step 1] [productName {}] Building product", dto.getObjectStorageKey());
				EdrsSessionFile file = edrsSessionFileService.createSessionFile(dto.getObjectStorageKey());

				// If session exist and raws of each channel are available => send the session
				// to the job dispatcher
				// Else set in cached sessions
				EdrsSessionProduct session = null;
				if (cachedSessions.containsKey(file.getSessionId())) {
					session = cachedSessions.get(file.getSessionId());
					session.getObject().setChannel(file, dto.getChannelId());
					if (session.getObject().getChannel1() != null && session.getObject().getChannel2() != null) {
						step = 2;
						this.cachedSessions.remove(file.getSessionId());
						LOGGER.info("[MONITOR] [step 2] [productName {}] Dispatching session", file.getSessionId());
						this.edrsSessionJobDispatcher.dispatch(new Job<EdrsSession>(session));
					} else {
						session.getObject().setLastTimestampMessageReception(System.currentTimeMillis());
					}
				} else {
					// Check mx nb session not reached
					if (this.cachedSessions.size() < this.maxNbSessions) {
						session = new EdrsSessionProduct(file.getSessionId(), dto.getSatelliteId(), dto.getMissionId(),
								file.getStartTime(), file.getStopTime(), new EdrsSession());
						session.getObject().setChannel(file, dto.getChannelId());
						cachedSessions.put(file.getSessionId(), session);
					} else {
						throw new MaxNumberCachedSessionsReachException("Maximal number of cached sessions reached");
					}
				}

			} catch (AbstractCodedException e) {
				LOGGER.error("[MONITOR] [step {}] [productName {}] [code {}] {} ", step, dto.getObjectStorageKey(),
						e.getCode().getCode(), e.getLogMessage());
			}

			LOGGER.info("[MONITOR] [step 0] [productName {}] End", dto.getObjectStorageKey());
		}
	}

}
