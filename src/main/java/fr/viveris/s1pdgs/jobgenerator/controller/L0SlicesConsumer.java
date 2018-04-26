package fr.viveris.s1pdgs.jobgenerator.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.jobgenerator.config.L0SlicePatternSettings;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.L0SliceDto;
import fr.viveris.s1pdgs.jobgenerator.exception.JobDispatcherException;
import fr.viveris.s1pdgs.jobgenerator.exception.JobGenerationException;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.model.product.L0Slice;
import fr.viveris.s1pdgs.jobgenerator.model.product.L0SliceProduct;
import fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher.L0SliceJobsDispatcher;

/**
 * KAFKA consumer for EDRS session files. Once the 2 files of the same session
 * are received, the consumer sends the session to the job dispatcher
 * 
 * @author Cyrielle Gailliard
 *
 */
@Component
@ConditionalOnProperty(prefix = "kafka.enable-consumer", name = "l0-slices")
public class L0SlicesConsumer {
	
	protected static final String DATE_FORMAT = "yyyyMMdd'T'HHmmss";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(L0SlicesConsumer.class);

	private final L0SliceJobsDispatcher l0SliceJobsDispatcher;

	private final L0SlicePatternSettings l0SlicePatternSettings;

	private final Pattern l0SLicesPattern;

	/**
	 * 
	 * @param jobsDispatcher
	 * @param edrsSessionFileService
	 */
	@Autowired
	public L0SlicesConsumer(final L0SliceJobsDispatcher l0SliceJobsDispatcher,
			final L0SlicePatternSettings l0SlicePatternSettings) {
		this.l0SliceJobsDispatcher = l0SliceJobsDispatcher;
		this.l0SlicePatternSettings = l0SlicePatternSettings;
		this.l0SLicesPattern = Pattern.compile(this.l0SlicePatternSettings.getRegexp(), Pattern.CASE_INSENSITIVE);
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
	@KafkaListener(topics = "${kafka.topics.l0-slices}", groupId = "${kafka.group-id}", containerFactory = "l0SlicesKafkaListenerContainerFactory")
	public void receive(L0SliceDto dto) {
		
		LOGGER.info("[MONITOR] [Step 0] [productName {}] Starting job generation", dto.getProductName());
		
		try {
			
			LOGGER.info("[MONITOR] [Step 1] [productName {}] Building product", dto.getProductName());
			Matcher m = l0SLicesPattern.matcher(dto.getProductName());
			if (m.matches()) {
				String satelliteId = m.group(this.l0SlicePatternSettings.getPlaceMatchSatelliteId());
				String missionId = m.group(this.l0SlicePatternSettings.getPlaceMatchMissionId());
				String acquisition = m.group(this.l0SlicePatternSettings.getPlaceMatchAcquisition());
				String startTime = m.group(this.l0SlicePatternSettings.getPlaceMatchStartTime());
				String stopTime = m.group(this.l0SlicePatternSettings.getPlaceMatchStopTime());
				Date dateStart = this.convertDate(startTime);
				Date dateStop = this.convertDate(stopTime);

				// Initialize the JOB
				L0Slice slice = new L0Slice(acquisition);
				L0SliceProduct product = new L0SliceProduct(dto.getProductName(), satelliteId, missionId, dateStart,
						dateStop, slice);
				Job<L0Slice> job = new Job<>(product);

				// Dispatch job
				LOGGER.info("[MONITOR] [Step 2] [productName {}] Dispatching product", dto.getProductName());
				this.l0SliceJobsDispatcher.dispatch(job);
			} else {
				LOGGER.error("[MONITOR] [productName {}] Don't match with regular expression {}", dto.getProductName(),
						this.l0SlicePatternSettings.getRegexp());
			}
		} catch (JobDispatcherException e) {
			LOGGER.error("[MONITOR] [productName {}] {}", dto.getProductName(), e.getMessage());
		} catch (JobGenerationException e) {
			LOGGER.error("[MONITOR] [productName {}] {}", dto.getProductName(), e.getMessage());
		} catch (ParseException e) {
			LOGGER.error("[MONITOR] [productName {}] {}", dto.getProductName(), e.getMessage());
		}

		LOGGER.info("[MONITOR] [Step 0] [productName {}] End", dto.getProductName());
	}

	private Date convertDate(String dateStr) throws ParseException {
		DateFormat format = new SimpleDateFormat(DATE_FORMAT);
		return format.parse(dateStr);
	}

}
