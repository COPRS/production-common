package fr.viveris.s1pdgs.jobgenerator.tasks.consumer;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InvalidFormatProduct;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMaxNumberCachedJobsReachException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMissingRoutingEntryException;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import fr.viveris.s1pdgs.jobgenerator.config.L0SlicePatternSettings;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.model.product.L0Slice;
import fr.viveris.s1pdgs.jobgenerator.model.product.L0SliceProduct;
import fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher.L0SliceJobsDispatcher;
import fr.viveris.s1pdgs.jobgenerator.utils.DateUtils;

@Component
@ConditionalOnProperty(name="process.level", havingValue="L1")

public class L0SlicesConsumer {
    /**
     * MQI service for reading message
     */

    /**
     * Format of dates used in filename of the products
     */
    protected static final String DATE_FORMAT = "yyyyMMdd'T'HHmmss";

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(L0SlicesConsumer.class);

    /**
     * Dispatcher of l0 slices
     */
    private final L0SliceJobsDispatcher jobsDispatcher;

    /**
     * Settings used to extract information from L0 product name
     */
    private final L0SlicePatternSettings patternSettings;

    /**
     * Pattern built from the regular expression given in configuration
     */
    private final Pattern l0SLicesPattern;

    private final GenericMqiService<LevelProductDto> mqiService;
    
    
    @Autowired
    public L0SlicesConsumer(final L0SliceJobsDispatcher jobsDispatcher, final L0SlicePatternSettings patternSettings,
            @Qualifier("mqiServiceForLevelProducts") final GenericMqiService<LevelProductDto> mqiService) {
        this.jobsDispatcher = jobsDispatcher;
        this.patternSettings = patternSettings;
        this.l0SLicesPattern = Pattern.compile(this.patternSettings.getRegexp(), Pattern.CASE_INSENSITIVE);
            
        this.mqiService = mqiService;
    }
    public void consumeMessages() {
        // First, consume message
        GenericMessageDto<LevelProductDto> message = null;
        try {
            message = mqiService.next();
        } catch (AbstractCodedException ace) {
            LOGGER.error("[MONITOR] [code {}] {}",
                    ace.getCode().getCode(), ace.getLogMessage());
            message = null;
        }
        if (message == null || message.getBody() == null) {
            LOGGER.trace("[MONITOR] [step 0] No message received: continue");
            return;
        }
        //    Second process message
        LevelProductDto leveldto = message.getBody();
        LOGGER.info("[MONITOR] [step 0] [productName {}] Starting job generation", leveldto.getProductName());
        int step = 1;

        try {

            LOGGER.info("[MONITOR] [step 1] [productName {}] Building product", leveldto.getProductName());
            Matcher m = l0SLicesPattern.matcher(leveldto.getProductName());
            if (!m.matches()) {
                throw new InvalidFormatProduct(
                        "Don't match with regular expression " + this.patternSettings.getRegexp());
            }
            String satelliteId = m.group(this.patternSettings.getMGroupSatId());
            String missionId = m.group(this.patternSettings.getMGroupMissionId());
            String acquisition = m.group(this.patternSettings.getMGroupAcquisition());
            String startTime = m.group(this.patternSettings.getMGroupStartTime());
            String stopTime = m.group(this.patternSettings.getMGroupStopTime());
            Date dateStart = DateUtils.convertWithSimpleDateFormat(startTime, DATE_FORMAT);
            Date dateStop = DateUtils.convertWithSimpleDateFormat(stopTime, DATE_FORMAT);

            // Initialize the JOB
            L0Slice slice = new L0Slice(acquisition);
            L0SliceProduct product = new L0SliceProduct(leveldto.getProductName(), satelliteId, missionId, dateStart,
                    dateStop, slice);
            Job<L0Slice> job = new Job<>(product, message);

            // Dispatch job
            step++;
            LOGGER.info("[MONITOR] [step 2] [productName {}] Dispatching product", leveldto.getProductName());
            this.jobsDispatcher.dispatch(job);

        } catch (JobGenMaxNumberCachedJobsReachException | JobGenMissingRoutingEntryException mnce) {
            LOGGER.error("[MONITOR] [step {}] [productName {}] [code {}] {} ", step,
                    leveldto.getKeyObjectStorage(), mnce.getCode().getCode(), mnce.getLogMessage());
        } catch (AbstractCodedException e) {
            LOGGER.error("[MONITOR] [step {}] [productName {}] [code {}] {} ", step, leveldto.getProductName(),
                    e.getCode().getCode(), e.getLogMessage());
        }

        LOGGER.info("[MONITOR] [step 0] [productName {}] End", leveldto.getProductName());



        
        // Ack message
        try {
            mqiService.ack(new AckMessageDto(message.getIdentifier(), Ack.OK, "OK", false));
        }
        catch (AbstractCodedException ace) {
            LOGGER.error("");
        }
        
    }
    
}
