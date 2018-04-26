package fr.viveris.s1pdgs.jobgenerator.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.jobgenerator.controller.dto.EdrsSessionDto;
import fr.viveris.s1pdgs.jobgenerator.exception.EdrsSessionException;
import fr.viveris.s1pdgs.jobgenerator.exception.JobDispatcherException;
import fr.viveris.s1pdgs.jobgenerator.exception.JobGenerationException;
import fr.viveris.s1pdgs.jobgenerator.exception.ObjectStorageException;
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
			LOGGER.info("[MONITOR] [Step 0] [obs {}] Starting job generation", dto.getObjectStorageKey());

			try {
				// Clean sessions for whom the last message has been consumed for too long
				// TODO set in a scheduled function, warning about concurrency
				LOGGER.info("[MONITOR] [Step x] Removing old sessions");
				cachedSessions.entrySet().stream()
						.filter(entry -> entry.getValue() != null && entry.getValue().getObject()
								.getLastTimestampMessageReception() < System.currentTimeMillis() - this.maxAgeSession)
						.forEach(entry -> {
							EdrsSessionProduct removedSession = cachedSessions.remove(entry.getKey());
							LOGGER.error(String.format(
									"[MONITOR] [Step x] [productName %s] Removed from cached because no message received for too long",
									removedSession.getIdentifier()));
						});

				// Create the EdrsSessionFile object from the consumed message
				LOGGER.info("[MONITOR] [Step 1] [obs {}] Building product", dto.getObjectStorageKey());
				EdrsSessionFile file = edrsSessionFileService.createSessionFile(dto.getObjectStorageKey(),
						dto.getChannelId());

				// If session exist and raws of each channel are available => send the session
				// to the job dispatcher
				// Else set in cached sessions
				EdrsSessionProduct session = null;
				if (cachedSessions.containsKey(file.getSessionId())) {
					session = cachedSessions.get(file.getSessionId());
					session.getObject().setChannel(file, dto.getChannelId());
					if (session.getObject().getChannel1() != null && session.getObject().getChannel2() != null) {
						this.cachedSessions.remove(file.getSessionId());
						LOGGER.info("[MONITOR] [Step 2] [productName {}] Dispatching session", file.getSessionId());
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
						LOGGER.error(String.format(
								"[MONITOR] [productName %s] Maximal number of cached sessions reached: message rejected",
								file.getSessionId()));
					}
				}
				
			} catch (EdrsSessionException | ObjectStorageException | JobGenerationException
					| JobDispatcherException e) {
				LOGGER.error("[MONITOR] [obs {}] {} ", dto.getObjectStorageKey(), e.getMessage());
			}
			
			LOGGER.info("[MONITOR] [Step 0] [obs {}] End", dto.getObjectStorageKey());
		}
	}

}
