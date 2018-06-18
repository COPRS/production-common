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
import fr.viveris.s1pdgs.jobgenerator.exception.MaxNumberCachedJobsReachException;
import fr.viveris.s1pdgs.jobgenerator.exception.MaxNumberCachedSessionsReachException;
import fr.viveris.s1pdgs.jobgenerator.exception.ObjectStorageException;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSession;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSessionFile;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.model.ResumeDetails;
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

	/**
	 * Name of the topic
	 */
	private final String topicName;

	/**
	 * 
	 * @param jobsDispatcher
	 * @param edrsSessionFileService
	 */
	@Autowired
	public EdrsSessionsConsumer(final EdrsSessionJobDispatcher jobDispatcher, final EdrsSessionFileService edrsService,
			@Value("${level0.maxagesession}") final long maxAgeSession,
			@Value("${level0.maxnumberofsessions}") final int maxNbSessions,
			@Value("${kafka.topics.edrs-sessions}") final String topicName) {
		this.jobDispatcher = jobDispatcher;
		this.edrsService = edrsService;
		this.maxAgeSession = maxAgeSession;
		this.maxNbSessions = maxNbSessions;
		this.cachedSessions = new ConcurrentHashMap<>(this.maxNbSessions);
		this.topicName = topicName;
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
	public void receive(final EdrsSessionDto dto) {

		if ("SESSION".equalsIgnoreCase(dto.getProductType())) {
			int step = 0;
			LOGGER.info("[MONITOR] [step {}] [productName {}] Starting job generation", step,
					dto.getObjectStorageKey());

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
				LOGGER.info("[MONITOR] [step {}] [productName {}] Building product", step, dto.getObjectStorageKey());
				if (dto.getChannelId() != 1 && dto.getChannelId() != 2) {
					throw new InvalidFormatProduct("Invalid channel identifier " + dto.getChannelId());
				}
				EdrsSessionFile file = edrsService.createSessionFile(dto.getObjectStorageKey());

				// If session exist and raws of each channel are available => send the session
				// to the job dispatcher
				// Else set in cached sessions
				step = 2;
				LOGGER.info("[MONITOR] [step {}] [productName {}] Treating session", step, file.getSessionId());
				EdrsSessionProduct session = null;
				if (cachedSessions.containsKey(file.getSessionId())) {
					session = cachedSessions.get(file.getSessionId());
					session.getObject().setChannel(file, dto.getChannelId());
					if (session.getObject().getChannel1() != null && session.getObject().getChannel2() != null) {
						step = 2;
						this.cachedSessions.remove(file.getSessionId());
						LOGGER.info("[MONITOR] [step {}] [productName {}] Dispatching session", step,
								file.getSessionId());
						this.jobDispatcher.dispatch(new Job<EdrsSession>(session, new ResumeDetails(topicName, dto)));
					} else {
						session.getObject().setLastTsMsg(System.currentTimeMillis());
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

			} catch (MaxNumberCachedSessionsReachException | MaxNumberCachedJobsReachException | ObjectStorageException mnce) {
				LOGGER.error("[MONITOR] [step {}] [productName {}] [resuming {}] [code {}] {} ", step,
						dto.getObjectStorageKey(), new ResumeDetails(topicName, dto), mnce.getCode().getCode(),
						mnce.getLogMessage());
			} catch (AbstractCodedException e) {
				LOGGER.error("[MONITOR] [step {}] [productName {}] [code {}] {} ", step, dto.getObjectStorageKey(),
						e.getCode().getCode(), e.getLogMessage());
			}

			step = 0;
			LOGGER.info("[MONITOR] [step {}] [productName {}] End", step, dto.getObjectStorageKey());
		}
	}

}
